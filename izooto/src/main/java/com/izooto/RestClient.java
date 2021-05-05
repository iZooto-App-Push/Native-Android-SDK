package com.izooto;


import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RestClient {

    public static final String BASE_URL = "https://aevents.izooto.com/";
    private static final int TIMEOUT = 120000;
    public static final int GET_TIMEOUT = 60000;
    public static final String EVENT_URL="https://et.izooto.com/evt";
    public static final String PROPERTIES_URL="https://prp.izooto.com/prp";
    public static final String IMPRESSION_URL="https://impr.izooto.com/imp";
    public static  final String NOTIFICATIONCLICK="https://clk.izooto.com/clk";
    public static final String SUBSCRIPTION_API="https://usub.izooto.com/sunsub";
    public static final String LASTNOTIFICATIONCLICKURL="https://lci.izooto.com/lci";
    public static final String LASTNOTIFICATIONVIEWURL="https://lim.izooto.com/lim";
    public static final String LASTVISITURL="https://lvi.izooto.com/lvi";
   // public static final String UPDATE_SDK=" https://vr.izooto.com/vr";
    public static final String MEDIATION_IMPRESSION="https://med.izooto.com/medi";
    public static final String MEDIATION_CLICKS="https://med.izooto.com/medc";
    public static final String UPDATE_SDK="https://svr.izooto.com/svr";
    private static int getThreadTimeout(int timeout) {
        return timeout + 5000;
    }


    static void get(final String url, final ResponseHandler responseHandler) {
        new Thread(new Runnable() {
            public void run() {
                makeApiCall(url, null, null, responseHandler, GET_TIMEOUT);
            }
        }).start();
    }
    static void getRequest(final String url, final int timeOut,final ResponseHandler responseHandler) {
        new Thread(new Runnable() {
            public void run() {
                if(timeOut==0)
                    makeApiCall(url, null, null, responseHandler, GET_TIMEOUT);
                else
                    makeApiCall(url, null, null, responseHandler,timeOut);
            }
        }).start();
    }
    static void postRequest(final String url,final ResponseHandler responseHandler)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeApiCall(url,AppConstant.POST,null,responseHandler,GET_TIMEOUT);
            }
        }).start();
    }
    static void postRequest1(final String url, final JSONObject jsonObject, final ResponseHandler responseHandler)

    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeApiCall(url,AppConstant.POST,jsonObject,responseHandler,GET_TIMEOUT);
            }
        }).start();
    }
    public static void makeApiCall(final String url, final String method, final JSONObject jsonBody, final ResponseHandler responseHandler, final int timeout) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new Runnable() {
            @Override
            public void run() {
                startHTTPConnection(url, method, jsonBody, responseHandler, timeout);
            }
        });
    }

    private static void startHTTPConnection(String url, String method, JSONObject jsonBody, ResponseHandler responseHandler, int timeout) {
        HttpURLConnection con = null;
        int httpResponse = -1;
        String json = null;

        try {
            if (url.contains(AppConstant.HTTPS) || url.contains(AppConstant.HTTP) || url.contains(AppConstant.IMPR)) {
                con = (HttpURLConnection) new URL(url).openConnection();

            } else {
                con = (HttpURLConnection) new URL(BASE_URL + url).openConnection();
            }

            con.setUseCaches(false);
            con.setConnectTimeout(timeout);
            con.setReadTimeout(timeout);

            if (jsonBody != null)
                con.setDoInput(true);

            if (method != null) {
                if(method.equalsIgnoreCase(AppConstant.POST)) {
                    con.setRequestProperty(AppConstant.CONTENT_TYPE, AppConstant.FORM_URL_ENCODED);
                }
                else {
                    con.setRequestProperty(AppConstant.CONTENT_TYPE, AppConstant.FORM_URL_JSON);
                }
                con.setRequestMethod(method);
                con.setDoOutput(true);
            }

            if (jsonBody != null) {
                String strJsonBody = jsonBody.toString();
                byte[] sendBytes = strJsonBody.getBytes(AppConstant.UTF);
                con.setFixedLengthStreamingMode(sendBytes.length);
                OutputStream outputStream = con.getOutputStream();
                outputStream.write(sendBytes);
            }

            httpResponse = con.getResponseCode();
            InputStream inputStream;
            Scanner scanner;
            if (httpResponse == HttpURLConnection.HTTP_OK) {
                if (url.equals(AppConstant.CDN+ iZooto.mIzooToAppId+AppConstant.DAT))
                    Lg.d(AppConstant.APP_NAME_TAG, AppConstant.SUCCESS);
                else
                    Lg.d(AppConstant.APP_NAME_TAG, AppConstant.SUCCESS);

                inputStream = con.getInputStream();
                scanner = new Scanner(inputStream, AppConstant.UTF);
                json = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
                if (responseHandler != null) {
                    callResponseHandlerOnSuccess(responseHandler, json);
                }
                else
                    Lg.w(AppConstant.APP_NAME_TAG, AppConstant.ATTACHREQUEST);

            } else {
                if (url.equals(AppConstant.CDN+ iZooto.mIzooToAppId+AppConstant.DAT))
                    Lg.d(AppConstant.APP_NAME_TAG, AppConstant.SUCCESS);
                else
                    Lg.d(AppConstant.APP_NAME_TAG, AppConstant.FAILURE);
                inputStream = con.getErrorStream();
                if (inputStream == null)
                    inputStream = con.getInputStream();

                if (inputStream != null) {
                    scanner = new Scanner(inputStream, AppConstant.UTF);

                    json = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                    scanner.close();
                }
                if (responseHandler != null) {
                    callResponseHandlerOnFailure(responseHandler, httpResponse, json, null);

                }
                else
                    Lg.w(AppConstant.APP_NAME_TAG, AppConstant.ATTACHREQUEST);

            }
        } catch (Throwable t) {
            if (t instanceof java.net.ConnectException || t instanceof java.net.UnknownHostException)
                Lg.i(AppConstant.APP_NAME_TAG,  AppConstant.EXCEPTIONERROR+ t.getClass().getName());
            else
                Lg.i(AppConstant.APP_NAME_TAG,  AppConstant.EXCEPTIONERROR+ t);

            if (responseHandler != null)
                callResponseHandlerOnFailure(responseHandler, httpResponse, null, t);
            else
                Lg.w(AppConstant.APP_NAME_TAG, AppConstant.ATTACHREQUEST);
        } finally {
            if (con != null)
                con.disconnect();
        }
    }


    private static void callResponseHandlerOnSuccess(final ResponseHandler handler, final String response) {
        new AppExecutors().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                handler.onSuccess(response);
            }
        });

    }

    private static void callResponseHandlerOnFailure(final ResponseHandler handler, final int statusCode, final String response, final Throwable throwable) {
        new AppExecutors().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                handler.onFailure(statusCode, response, throwable);
            }
        });
    }

    static class ResponseHandler {
        void onSuccess(String response) {
            Lg.d(AppConstant.APP_NAME_TAG,  AppConstant.APISUCESS);
        }

        void onFailure(int statusCode, String response, Throwable throwable) {
            Lg.e(AppConstant.APP_NAME_TAG, AppConstant.APIFAILURE  + response);
        }
    }
}