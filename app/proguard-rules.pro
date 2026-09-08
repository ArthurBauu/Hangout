# ProGuard rules for AndroidContactApp
# Add project-specific rules here. This file is intentionally conservative
# to avoid stripping app classes used by reflection or by frameworks.

# Keep application classes and Kotlin metadata
-keep class com.example.contactapp.** { *; }
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod

# Preserve classes/members annotated with @Keep
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Add other rules as needed for libraries/proguard optimizations.
