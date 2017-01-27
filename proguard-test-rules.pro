# Proguard rules that are applied to your test apk/code.
-ignorewarnings

-keepattributes *Annotation*

-dontnote junit.framework.**
-dontnote junit.runner.**

-dontwarn android.test.**
-dontwarn android.support.test.**
-dontwarn org.junit.**
#-dontwarn org.hamcrest.**
#-dontwarn com.squareup.javawriter.JavaWriter
# Uncomment this if you use Mockito
#-dontwarn org.mockito.**


-keep class io.cogswell.** { *; }
-dontwarn io.cogswell.**

-keep class io.** { *; }
-dontwarn io.**

# From https://android.googlesource.com/platform/sdk/+/master/files/proguard-android.txt

-keepclassmembers class **.R$* {
    public static <fields>;
}
# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}