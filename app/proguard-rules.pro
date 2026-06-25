# Keep useful stack trace metadata for internal testing builds.
-keepattributes SourceFile,LineNumberTable,*Annotation*

# Firebase and Google Play services use generated/runtime metadata.
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Hilt/Dagger generated classes and injection metadata.
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-dontwarn dagger.hilt.**

# Chaquopy loads Python runtime classes through native/runtime hooks.
-keep class com.chaquo.python.** { *; }
-keep class com.chaquo.python.android.** { *; }
-dontwarn com.chaquo.python.**

# Sora editor and TextMate grammars rely on reflective language/theme setup.
-keep class io.github.rosemoe.sora.** { *; }
-keep class org.eclipse.tm4e.** { *; }
-dontwarn io.github.rosemoe.sora.**
-dontwarn org.eclipse.tm4e.**

# YouTube player is WebView-backed and uses JavaScript bridge internals.
-keep class com.pierfrancescosoffritti.androidyoutubeplayer.** { *; }
-dontwarn com.pierfrancescosoffritti.androidyoutubeplayer.**
