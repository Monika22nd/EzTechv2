# Import EzTech Seed Data To Firestore

Current Firebase project: `eztech-v2`

## 1. Create Firestore First

The Firebase project currently has no default Firestore database. Open Firebase Console:

1. Build > Firestore Database.
2. Create database.
3. Choose Native mode.
4. Choose a region near you.
5. For the first import, choose test mode or otherwise allow temporary writes.

## 2. Import Seed Data

From the project root:

```powershell
python tools\import_seed_to_firestore.py --yes
```

Dry run:

```powershell
python tools\import_seed_to_firestore.py --dry-run
```

The importer writes:

- `programming_languages`
- `lesson_categories`
- `lessons`
- `problems`
- `problems/{problemId}/test_cases`
- `seed_metadata`

Expected current size:

- 973 MBPP problems
- More than 2,900 test cases
- 220 lessons (160 Corey Schafer videos + 60 written tutorials)
- 20 lesson categories

## 3. Secure Rules After Import

After the data is visible in Firestore, authenticate and deploy `firestore.rules` with Firebase CLI:

```powershell
npx --yes firebase-tools login
npx --yes firebase-tools deploy --only firestore:rules
```

The rules make lessons/problems readable by signed-in users and block normal client writes to seed data.

At runtime, Firestore is the primary source for lessons, problems, test cases, and user
progress. The bundled JSON assets are retained only as an offline fallback.
