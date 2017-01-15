package com.example.zpiao1.excited.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.User;
import com.example.zpiao1.excited.server.IUserRequest;
import com.example.zpiao1.excited.server.LoginResponse;
import com.example.zpiao1.excited.server.LogoutResponse;
import com.example.zpiao1.excited.server.ServerUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.HashMap;

import io.reactivex.functions.Consumer;

import static android.content.Context.MODE_PRIVATE;

public class UserManager {
    public static final int REQ_CODE_GOOGLE_SIGN_IN = 1;
    private static final String TAG = UserManager.class.getSimpleName();

    private static CallbackManager mCallbackManager = CallbackManager.Factory.create();
    private static User mUser = null;

    private UserManager() {
    }

    public synchronized static void getUser(Context context, final UserHandler handler) {
        if (mUser != null) {
            handler.handleUser(mUser);
            return;
        }
        if (!hasLoggedIn(context)) {
            handler.handleUser(null);
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                MODE_PRIVATE);
        String id = prefs.getString(context.getString(R.string.pref_id_key), null);
        String token = prefs.getString(context.getString(R.string.pref_token_key), null);
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.wrapObservable(request.getUser(id, token),
                new Consumer<User>() {
                    @Override
                    public synchronized void accept(User user) throws Exception {
                        mUser = user;
                        handler.handleUser(user);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public synchronized void accept(Throwable throwable) throws Exception {
                        handler.handleError(throwable);
                    }
                });
    }

    public static CallbackManager getCallbackManager() {
        return mCallbackManager;
    }

    public static void logOut(final Context context,
                              final GoogleApiClient client,
                              final UserActivityHandler handler) {
        final SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                MODE_PRIVATE);
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.wrapObservable(request.logOut(),
                new Consumer<LogoutResponse>() {
                    @Override
                    public void accept(LogoutResponse response) throws Exception {
                        if (response.success) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove(context.getString(R.string.pref_token_key));
                            editor.remove(context.getString(R.string.pref_id_key));
                            editor.apply();
                            if (hasFacebookLoggedIn()) {
                                facebookLogOut();
                            }
                            if (hasGoogleSignedIn(context)) {
                                googleSignOut(context, client, handler);
                            }
                            handler.onSuccess(UserActivityHandler.LOG_OUT_SUCCESSFUL);
                        } else {
                            handler.onFail(new Exception(response.status),
                                    UserActivityHandler.LOG_OUT_FAILED);
                        }
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        handler.onFail(throwable, UserActivityHandler.LOG_OUT_FAILED);
                    }
                });
    }

    private static void facebookLogOut() {
        if (AccessToken.getCurrentAccessToken() != null) {
            LoginManager.getInstance().logOut();
        }
    }

    private static void googleSignOut(final Context context,
                                      GoogleApiClient client,
                                      final UserActivityHandler handler) {
        final SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                MODE_PRIVATE);
        if (!hasGoogleSignedIn(context)) {
            handler.onSuccess(UserActivityHandler.GOOGLE_SIGN_OUT_SUCCESSFUL);
            handler.onSuccess(UserActivityHandler.LOG_OUT_SUCCESSFUL);
            return;
        }
        Auth.GoogleSignInApi.signOut(client).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(context.getString(R.string.pref_has_google_signed_in_key));
                    editor.apply();
                    handler.onSuccess(UserActivityHandler.GOOGLE_SIGN_OUT_SUCCESSFUL);
                } else {
                    handler.onFail(new Exception(status.getStatusMessage()),
                            UserActivityHandler.GOOGLE_SIGN_OUT_FAILED);
                }
            }
        });
    }

    public static boolean hasLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                MODE_PRIVATE);
        Log.d(TAG, "hasLoggedIn: " + (prefs.contains(context.getString(R.string.pref_id_key))
                && prefs.contains(context.getString(R.string.pref_token_key))));
        return prefs.contains(context.getString(R.string.pref_id_key))
                && prefs.contains(context.getString(R.string.pref_token_key));
    }

    public static boolean hasGoogleSignedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                MODE_PRIVATE);
        return prefs.getBoolean(context.getString(R.string.pref_has_google_signed_in_key), false);
    }

    public static boolean hasFacebookLoggedIn() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    public static void googleSignIn(GoogleApiClient client, Activity activity) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        activity.startActivityForResult(signInIntent, REQ_CODE_GOOGLE_SIGN_IN);
    }

    public static void handleActivityResult(Activity activity,
                                            int requestCode,
                                            int resultCode,
                                            Intent data,
                                            UserActivityHandler handler) {
        if (requestCode == REQ_CODE_GOOGLE_SIGN_IN) {
            SharedPreferences prefs = activity.getSharedPreferences(
                    activity.getString(R.string.shared_pref_name_server),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(activity.getString(R.string.pref_has_google_signed_in_key), true);
            editor.apply();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result, handler, activity);
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private static void handleGoogleSignInResult(GoogleSignInResult result,
                                                 UserActivityHandler handler,
                                                 Context context) {
        if (!result.isSuccess()) {
            handler.onFail(new Exception("Failed to Sign In to Google"),
                    UserActivityHandler.LOG_IN_FAILED);
            handler.onFail(new Exception("Failed to Sign In to Google"),
                    UserActivityHandler.GOOGLE_SIGN_IN_FAILED);
        } else {
            GoogleSignInAccount account = result.getSignInAccount();
            if (account == null) {
                handler.onFail(new NullPointerException("Google Sign In Account is null"),
                        UserActivityHandler.LOG_IN_FAILED);
                handler.onFail(new NullPointerException("Google Sign In Account is null"),
                        UserActivityHandler.GOOGLE_SIGN_IN_FAILED);
                return;
            }
            signInGoogleToServer(account.getIdToken(), handler, context);
        }
    }

    private static void signInGoogleToServer(String idToken,
                                             final UserActivityHandler handler,
                                             final Context context) {
        Log.d(TAG, "Google ID Token: " + idToken);
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.wrapObservable(request.googleSignIn(idToken),
                new Consumer<LoginResponse>() {
                    @Override
                    public void accept(LoginResponse response) throws Exception {
                        handleLoginResponse(response, handler, context);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        handler.onFail(throwable, UserActivityHandler.LOG_IN_FAILED);
                    }
                });
    }

    public static void facebookLogIn(LoginResult result,
                                     final UserActivityHandler handler,
                                     final Context context) {
        String accessToken = result.getAccessToken().getToken();
        Log.d(TAG, "Facebook Access Token: " + accessToken);
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.wrapObservable(request.facebookLogin(accessToken),
                new Consumer<LoginResponse>() {
                    @Override
                    public void accept(LoginResponse response) throws Exception {
                        // save the token and user id to SharedPreferences to be used later
                        handleLoginResponse(response, handler, context);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        handler.onFail(throwable, UserActivityHandler.LOG_IN_FAILED);
                    }
                });
    }

    public static void emailLogIn(String email,
                                  String password,
                                  final UserActivityHandler handler,
                                  final Context context) {
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        ServerUtils.wrapObservable(request.emailLogin(body),
                new Consumer<LoginResponse>() {
                    @Override
                    public void accept(LoginResponse response) throws Exception {
                        handleLoginResponse(response, handler, context);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        handler.onFail(throwable, UserActivityHandler.LOG_IN_FAILED);
                        handler.onFail(throwable, UserActivityHandler.EMAIL_LOG_IN_FAILED);
                    }
                });
    }

    private static void handleLoginResponse(LoginResponse response,
                                            UserActivityHandler handler,
                                            Context context) {
        Log.d(TAG, "loginResponse: token = " + response.token);
        Log.d(TAG, "loginResponse: id = " + response.id);
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(context.getString(R.string.pref_token_key), response.token);
        editor.putString(context.getString(R.string.pref_id_key), response.id);
        editor.apply();
        handler.onSuccess(UserActivityHandler.LOG_IN_SUCCESSFUL);
    }

    public interface UserHandler {
        void handleUser(User user);

        void handleError(Throwable throwable);
    }

    public interface UserActivityHandler {
        int GOOGLE_SIGN_IN_SUCCESSFUL = 0;
        int GOOGLE_SIGN_IN_FAILED = 1;
        int FACEBOOK_LOG_IN_SUCCESSFUL = 2;
        int FACEBOOK_LOG_IN_FAILED = 3;
        int EMAIL_LOG_IN_SUCCESSFUL = 4;
        int EMAIL_LOG_IN_FAILED = 5;
        int LOG_IN_SUCCESSFUL = 6;
        int LOG_IN_FAILED = 7;
        int GOOGLE_SIGN_OUT_SUCCESSFUL = 7;
        int GOOGLE_SIGN_OUT_FAILED = 8;
        int LOG_OUT_SUCCESSFUL = 9;
        int LOG_OUT_FAILED = 10;

        void onSuccess(int code);

        void onFail(Throwable throwable, int code);
    }
}
