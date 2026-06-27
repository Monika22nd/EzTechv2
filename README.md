# PyQuest

## APK test nhanh

- File APK debug để cài lên điện thoại/emulator: [PyQuest-debug.apk](./PyQuest-debug.apk)
- Bản debug dùng Firebase project `eztech-v2` đã cấu hình trong `app/google-services.json`.
- Bản final demo đã build thành công bằng `.\gradlew.bat :app:assembleDebug` vào ngày 25/06/2026.

PyQuest là ứng dụng Android học lập trình Python. App có bài học dạng tutorial, video tutorial, danh sách bài tập Python, màn hình giải bài có editor, IDE Python trong app, hệ thống gợi ý học tập, EXP, level, streak, badge, leaderboard, bookmark, profile và lưu tiến độ bằng Firebase.

## Trạng thái final demo

- App đã đổi tên hiển thị thành `PyQuest`; package nội bộ vẫn giữ `com.eztech...` để ổn định Firebase config và tránh phải tạo app Firebase mới.
- Firestore là nguồn dữ liệu chính cho lessons, problems, test cases, user progress, draft, submissions, leaderboard và gamification.
- Local seed vẫn được đóng gói trong APK để app có dữ liệu fallback nếu Firestore lỗi/mất mạng.
- Firebase Storage không dùng trong bản này, vì Storage yêu cầu nâng cấp gói Firebase; app không cần upload file media.
- Google Sign-In và release signing chính thức không nằm trong phạm vi bản final demo hiện tại; app dùng Email/Password Firebase Auth và APK debug để nộp/test.
- APK debug mới nhất nằm ngay ở gốc repo: [PyQuest-debug.apk](./PyQuest-debug.apk).

## Chức năng chính

- Authentication: đăng ký, đăng nhập, quên mật khẩu bằng Firebase Authentication.
- Home dashboard: thống kê level, EXP, streak, rank, bài học tiếp theo, bài tập tiếp theo và khuyến nghị học tập.
- Learn: danh sách tutorial dạng bài viết và video tutorial Python.
- Problems: xem danh sách bài Python, tìm kiếm, lọc theo độ khó, lọc theo dạng bài/curriculum, bookmark và vào màn hình solve.
- Solve: editor Python, visible test case, custom input, submit toàn bộ test, lưu draft, lịch sử submit, import/export file `.py`.
- IDE: viết/chạy Python tự do bằng Chaquopy, có import/export file code.
- Recommendation: khuyến nghị bài học/bài tập dựa trên tiến độ, độ khó đã giải và stage còn yếu.
- Gamification: EXP, level, streak, badge và leaderboard.
- Profile/Settings: thông tin người dùng, badge, đổi tên/avatar URL, theme System/Light/Dark, notification và logout.

## Công nghệ sử dụng

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt Dependency Injection
- Kotlin Coroutines và Flow
- Firebase Authentication
- Cloud Firestore
- DataStore Preferences
- Chaquopy Python Runtime
- Coil image loading
- YouTube Player library

## Kiến trúc MVVM và Clean Architecture

App được chia theo nhiều module. Mỗi feature có `Screen`, `UiState`, `ViewModel`; domain chứa model/use case/repository interface; data implement repository và kết nối Firebase/local/Chaquopy.

```text
UI Screen / Composable
    -> gửi event người dùng
ViewModel
    -> cập nhật UiState và gọi UseCase
Domain UseCase
    -> xử lý nghiệp vụ và gọi Repository interface
Repository interface
    -> được implement ở core:data
Repository implementation
    -> đọc/ghi Firestore, local seed, DataStore hoặc Python engine
Firebase / Local Seed / Chaquopy
```

Lý do dùng cấu trúc này:

- UI không gọi Firebase trực tiếp.
- Use case có thể test bằng unit test.
- Repository có thể fallback local khi Firestore lỗi hoặc mất mạng.
- Dễ mở rộng thêm ngôn ngữ khác sau này bằng cách thêm data/use case/feature tương ứng.

## Cấu trúc thư mục và chức năng từng phần

```text
EzTechv2/
├── PyQuest-debug.apk                                             # APK debug đặt ở gốc repo để dễ tải/cài thử
├── README.md                                                     # Tài liệu project, kiến trúc, cách build/test/demo
├── firebase.json                                                 # Cấu hình Firebase deploy Firestore rules
├── firestore.rules                                               # Security rules cho Firestore
├── settings.gradle.kts                                           # Khai báo toàn bộ Gradle modules
├── build.gradle.kts                                              # Cấu hình Gradle cấp project
├── gradle.properties                                             # Cấu hình Gradle/Android build
├── local.properties                                              # SDK local path, chỉ dùng trên máy dev
│
├── app/                                                          # Android app shell, entry point và navigation tổng
│   ├── build.gradle.kts                                          # Cấu hình module app, Firebase, Hilt, dependency modules
│   ├── google-services.json                                      # Firebase config cho Android app
│   └── src/main/
│       ├── AndroidManifest.xml                                   # Manifest chính của app
│       ├── java/com/eztech/app/
│       │   ├── EzTechApplication.kt                              # Application class dùng Hilt
│       │   ├── MainActivity.kt                                   # Activity chính, gắn Compose content và theme
│       │   ├── EzTechAppViewModel.kt                             # Theo dõi auth/session/theme ở cấp app shell
│       │   └── navigation/
│       │       ├── EzTechApp.kt                                  # App shell, bottom navigation và route tổng
│       │       ├── EzTechNavHost.kt                              # Khai báo NavHost, nối route các feature
│       │       ├── NavigationExtensions.kt                       # Helper điều hướng Compose
│       │       └── TopLevelDestination.kt                        # Danh sách tab: Home, Learn, IDE, Problems, Rank, Me
│       └── res/                                                  # Resource Android của app module
│
├── core/                                                         # Các module dùng chung cho toàn app
│   ├── common/                                                   # Common utility module
│   │   └── src/main/kotlin/com/eztech/core/common/
│   │       └── Resource.kt                                       # Loading/Success/Error dùng chung cho data flow
│   │
│   ├── domain/                                                   # Business/domain layer, không phụ thuộc Firebase/UI
│   │   ├── build.gradle.kts                                      # Cấu hình module domain
│   │   └── src/main/kotlin/com/eztech/core/domain/
│   │       ├── model/                                            # Data model nghiệp vụ
│   │       │   ├── User.kt                                       # Thông tin user, level, exp, progress
│   │       │   ├── Lesson.kt                                     # Model bài học/tutorial/video
│   │       │   ├── Problem.kt                                    # Model bài tập lập trình
│   │       │   ├── TestCase.kt                                   # Model test case cho problem
│   │       │   ├── DashboardSummary.kt                           # Model dữ liệu Home dashboard
│   │       │   ├── Recommendation.kt                             # Model recommendation, stats, metric chip
│   │       │   └── PythonProblemCurriculum.kt                    # Phân loại/sort bài theo lộ trình Python
│   │       ├── repository/                                       # Repository interface để domain độc lập data source
│   │       │   ├── AuthRepository.kt                             # Interface đăng nhập/đăng ký/current user
│   │       │   ├── UserRepository.kt                             # Interface đọc/ghi profile user
│   │       │   ├── LessonRepository.kt                           # Interface bài học, progress, bookmark
│   │       │   ├── ProblemRepository.kt                          # Interface problems, test cases, submissions
│   │       │   └── GamificationRepository.kt                     # Interface EXP, badge, streak, leaderboard
│   │       └── usecase/                                          # Business logic cấp application
│   │           ├── GetDashboardSummaryUseCase.kt                 # Gom user/lesson/problem/rank cho Home
│   │           ├── ExecuteCodeUseCase.kt                         # Chạy code qua execution repository
│   │           ├── recommendation/
│   │           │   ├── GetRecommendationsUseCase.kt              # Gom dữ liệu rồi gọi engine khuyến nghị
│   │           │   └── RecommendationEngine.kt                   # Thuật toán next path, difficulty, weak stage
│   │           ├── problem/
│   │           │   ├── GetProblemsUseCase.kt                     # Lấy danh sách problems
│   │           │   ├── GetProblemDetailUseCase.kt                # Lấy chi tiết một problem
│   │           │   ├── GetVisibleTestCasesUseCase.kt             # Lấy visible test cases
│   │           │   ├── SubmitSolutionUseCase.kt                  # Chạy tất cả test case và phân loại kết quả
│   │           │   ├── RunCustomInputUseCase.kt                  # Chạy code với stdin tự nhập
│   │           │   ├── SaveCodeDraftUseCase.kt                   # Lưu draft code
│   │           │   ├── GetCodeDraftUseCase.kt                    # Đọc draft code
│   │           │   ├── RecordProblemSubmissionUseCase.kt         # Lưu lịch sử submit
│   │           │   └── GetProblemSubmissionHistoryUseCase.kt     # Đọc lịch sử submit
│   │           ├── gamification/                                 # Use case EXP, complete problem, badges
│   │           └── lesson/                                       # Use case bài học, bookmark, progress
│   │
│   ├── data/                                                     # Data layer: Firebase, local seed, DataStore, Chaquopy
│   │   ├── build.gradle.kts                                      # Cấu hình Firebase/Chaquopy/dependency data
│   │   └── src/main/
│   │       ├── assets/seed_data/
│   │       │   ├── problems.json                                 # 973 MBPP problems + 1 PyQuest test problem
│   │       │   ├── lessons.json                                  # Seed tutorial/video lesson Python
│   │       │   └── README.md                                     # Nguồn seed data và license
│   │       └── kotlin/com/eztech/core/data/
│   │           ├── di/                                           # Hilt bindings cho repository/use case/data source
│   │           ├── engine/                                       # Python execution bằng Chaquopy
│   │           ├── repository/
│   │           │   ├── AuthRepositoryImpl.kt                     # Firebase Auth implementation
│   │           │   ├── UserRepositoryImpl.kt                     # Firestore profile/user settings
│   │           │   ├── LessonRepositoryImpl.kt                   # Firestore/local lessons + progress/bookmarks
│   │           │   ├── ProblemRepositoryImpl.kt                  # Remote-first Firestore, fallback local, cache
│   │           │   └── GamificationRepositoryImpl.kt             # EXP, streak, badge, leaderboard Firestore logic
│   │           └── source/
│   │               ├── remote/                                   # Firebase data sources
│   │               │   ├── FirebaseProblemDataSource.kt          # Đọc problems/test_cases từ Firestore
│   │               │   └── FirebaseLessonDataSource.kt           # Đọc lessons/categories từ Firestore
│   │               └── local/                                    # Local fallback data sources
│   │                   ├── LocalProblemDataSource.kt             # Parse problems.json trong assets
│   │                   └── LocalLessonDataSource.kt              # Parse lessons.json trong assets
│   │
│   └── ui/                                                       # UI shared module
│       ├── build.gradle.kts                                      # Cấu hình Compose shared UI
│       └── src/main/kotlin/com/eztech/core/ui/
│           ├── component/                                        # Component dùng chung: empty state, cards, loading
│           ├── file/
│           │   ├── CodeFileIo.kt                                 # Đọc/ghi text UTF-8 qua Android ContentResolver
│           │   └── CodeFilePicker.kt                             # Shared import/export picker cho IDE và Solve screen
│           └── theme/                                            # Material 3 colors, typography, dimens, theme
│
├── feature/                                                      # Các module tính năng độc lập theo màn hình
│   ├── auth/                                                     # Đăng nhập, đăng ký, quên mật khẩu
│   │   └── src/main/kotlin/com/eztech/feature/auth/
│   │       ├── navigation/                                       # Auth routes
│   │       └── presentation/                                     # Auth screens, state, ViewModel
│   │
│   ├── home/                                                     # Dashboard và recommendation
│   │   └── src/main/kotlin/com/eztech/feature/home/
│   │       ├── navigation/                                       # Home/recommendation routes
│   │       └── presentation/
│   │           ├── HomeScreen.kt                                 # UI dashboard, progress, quick actions
│   │           ├── HomeViewModel.kt                              # Gọi dashboard + recommendation use cases
│   │           ├── HomeUiState.kt                                # State tổng cho Home
│   │           ├── component/RecommendationSection.kt            # Stats card, recommendation card, metric chip
│   │           └── recommendation/
│   │               ├── RecommendationsScreen.kt                  # Trang riêng xem tất cả recommendation
│   │               ├── RecommendationsViewModel.kt               # Load recommendation list dài hơn Home
│   │               └── RecommendationsUiState.kt                 # State trang Recommendations
│   │
│   ├── learn/                                                    # Tutorial, video, category, bookmark bài học
│   │   └── src/main/kotlin/com/eztech/feature/learn/
│   │       ├── navigation/                                       # Learn routes
│   │       └── presentation/
│   │           ├── list/                                         # Danh sách lessons/videos
│   │           ├── category/                                     # Danh mục bài học
│   │           ├── tutorial/                                     # Màn hình đọc tutorial
│   │           ├── video/                                        # Màn hình xem YouTube tutorial
│   │           ├── bookmarks/                                    # Danh sách lesson đã bookmark
│   │           └── component/                                    # Component riêng của Learn
│   │
│   ├── problems/                                                 # Danh sách, chi tiết và solve bài tập
│   │   └── src/main/kotlin/com/eztech/feature/problems/
│   │       ├── navigation/                                       # Problem routes và argument problemId
│   │       └── presentation/
│   │           ├── list/
│   │           │   ├── ProblemListScreen.kt                      # UI list, search, filter, sort
│   │           │   ├── ProblemListViewModel.kt                   # Logic filter/search/sort in-memory
│   │           │   └── ProblemListUiState.kt                     # State danh sách problem
│   │           ├── model/ProblemTypeCatalog.kt                   # Filter dạng bài theo curriculum/tag
│   │           ├── detail/                                       # Màn hình chi tiết problem
│   │           ├── solve/
│   │           │   ├── ProblemSolveScreen.kt                     # Editor, examples, custom input, submit, history
│   │           │   ├── ProblemSolveViewModel.kt                  # Load bài, autosave draft, submit, save progress
│   │           │   └── ProblemSolveUiState.kt                    # State màn hình solve
│   │           └── component/                                    # Difficulty badge, test case card, result card
│   │
│   ├── ide/                                                      # IDE Python tự do trong app
│   │   └── src/main/kotlin/com/eztech/feature/ide/
│   │       ├── navigation/                                       # IDE route
│   │       └── presentation/
│   │           ├── IdeScreen.kt                                  # Editor + console + import/export .py
│   │           ├── IdeViewModel.kt                               # Run code và cập nhật stdout/stderr
│   │           └── component/
│   │               ├── EditorToolbar.kt                          # Run, undo, redo, import, export, clear, paste
│   │               ├── CodeEditorComposable.kt                   # Code editor UI
│   │               ├── ConsoleOutputView.kt                      # Console stdout/stderr/stdin
│   │               └── QuickKeyboard.kt                          # Phím nhanh cho ký tự code
│   │
│   ├── leaderboard/                                              # Bảng xếp hạng
│   │   └── src/main/kotlin/com/eztech/feature/leaderboard/
│   │       └── presentation/                                     # Leaderboard screen/state/ViewModel
│   │
│   └── profile/                                                  # Profile, badges, edit profile, settings
│       └── src/main/kotlin/com/eztech/feature/profile/
│           ├── navigation/                                       # Profile routes
│           └── presentation/
│               ├── screen/                                       # Trang profile chính
│               ├── badges/                                       # Danh sách huy hiệu
│               ├── edit/                                         # Đổi display name/avatar URL
│               ├── settings/                                     # Theme, notification, logout
│               └── component/                                    # Component riêng của Profile
│
└── tools/                                                        # Script hỗ trợ seed/import dữ liệu
    ├── generate_mbpp_seed.py                                     # Convert MBPP thành problems.json
    ├── generate_lesson_seed.py                                   # Generate lessons.json
    └── import_seed_to_firestore.py                               # Import seed lên Firestore bằng REST API
```

## Luồng dữ liệu chính

### Home dashboard

```text
HomeScreen
    -> HomeViewModel
        -> GetDashboardSummaryUseCase
            -> AuthRepository / UserRepository / LessonRepository / ProblemRepository / GamificationRepository
        -> GetRecommendationsUseCase
            -> RecommendationEngine
```

Kết quả:

- `DashboardSummary`: level, EXP, streak, rank, lesson progress, problem progress, next lesson, next problem.
- `RecommendationDashboard`: stats gợi ý và danh sách card gợi ý.

### Problems

```text
ProblemListScreen
    -> ProblemListViewModel
        -> GetProblemsUseCase
            -> ProblemRepositoryImpl
                -> FirebaseProblemDataSource
                -> LocalProblemDataSource fallback
```

Sort/filter:

- `PythonProblemCurriculum` đưa bài về thứ tự học hợp lý.
- `ProblemTypeCatalog` tạo filter như Syntax, Operators, Conditionals, Loops, Strings, Lists,...
- Search hỗ trợ title, description, tag, stage label và số thứ tự.

### Solve problem

```text
ProblemSolveScreen
    -> ProblemSolveViewModel
        -> GetProblemDetailUseCase
        -> GetVisibleTestCasesUseCase
        -> RunCustomInputUseCase
        -> SubmitSolutionUseCase
        -> SaveCodeDraftUseCase
        -> RecordProblemSubmissionUseCase
        -> CompleteProblemUseCase
```

Khi submit:

1. Lấy toàn bộ test case của bài.
2. Ghép code người dùng với assert test.
3. Chạy bằng Python engine.
4. Phân loại Accepted/Wrong Answer/Runtime Error/Time Limit.
5. Nếu Accepted thì lưu solved problem, cộng EXP, cập nhật badge/leaderboard.

### Recommendation

```text
GetRecommendationsUseCase
    -> RecommendationEngine.generateDashboard()
        -> buildStats()
        -> learningPathRecommendations()
        -> adaptiveDifficultyRecommendations()
        -> weakStageRecommendations()
        -> lessonRecommendations()
```

Logic:

- Người mới được gợi ý bài dễ ở stage đầu như Syntax/Operators/Loops.
- Khi giải đủ Easy thì gợi ý Medium.
- Khi giải đủ Medium thì gợi ý Hard.
- Nếu ít làm một stage nào đó, app ưu tiên bài ở stage yếu.
- Card hiển thị lý do gợi ý và các thông số như Stage solved, Difficulty, Path, Weak area.

## Dữ liệu

### Trạng thái dữ liệu bản final

- Firestore đã được thiết kế là nguồn dữ liệu chính.
- Seed local gồm `973` bài MBPP Python và `1` bài test PyQuest để demo nhanh chức năng Problems.
- Lessons seed gồm tutorial dạng bài viết và video tutorial Python.
- Problems được sắp xếp lại theo lộ trình học Python bằng `PythonProblemCurriculum`, ví dụ Syntax/Operators trước rồi mới đến Loops, Strings, Lists và các dạng nâng cao.
- Problems có filter theo dạng bài thông qua `ProblemTypeCatalog`, giúp demo tìm kiếm/lọc bài dễ hơn.

### Firestore collections chính

- `programming_languages`: ngôn ngữ học, hiện dùng Python.
- `lesson_categories`: nhóm bài học.
- `lessons`: tutorial/video lessons.
- `problems`: bài tập Python.
- `problems/{problemId}/test_cases`: test case của từng bài.
- `users`: profile, EXP, level, streak.
- `users/{userId}/lessonProgress`: tiến độ bài học.
- `users/{userId}/lessonBookmarks`: bookmark bài học.
- `users/{userId}/solvedProblems`: bài đã giải.
- `users/{userId}/problemDrafts`: draft code.
- `users/{userId}/problemSubmissions/{problemId}/items`: lịch sử submit.
- `leaderboard`: bảng xếp hạng.
- `seed_metadata`: thông tin lần import seed.

### Seed local

- `core/data/src/main/assets/seed_data/problems.json`: 973 MBPP problems + 1 PyQuest test problem.
- `core/data/src/main/assets/seed_data/lessons.json`: lesson/video seed.
- App ưu tiên Firestore nhưng có fallback local nếu remote lỗi hoặc timeout.
- Bài `eztech_test_0001` được merge local nếu Firestore chưa có, giúp test chức năng Problems ngay trong APK.

## Firebase rules

Deploy rules khi `firestore.rules` thay đổi:

```powershell
firebase deploy --only firestore
```

Khi demo, nếu rules đã deploy rồi thì không cần nhập lại lệnh. Chỉ cần cài APK, đăng nhập và app tự kết nối Firestore.

## Import seed lên Firestore

Chạy ở thư mục gốc project:

```powershell
python tools/import_seed_to_firestore.py --yes --clean-problems
```

Nếu chỉ muốn kiểm tra số lượng write:

```powershell
python tools/import_seed_to_firestore.py --dry-run
```

## Build app

Chạy ở thư mục gốc project:

```powershell
.\gradlew.bat :app:assembleDebug
```

APK debug sinh ra tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

File copy dễ tải ở gốc repo:

```text
PyQuest-debug.apk
```

## Unit test

Project hiện có **36 unit test** trong **11 file test**. Unit test tập trung vào các logic chính như repository, use case, recommendation engine, submit solution, gamification, IDE, Problems và Profile. Các test dùng fake repository/fake engine để không phụ thuộc Firebase thật hoặc mạng.

| File test | Số test | Chức năng được kiểm thử |
|---|---:|---|
| `app/src/test/java/com/example/eztechv2/ExampleUnitTest.kt` | 1 | Test mẫu mặc định của Android |
| `core/data/src/test/kotlin/com/eztech/core/data/repository/CodeExecutionRepositoryImplTest.kt` | 2 | Kiểm tra repository chạy code delegate sang Python engine và trả lỗi khi engine lỗi |
| `core/data/src/test/kotlin/com/eztech/core/data/repository/ProblemRepositoryImplTest.kt` | 5 | Kiểm tra load problems, lấy problem detail, remote-first, fallback local và lỗi test cases |
| `core/domain/src/test/kotlin/com/eztech/core/domain/usecase/ExecuteCodeUseCaseTest.kt` | 2 | Kiểm tra code rỗng và việc gọi repository khi code hợp lệ |
| `core/domain/src/test/kotlin/com/eztech/core/domain/usecase/gamification/CompleteProblemUseCaseTest.kt` | 3 | Kiểm tra cộng EXP lần đầu, không cộng lại khi solve lại và daily login |
| `core/domain/src/test/kotlin/com/eztech/core/domain/usecase/problem/ProblemQueryUseCasesTest.kt` | 3 | Kiểm tra get problems, get problem detail và lọc visible test cases |
| `core/domain/src/test/kotlin/com/eztech/core/domain/usecase/problem/SubmitSolutionUseCaseTest.kt` | 7 | Kiểm tra submit solution: accepted, wrong answer, assertion harness, runtime error và timeout |
| `core/domain/src/test/kotlin/com/eztech/core/domain/usecase/recommendation/RecommendationEngineTest.kt` | 5 | Kiểm tra thuật toán recommendation theo learning path, độ khó, curriculum và lesson liên quan |
| `feature/ide/src/test/kotlin/com/eztech/feature/ide/presentation/IdeViewModelTest.kt` | 2 | Kiểm tra IDE ViewModel khi run code thành công và khi repository lỗi |
| `feature/problems/src/test/kotlin/com/eztech/feature/problems/presentation/ProblemsViewModelTest.kt` | 3 | Kiểm tra Problems list filter và solve accepted result |
| `feature/profile/src/test/kotlin/com/eztech/feature/profile/presentation/ProfileViewModelTest.kt` | 3 | Kiểm tra settings, edit profile và validate tên |

Chạy tất cả unit test:

```powershell
.\gradlew.bat test
```

Chạy nhóm test quan trọng khi demo/bảo vệ:

```powershell
.\gradlew.bat :core:data:testDebugUnitTest :core:domain:test :feature:problems:testDebugUnitTest
```

Chạy test theo từng chức năng:

```powershell
# Problems
.\gradlew.bat :feature:problems:testDebugUnitTest

# IDE
.\gradlew.bat :feature:ide:testDebugUnitTest

# Profile
.\gradlew.bat :feature:profile:testDebugUnitTest

# Data repository
.\gradlew.bat :core:data:testDebugUnitTest

# Domain use cases và recommendation
.\gradlew.bat :core:domain:test
```

Chạy riêng một file test:

```powershell
.\gradlew.bat :core:domain:test --tests "*SubmitSolutionUseCaseTest"
.\gradlew.bat :core:domain:test --tests "*RecommendationEngineTest"
.\gradlew.bat :feature:problems:testDebugUnitTest --tests "*ProblemsViewModelTest"
```

## Test nhanh chức năng Problems

Mở Problems và tìm:

```text
Test 1: Sum numbers from 1 to n
```

Code đúng để submit:

```python
def sum_to_n(n):
    total = 0
    for number in range(1, n + 1):
        total += number
    return total
```

Bài này có:

- 2 visible test case.
- 1 hidden test case.
- Tags: syntax, variables, operators, loops, functions, test-problem.
- Dùng để kiểm tra search, filter, bookmark, editor, import/export `.py`, submit, draft và lưu progress.

## Checklist demo

1. Cài [PyQuest-debug.apk](./PyQuest-debug.apk).
2. Đăng ký hoặc đăng nhập.
3. Mở Home để xem dashboard và recommendation stats.
4. Mở Learn để xem tutorial/video.
5. Mở Problems để search/filter/sort bài.
6. Vào bài test `Test 1: Sum numbers from 1 to n`.
7. Import/export file `.py`, chạy custom input, submit và xem lịch sử.
8. Mở IDE để chạy Python tự do.
9. Mở Rank/Profile để xem gamification, badge và thông tin user.

## Ghi chú phát triển

- Các file chức năng chính đã có comment/KDoc mô tả vai trò hàm và luồng xử lý.
- Không dùng Firebase Storage để tránh yêu cầu nâng cấp Blaze.
- Nếu tăng số lượng problems lớn hơn nhiều, nên bổ sung pagination/lazy loading cho Firestore để giảm lượt đọc.
- Nếu mở rộng sang MySQL/Java/C++ sau này, nên thêm `languageId`, seed riêng, engine riêng và curriculum riêng thay vì sửa cứng Python.
