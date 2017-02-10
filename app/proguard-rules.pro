# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /AndroidSdk/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# hopefully removed Log from release build
#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int v(...);
#    public static int i(...);
#    public static int w(...);
#    public static int d(...);
#    public static int e(...);
#}

#-dontwarn android.support.v4.**
#-keep class android.support.v4.** { *; }
#-dontwarn android.support.v7.**
#-keep class android.support.v7.** { *; }
#-keep interface android.support.v7.** { *; }

-keepattributes SourceFile,LineNumberTable
-keep class org.jaudiotagger.**  { *; }
#-keep interface org.jaudiotagger.**
#-keep class de.kromke.andreas.unpopmusicplayerfree.**  { *; }
#-keep interface de.kromke.andreas.unpopmusicplayerfree.**
#-keep class android.**  { *; }
#-keep interface android.**
