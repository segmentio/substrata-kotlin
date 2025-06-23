# Keep all Substrata and LivePlugins classes
-keep class com.segment.analytics.substrata.kotlin.** { *; }

# Keep native methods - critical for QuickJS JNI integration
-keepclasseswithmembernames class * {
    native <methods>;
}