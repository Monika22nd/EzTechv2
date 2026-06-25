# EzTechv2

## APK Test

- File APK debug de cai tren dien thoai: [EzTechv2-debug.apk](./EzTechv2-debug.apk)

EzTechv2 là ứng dụng Android hỗ trợ học lập trình Python. Ứng dụng kết hợp bài học dạng tutorial, video, bài tập lập trình, IDE Python trong app, hệ thống điểm kinh nghiệm, huy hiệu, bảng xếp hạng, bookmark và quản lý hồ sơ người dùng.

## Mục Tiêu

- Xây dựng một app học Python có thể demo trực tiếp trên emulator hoặc điện thoại Android.
- Lưu dữ liệu người dùng, bài học, video, bài tập và tiến độ học tập bằng Firebase.
- Cho phép người học xem bài, làm bài tập, chạy code Python, lưu nháp code và theo dõi tiến độ.
- Tổ chức code theo kiến trúc nhiều module, MVVM và clean architecture nhẹ để dễ mở rộng.

## Tính Năng Chính

- Đăng ký, đăng nhập, quên mật khẩu bằng Firebase Authentication.
- Trang Home dashboard hiển thị tiến độ học, bài học gần đây, số bài đã giải và thông tin xếp hạng.
- Mục Learn gồm danh sách video tutorial và danh sách bài học dạng bài viết.
- Mục Problems gồm danh sách bài tập Python theo thứ tự, tìm kiếm, lọc độ khó, xem chi tiết và giải bài.
- Màn hình Solve có editor, custom input, chạy thử code, submit, lưu draft và xem lịch sử nộp bài.
- IDE Python trong app sử dụng Chaquopy để chạy code.
- Bookmark bài học để lưu các bài cần xem lại.
- Gamification: EXP, level, streak, badge và leaderboard.
- Profile: xem thông tin cá nhân, thống kê học tập, huy hiệu, đổi display name và avatar URL.
- Settings: đổi theme System/Light/Dark, bật/tắt notification, logout.
- Dữ liệu chính lấy từ Cloud Firestore, có fallback local và cache nhẹ để giảm đọc lặp.

## Công Nghệ Sử Dụng

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

## Cấu Trúc Thư Mục

```text
EzTechv2/
├── app/
│   ├── src/main/java/com/eztech/app/
│   │   ├── EzTechApplication.kt
│   │   ├── EzTechAppViewModel.kt
│   │   ├── MainActivity.kt
│   │   └── navigation/
│   │       ├── EzTechApp.kt
│   │       ├── EzTechNavHost.kt
│   │       ├── NavigationExtensions.kt
│   │       └── TopLevelDestination.kt
│   ├── google-services.json
│   └── build.gradle.kts
│
├── core/
│   ├── common/
│   │   └── Resource.kt
│   ├── domain/
│   │   └── src/main/kotlin/com/eztech/core/domain/
│   │       ├── model/
│   │       ├── repository/
│   │       └── usecase/
│   ├── data/
│   │   └── src/main/kotlin/com/eztech/core/data/
│   │       ├── di/
│   │       ├── engine/
│   │       ├── repository/
│   │       └── source/
│   └── ui/
│       └── src/main/kotlin/com/eztech/core/ui/
│           ├── component/
│           └── theme/
│
├── feature/
│   ├── auth/
│   │   └── presentation/
│   ├── home/
│   │   ├── di/
│   │   ├── navigation/
│   │   └── presentation/
│   ├── learn/
│   │   ├── navigation/
│   │   └── presentation/
│   │       ├── bookmarks/
│   │       ├── category/
│   │       ├── component/
│   │       ├── list/
│   │       ├── tutorial/
│   │       └── video/
│   ├── problems/
│   │   ├── navigation/
│   │   └── presentation/
│   │       ├── component/
│   │       ├── detail/
│   │       ├── list/
│   │       └── solve/
│   ├── ide/
│   │   └── presentation/
│   ├── leaderboard/
│   │   └── presentation/
│   └── profile/
│       ├── navigation/
│       └── presentation/
│           ├── badges/
│           ├── component/
│           ├── edit/
│           ├── screen/
│           └── settings/
│
├── firestore.rules
├── firebase.json
├── gradle/
├── settings.gradle.kts
└── README.md
```

## Kiến Trúc App

Ứng dụng được tổ chức theo hướng nhiều module và MVVM.

### Các Lớp Chính

```text
UI Screen / Composable
        ↓ gọi event
ViewModel
        ↓ gọi use case hoặc repository
Domain UseCase
        ↓ gọi interface repository
Repository Interface
        ↓ được implement ở core:data
Repository Implementation
        ↓ đọc/ghi dữ liệu
Firebase / Local Source / Python Engine
```

### Vai Trò Từng Layer

- `app`: chứa `MainActivity`, app shell, bottom navigation và nav host tổng.
- `feature/*`: mỗi feature là một module riêng, chứa UI screen, state, ViewModel và navigation của feature đó.
- `core/domain`: chứa model, repository interface và use case. Layer này không phụ thuộc Firebase hay Android UI.
- `core/data`: implement repository, làm việc với Firebase, local fallback, DataStore và Chaquopy.
- `core/ui`: chứa theme, màu sắc, typography và component UI dùng chung.
- `core/common`: chứa class dùng chung như `Resource`.

### MVVM Trong Feature

Mỗi màn hình lớn thường có 3 phần:

```text
ExampleScreen.kt       Hiển thị UI bằng Jetpack Compose
ExampleUiState.kt      Gom dữ liệu/trạng thái màn hình
ExampleViewModel.kt    Xử lý logic, gọi use case/repository, cập nhật UiState
```

Ví dụ trong `feature/problems`:

```text
ProblemListScreen.kt
ProblemListUiState.kt
ProblemListViewModel.kt

ProblemSolveScreen.kt
ProblemSolveUiState.kt
ProblemSolveViewModel.kt
```

Luồng hoạt động:

1. Người dùng thao tác trên `Screen`.
2. `Screen` gọi function trong `ViewModel`.
3. `ViewModel` gọi `UseCase` hoặc `Repository`.
4. Repository đọc/ghi Firestore hoặc chạy Python engine.
5. Kết quả được trả về dạng `Resource.Loading`, `Resource.Success`, `Resource.Error`.
6. `ViewModel` cập nhật `UiState`.
7. Compose tự render lại giao diện.

## Firebase

Ứng dụng dùng:

- Firebase Authentication: đăng ký, đăng nhập, quên mật khẩu.
- Cloud Firestore: lưu user profile, bài học, video, bài tập, progress, bookmark, leaderboard, submission history.

Ứng dụng không dùng Firebase Storage để tránh phải nâng cấp Blaze. Avatar được lưu bằng trường `avatarUrl` trong Firestore.

### Deploy Firestore Rules

Chỉ cần chạy lệnh này khi file `firestore.rules` thay đổi:

```powershell
firebase deploy --only firestore
```

Khi demo, nếu rules đã deploy rồi thì không cần chạy lệnh này nữa. Chỉ cần cài APK, đăng nhập và app sẽ tự kết nối Firestore.

## Build App

Chạy từ thư mục gốc project:

```powershell
.\gradlew.bat :app:assembleDebug
```

APK debug nằm tại:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Build release unsigned:

```powershell
.\gradlew.bat :app:assembleRelease
```

File release unsigned:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

## Test

```powershell
.\gradlew.bat :core:domain:test :core:data:testDebugUnitTest :feature:problems:testDebugUnitTest :feature:profile:testDebugUnitTest
```

Các test chính hiện kiểm tra repository, problems ViewModel và profile/settings ViewModel.

## Dữ Liệu Firestore

App hiện hỗ trợ dữ liệu học Python và bài tập lập trình lấy từ Firestore.

Các nhóm dữ liệu chính:

- `users`: thông tin người dùng, EXP, level, streak, solved problems, bookmarked lessons.
- `lessons` hoặc nhóm collection bài học: tutorial/video lesson.
- `problems`: danh sách bài tập Python.
- `problemSubmissions`: lịch sử nộp bài.
- `codeDrafts`: code nháp của người dùng.
- `leaderboard`: dữ liệu bảng xếp hạng.

Seed data hiện có 973 bài problems từ full MBPP dataset. Mỗi problem được import kèm field `order` để app hiển thị theo thứ tự câu 1, 2, 3... Nếu sau này tăng lên hàng nghìn bài, nên bổ sung pagination để tiết kiệm lượt đọc Firestore.

## Checklist Demo

- Cài APK debug lên emulator hoặc điện thoại.
- Đăng ký hoặc đăng nhập.
- Kiểm tra Home dashboard.
- Mở Learn, video tutorial và tutorial article.
- Mở Problems, xem thứ tự bài, tìm kiếm/lọc, vào chi tiết và solve.
- Chạy code trong IDE Python.
- Kiểm tra Rank/Leaderboard.
- Kiểm tra Profile, Badges, Edit Profile và Settings.
- Mở Firebase Console để show dữ liệu user/progress trên Firestore nếu cần.

## Ghi Chú

- App chưa ký release chính thức, nên dùng `app-debug.apk` để demo.
- Google Sign-In và Play Store release không nằm trong phạm vi bản demo hiện tại.
- Firebase Storage đã được bỏ, vì bản demo không cần upload file thật.
