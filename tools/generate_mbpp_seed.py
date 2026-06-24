import ast
import copy
import json
import re
import sys
import textwrap
import urllib.request
from pathlib import Path


MBPP_URL = (
    "https://raw.githubusercontent.com/google-research/google-research/"
    "master/mbpp/sanitized-mbpp.json"
)
PROJECT_ROOT = Path(__file__).resolve().parents[1]
PROBLEMS_PATH = PROJECT_ROOT / "core/data/src/main/assets/seed_data/problems.json"
DOWNLOAD_PATH = PROJECT_ROOT / "tools/cache/sanitized-mbpp.json"
TARGET_MBPP_PROBLEMS = 220
SOURCE_URL = "https://huggingface.co/datasets/Muennighoff/mbpp"
SOURCE_LICENSE = "CC BY 4.0"


TAG_KEYWORDS = [
    ("list", "lists"),
    ("array", "lists"),
    ("tuple", "tuples"),
    ("string", "strings"),
    ("dictionary", "dictionaries"),
    ("dict", "dictionaries"),
    ("set", "sets"),
    ("sort", "sorting"),
    ("heap", "heap"),
    ("prime", "math"),
    ("number", "math"),
    ("integer", "math"),
    ("matrix", "matrix"),
    ("regex", "regex"),
    ("regular expression", "regex"),
    ("recursion", "recursion"),
    ("recursive", "recursion"),
    ("date", "date-time"),
    ("time", "date-time"),
    ("binary", "binary"),
    ("tree", "trees"),
    ("graph", "graphs"),
]


def main() -> int:
    mbpp_items = load_mbpp()
    seed = json.loads(PROBLEMS_PATH.read_text(encoding="utf-8"))
    custom_problems = [
        item for item in seed["problems"] if not item["id"].startswith("mbpp_")
    ]
    custom_tests = [
        item for item in seed["testCases"] if not item["problemId"].startswith("mbpp_")
    ]

    generated_problems = []
    generated_tests = []
    skipped = 0
    for item in mbpp_items:
        if len(generated_problems) >= TARGET_MBPP_PROBLEMS:
            break
        converted = convert_item(item)
        if converted is None:
            skipped += 1
            continue
        problem, tests = converted
        generated_problems.append(problem)
        generated_tests.extend(tests)

    if len(generated_problems) < 200:
        raise RuntimeError(
            f"Only generated {len(generated_problems)} MBPP problems; expected at least 200."
        )
    balance_difficulties(generated_problems)

    output = {
        "problems": custom_problems + generated_problems,
        "testCases": custom_tests + generated_tests,
        "sources": [
            {
                "name": "MBPP - Mostly Basic Python Problems",
                "url": SOURCE_URL,
                "license": SOURCE_LICENSE,
                "notes": (
                    "MBPP problem prompts and assert-style tests are used for "
                    "Python practice data. Keep attribution when importing to Firestore."
                ),
            }
        ],
    }
    PROBLEMS_PATH.write_text(
        json.dumps(output, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print(
        f"Wrote {len(output['problems'])} problems "
        f"({len(generated_problems)} MBPP, {len(custom_problems)} custom)."
    )
    print(f"Wrote {len(output['testCases'])} test cases. Skipped {skipped} MBPP rows.")
    return 0


def balance_difficulties(problems: list[dict]) -> None:
    ranked = sorted(
        problems,
        key=lambda problem: len(problem["solutionCode"]) + len(problem["description"]),
    )
    easy_cutoff = int(len(ranked) * 0.45)
    medium_cutoff = int(len(ranked) * 0.82)
    for index, problem in enumerate(ranked):
        if index < easy_cutoff:
            problem["difficulty"] = "EASY"
        elif index < medium_cutoff:
            problem["difficulty"] = "MEDIUM"
        else:
            problem["difficulty"] = "HARD"


def load_mbpp() -> list[dict]:
    DOWNLOAD_PATH.parent.mkdir(parents=True, exist_ok=True)
    if not DOWNLOAD_PATH.exists():
        print(f"Downloading MBPP from {MBPP_URL}")
        urllib.request.urlretrieve(MBPP_URL, DOWNLOAD_PATH)
    return json.loads(DOWNLOAD_PATH.read_text(encoding="utf-8"))


def convert_item(item: dict) -> tuple[dict, list[dict]] | None:
    task_id = int(item["task_id"])
    problem_id = f"mbpp_{task_id:04d}"
    prompt = clean_text(item.get("prompt") or item.get("text") or "")
    solution = normalize_code(item.get("code", ""))
    assertions = [
        normalize_assertion(test)
        for test in item.get("test_list", [])
        if normalize_assertion(test)
    ]
    imports = [normalize_code(line) for line in item.get("test_imports", [])]
    imports = [line for line in imports if line]

    if not prompt or not solution or not assertions:
        return None
    if not is_solution_valid(solution, imports, assertions):
        return None

    starter = build_starter(solution)
    if not starter:
        return None

    difficulty = infer_difficulty(solution, assertions)
    tags = infer_tags(prompt, solution)
    problem = {
        "id": problem_id,
        "title": build_title(task_id, prompt),
        "description": (
            f"{prompt}\n\n"
            "Write the required Python function in the starter code. "
            "The judge will run assert-based examples against your function."
        ),
        "difficulty": difficulty,
        "constraints": [
            "Function name and parameters must match the starter code.",
            "Return the value requested by the prompt instead of printing it unless stated otherwise.",
            f"Source: MBPP task {task_id}, {SOURCE_LICENSE}.",
        ],
        "starterCode": starter,
        "solutionCode": solution,
        "tags": tags,
        "source": {
            "name": "MBPP - Mostly Basic Python Problems",
            "taskId": task_id,
            "url": SOURCE_URL,
            "license": SOURCE_LICENSE,
        },
    }
    tests = []
    for index, assertion in enumerate(assertions[:6], start=1):
        test_code = "\n".join(imports + [assertion])
        tests.append(
            {
                "id": f"{problem_id}_case_{index}",
                "problemId": problem_id,
                "input": test_code,
                "expectedOutput": "Assertion passes",
                "isHidden": index > 2,
            }
        )
    return problem, tests


def build_starter(solution: str) -> str:
    try:
        tree = ast.parse(solution)
    except SyntaxError:
        return ""

    starter_nodes = []
    for node in tree.body:
        if isinstance(node, (ast.Import, ast.ImportFrom)):
            starter_nodes.append(ast.unparse(node))
        elif isinstance(node, ast.Assign) and is_safe_top_level_assignment(node):
            starter_nodes.append(ast.unparse(node))
        elif isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef)):
            function_node = copy.deepcopy(node)
            function_node.body = [ast.Pass()]
            function_node.decorator_list = []
            starter_nodes.append(ast.unparse(function_node))

    return "\n\n".join(starter_nodes).strip() + "\n"


def is_safe_top_level_assignment(node: ast.Assign) -> bool:
    if not all(isinstance(target, ast.Name) for target in node.targets):
        return False
    return isinstance(
        node.value,
        (
            ast.Constant,
            ast.List,
            ast.Tuple,
            ast.Set,
            ast.Dict,
        ),
    )


def is_solution_valid(solution: str, imports: list[str], assertions: list[str]) -> bool:
    namespace: dict[str, object] = {}
    try:
        exec(solution, namespace)
        for assertion in assertions:
            exec("\n".join(imports + [assertion]), namespace)
    except Exception:
        return False
    return True


def infer_difficulty(solution: str, assertions: list[str]) -> str:
    score = len(solution) + sum(len(assertion) for assertion in assertions)
    if score < 450:
        return "EASY"
    if score < 950:
        return "MEDIUM"
    return "HARD"


def infer_tags(prompt: str, solution: str) -> list[str]:
    text = f"{prompt}\n{solution}".lower()
    tags = []
    for needle, tag in TAG_KEYWORDS:
        if needle in text and tag not in tags:
            tags.append(tag)
    if "function" not in tags:
        tags.insert(0, "functions")
    return tags[:5] or ["functions"]


def build_title(task_id: int, prompt: str) -> str:
    title = prompt
    title = re.sub(r"^write (a|an)?\s*(python)?\s*function to\s+", "", title, flags=re.I)
    title = re.sub(r"^write (a|an)?\s*", "", title, flags=re.I)
    title = title.strip(" .")
    title = title[:1].upper() + title[1:]
    if len(title) > 72:
        title = title[:69].rstrip() + "..."
    return f"MBPP {task_id}: {title}"


def clean_text(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip()


def normalize_assertion(value: str) -> str:
    value = normalize_code(value)
    if not value.startswith("assert "):
        return ""
    return value


def normalize_code(value: str) -> str:
    return textwrap.dedent(value).replace("\r\n", "\n").replace("\r", "\n").strip()


if __name__ == "__main__":
    sys.exit(main())
