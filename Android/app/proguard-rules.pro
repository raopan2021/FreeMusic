# Add project specific ProGuard rules here.

# ========== Kotlin ==========
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepattributes Signature
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# ========== Retrofit ==========
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Serialization
-dontwarn kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.freemusic.**$$serializer { *; }
-keepclassmembers class com.freemusic.** { *** Companion; }
-keepclasseswithmembers class com.freemusic.** { kotlinx.serialization.KSerializer serializer(...); }

# ========== Media3 / ExoPlayer ==========
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ========== Coil ==========
-dontwarn coil.**

# ========== Hilt ==========
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ========== Room ==========
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# ========== Keep Data Classes ==========
-keep class com.freemusic.data.remote.dto.** { *; }
-keep class com.freemusic.domain.model.** { *; }
