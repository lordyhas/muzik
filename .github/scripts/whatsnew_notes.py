#!/usr/bin/env python3
"""Prints the user-facing release notes for one version as Markdown.

Reads app/src/main/assets/whatsnew.json, the same text the app shows in "What's new".
Fails (exit 1, with a GitHub Actions error annotation) if the version has no entry,
if the entry's versionName doesn't match, or if it has no highlights.

    whatsnew_notes.py <versionCode> <versionName>
"""
import json
import sys

FILE = "app/src/main/assets/whatsnew.json"
ICONS = {"new": "✨", "improved": "⚡", "fixed": "🛠️"}


def fail(message: str) -> None:
    print(f"::error file={FILE}::{message}", file=sys.stderr)
    sys.exit(1)


def main() -> None:
    code, name = int(sys.argv[1]), sys.argv[2]
    with open(FILE, encoding="utf-8") as f:
        releases = json.load(f)["releases"]

    entry = next((r for r in releases if r["versionCode"] == code), None)
    if entry is None:
        fail(f"No \"What's new\" entry for versionCode {code}. "
             f"Add one at the top of {FILE}, in plain English for listeners.")
    if entry["versionName"] != name:
        fail(f"The entry for versionCode {code} says versionName \"{entry['versionName']}\", "
             f"but app/build.gradle.kts says \"{name}\".")
    if not entry.get("highlights"):
        fail(f"The entry for {name} has no highlights.")

    print(f"## What's new in {name}\n")
    for h in entry["highlights"]:
        icon = ICONS.get(h.get("type", ""), ICONS["improved"])
        print(f"- {icon} **{h['title']}**: {h['text']}")


if __name__ == "__main__":
    main()
