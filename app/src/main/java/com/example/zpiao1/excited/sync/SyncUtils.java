package com.example.zpiao1.excited.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.zpiao1.excited.data.EventContract;

/**
 * Created by zpiao on 10/31/2016.
 */

public class SyncUtils {
    public static final String ACCOUNT_TYPE = "com.example.zpiao1.excited.account";
    private static final long SYNC_FREQUENCY = 60 * 60;  // 1 hour
    private static final String CONTENT_AUTHORITY = EventContract.CONTENT_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void createSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = AccountService.getAccount(ACCOUNT_TYPE);
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);
            // Inform the system that this account is eligible for auto syn when the network is up
            ContentResolver.setSyncAutomatically(account, CONTENT_AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, CONTENT_AUTHORITY, new Bundle(), SYNC_FREQUENCY);

            newAccount = true;
        }
        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, se we need to check both.)
        if (newAccount || !setupComplete) {
            triggerRefresh();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).apply();
        }
    }

    public static void triggerRefresh() {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words... perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                AccountService.getAccount(ACCOUNT_TYPE),
                EventContract.CONTENT_AUTHORITY,
                b);
    }
}
