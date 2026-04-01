# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Preserve annotations/signatures/inner info for Retrofit/Gson/Room/Hilt/Kotlin
-keepattributes Signature,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault,EnclosingMethod,InnerClasses
-keep class kotlin.Metadata { *; }

# Keep Retrofit/Gson models (network DTOs)
-keep class airsign.signage.player.data.remote.** { *; }
-keepclassmembers,allowobfuscation class airsign.signage.player.data.remote.** {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Keep Retrofit service interfaces so endpoint annotations survive obfuscation
-keep interface airsign.signage.player.data.remote.DeviceApiService { *; }
-keep class airsign.signage.player.data.remote.DeviceApiService { *; }  # defensive

# Keep domain models and repositories used with Gson/Room
-keep class airsign.signage.player.domain.model.** { *; }
-keep class airsign.signage.player.domain.repository.** { *; }
-keep class airsign.signage.player.data.repository.** { *; }
-keep class airsign.signage.player.ui.main.viewmodel.** { *; }

# Keep Room (entities/daos)
-keep class airsign.signage.player.data.local.entity.** { *; }
-keep @androidx.room.Dao class airsign.signage.player.data.local.dao.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class ** implements androidx.room.RoomDatabase
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

# Keep Hilt/Dagger generated classes
-dontwarn dagger.hilt.**
-dontwarn javax.annotation.**
-dontwarn dagger.**
-dontwarn javax.inject.**

# Ktor/Supabase: keep serialization metadata
-dontwarn io.ktor.**
-dontwarn io.github.jan.supabase.**
-keep class io.github.jan.supabase.** { *; }
-keepclassmembers class io.github.jan.supabase.** { *; }

# Kotlinx serialization (used by Supabase/Ktor)
-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <methods>;
}

# Ktor engine/plugins (Supabase depends on these; keep to avoid stripping service loaders)
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.client.plugins.** { *; }
-keep class io.ktor.client.request.** { *; }
-keep class io.ktor.client.statement.** { *; }

# R8 missing class warnings (from generated missing_rules.txt)
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder

# Retrofit/OkHttp/Gson core
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit2.adapter.kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
-keep class kotlin.coroutines.** { *; }
-dontwarn kotlin.coroutines.**
-keep class retrofit2.KotlinExtensions { *; }
-keep class retrofit2.HttpServiceMethod { *; }
-keep class retrofit2.HttpServiceMethod$** { *; }
-keep class retrofit2.Utils { *; }
-keep class retrofit2.ParameterHandler$** { *; }

# Gson core
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Glide (generated APIs). If no generated module exists, suppress warnings.
-dontwarn com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule