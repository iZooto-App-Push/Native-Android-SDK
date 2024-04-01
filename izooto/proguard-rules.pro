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
-keep class com.izooto.XiaomiPushReceiver{*;}
-dontwarn com.xiaomi.push.**
-keep class com.izooto.iZootoHmsMessagingService{*;}
-keep class com.izooto.HMSTokenGenerator{*;}
-dontwarn com.huawei.**
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile
-keep public class com.izooto.iZooto{*;}
-keep public class com.izooto.iZooto$Builder{*;}
-keep public class com.izooto.PushTemplate{*;}
-keep public class com.izooto.PreferenceUtil{*;}
-keep public class com.izooto.AppConstant{*;}
-keep public class com.izooto.NotificationWebViewListener{*;}
-keep public class com.izooto.NotificationHelperListener{*;}
-keep public class com.izooto.NotificationReceiveHybridListener{*;}
-keep public class com.izooto.TokenReceivedListener{*;}
-keep public class com.izooto.Payload{*;}
-keep public interface com.izooto.OneTapCallback{*;}
-keep public class com.izooto.iZootoPulse{*;}
-repackageclasses 'com.izooto'
-useuniqueclassmembernames



