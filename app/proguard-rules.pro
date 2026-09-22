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

# --- Room Database ProGuard Rules ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers @androidx.room.Dao class * { *; }

-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }

-keepclassmembers class * {
    @androidx.room.TypeConverter <methods>;
}

-keep class *_Impl extends androidx.room.RoomDatabase { *; }
-keep class *_Impl implements * { *; }
-keep class com.royaraqamia.rabwa.data.local.** { *; }
-dontwarn androidx.room.paging.**

# --- Kotlinx Serialization & Models ---
-keepattributes *Annotation*, InnerClasses
-keep @kotlinx.serialization.Serializable class * { *; }
-keep class com.royaraqamia.rabwa.data.remote.dto.** { *; }
-keep class com.royaraqamia.rabwa.domain.model.** { *; }

# --- Supabase & Ktor ---
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Ktor ships a JVM-only debugger detector that references java.lang.management.*,
# which does not exist on Android. The class is never reached at runtime, but
# keeping io.ktor.** makes R8 try to resolve it. Suppress the dangling refs.
# See app/build/outputs/mapping/release/missing_rules.txt.
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
