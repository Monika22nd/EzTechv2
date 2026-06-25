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
|-- EzTechv2-debug.apk
|   |-- APK debug đặt ở gốc repo để dễ tải/cài thử.
|
|-- app/
|   |-- build.gradle.kts
|   |   |-- Cấu hình module app, Firebase Google Services, Hilt và module dependency.
|   |-- google-services.json
|   |   |-- File cấu hình Firebase Android app.
|   |-- src/main/java/com/eztech/app/
|   |   |-- EzTechApplication.kt
|   |   |   |-- Application class dùng Hilt.
|   |   |-- MainActivity.kt
|   |   |   |-- Activity chính, gọi Compose content và theme.
|   |   |-- EzTechAppViewModel.kt
|   |   |   |-- Theo dõi auth/session/theme ở cấp app shell.
|   |   |-- navigation/
|   |   |   |-- EzTechApp.kt
|   |   |   |   |-- App shell, bottom navigation và route tổng.
|   |   |   |-- EzTechNavHost.kt
|   |   |   |   |-- Khai báo NavHost và liên kết các feature.
|   |   |   |-- NavigationExtensions.kt
|   |   |   |   |-- Helper điều hướng Compose.
|   |   |   |-- TopLevelDestination.kt
|   |   |   |   |-- Danh sách tab chính: Home, Learn, IDE, Problems, Rank, Me.
|
|-- core/
|   |-- common/
|   |   |-- Resource.kt
|   |   |   |-- Kiểu Loading/Success/Error dùng chung cho repository và ViewModel.
|   |
|   |-- domain/
|   |   |-- model/
|   |   |   |-- User.kt, Lesson.kt, Problem.kt, TestCase.kt, DashboardSummary.kt
|   |   |   |   |-- Các model nghiệp vụ độc lập Firebase/UI.
|   |   |   |-- Recommendation.kt
|   |   |   |   |-- Model dashboard gợi ý, thống kê gợi ý, metric chip và card gợi ý.
|   |   |   |-- PythonProblemCurriculum.kt
|   |   |   |   |-- Phân loại problem theo lộ trình Python: syntax, operators, conditionals, loops, strings, lists, collections, functions, algorithms.
|   |   |-- repository/
|   |   |   |-- AuthRepository.kt, LessonRepository.kt, ProblemRepository.kt, UserRepository.kt, GamificationRepository.kt
|   |   |   |   |-- Interface repository để domain không phụ thuộc Firebase.
|   |   |-- usecase/
|   |   |   |-- GetDashboardSummaryUseCase.kt
|   |   |   |   |-- Gom user, lesson, problem, leaderboard thành dữ liệu Home dashboard.
|   |   |   |-- recommendation/GetRecommendationsUseCase.kt
|   |   |   |   |-- Gom user progress, lessons, problems rồi gọi RecommendationEngine.
|   |   |   |-- recommendation/RecommendationEngine.kt
|   |   |   |   |-- Thuật toán khuyến nghị: next path, adaptive difficulty, weak stage, lesson review.
|   |   |   |-- problem/
|   |   |   |   |-- GetProblemsUseCase.kt: lấy danh sách bài.
|   |   |   |   |-- GetProblemDetailUseCase.kt: lấy chi tiết bài.
|   |   |   |   |-- GetVisibleTestCasesUseCase.kt: lấy test case hiển thị.
|   |   |   |   |-- SubmitSolutionUseCase.kt: chạy code với toàn bộ test case và trả kết quả.
|   |   |   |   |-- RunCustomInputUseCase.kt: chạy code với input tự nhập.
|   |   |   |   |-- SaveCodeDraftUseCase.kt / GetCodeDraftUseCase.kt: lưu và đọc draft code.
|   |   |   |   |-- RecordProblemSubmissionUseCase.kt / GetProblemSubmissionHistoryUseCase.kt: lưu và đọc lịch sử submit.
|   |
|   |-- data/
|   |   |-- src/main/assets/seed_data/
|   |   |   |-- problems.json
|   |   |   |   |-- 973 bài MBPP + 1 bài EzTech test problem, kèm test case assert.
|   |   |   |-- lessons.json
|   |   |   |   |-- Seed tutorial/video lesson Python.
|   |   |   |-- README.md
|   |   |   |   |-- Nguồn dữ liệu seed và license.
|   |   |-- src/main/kotlin/com/eztech/core/data/
|   |   |   |-- repository/
|   |   |   |   |-- ProblemRepositoryImpl.kt
|   |   |   |   |   |-- Remote-first Firestore, fallback local seed, cache problems/test cases.
|   |   |   |   |-- LessonRepositoryImpl.kt
|   |   |   |   |   |-- Đọc bài học/video và progress bài học.
|   |   |   |   |-- UserRepositoryImpl.kt
|   |   |   |   |   |-- Đọc/ghi profile người dùng.
|   |   |   |   |-- GamificationRepositoryImpl.kt
|   |   |   |   |   |-- EXP, level, streak, badge, leaderboard.
|   |   |   |   |-- AuthRepositoryImpl.kt
|   |   |   |   |   |-- Firebase Auth.
|   |   |   |-- source/
|   |   |   |   |-- remote/
|   |   |   |   |   |-- Firebase...DataSource.kt: đọc/ghi Firestore.
|   |   |   |   |-- local/
|   |   |   |   |   |-- Local...DataSource.kt: đọc seed data từ assets.
|   |   |   |-- engine/
|   |   |   |   |-- PythonCodeExecutionRepository.kt hoặc engine liên quan.
|   |   |   |   |   |-- Chạy code Python bằng Chaquopy.
|   |
|   |-- ui/
|   |   |-- src/main/kotlin/com/eztech/core/ui/
|   |   |   |-- component/
|   |   |   |   |-- Component dùng chung: empty state, card, loading,...
|   |   |   |-- file/CodeFileIo.kt
|   |   |   |   |-- Helper import/export text UTF-8 qua Android Storage Access Framework.
|   |   |   |-- theme/
|   |   |   |   |-- Màu, typography, dimens và theme Material 3.
|
|-- feature/
|   |-- auth/
|   |   |-- presentation/
|   |   |   |-- Màn hình đăng nhập, đăng ký, quên mật khẩu.
|   |
|   |-- home/
|   |   |-- presentation/
|   |   |   |-- HomeScreen.kt
|   |   |   |   |-- UI dashboard, progress, recommendation, quick action.
|   |   |   |-- HomeViewModel.kt
|   |   |   |   |-- Gọi GetDashboardSummaryUseCase và GetRecommendationsUseCase.
|   |   |   |-- HomeUiState.kt
|   |   |   |   |-- State tổng cho dashboard.
|   |   |   |-- component/RecommendationSection.kt
|   |   |   |   |-- Card gợi ý, stats gợi ý và metric chip.
|   |   |   |-- recommendation/
|   |   |   |   |-- RecommendationsScreen.kt
|   |   |   |   |   |-- Trang riêng xem tất cả recommendation.
|   |   |   |   |-- RecommendationsViewModel.kt
|   |   |   |   |   |-- Load 12 recommendation cho trang riêng.
|   |
|   |-- learn/
|   |   |-- presentation/
|   |   |   |-- list/
|   |   |   |   |-- Danh sách lesson/video.
|   |   |   |-- category/
|   |   |   |   |-- Nhóm lesson theo chủ đề.
|   |   |   |-- tutorial/
|   |   |   |   |-- Màn hình đọc tutorial.
|   |   |   |-- video/
|   |   |   |   |-- Màn hình xem YouTube tutorial.
|   |   |   |-- bookmarks/
|   |   |   |   |-- Danh sách lesson đã bookmark.
|   |
|   |-- problems/
|   |   |-- presentation/
|   |   |   |-- list/
|   |   |   |   |-- ProblemListScreen.kt: UI danh sách bài.
|   |   |   |   |-- ProblemListViewModel.kt: search/filter/sort problem.
|   |   |   |   |-- ProblemListUiState.kt: state danh sách problem.
|   |   |   |-- model/ProblemTypeCatalog.kt
|   |   |   |   |-- Tạo filter dạng bài và match search theo curriculum/tag.
|   |   |   |-- detail/
|   |   |   |   |-- Màn hình chi tiết bài.
|   |   |   |-- solve/
|   |   |   |   |-- ProblemSolveScreen.kt: editor, test case, submit, history, import/export.
|   |   |   |   |-- ProblemSolveViewModel.kt: load bài, lưu draft, run custom input, submit, lưu tiến độ.
|   |   |   |   |-- ProblemSolveUiState.kt: state màn hình solve.
|   |
|   |-- ide/
|   |   |-- presentation/
|   |   |   |-- IdeScreen.kt
|   |   |   |   |-- IDE Python tự do, import/export file `.py`.
|   |   |   |-- IdeViewModel.kt
|   |   |   |   |-- Chạy code và cập nhật console.
|   |   |   |-- component/EditorToolbar.kt
|   |   |   |   |-- Toolbar run, undo, redo, import, export, clear, paste, font size.
|   |
|   |-- leaderboard/
|   |   |-- presentation/
|   |   |   |-- Bảng xếp hạng người dùng.
|   |
|   |-- profile/
|   |   |-- presentation/
|   |   |   |-- screen/
|   |   |   |   |-- Trang profile.
|   |   |   |-- badges/
|   |   |   |   |-- Danh sách huy hiệu.
|   |   |   |-- edit/
|   |   |   |   |-- Sửa display name/avatar URL.
|   |   |   |-- settings/
|   |   |   |   |-- Theme, notification, logout.
|
|-- tools/
|   |-- generate_mbpp_seed.py
|   |   |-- Convert MBPP thành seed problems.json.
|   |-- generate_lesson_seed.py
|   |   |-- Generate lesson seed.
|   |-- import_seed_to_firestore.py
|   |   |-- Import seed data lên Firestore bằng REST API.
|
|-- firestore.rules
|   |-- Security rules cho Firestore.
|-- firebase.json
|   |-- Cấu hình Firebase deploy rules.
|-- settings.gradle.kts
|   |-- Khai báo module Gradle.
|-- build.gradle.kts
|   |-- Cấu hình Gradle cấp project.
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
