# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/max/Android/sdk/tools/proguard/proguard-android.txt
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
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}


-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient

-keep class android.support.v4.** {*;}
-dontwarn android.support.v4.**


-dontwarn com.squareup.picasso.OkHttpDownloader
#不混淆内部类
-keepattributes Exceptions,InnerClasses,Signature

-dontwarn org.apache.**
-keep class org.apache.** { *;}

-keep class com.yunzhanghu.redpacketsdk.** {*;}

-keep public class com.yunzhanghu.redpacketui.utils.RPRedPacketUtil{*;}

#保持内部类不被混淆
-keep public class com.yunzhanghu.redpacketui.utils.RPRedPacketUtil$RPOpenPacketCallback{
      public <fields>;
      public <methods>;
}


-keep class com.android.volley.** {*;}

-keep public class com.yunzhanghu.redpacketui.R$*{
    public static final int *;
}
#支付宝不被混淆
-keep class com.alipay.**{*;}
-dontwarn com.alipay.**
-keep class com.ta.utdid2.**{*;}
-keep class com.ut.device.**{*;}
-keep class org.json.alipay.**{*;}
-keepattributes EnclosingMethod
