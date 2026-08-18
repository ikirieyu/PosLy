# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/<user>/Library/Android/sdk/tools/proguard/proguard-android.txt

# Supabase / Ktor — keep serialization
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class ** { @kotlinx.serialization.Serializable <fields>; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Apache POI
-keep class org.apache.poi.** { *; }
-keep class org.openxmlformats.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }

# SQLCipher
-keep class net.sqlcipher.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
