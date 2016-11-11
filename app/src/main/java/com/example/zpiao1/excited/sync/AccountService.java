package com.example.zpiao1.excited.sync;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * The service to handle the account used for syncing
 */
public class AccountService extends Service {
    public static final String ACCOUNT_NAME = "Account";
    private static final String LOG_TAG = AccountService.class.getSimpleName();
    private Authenticator mAuthenticator;

    public AccountService() {
    }

    public static Account getAccount(String accountType) {
        return new Account(ACCOUNT_NAME, accountType);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }
}
