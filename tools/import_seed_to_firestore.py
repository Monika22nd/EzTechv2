import argparse
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


PROJECT_ROOT = Path(__file__).resolve().parents[1]
GOOGLE_SERVICES_PATH = PROJECT_ROOT / "app/google-services.json"
PROBLEMS_PATH = PROJECT_ROOT / "core/data/src/main/assets/seed_data/problems.json"
LESSONS_PATH = PROJECT_ROOT / "core/data/src/main/assets/seed_data/lessons.json"
BATCH_SIZE = 400
DEFAULT_PROBLEM_LIMIT = 1000


def main() -> int:
    args = parse_args()
    firebase_config = load_firebase_config()
    project_id = args.project_id or firebase_config["project_id"]
    api_key = args.api_key or firebase_config["api_key"]

    writes = build_writes(
        project_id=project_id,
        limit_problems=args.limit_problems,
    )
    print(f"Project: {project_id}")
    print(f"Prepared {len(writes)} Firestore writes.")

    if args.dry_run:
        print("Dry run only. No data was uploaded.")
        return 0

    if not args.yes:
        print("Add --yes to confirm importing seed data.")
        return 2

    if args.clean_lessons:
        clean_writes = build_clean_writes(
            project_id=project_id,
            api_key=api_key,
            collection_ids=("lesson_categories", "lessons"),
        )
        deleted = 0
        for batch_index, batch in enumerate(chunked(clean_writes, BATCH_SIZE), start=1):
            commit_writes(project_id, api_key, batch)
            deleted += len(batch)
            print(f"Deleted old lesson batch {batch_index}: {deleted}/{len(clean_writes)} docs")
            time.sleep(0.2)

    if args.clean_problems:
        clean_writes = build_problem_clean_writes(
            project_id=project_id,
            api_key=api_key,
        )
        deleted = 0
        for batch_index, batch in enumerate(chunked(clean_writes, BATCH_SIZE), start=1):
            commit_writes(project_id, api_key, batch)
            deleted += len(batch)
            print(f"Deleted old problem batch {batch_index}: {deleted}/{len(clean_writes)} docs")
            time.sleep(0.2)

    imported = 0
    for batch_index, batch in enumerate(chunked(writes, BATCH_SIZE), start=1):
        commit_writes(project_id, api_key, batch)
        imported += len(batch)
        print(f"Imported batch {batch_index}: {imported}/{len(writes)} writes")
        time.sleep(0.2)

    print("Seed data import finished.")
    return 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Import EzTech local seed data into Cloud Firestore.",
    )
    parser.add_argument("--project-id", help="Firebase project ID. Defaults to google-services.json.")
    parser.add_argument("--api-key", help="Firebase Web/API key. Defaults to google-services.json.")
    parser.add_argument(
        "--limit-problems",
        type=int,
        default=DEFAULT_PROBLEM_LIMIT,
        help=f"Import only the first N problems. Defaults to {DEFAULT_PROBLEM_LIMIT}.",
    )
    parser.add_argument("--dry-run", action="store_true", help="Print counts without uploading.")
    parser.add_argument("--yes", action="store_true", help="Confirm the import.")
    parser.add_argument(
        "--clean-lessons",
        action="store_true",
        help="Delete current lesson and lesson_category docs before importing them again.",
    )
    parser.add_argument(
        "--clean-problems",
        action="store_true",
        help="Delete current problem docs before importing them again.",
    )
    return parser.parse_args()


def load_firebase_config() -> dict[str, str]:
    config = json.loads(GOOGLE_SERVICES_PATH.read_text(encoding="utf-8"))
    return {
        "project_id": config["project_info"]["project_id"],
        "api_key": config["client"][0]["api_key"][0]["current_key"],
    }


def build_writes(project_id: str, limit_problems: int | None) -> list[dict[str, Any]]:
    problems_seed = json.loads(PROBLEMS_PATH.read_text(encoding="utf-8"))
    lessons_seed = json.loads(LESSONS_PATH.read_text(encoding="utf-8"))

    problems = problems_seed["problems"]
    if limit_problems is not None:
        problems = problems[:limit_problems]
    problem_ids = {problem["id"] for problem in problems}
    test_cases = [
        test_case for test_case in problems_seed["testCases"]
        if test_case["problemId"] in problem_ids
    ]

    writes: list[dict[str, Any]] = []
    for language in lessons_seed["languages"]:
        writes.append(update_doc(project_id, f"programming_languages/{language['id']}", language))
    for category in lessons_seed["categories"]:
        writes.append(update_doc(project_id, f"lesson_categories/{category['id']}", category))
    for lesson in lessons_seed["lessons"]:
        writes.append(update_doc(project_id, f"lessons/{lesson['id']}", lesson))
    for order, problem in enumerate(problems, start=1):
        numbered_problem = {**problem, "order": order}
        writes.append(update_doc(project_id, f"problems/{problem['id']}", numbered_problem))
    for test_case in test_cases:
        path = f"problems/{test_case['problemId']}/test_cases/{test_case['id']}"
        writes.append(update_doc(project_id, path, test_case))

    metadata = {
        "id": "eztech_python_seed_v1",
        "problemCount": len(problems),
        "testCaseCount": len(test_cases),
        "lessonCount": len(lessons_seed["lessons"]),
        "lessonCategoryCount": len(lessons_seed["categories"]),
        "source": "Local EzTech seed data generated from MBPP and original lesson summaries.",
        "importedAt": datetime.now(timezone.utc).isoformat(),
    }
    writes.append(update_doc(project_id, "seed_metadata/eztech_python_seed_v1", metadata))
    return writes


def build_clean_writes(
    project_id: str,
    api_key: str,
    collection_ids: tuple[str, ...],
) -> list[dict[str, Any]]:
    writes: list[dict[str, Any]] = []
    for collection_id in collection_ids:
        for document_name_value in list_collection_document_names(
            project_id=project_id,
            api_key=api_key,
            collection_id=collection_id,
        ):
            writes.append({"delete": document_name_value})
    return writes


def build_problem_clean_writes(
    project_id: str,
    api_key: str,
) -> list[dict[str, Any]]:
    problem_names = list_collection_document_names(
        project_id=project_id,
        api_key=api_key,
        collection_id="problems",
    )
    writes: list[dict[str, Any]] = []
    for problem_name in problem_names:
        problem_id = problem_name.rsplit("/", 1)[-1]
        test_case_names = list_collection_document_names(
            project_id=project_id,
            api_key=api_key,
            collection_id=f"problems/{problem_id}/test_cases",
        )
        writes.extend({"delete": test_case_name} for test_case_name in test_case_names)
        writes.append({"delete": problem_name})
    return writes


def list_collection_document_names(
    project_id: str,
    api_key: str,
    collection_id: str,
) -> list[str]:
    document_names: list[str] = []
    page_token = ""
    while True:
        query = {
            "pageSize": "300",
            "key": api_key,
        }
        if page_token:
            query["pageToken"] = page_token
        url = (
            f"https://firestore.googleapis.com/v1/projects/{project_id}"
            f"/databases/(default)/documents/{collection_id}?"
            f"{urllib.parse.urlencode(query)}"
        )
        request = urllib.request.Request(url, method="GET")
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                payload = json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as error:
            if error.code == 404:
                return document_names
            body = error.read().decode("utf-8", errors="replace")
            raise SystemExit(format_firestore_error(error.code, body)) from error

        document_names.extend(
            document["name"]
            for document in payload.get("documents", [])
            if "name" in document
        )
        page_token = payload.get("nextPageToken", "")
        if not page_token:
            return document_names


def update_doc(project_id: str, document_path: str, data: dict[str, Any]) -> dict[str, Any]:
    return {
        "update": {
            "name": document_name(project_id, document_path),
            "fields": firestore_fields(data),
        }
    }


def document_name(project_id: str, document_path: str) -> str:
    return (
        f"projects/{project_id}/databases/(default)/documents/"
        f"{document_path.strip('/')}"
    )


def commit_writes(project_id: str, api_key: str, writes: list[dict[str, Any]]) -> None:
    url = (
        f"https://firestore.googleapis.com/v1/projects/{project_id}"
        f"/databases/(default)/documents:commit?key={api_key}"
    )
    body = json.dumps({"writes": writes}).encode("utf-8")
    request = urllib.request.Request(
        url,
        data=body,
        method="POST",
        headers={"Content-Type": "application/json"},
    )
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            if response.status >= 300:
                raise RuntimeError(f"Firestore returned HTTP {response.status}.")
    except urllib.error.HTTPError as error:
        payload = error.read().decode("utf-8", errors="replace")
        raise SystemExit(format_firestore_error(error.code, payload)) from error


def format_firestore_error(status_code: int, payload: str) -> str:
    message = payload
    try:
        message = json.loads(payload)["error"]["message"]
    except Exception:
        pass
    if status_code == 404 and "database (default) does not exist" in message:
        return (
            "Firestore database has not been created yet.\n"
            "Open Firebase Console > Build > Firestore Database > Create database, "
            "choose Native mode, then run this script again."
        )
    if status_code == 403:
        return (
            "Firestore rejected the import with PERMISSION_DENIED.\n"
            "For this REST importer, create Firestore in test mode temporarily, "
            "or use an Admin SDK/service-account importer.\n"
            f"Original error: {message}"
        )
    return f"Firestore import failed with HTTP {status_code}: {message}"


def firestore_fields(data: dict[str, Any]) -> dict[str, Any]:
    return {key: firestore_value(value) for key, value in data.items()}


def firestore_value(value: Any) -> dict[str, Any]:
    if value is None:
        return {"nullValue": None}
    if isinstance(value, bool):
        return {"booleanValue": value}
    if isinstance(value, int) and not isinstance(value, bool):
        return {"integerValue": str(value)}
    if isinstance(value, float):
        return {"doubleValue": value}
    if isinstance(value, str):
        return {"stringValue": value}
    if isinstance(value, list):
        return {"arrayValue": {"values": [firestore_value(item) for item in value]}}
    if isinstance(value, dict):
        return {"mapValue": {"fields": firestore_fields(value)}}
    return {"stringValue": str(value)}


def chunked(items: list[dict[str, Any]], size: int):
    for index in range(0, len(items), size):
        yield items[index:index + size]


if __name__ == "__main__":
    sys.exit(main())
