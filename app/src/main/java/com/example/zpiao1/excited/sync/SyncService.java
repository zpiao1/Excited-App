package com.example.zpiao1.excited.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.zpiao1.excited.adapters.SyncAdapter;

public class SyncService extends Service {
    private static final String LOG_TAG = SyncService.class.getSimpleName();

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    /**
     * Thread-safe constructor, creates static {@link SyncAdapter} instance.
     */
    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null)
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
        }
    }
}
