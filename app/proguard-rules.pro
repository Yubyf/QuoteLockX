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

-dontobfuscate
-dontoptimize

-keepattributes SourceFile,LineNumberTable

# Xposed
-keep class com.crossbowffs.quotelock.xposed.** {*;}

# OpenCSV
-keep class org.apache.commons.logging.LogConfigurationException {*;}
-keep class org.apache.commons.logging.impl.LogFactoryImpl {*;}
# Do not keep Log4JLogger class since the dependency org.apache.log4j.Priority is not available here.
# Keep the alternative log implementation Jdk14Logger so that the OpenCSV process can continue
-keep class org.apache.commons.logging.impl.Jdk14Logger {*;}
