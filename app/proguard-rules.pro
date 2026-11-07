# openStream ProGuard Rules

# Retrofit & OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod

# Gson Serialization
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.openstream.app.data.remote.dto.** { *; }
-keep class com.openstream.app.domain.model.** { *; }
-keep class com.openstream.app.data.local.** { *; }

# Media3 ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Coroutines
-dontwarn kotlinx.coroutines.**
