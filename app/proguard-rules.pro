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

# Preserve all resource IDs (prevents navigation/menu components from losing XML ID references)
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep your specific fragment names intact so reflection based navigation works
-keep public class * extends androidx.fragment.app.Fragment

# If you are using Jetpack Navigation components, keep its internal structural classes
-keep class androidx.navigation.** { *; }
