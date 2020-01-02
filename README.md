t# izooto
App notification service



Introduction :
 This document will talk about iZooto’s SDK integration process for Android. 

Process:
The following process is a step by step guide to integrate iZooto’s SDK.

Copy the android-release.AAR (shared) file to the libs folder of your project.
Open app/build.gradle (Module: app) file and add the following lines of code: 

Inside defaultConfig{} tag :

      manifestPlaceholders = [
               izooto_enc_key: "izooto_seceret-key”
               izooto_app_id : "izotot_app_id" 
       ]
	

	Inside dependancies{} tag :

implementation project(path: ':izooto-release')
implementation 'com.google.firebase:firebase-messaging:20.0.1'

Once done, go to File → Project Structure and :

Under the dependency tab, click on New Module, represented by + icon.



Select ‘Import JAR/AAR Package’ and click ‘Next’.
Import ‘izooto-release.aar’ file shared and click on Finish.
Sync the project

Add the following lines of code under the Manifest file.

For Internet Permissions, define a new user-permission tag:

 <uses-permission android:name="android.permission.INTERNET"/>

Inside the application tag:

   android:name=".yourApplicationName"

<meta-data
   android:name="izooto_enc_key"
   android:value="${izooto_enc_key}" />

<meta-data
   android:name="izooto_app_id"
   android:value="${izooto_app_id}" />

Sync the project.
Create an Application File and include the following lines of code:

public class myApplicationName extends Application implements TokenReceivedListener
{

   @Override
   public void onCreate() {
       super.onCreate();
    iZooto.initialize(this).setTokenReceivedListener(this).build(); }
 @Override
   public void onTokenReceived(String token) {
       Lg.i("Device token", token + "");
 }}

Clean and re-build the project.

Congratulations! You have successfully integrated iZooto’s Android SDK. When you now run the project, the FCM key would be generated and captured inside Android Studio’s Logs.
