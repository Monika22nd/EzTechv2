# EzTechv2

## APK test nhanh

- File APK debug để cài lên điện thoại/emulator: [EzTechv2-debug.apk](./EzTechv2-debug.apk)
- Bản debug dùng Firebase project `eztech-v2` đã cấu hình trong `app/google-services.json`.

EzTechv2 là ứng dụng Android học lập trình Python. App có bài học dạng tutorial, video tutorial, danh sách bài tập Python, màn hình giải bài có editor, IDE Python trong app, hệ thống gợi ý học tập, EXP, level, streak, badge, leaderboard, bookmark, profile và lưu tiến độ bằng Firebase.

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
├── EzTechv2-debug.apk                                            # APK debug đặt ở gốc repo để dễ tải/cài thử
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
│   │       │   ├── problems.json                                 # 973 MBPP problems + 1 EzTech test problem
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
│           │   └── CodeFileIo.kt                                 # Import/export text UTF-8 qua Android document picker
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

- `core/data/src/main/assets/seed_data/problems.json`: 973 MBPP problems + 1 EzTech test problem.
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
EzTechv2-debug.apk
```

## Test

Các lệnh test thường dùng:

```powershell
.\gradlew.bat :core:domain:test --tests "*RecommendationEngineTest*"
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :feature:home:testDebugUnitTest :feature:problems:testDebugUnitTest
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

1. Cài [EzTechv2-debug.apk](./EzTechv2-debug.apk).
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
