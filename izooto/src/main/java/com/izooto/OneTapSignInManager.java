package com.izooto;

import android.content.Context;
import android.os.CancellationSignal;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.izooto.core.Utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OneTapSignInManager {

    private static final String IZ_CLASS_NAME = "OneTapSignInManager";

    static void manageSignInRequest(Context context, String serverClientId, OneTapCallback callback) {
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            GetCredentialRequest request = authenticationRequest(context, serverClientId);

            if (!preferenceUtil.getBoolean(AppConstant.OT_SIGN_IN) && request != null) {
                initiateUiAndFetchCredentials(context, request, preferenceUtil, callback);
            } else {
                Log.d(AppConstant.APP_NAME_TAG, "Already signed in or Authentication failed");
            }
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), IZ_CLASS_NAME, "manageSignInRequest");
        }
    }

    // Instantiate a Google sign-in request
    private static GetCredentialRequest authenticationRequest(Context context, String serverClientId) {
        try {
            GetCredentialRequest request;
            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .build();

            request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();
            return request;
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "authenticationRequest");
        }
        return null;
    }

    // Requests a credential from the user
    private static void initiateUiAndFetchCredentials(Context context, GetCredentialRequest request, PreferenceUtil preferenceUtil, OneTapCallback callback) {
        try {
            CredentialManager credentialManager = CredentialManager.create(context);
            CancellationSignal cancellationSignal = new CancellationSignal();
            Executor executor = ContextCompat.getMainExecutor(context);
            credentialManager.getCredentialAsync(context, request, cancellationSignal, executor, new CredentialManagerCallback<>() {
                @Override
                public void onResult(GetCredentialResponse getCredentialResponse) {
                    handleSuccessResponse(context, getCredentialResponse, callback);
                    preferenceUtil.setBooleanData(AppConstant.OT_SIGN_IN, true);
                }

                @Override
                public void onError(@NonNull GetCredentialException error) {
                    handleErrorResponse(context, error);
                    preferenceUtil.setBooleanData(AppConstant.OT_SIGN_IN, false);
                }
            });
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "initiateUiAndFetchCredentials");
        }
    }

    // Handle for success Response
    private static void handleSuccessResponse(Context context, GetCredentialResponse response, OneTapCallback callback) {
        try {
            Credential credential = response.getCredential();
            if (credential instanceof CustomCredential) {
                if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and authenticate on your server
                        GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.getData());
                        String email = googleIdTokenCredential.getId();
                        String firstName = googleIdTokenCredential.getGivenName();
                        String lastName = googleIdTokenCredential.getFamilyName();
                        if (callback != null) {
                            callback.syncOneTapResponse(email, firstName, lastName);
                        }
                        syncUserDetails(context, email, firstName, lastName);
                    } catch (Exception e) {
                        Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "handleSuccessResponse->");
                    }
                } else {
                    Log.d(AppConstant.APP_NAME_TAG, "Unexpected type of credential");
                }
            } else {
                Log.d(AppConstant.APP_NAME_TAG, "Unexpected type of credential");
            }
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "handleSuccessResponse");
        }
    }

    // Handle for error Response
    private static void handleErrorResponse(Context context, GetCredentialException error) {
        try {
            Log.d(AppConstant.APP_NAME_TAG, Objects.requireNonNull(error.getMessage()));
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "handleErrorResponse");
        }
    }

    // Generalized method for get user details
    protected static void syncUserDetails(Context context, String email, String firstName, String lastName) {
        try {
            if (context == null) {return;}
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            boolean isValidEmail = validateAndStoreEmail(email.trim(), preferenceUtil);
            if (!isValidEmail) {return;}
            String fn = trimmedString(firstName);
            String ln = trimmedString(lastName);
            storeOfflineRecord(context, fn, ln);
            if (!Utilities.hasUserRegistered(context)) {
                handleSyncFailure(context);
                return;
            }
            preferenceUtil.setBooleanData(AppConstant.OT_SYNC_FAILURE, false);
            prepareAndInvokeApi(context, fn, ln);

        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "syncUserDetails");
        }
    }

    // To prepare and invoke Api
    private static void prepareAndInvokeApi(Context context, String firstName, String lastName) {
        if (context == null) {return;}
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            Map<String, String> mapData = new HashMap<>();
            mapData.put(AppConstant.PID, preferenceUtil.getStringData(AppConstant.APPPID));
            mapData.put(AppConstant.BTYPE_, "" + AppConstant.BTYPE);
            mapData.put(AppConstant.DTYPE_, "" + AppConstant.DTYPE);
            mapData.put(AppConstant.QSDK_VERSION, AppConstant.SDKVERSION);
            mapData.put(AppConstant.OS, "" + AppConstant.SDKOS);
            mapData.put(AppConstant.ANDROID_ID, Util.getAndroidId(context));
            mapData.put(AppConstant.IZ_EMAIL, preferenceUtil.getStringData(AppConstant.IZ_STORED_EMAIL));
            mapData.put(AppConstant.IZ_FIRST_NAME, firstName);
            mapData.put(AppConstant.IZ_LAST_NAME, lastName);
            handleOneTapSubscription(context, mapData);

        } catch (Exception ex) {
            handleSyncFailure(context);
            Util.handleExceptionOnce(context, ex.toString(), IZ_CLASS_NAME, "prepareAndInvokeApi");
        }

    }

    private static void handleOneTapSubscription(Context context, Map<String, String> data) {
        try {
            RestClient.postRequest(RestClient.ONE_TAP_SUBSCRIPTION, data, null, new RestClient.ResponseHandler() {
                @Override
                void onSuccess(String response) {
                    super.onSuccess(response);
                    defaultNewsletterProperty(context);
                    clearOfflineRecord(context);
                }

                @Override
                void onFailure(int statusCode, String response, Throwable throwable) {
                    super.onFailure(statusCode, response, throwable);
                    handleSyncFailure(context);
                }
            });
        } catch (Exception e) {
            handleSyncFailure(context);
            Util.handleExceptionOnce(context, AppConstant.FAILURE + " " + e, IZ_CLASS_NAME, "handleOneTapSubscription");
        }
    }

    // Return First and Last Name with Max length 50.
    private static String trimmedString(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        try {
            String spaceTrimmedString = str.trim();
            if (spaceTrimmedString.length() <= 50) {
                return spaceTrimmedString;
            } else {
                return spaceTrimmedString.substring(0, 50);
            }
        } catch (Exception e) {
            Log.d(AppConstant.APP_NAME_TAG, "Exception while trimming string: " + e);
            return "";
        }
    }

    // Validate email format and store once in the shared preferences
    private static boolean validateAndStoreEmail(String email, PreferenceUtil preferenceUtil) {
        try {
            Pattern pattern = Pattern.compile(AppConstant.EMAIL_VALIDATION_REGx);
            Matcher matcher = pattern.matcher(email);
            if (matcher.matches() && (!preferenceUtil.getStringData(AppConstant.IZ_STORED_EMAIL).equals(email))) {
                preferenceUtil.setStringData(AppConstant.IZ_STORED_EMAIL, email);
            } else {
                Log.d(AppConstant.APP_NAME_TAG, "Email Already exists or Invalid");
                return false;
            }
            return matcher.matches();
        } catch (Exception e) {
            Log.d(AppConstant.APP_NAME_TAG, e.toString());
            return false;
        }
    }

    // To Add default newsletter-property
    private static void defaultNewsletterProperty(Context context) {
        try {
            HashMap<String, Object> newsletterObtainedProperty = new HashMap<>();
            newsletterObtainedProperty.put(AppConstant.IZ_DEFAULT_NEWSLETTER_KEY, new String[]{"0"});
            iZooto.addUserProperty(newsletterObtainedProperty);
        } catch (Exception e) {
            Util.handleExceptionOnce(context, e.toString(), IZ_CLASS_NAME, "defaultNewsletterProperty");
        }
    }

    // To store offline oneTap record
    private static void storeOfflineRecord(Context context, String firstName, String lastName) {
        if (context == null) {return;}
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            preferenceUtil.setStringData(AppConstant.IZ_EMAIL, preferenceUtil.getStringData(AppConstant.IZ_STORED_EMAIL));
            preferenceUtil.setStringData(AppConstant.IZ_FIRST_NAME, firstName);
            preferenceUtil.setStringData(AppConstant.IZ_LAST_NAME, lastName);
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), IZ_CLASS_NAME, "storeOfflineRecord");
        }
    }

    // To clear offline record
    private static void clearOfflineRecord(Context context) {
        if (context == null) {return;}
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            preferenceUtil.setStringData(AppConstant.IZ_EMAIL, "");
            preferenceUtil.setStringData(AppConstant.IZ_FIRST_NAME, "");
            preferenceUtil.setStringData(AppConstant.IZ_LAST_NAME, "");
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), IZ_CLASS_NAME, "clearOfflineRecord");
        }
    }

    // To handle sync failure state
    private static void handleSyncFailure(Context context) {
        if (context == null) {return;}
        try {
            PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(context);
            preferenceUtil.setBooleanData(AppConstant.OT_SYNC_FAILURE, true);
            preferenceUtil.setStringData(AppConstant.IZ_STORED_EMAIL, "");
        } catch (Exception ex) {
            Util.handleExceptionOnce(context, ex.toString(), IZ_CLASS_NAME, "handleSyncFailure");
        }
    }

}
