<p align="center">
	<img src="https://user-images.githubusercontent.com/60651012/129727793-bc8b8f01-b317-4f1c-bace-c6882b86bff7.png" height="220">
</p>

## 👋 Introduction

The iZooto Android SDK provides push notification service for mobile apps. This plugin makes it easy to implement push notifications on your Android app built on the Native framework.

For more information check out our  [website](https://www.izooto.com)  and  [documentation](https://help.izooto.com/docs/app-push-notifications-overview).

To get started, sign up [here](https://panel.izooto.com/en/signup)

## 🎉 Installation

We publish the SDK to `mavenCentral` as an `AAR` file. Just declare it as dependency in your `build.gradle` file.
```groovy
android {
    defaultConfig{
        manifestPlaceholders = [
                izooto_app_id : 'YOUR_iZOOTO_APP_ID_HERE'
        ]
    }
}
```
```groovy
    dependencies {
    implementation 'com.izooto:android-sdk:2.6.0'
    implementation("androidx.work:work-runtime:2.9.0")

}
```

Alternatively, you can download and add the AAR file included in this repo in your Module libs directory and tell gradle to install it like this:

### 📖 Dependencies

Add the Firebase Messaging library and Android Support Library v4 as dependencies to your Module `build.gradle` file.

```groovy
     dependencies {
         implementation 'com.izooto:android-sdk:2.5.3'
         implementation("androidx.work:work-runtime:2.9.0")
         implementation "com.google.firebase:firebase-messaging:23.0.6"
     }
```

Also be sure to include the `google-services.json` classpath in your Project level `build.gradle` file:

```groovy
    // Top-level build file where you can add configuration options common to all sub-projects/modules.         
        
    buildscript {       
         repositories {      
             google()
             mavenCentral()
         }       
         dependencies {      
             classpath "com.android.tools.build:gradle:7.3.0"
             classpath "com.google.gms:google-services:4.3.3"
        
             // NOTE: Do not place your application dependencies here; they belong       
             // in the individual module build.gradle files      
         }       
    }
```

Add your FCM generated `google-services.json` file to your project and add the following to the end of your `build.gradle`:

```groovy
apply plugin: 'com.google.gms.google-services'
```

Once you've updated your module `build.gradle` file, make sure you have specified `mavenCentral()` and `google()` as a repositories in your project `build.gradle` and then sync your project in File -> Sync Project with Gradle Files.


## 📲  iZooto FCM Push SDK

Please refer to iZooto's [Android Native SDK Setup](https://help.izooto.com/docs/android-sdk-setup-1) page for step-by-step instructions on how to install the plugin.


##  📲  iZooto Xiaomi Push SDK

iZooto Xiaomi Push SDK provides an out of the box service to use the Xiaomi Push SDK. Find the integration steps for the iZooto Xiaomi Push SDK [Xiaomi Push Integration](https://help.izooto.com/docs/power-push-setting-up-xiaomi-cloud-push)

##  📲 iZooto Huawei Push SDK

iZooto Huawei Push SDK provides an out of the box service to use the Huawei Messaging Service. Find the integration steps for the iZooto Huawei Push SDK [Huawei Push Integration](https://help.izooto.com/docs/power-push-setting-up-huawei-messenger-service)


## SDK Methods

Please see iZooto's [Android Native SDK References](https://help.izooto.com/docs/sdk-reference) page for a list of all the available callbacks and methods.

#### Change Log

Please refer to this repository's [release tags](https://github.com/izooto-mobile-sdk/android-X/releases) for a complete change log of every released version.

#### Support

Please visit [izooto.com](https://www.izooto.com) or write to [support@izooto.com](mailto:support@izooto.com) for any kind of issues.

#### Demo Project

For reference, we have uploaded a demo project with the latest SDK in the <code>master</code> folder of this repository.

#### Supports:

* Tested and validated from Android 5.0 (API level 21) to Android 14 (API level 33).