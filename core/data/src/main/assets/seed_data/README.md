# EzTech Seed Data Sources

This folder contains local seed data used by the Android app.

## Problems

- File: `problems.json`
- Count: 973 MBPP problems converted from the full MBPP dataset, plus 1 EzTech manual test problem.
- Source: MBPP - Mostly Basic Python Problems
- URL: https://huggingface.co/datasets/Muennighoff/mbpp
- License: CC BY 4.0
- Notes: MBPP tasks are converted into assert-based Python practice problems. Keep attribution when importing the data into Firestore or presenting the dataset.

## Lessons

- File: `lessons.json`
- Count: 36 Python lessons across 8 categories.
- Text content: original EzTech lesson summaries written for this app.
- Reference links:
  - https://docs.python.org/3/tutorial/
  - https://www.w3schools.com/python/
  - https://www.youtube.com/
- Notes: W3Schools is linked only as a reference. Its tutorial text is not copied into this seed file.
