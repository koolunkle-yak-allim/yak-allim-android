# Retrofit 2
-keepattributes Signature, InnerClasses, AnnotationDefault
-dontwarn retrofit2.**
# Keep the top-level classes and specific members needed for reflection
-keep class retrofit2.Response { *; }
-keep class retrofit2.Retrofit { *; }
-keep interface * {
    @retrofit2.http.* <methods>;
}

# Moshi
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class *JsonAdapter { *; }
-keep @com.squareup.moshi.JsonClass class *
-keepclassmembers @com.squareup.moshi.JsonClass class * {
    <fields>;
    <init>(...);
}

# OkHttp
-keepattributes Signature, InnerClasses
-dontwarn okhttp3.**
# Keeping specific OkHttp classes instead of the whole package
-keep class okhttp3.OkHttpClient { *; }
-keep class okhttp3.Request { *; }
-keep class okhttp3.Response { *; }

# Hilt / Dagger
-dontwarn dagger.hilt.**
-dontwarn com.google.dagger.hilt.**
# Hilt handles most of its own ProGuard rules via consumer-proguard-rules.
# We only add specific entry points if necessary.

# Firebase
-dontwarn com.google.firebase.**
# Firebase also provides its own ProGuard rules in the AAR.

# Project specific: Keep DTOs that use Moshi
-keep @com.squareup.moshi.JsonClass class io.github.koolunkle.yakallim.data.datasource.remote.dto.** { *; }
