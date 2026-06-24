from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path
from typing import Any


PROJECT_ROOT = Path(__file__).resolve().parents[1]
LESSONS_PATH = PROJECT_ROOT / "core/data/src/main/assets/seed_data/lessons.json"
CACHE_DIR = PROJECT_ROOT / "tools/cache"
COREY_CHANNEL_CACHE = CACHE_DIR / "corey_channel_videos.json"
COREY_PYTHON_CACHE = CACHE_DIR / "corey_python_tutorials.json"

COREY_CHANNEL_URL = "https://www.youtube.com/@coreyms/videos"
COREY_PYTHON_PLAYLIST_URL = (
    "https://www.youtube.com/playlist?list=PL-osiE80TeTt2d9bfVyTiXJA-UTHn6WwU"
)
COREY_CHANNEL_PAGE = "https://www.youtube.com/@coreyms"

VIDEO_CATEGORY_ID = "corey_schafer_python_videos"

RELEVANT_VIDEO_KEYWORDS = (
    "python",
    "django",
    "flask",
    "fastapi",
    "pandas",
    "matplotlib",
    "tkinter",
    "jupyter",
    "anaconda",
    "sublime text",
    "visual studio code",
    "vscode",
    "git",
    "github",
    "sql",
    "database",
    "sqlite",
    "postgres",
    "command line",
    "terminal",
)

TUTORIAL_CATEGORIES = [
    ("python_getting_started", "Getting Started", "Install Python and write your first scripts."),
    ("python_syntax_basics", "Syntax", "Read Python statements, indentation, and errors."),
    ("python_variables", "Variables", "Store values with clear names and convert user input."),
    ("python_data_types", "Data Types", "Understand numbers, text, booleans, and None."),
    ("python_numbers", "Numbers", "Use arithmetic, rounding, and math helpers."),
    ("python_strings", "Strings", "Work with text, slicing, formatting, and methods."),
    ("python_lists", "Lists (Arrays)", "Use Python's array-like list collection."),
    ("python_tuples_sets", "Tuples and Sets", "Model fixed records and unique values."),
    ("python_dictionaries", "Dictionaries", "Map keys to values for lookup and counting."),
    ("python_control_flow", "If...Else", "Choose different paths with conditions."),
    ("python_loops", "Loops", "Repeat work with for loops and while loops."),
    ("python_functions", "Functions", "Package logic into testable reusable blocks."),
    ("python_oop", "Classes and Objects", "Model data and behavior with objects."),
    ("python_files_modules", "Files and Modules", "Read files and split code across modules."),
    ("python_exceptions", "Exceptions", "Handle expected failures clearly."),
    ("python_json", "JSON", "Read and write structured JSON data."),
    ("python_datetime", "Date and Time", "Use dates, times, and formatting."),
    ("python_testing", "Testing", "Check code with asserts and unit tests."),
    ("python_problem_solving", "Problem Solving", "Turn coding prompts into small steps."),
]

TUTORIAL_TOPICS = [
    ("python_getting_started", "intro", "What Python is used for", "See where Python fits in apps, scripts, data, web backends, and automation.", "Python is a general purpose language with a small readable syntax. In EzTech, use it first for problem solving and small console programs.", "List three tasks you could automate with Python."),
    ("python_getting_started", "install_setup", "Install Python and run a file", "Install Python, check the version, and run a .py file from a terminal.", "Use python --version to confirm the install. Create one small file, save it, then run python filename.py.", "Create hello.py and print your name."),
    ("python_getting_started", "first_program", "Your first Python program", "Use print, comments, and simple expressions to understand output.", "A program runs from top to bottom. print() sends text to the console. Comments start with # and explain intent.", "Print three lines about yourself."),
    ("python_getting_started", "input_output", "Input and output", "Read text with input() and display results with print().", "input() always returns a string. Convert it with int() or float() before arithmetic.", "Read a name and age, then print a greeting."),
    ("python_syntax_basics", "indentation", "Indentation", "Use indentation to show which lines belong together.", "Python uses indentation instead of braces. Keep one consistent indent level, usually four spaces.", "Fix an if statement with incorrect indentation."),
    ("python_syntax_basics", "statements", "Statements and expressions", "Understand the difference between instructions and values.", "A statement does work. An expression produces a value. Many beginner bugs come from using one where the other is expected.", "Predict the value of 2 + 3 * 4."),
    ("python_syntax_basics", "comments", "Comments", "Write short comments that explain why code exists.", "Comments start with #. Use them for intent, edge cases, and reminders, not for repeating obvious code.", "Add helpful comments to a small calculator."),
    ("python_syntax_basics", "common_errors", "Common syntax errors", "Recognize missing colons, quotes, parentheses, and indentation mistakes.", "When code fails before running, read the line number and also inspect the line just above it.", "Fix three broken snippets by reading their error messages."),
    ("python_variables", "variables_assignment", "Variables and assignment", "Store values with meaningful names.", "A variable points to a value. Assignment uses =. Comparison uses ==.", "Calculate a rectangle area from width and height."),
    ("python_variables", "naming", "Naming style", "Choose names that make code easy to scan.", "Use lowercase_with_underscores for variables and functions. Prefer total_score over x when meaning matters.", "Rename unclear variables in a small program."),
    ("python_variables", "casting", "Casting input", "Convert strings into numbers or other types when needed.", "Use int(), float(), str(), and bool() intentionally. Convert near the input boundary so later code receives the right type.", "Read two numbers as strings and print their sum."),
    ("python_data_types", "type_checking", "Check data types", "Use type() and isinstance() while debugging.", "type(value) shows the exact type. isinstance(value, int) is better when checking categories of values.", "Print the type of five different values."),
    ("python_data_types", "booleans", "Booleans", "Represent true or false conditions.", "Comparisons such as age >= 18 return booleans. Combine them with and, or, and not.", "Check whether a score is between 0 and 100."),
    ("python_data_types", "none", "None", "Represent an intentionally missing value.", "None is not 0, an empty string, or False. Use is None when checking for missing values.", "Return None when a search does not find a match."),
    ("python_numbers", "integers_floats", "Integers and floats", "Use whole numbers and decimal numbers correctly.", "int is exact for whole numbers. float is approximate for decimal calculations.", "Read two prices and print the total."),
    ("python_numbers", "operators", "Math operators", "Use +, -, *, /, //, %, and **.", "// gives floor division. % gives the remainder. ** calculates powers.", "Print quotient, remainder, and square of a number."),
    ("python_numbers", "rounding", "Rounding numbers", "Round values for display without changing core logic too early.", "round(value, 2) is useful for display. Keep exact values while calculating, then format at the edge.", "Print an average rounded to two decimals."),
    ("python_strings", "string_basics", "String basics", "Create, combine, and measure text.", "Strings are sequences of characters. Use len() for length and + or f-strings to combine text.", "Print the length of a username."),
    ("python_strings", "slicing", "Indexing and slicing", "Read parts of a string with indexes and ranges.", "Indexes start at 0. Slices use start:stop and do not include the stop index.", "Print the first, last, and reversed version of a word."),
    ("python_strings", "f_strings", "f-strings", "Format variables into readable output.", "Put f before the string and write variables inside braces. This keeps output clear.", "Print 'Alice scored 95 points' from variables."),
    ("python_strings", "string_methods", "String methods", "Use built-in helpers like strip, lower, split, and replace.", "Methods return new strings because strings are immutable. Assign the result if you need to keep it.", "Normalize an email by trimming spaces and lowercasing it."),
    ("python_lists", "list_basics", "List basics", "Store ordered, changeable data.", "A Python list is the everyday array-like container. Use append(), indexing, slicing, and loops.", "Store five scores and print the highest."),
    ("python_lists", "list_methods", "List methods", "Add, remove, sort, and count items.", "append adds one item, extend adds many, remove deletes by value, and pop removes by position.", "Remove negative numbers from a list."),
    ("python_lists", "list_comprehensions", "List comprehensions", "Build lists from existing data compactly.", "A comprehension is a loop expression. Use it when it stays readable.", "Create a list of squares from 1 to 10."),
    ("python_lists", "nested_lists", "Nested lists", "Represent grids and tables.", "A nested list is a list of lists. Access values with two indexes, such as grid[row][col].", "Print every value in a 3 by 3 grid."),
    ("python_tuples_sets", "tuples", "Tuples", "Represent fixed groups of values.", "Tuples are immutable. Use them for coordinates, pairs, and multiple return values.", "Return both min and max from a function."),
    ("python_tuples_sets", "unpacking", "Unpacking", "Assign multiple values from a tuple or list.", "Unpacking makes structured values readable. The number of variables must match the values.", "Unpack a name and score from a pair."),
    ("python_tuples_sets", "sets", "Sets", "Keep unique values and compare groups.", "Sets remove duplicates and support union, intersection, and difference.", "Find common items between two lists."),
    ("python_dictionaries", "dict_basics", "Dictionary basics", "Map keys to values.", "Dictionaries are ideal for records, lookup tables, and counting. Keys should be stable values such as strings or numbers.", "Create a profile dictionary with name and age."),
    ("python_dictionaries", "dict_loops", "Loop through dictionaries", "Read keys and values clearly.", "Use items() when you need both key and value. Use get() when a key may be missing.", "Print each product name and price."),
    ("python_dictionaries", "counting", "Counting with dictionaries", "Count frequency of values.", "For each item, update a count. This pattern appears often in coding interviews and data cleanup.", "Count words in a sentence."),
    ("python_control_flow", "if_else", "if, elif, and else", "Choose a branch based on conditions.", "Use if for the first condition, elif for alternatives, and else as the fallback.", "Classify a score as fail, pass, or excellent."),
    ("python_control_flow", "comparison_chains", "Comparison chains", "Write readable range checks.", "Python allows 0 <= score <= 100. This is clearer than two separate comparisons.", "Check whether a temperature is in a safe range."),
    ("python_control_flow", "match_case", "match and case", "Choose behavior from known patterns.", "match is useful for command-style values. Keep cases simple when learning.", "Handle commands start, stop, and help."),
    ("python_loops", "for_loops", "for loops", "Repeat work over ranges and collections.", "A for loop visits each item in a sequence. range(n) gives 0 through n - 1.", "Sum all even numbers from 1 to n."),
    ("python_loops", "while_loops", "while loops", "Repeat while a condition remains true.", "A while loop is useful when you do not know the number of repeats in advance.", "Keep asking for input until the user types quit."),
    ("python_loops", "break_continue", "break and continue", "Control loop flow.", "break exits the loop. continue jumps to the next iteration. Use them when they simplify the loop.", "Find the first number divisible by 7."),
    ("python_functions", "define_functions", "Define functions", "Create named blocks of reusable logic.", "A function has a name, parameters, and a body. return sends a value back to the caller.", "Write a function that returns the larger of two numbers."),
    ("python_functions", "parameters", "Parameters and defaults", "Pass data into functions safely.", "Parameters are names inside the function. Defaults are useful when one value is common.", "Write a greeting function with a default language."),
    ("python_functions", "return_values", "Return values", "Design functions that are easy to test.", "Returning is different from printing. A returned value can be reused by other code.", "Return a list of squares from a list of numbers."),
    ("python_functions", "scope", "Scope", "Understand local and global names.", "Variables created inside a function are local. Prefer returning values instead of changing globals.", "Fix a function that accidentally depends on a global variable."),
    ("python_oop", "classes", "Classes and objects", "Group data and behavior into a custom type.", "A class defines the shape. An object is an instance. __init__ sets starting attributes.", "Create a Student class with name and score."),
    ("python_oop", "methods", "Methods and self", "Attach behavior to objects.", "Methods are functions inside classes. self refers to the current object.", "Add a passed() method to Student."),
    ("python_oop", "inheritance", "Inheritance basics", "Share behavior between related classes.", "Inheritance can remove duplication, but use it only when objects truly have an is-a relationship.", "Create Animal and Dog classes."),
    ("python_oop", "dunder_methods", "Useful dunder methods", "Customize string display and equality.", "__str__ makes objects readable when printed. __repr__ helps debugging.", "Implement __str__ for a Book class."),
    ("python_files_modules", "read_files", "Read files", "Use with open(...) to read text safely.", "The with block closes files automatically. Read small files with read(), or loop line by line.", "Read all lines from notes.txt."),
    ("python_files_modules", "write_files", "Write files", "Save text output from a program.", "Use mode 'w' to overwrite and 'a' to append. Be careful with paths when running from different folders.", "Write a list of names to a file."),
    ("python_files_modules", "imports", "Imports and modules", "Split code across files and use standard library modules.", "import gives access to another module. Keep imports at the top so dependencies are visible.", "Use math.sqrt and random.randint in a script."),
    ("python_files_modules", "paths", "Paths", "Work with file and folder paths.", "pathlib.Path gives readable path operations across operating systems.", "Build a path to a data file beside your script."),
    ("python_exceptions", "try_except", "try and except", "Handle expected failures without crashing.", "Put risky code in try and recovery logic in except. Catch specific exceptions when possible.", "Keep asking for a number until parsing succeeds."),
    ("python_exceptions", "finally_else", "else and finally", "Run cleanup or success-only logic.", "else runs when no exception occurs. finally runs whether an exception occurred or not.", "Print a cleanup message after reading a file."),
    ("python_json", "json_loads_dumps", "JSON strings", "Convert between JSON text and Python data.", "json.loads parses a string. json.dumps creates a JSON string.", "Turn a profile dictionary into JSON text."),
    ("python_json", "json_files", "JSON files", "Save and load structured data files.", "Use json.dump for files and json.load to read them back.", "Save app settings to settings.json."),
    ("python_datetime", "datetime_basics", "datetime basics", "Represent dates and times.", "The datetime module gives date, time, datetime, and timedelta types.", "Print today's date."),
    ("python_datetime", "format_dates", "Format dates", "Convert dates to and from readable strings.", "strftime formats a datetime. strptime parses a string into a datetime.", "Parse '2026-06-24' into a date."),
    ("python_testing", "asserts", "Assertions", "Check assumptions while learning.", "assert stops the program if a condition is false. It is useful for tiny practice checks.", "Assert that add(2, 3) equals 5."),
    ("python_testing", "unit_tests", "Unit tests", "Test functions in repeatable files.", "Unit tests call functions with known inputs and compare the output to expected values.", "Write two tests for a max_of_two function."),
    ("python_problem_solving", "read_prompt", "Read the prompt", "Turn a coding prompt into inputs, outputs, and constraints.", "Before coding, identify what data comes in, what must go out, and which edge cases matter.", "Annotate a two-sum style prompt."),
    ("python_problem_solving", "small_steps", "Solve in small steps", "Break a problem into helper decisions.", "Write a simple version first, then improve. Print intermediate values while learning.", "Solve a list sum problem before adding validation."),
    ("python_problem_solving", "debugging", "Debugging practice", "Use tracebacks and small examples to find bugs.", "Debugging is narrowing the problem. Reproduce it, inspect values, change one thing, and run again.", "Fix an off-by-one loop bug."),
]


def main() -> int:
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    videos = build_corey_video_entries()
    if len(videos) < 160:
        print(f"Warning: only found {len(videos)} Corey videos; expected 160.", file=sys.stderr)
    videos = videos[:160]

    language = {
        "id": "python",
        "name": "Python",
        "description": "Learn Python with Corey Schafer videos, written tutorials, and practice problems.",
        "order": 1,
        "isEnabled": True,
    }

    categories = build_categories()
    lessons = build_video_lessons(videos) + build_tutorial_lessons()

    output = {
        "languages": [language],
        "categories": categories,
        "lessons": lessons,
        "sources": [
            {
                "name": "Corey Schafer YouTube Channel",
                "url": COREY_CHANNEL_PAGE,
                "license": "Linked video metadata and YouTube embeds only.",
            },
            {
                "name": "Corey Schafer Python Tutorials Playlist",
                "url": COREY_PYTHON_PLAYLIST_URL,
                "license": "Linked video metadata and YouTube embeds only.",
            },
            {
                "name": "Python Documentation Tutorial",
                "url": "https://docs.python.org/3/tutorial/",
                "license": "PSF License Version 2.",
            },
            {
                "name": "W3Schools Python Tutorial",
                "url": "https://www.w3schools.com/python/",
                "license": "Used only as topic inspiration; tutorial text is original EzTech content.",
            },
        ],
    }
    LESSONS_PATH.write_text(
        json.dumps(output, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    tutorial_count = len(lessons) - len(videos)
    print(
        f"Wrote {len(categories)} categories, {len(videos)} videos, "
        f"and {tutorial_count} written tutorials."
    )
    return 0


def build_corey_video_entries() -> list[dict[str, Any]]:
    playlist = load_youtube_json(COREY_PYTHON_CACHE, COREY_PYTHON_PLAYLIST_URL)
    channel = load_youtube_json(COREY_CHANNEL_CACHE, COREY_CHANNEL_URL)
    ordered: list[dict[str, Any]] = []
    seen_ids: set[str] = set()

    for entry in playlist.get("entries", []):
        add_video(entry, ordered, seen_ids)
    for entry in channel.get("entries", []):
        title = str(entry.get("title") or "").lower()
        if any(keyword in title for keyword in RELEVANT_VIDEO_KEYWORDS):
            add_video(entry, ordered, seen_ids)

    return ordered


def add_video(
    entry: dict[str, Any],
    ordered: list[dict[str, Any]],
    seen_ids: set[str],
) -> None:
    video_id = str(entry.get("id") or "").strip()
    title = str(entry.get("title") or "").strip()
    if not video_id or not title or video_id in seen_ids:
        return
    seen_ids.add(video_id)
    ordered.append(
        {
            "id": video_id,
            "title": title,
            "duration": int(float(entry.get("duration") or 0)),
            "url": entry.get("url") or f"https://www.youtube.com/watch?v={video_id}",
        }
    )


def load_youtube_json(cache_path: Path, url: str) -> dict[str, Any]:
    if cache_path.exists():
        return read_json(cache_path)

    try:
        completed = subprocess.run(
            [sys.executable, "-m", "yt_dlp", "--flat-playlist", "--dump-single-json", url],
            check=True,
            capture_output=True,
            text=True,
            encoding="utf-8",
            timeout=240,
        )
    except Exception as error:
        print(f"Warning: could not fetch {url}: {error}", file=sys.stderr)
        return {"entries": []}

    cache_path.write_text(completed.stdout, encoding="utf-8")
    return json.loads(completed.stdout)


def read_json(path: Path) -> dict[str, Any]:
    for encoding in ("utf-8", "utf-8-sig", "utf-16"):
        try:
            return json.loads(path.read_text(encoding=encoding))
        except (UnicodeError, json.JSONDecodeError):
            continue
    raise ValueError(f"Could not read JSON cache: {path}")


def build_categories() -> list[dict[str, Any]]:
    categories = [
        {
            "id": VIDEO_CATEGORY_ID,
            "languageId": "python",
            "name": "Corey Schafer Videos",
            "description": "A curated 160-video programming track from Corey Schafer.",
            "type": "VIDEO",
            "order": 1,
        }
    ]
    categories.extend(
        {
            "id": category_id,
            "languageId": "python",
            "name": name,
            "description": description,
            "type": "TUTORIAL",
            "order": index,
        }
        for index, (category_id, name, description) in enumerate(TUTORIAL_CATEGORIES, start=2)
    )
    return categories


def build_video_lessons(videos: list[dict[str, Any]]) -> list[dict[str, Any]]:
    lessons = []
    for index, video in enumerate(videos, start=1):
        video_id = video["id"]
        lessons.append(
            {
                "id": f"corey_video_{safe_id(video_id)}",
                "languageId": "python",
                "categoryId": VIDEO_CATEGORY_ID,
                "title": video["title"],
                "description": "Corey Schafer video tutorial for Python and related developer skills.",
                "content": "",
                "type": "VIDEO",
                "sourceName": "Corey Schafer",
                "videoId": video_id,
                "durationSeconds": video["duration"],
                "order": index,
                "referenceUrls": [video["url"]],
            }
        )
    return lessons


def build_tutorial_lessons() -> list[dict[str, Any]]:
    category_order = {category_id: 0 for category_id, _, _ in TUTORIAL_CATEGORIES}
    lessons = []
    for category_id, slug, title, description, key_idea, practice in TUTORIAL_TOPICS:
        category_order[category_id] += 1
        lessons.append(
            {
                "id": f"tutorial_{slug}",
                "languageId": "python",
                "categoryId": category_id,
                "title": title,
                "description": description,
                "content": build_content(key_idea, practice),
                "type": "TUTORIAL",
                "sourceName": "EzTech Tutorial",
                "videoId": "",
                "durationSeconds": 0,
                "order": category_order[category_id],
                "referenceUrls": [
                    "https://docs.python.org/3/tutorial/",
                    "https://www.w3schools.com/python/",
                ],
            }
        )
    return lessons


def build_content(key_idea: str, practice: str) -> str:
    return (
        "Key idea\n"
        f"{key_idea}\n\n"
        "Beginner checklist\n"
        "- Identify the input, output, and important variables.\n"
        "- Run one tiny example before the full solution.\n"
        "- Read errors from the first useful line, then fix one thing at a time.\n\n"
        "Practice\n"
        f"{practice}"
    )


def safe_id(value: str) -> str:
    return re.sub(r"[^A-Za-z0-9_-]+", "_", value).strip("_")


if __name__ == "__main__":
    raise SystemExit(main())
