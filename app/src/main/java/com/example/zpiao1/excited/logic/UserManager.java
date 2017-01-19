package com.example.zpiao1.excited.logic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.zpiao1.excited.R;
import com.example.zpiao1.excited.data.User;
import com.example.zpiao1.excited.server.IUserRequest;
import com.example.zpiao1.excited.server.LoginResponse;
import com.example.zpiao1.excited.server.NormalResponse;
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


public class UserManager {
    public static final int REQ_CODE_GOOGLE_SIGN_IN = 1;
    public static final int REQ_CODE_LINK_GOOGLE = 2;
    private static final String TAG = UserManager.class.getSimpleName();

    private static CallbackManager mCallbackManager = CallbackManager.Factory.create();
    private static User mUser;
    private static Subject<User> mUserSubject;

    static {
        mUser = new User();
        mUser.status = User.STATUS_INIT;
        mUserSubject = new Subject<>(mUser);
    }

    private UserManager() {
    }

    private static synchronized void setUser(User user) {
        mUser = user;
        mUserSubject.update(user);
    }

    public static void init(Context appContext) {
        mUser = new User();
        mUser.status = User.STATUS_INIT;
        // Add mUserSubject to observe the user got from retrofit.
        fetchUser(appContext);
    }

    public static void subscribeForUser(int id, Observer<User> observer) {
        mUserSubject.subscribe(id, observer);
    }

    public static void unsubscribeForUser(int id) {
        mUserSubject.unsubscribe(id);
    }

    public static void fetchUser(Context context) {
        Log.d(TAG, "fetchUser");
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        String id = prefs.getString(context.getString(R.string.pref_id_key), null);
        String token = prefs.getString(context.getString(R.string.pref_token_key), null);
        if (id == null || token == null)
            return;
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.wrapObservable(request.getUser(id, token),
                new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        user.status = User.STATUS_LOGGED_IN;
                        setUser(user);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mUserSubject.updateError(throwable);
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
                Context.MODE_PRIVATE);
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.wrapObservable(request.logOut(),
                new Consumer<NormalResponse>() {
                    @Override
                    public void accept(NormalResponse response) throws Exception {
                        Log.d(TAG, "logout " + response.success);
                        if (response.success) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.remove(context.getString(R.string.pref_token_key));
                            editor.remove(context.getString(R.string.pref_id_key));
                            // Remove the email logged in key
                            if (prefs.contains(
                                    context.getString(R.string.pref_has_email_logged_in_key))) {
                                editor.remove(
                                        context.getString(R.string.pref_has_email_logged_in_key));
                            }
                            editor.commit();
                            if (hasFacebookLoggedIn()) {
                                facebookLogOut();
                            }
                            if (hasGoogleSignedIn(context)) {
                                googleSignOut(context, client, handler);
                            }

                            User loggedOutUser = new User();
                            loggedOutUser.status = User.STATUS_LOGGED_OUT;
                            setUser(loggedOutUser);
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
        if (!hasGoogleSignedIn(context)) {
            handler.onFail(new IllegalStateException("User has not signed in Google account!"),
                    UserActivityHandler.GOOGLE_SIGN_IN_FAILED);
            return;
        }
        Auth.GoogleSignInApi.signOut(client).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    SharedPreferences prefs = context.getSharedPreferences(
                            context.getString(R.string.shared_pref_name_server),
                            Context.MODE_PRIVATE);
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
                Context.MODE_PRIVATE);
        Log.d(TAG, "hasLoggedIn: " + (prefs.contains(context.getString(R.string.pref_id_key))
                && prefs.contains(context.getString(R.string.pref_token_key))));
        return prefs.contains(context.getString(R.string.pref_id_key))
                && prefs.contains(context.getString(R.string.pref_token_key));
    }

    public static boolean hasEmailLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        return prefs.getBoolean(context.getString(R.string.pref_has_email_logged_in_key), false);
    }

    public static boolean hasGoogleSignedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        return prefs.getBoolean(context.getString(R.string.pref_has_google_signed_in_key), false);
    }

    public static boolean hasFacebookLoggedIn() {
        return AccessToken.getCurrentAccessToken() != null;
    }

    public static void googleSignIn(GoogleApiClient client, Activity activity) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        activity.startActivityForResult(signInIntent, REQ_CODE_GOOGLE_SIGN_IN);
    }

    public static void googleSignIn(GoogleApiClient client, Fragment fragment) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        fragment.startActivityForResult(signInIntent, REQ_CODE_LINK_GOOGLE);
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

    public static void handleActivityResult(Fragment fragment,
                                            int requestCode,
                                            int resultCode,
                                            Intent data,
                                            UserActivityHandler handler) {
        Context context = fragment.getContext();
        if (requestCode == REQ_CODE_LINK_GOOGLE) {
            SharedPreferences prefs = context.getSharedPreferences(
                    context.getString(R.string.shared_pref_name_server),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(context.getString(R.string.pref_has_google_signed_in_key), true);
            editor.apply();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            linkGoogleAccount(result, context, handler);
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
                        handler.onSuccess(UserActivityHandler.FACEBOOK_LOG_IN_SUCCESSFUL);
                        handleLoginResponse(response, handler, context);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        handler.onFail(throwable, UserActivityHandler.FACEBOOK_LOG_IN_FAILED);
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
                        SharedPreferences prefs = context.getSharedPreferences(
                                context.getString(R.string.shared_pref_name_server),
                                Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(context.getString(R.string.pref_has_email_logged_in_key), true);
                        editor.commit();
                        handler.onSuccess(UserActivityHandler.EMAIL_LOG_IN_SUCCESSFUL);
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
        editor.commit();
        handler.onSuccess(UserActivityHandler.LOG_IN_SUCCESSFUL);
    }

    public static void changeUserName(String name, Context context) {
        Log.d(TAG, "changeUserName: " + name);
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        String id = mUser.getId();
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        String token = prefs.getString(context.getString(R.string.pref_token_key), null);
        HashMap<String, Object> map = new HashMap<>();
        map.put("displayName", name);
        if (id != null && token != null)
            ServerUtils.wrapObservable(request.changeName(id, token, map),
                    new Consumer<User>() {
                        @Override
                        public void accept(User user) throws Exception {
                            user.status = User.STATUS_LOGGED_IN;
                            setUser(user);
                        }
                    },
                    new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            mUserSubject.updateError(throwable);
                        }
                    });
    }

    public static void changeUserPassword(String originalPassword,
                                          String password,
                                          Context context) {
        Log.d(TAG, "changeUserPassword");
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        String id = prefs.getString(context.getString(R.string.pref_id_key), null);
        String token = prefs.getString(context.getString(R.string.pref_token_key), null);
        if (id == null || token == null)
            return;
        HashMap<String, Object> map = new HashMap<>();
        map.put("originalPassword", originalPassword);
        map.put("password", password);
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.wrapObservable(request.changePassword(id, token, map),
                new Consumer<NormalResponse>() {
                    @Override
                    public void accept(NormalResponse normalResponse) throws Exception {
                        if (normalResponse.success) {
                            User passwordChangedUser = new User();
                            passwordChangedUser.status = User.STATUS_PASSWORD_CHANGED;
                            setUser(passwordChangedUser);
                        } else {
                            mUserSubject.updateError(new Exception("Error in changing password"));
                        }
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mUserSubject.updateError(throwable);
                    }
                });
    }

    public static void linkFacebookAccount(LoginResult result,
                                           Context context,
                                           final UserActivityHandler handler) {
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        String id = prefs.getString(context.getString(R.string.pref_id_key), null);
        String token = prefs.getString(context.getString(R.string.pref_token_key), null);
        if (id == null || token == null)
            return;
        ServerUtils.wrapObservable(request.linkFacebook(id, token,
                result.getAccessToken().getToken()),
                new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        user.status = User.STATUS_LOGGED_IN;
                        setUser(user);
                        handler.onSuccess(UserActivityHandler.LINK_FACEBOOK_SUCCESSFUL);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        handler.onFail(throwable, UserActivityHandler.LINK_FACEBOOK_FAILED);
                    }
                });
    }

    public static void unlinkFacebookAccount(Context context,
                                             final UserActivityHandler handler) {
        facebookLogOut();
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        String id = prefs.getString(context.getString(R.string.pref_id_key), null);
        String token = prefs.getString(context.getString(R.string.pref_token_key), null);
        if (id == null || token == null) {
            handler.onFail(new Exception("User has not logged in!"),
                    UserActivityHandler.UNLINK_FACEBOOK_FAILED);
            return;
        }
        ServerUtils.wrapObservable(request.unlinkFacebook(id, token),
                new Consumer<NormalResponse>() {
                    @Override
                    public void accept(NormalResponse normalResponse) throws Exception {
                        if (normalResponse.success) {
                            mUser.status = User.STATUS_LOGGED_IN;
                            mUser.facebookProfile = null;
                            setUser(mUser);
                            handler.onSuccess(UserActivityHandler.UNLINK_FACEBOOK_SUCCESSFUL);
                        } else {
                            mUserSubject.updateError(new Exception("Failed to log out from Facebook"));
                        }
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mUserSubject.updateError(throwable);
                        handler.onFail(throwable, UserActivityHandler.UNLINK_FACEBOOK_FAILED);
                    }
                });
    }


    public static void linkGoogleAccount(GoogleSignInResult result,
                                         Context context,
                                         final UserActivityHandler handler) {
        if (!result.isSuccess()) {
            handler.onFail(new Exception("Failed to Sign In to Google"),
                    UserActivityHandler.LINK_GOOGLE_FAILED);
        } else {
            GoogleSignInAccount account = result.getSignInAccount();
            if (account == null) {
                handler.onFail(new NullPointerException("Google Sign In Account is null"),
                        UserActivityHandler.LINK_GOOGLE_FAILED);
                return;
            }
            linkGoogleOnServer(account.getIdToken(), context, handler);
        }
    }

    private static void linkGoogleOnServer(String idToken,
                                           Context context,
                                           final UserActivityHandler handler) {
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        String id = prefs.getString(context.getString(R.string.pref_id_key), null);
        String token = prefs.getString(context.getString(R.string.pref_token_key), null);
        if (id == null || token == null) {
            handler.onFail(new Exception("User has not logged in!"),
                    UserActivityHandler.LINK_GOOGLE_FAILED);
            return;
        }
        ServerUtils.wrapObservable(request.linkGoogle(id, token, idToken),
                new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        user.status = User.STATUS_LOGGED_IN;
                        setUser(user);
                        handler.onSuccess(UserActivityHandler.LINK_GOOGLE_SUCCESSFUL);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mUserSubject.updateError(throwable);
                        handler.onFail(throwable, UserActivityHandler.LINK_GOOGLE_FAILED);
                    }
                });
    }

    public static void unlinkGoogleAccount(final Context context,
                                           final GoogleApiClient client,
                                           final UserActivityHandler handler) {
        if (!hasGoogleSignedIn(context)) {
            handler.onFail(new IllegalStateException("User has not signed in Google account!"),
                    UserActivityHandler.UNLINK_GOOGLE_FAILED);
        }
        Auth.GoogleSignInApi.signOut(client).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    SharedPreferences prefs = context.getSharedPreferences(
                            context.getString(R.string.shared_pref_name_server),
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(context.getString(R.string.pref_has_google_signed_in_key));
                    editor.apply();
                    unlinkGoogleOnServer(context, handler);
                } else {
                    handler.onFail(new Exception(status.getStatusMessage()),
                            UserActivityHandler.UNLINK_GOOGLE_FAILED);
                }
            }
        });
    }

    private static void unlinkGoogleOnServer(Context context, final UserActivityHandler handler) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_pref_name_server),
                Context.MODE_PRIVATE);
        String id = prefs.getString(context.getString(R.string.pref_id_key), null);
        String token = prefs.getString(context.getString(R.string.pref_token_key), null);
        if (id == null || token == null) {
            handler.onFail(new IllegalStateException("User has not logged in!"),
                    UserActivityHandler.UNLINK_GOOGLE_FAILED);
        }
        IUserRequest request = ServerUtils.getRetrofit()
                .create(IUserRequest.class);
        ServerUtils.wrapObservable(request.unlinkGoogle(id, token),
                new Consumer<NormalResponse>() {
                    @Override
                    public void accept(NormalResponse normalResponse) throws Exception {
                        if (normalResponse.success) {
                            mUser.googleProfile = null;
                            mUser.status = User.STATUS_LOGGED_IN;
                            setUser(mUser);
                            handler.onSuccess(UserActivityHandler.UNLINK_GOOGLE_SUCCESSFUL);
                        } else {
                            handler.onFail(new Exception("Server error"),
                                    UserActivityHandler.UNLINK_GOOGLE_FAILED);
                        }
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        handler.onFail(throwable, UserActivityHandler.UNLINK_GOOGLE_FAILED);
                    }
                });
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
        int GOOGLE_SIGN_OUT_SUCCESSFUL = 8;
        int GOOGLE_SIGN_OUT_FAILED = 9;
        int LOG_OUT_SUCCESSFUL = 10;
        int LOG_OUT_FAILED = 11;
        int LINK_GOOGLE_SUCCESSFUL = 12;
        int LINK_GOOGLE_FAILED = 13;
        int LINK_FACEBOOK_SUCCESSFUL = 14;
        int LINK_FACEBOOK_FAILED = 15;
        int UNLINK_GOOGLE_SUCCESSFUL = 16;
        int UNLINK_GOOGLE_FAILED = 17;
        int UNLINK_FACEBOOK_SUCCESSFUL = 18;
        int UNLINK_FACEBOOK_FAILED = 19;

        String[] MESSAGES = {
                "GOOGLE_SIGN_IN_SUCCESSFUL",
                "GOOGLE_SIGN_IN_FAILED",
                "FACEBOOK_LOG_IN_SUCCESSFUL",
                "FACEBOOK_LOG_IN_FAILED",
                "EMAIL_LOG_IN_SUCCESSFUL",
                "EMAIL_LOG_IN_FAILED",
                "LOG_IN_SUCCESSFUL",
                "LOG_IN_FAILED",
                "GOOGLE_SIGN_OUT_SUCCESSFUL",
                "GOOGLE_SIGN_OUT_FAILED",
                "LOG_OUT_SUCCESSFUL",
                "LOG_OUT_FAILED",
                "LINK_GOOGLE_SUCCESSFUL",
                "LINK_GOOGLE_FAILED",
                "LINK_FACEBOOK_SUCCESSFUL",
                "LINK_FACEBOOK_FAILED",
                "UNLINK_GOOGLE_SUCCESSFUL",
                "UNLINK_GOOGLE_FAILED",
                "UNLINK_FACEBOOK_SUCCESSFUL",
                "UNLINK_FACEBOOK_FAILED"
        };

        void onSuccess(int code);

        void onFail(Throwable throwable, int code);
    }
}
