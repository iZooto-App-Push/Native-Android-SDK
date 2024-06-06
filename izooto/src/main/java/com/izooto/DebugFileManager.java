package com.izooto;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;

public class DebugFileManager {

    static final String IZ_TAG_NAME = "DebugFileManager";

    public static void createExternalStoragePublic(Context context, String data, String requestName) {
        try {
            File outputDirectory = CheckDirectory_ExitsORNot(AppConstant.DIRECTORY_NAME);

            GenerateTimeStampAppData(context, outputDirectory, AppConstant.IZ_DEBUG_FILE_NAME, data, requestName);

        } catch (Exception e) {
            Log.v(AppConstant.IZ_DEBUG_EXCEPTION, e.toString());
        }
    }

    static void createPublicDirectory(Context context) {
        try {
            if (context != null) {
                String externalStorageState = Environment.getExternalStorageState();
                if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {

                    File fileDirectory = Environment.getExternalStoragePublicDirectory(AppConstant.DIRECTORY_NAME);
                    if (fileDirectory.exists() && fileDirectory.isDirectory()) {
                        Log.v(IZ_TAG_NAME, "FileAlready exits");

                    } else {
                        if (!fileDirectory.exists()) {
                            if (fileDirectory.mkdir()) ;
                            createExternalStoragePublic(context, "", "");
                            PreferenceUtil.getInstance(context).setBooleanData(AppConstant.FILE_EXIST, false);

                        }
                    }

                }
            }
        } catch (Exception ex) {
            Log.v(AppConstant.IZ_DEBUG_EXCEPTION, ex.toString());

        }

    }

    static void deletePublicDirectory(Context context) {
        try {
            if (context != null) {
                String externalStorageState = Environment.getExternalStorageState();
                if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {

                    File fileDirectory = Environment.getExternalStoragePublicDirectory(AppConstant.DIRECTORY_NAME);
                    if (fileDirectory.exists() && fileDirectory.isDirectory()) {
                        if (!fileDirectory.isDirectory()) {
                            Log.v(IZ_TAG_NAME, "Directory exits");

                        }

                        File[] files = fileDirectory.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            File file = files[i];
                            file.delete();

                            if (file.isDirectory()) {
                                file.delete();
                            } else {
                                boolean deleted = file.delete();
                                if (!deleted) {
                                    Log.e("Not a directory ", fileDirectory.getAbsolutePath());
                                }
                            }
                        }

                        fileDirectory.delete();
                    }

                    File fileDirectory1 = Environment.getExternalStoragePublicDirectory(AppConstant.DIRECTORY_NAME);
                    fileDirectory1.delete();
                    PreferenceUtil.getInstance(context).setBooleanData(AppConstant.FILE_EXIST, false);

                } else {
                    PreferenceUtil.getInstance(context).setBooleanData(AppConstant.FILE_EXIST, false);


                }

            }
        } catch (Exception ex) {
            Log.v(AppConstant.IZ_DEBUG_EXCEPTION, ex.toString());

        }
    }

    static void GenerateTimeStampAppData(Context context, File outputDirectory, String fileName, String data, String requestName) {

        if (outputDirectory != null) {
            File file = new File(outputDirectory, fileName);
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);

            try {
                FileWriter writer = new FileWriter(file, true);
                writer.append(requestName + "\n");
                writer.append(data + "\n");
                writer.flush();
                writer.close();
                if (requestName.equalsIgnoreCase("RegisterToken")) {
                    preferenceUtil.setBooleanData(AppConstant.FILE_EXIST, true);
                }
            } catch (Exception e) {
                preferenceUtil.setBooleanData(AppConstant.FILE_EXIST, false);

            }
        }
    }

    public static File CheckDirectory_ExitsORNot(String inWhichFolder) {

        File outputDirectory = null;
        String externalStorageState = Environment.getExternalStorageState();
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {

            File fileDirectory = Environment.getExternalStoragePublicDirectory(inWhichFolder);
            if (fileDirectory.exists() && fileDirectory.isDirectory()) {

                outputDirectory = fileDirectory;
                return outputDirectory;

            }

        }

        return null;
    }

    public static void shareDebuginfo(Context context) {


        String externalStorageState = Environment.getExternalStorageState();
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {

            File fileDirectory = Environment.getExternalStoragePublicDirectory(AppConstant.DIRECTORY_NAME);
            if (fileDirectory.exists() && fileDirectory.isDirectory()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    String path = String.valueOf(Environment.getExternalStoragePublicDirectory(AppConstant.DIRECTORY_NAME + "/pid.debug"));
                    File file = new File(path);
                    Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    emailIntent.setType("message/rfc822");
                    emailIntent.setType("*/*");
                    String[] to = {"amit@datability.co"};
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                    emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Token->" + PreferenceUtil.getInstance(context).getStringData(AppConstant.FCM_DEVICE_TOKEN) );
                    context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                } else {
                    String path = String.valueOf(Environment.getExternalStoragePublicDirectory(AppConstant.DIRECTORY_NAME + "/pid.debug"));
                    File file = new File(path);

                    Uri pngUri = Uri.fromFile(file);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    emailIntent.setType("message/rfc822");
                    emailIntent.setType("*/*");
                    String[] to = {"amit@datability.co"};
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                    emailIntent.putExtra(Intent.EXTRA_STREAM, pngUri);
                    if (Build.MANUFACTURER.equalsIgnoreCase("Huawei")) {
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "HMS Token ->" + PreferenceUtil.getInstance(context).getStringData(AppConstant.HMS_TOKEN));
                    } else {
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FCM Token ->" + PreferenceUtil.getInstance(context).getStringData(AppConstant.FCM_DEVICE_TOKEN));
                    }
                    context.startActivity(Intent.createChooser(emailIntent, "Send email..."));

                }
            }

        }


    }
}
