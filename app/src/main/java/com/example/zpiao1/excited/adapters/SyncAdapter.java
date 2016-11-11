package com.example.zpiao1.excited.adapters;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.example.zpiao1.excited.data.EventContract;
import com.example.zpiao1.excited.data.EventContract.EventEntry;
import com.example.zpiao1.excited.data.EventEntity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zpiao on 10/31/2016.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_TITLE = 1;
    public static final int COLUMN_CATEGORY = 2;
    public static final int COLUMN_DATE = 3;
    public static final int COLUMN_VENUE = 4;
    public static final int COLUMN_PICTURE_URL = 5;
    public static final int COLUMN_POSTAL_ADDRESS = 6;
    // Basic URL
    private static final Uri BASE_URL = Uri.parse("http://thehoneycombers.com/singapore/event-category");
    private static final String[] PATHS = {"kids", "arts-and-culture", "music-and-nightlife", "sports-and-fitness"};
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.71 Safari/537.36";
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 100000;
    private static final String[] PROJECTION = {
            EventEntry._ID,
            EventEntry.COLUMN_TITLE,
            EventEntry.COLUMN_CATEGORY,
            EventEntry.COLUMN_DATE,
            EventEntry.COLUMN_VENUE,
            EventEntry.COLUMN_PICTURE_URL,
            EventEntry.COLUMN_POSTAL_ADDRESS};
    private final ContentResolver mContentResolver;
    private List<String> mUrls;
    private Set<EventEntity> mEventEntities;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mUrls = new ArrayList<>();
        mEventEntities = new HashSet<>();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        mUrls = new ArrayList<>();
        mEventEntities = new HashSet<>();
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        try {
            getEventURLs();
            getEventData();
            updateDatabase(syncResult);
        } catch (IOException e) {
            Log.e(TAG, "Error in fetching/parsing data", e);
            syncResult.stats.numIoExceptions++;
        } catch (RemoteException e) {
            Log.e(TAG, "Error in updating database", e);
            syncResult.databaseError = true;
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Error in updating database", e);
            syncResult.databaseError = true;
        }
    }

    private void getEventURLs() throws IOException {
        for (String path : PATHS) {
            String url = Uri.withAppendedPath(BASE_URL, path).toString();
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(NET_CONNECT_TIMEOUT_MILLIS)
                    .get();
            Elements elements = document.select("a[rel=bookmark]");
            int i = 0;
            for (Element element : elements) {
                if (i == 10)
                    break;
                mUrls.add(element.attr("href"));
                ++i;
            }
        }
    }

    private void getEventData() throws IOException {
        for (String url : mUrls) {
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(NET_CONNECT_TIMEOUT_MILLIS)
                    .get();
            String title = document.select("h1[class=entry-title]").text();
            String category = document.select("p[class=entry-meta]").text();
            String date = document.select("dt:contains(Date) + dd").text();
            String venue = document.select("dt:contains(Venue) + dd").text();
            Log.d(TAG, "title: " + title);
            Log.d(TAG, "category: " + category);
            Log.d(TAG, "date: " + date);
            Log.d(TAG, "venue: " + venue);
            String pictureUrl = "";
            String postalAddress = "";
            Elements pics = document.select("img[class=aligncenter]");
            if (!pics.isEmpty()) {
                pictureUrl = pics.get(0).attr("src");
                postalAddress = "";
                if (pics.size() > 2)
                    postalAddress = pics.get(2).attr("alt");
            } else {
                Log.e(TAG, "no space available");
            }
            Log.d(TAG, "pictureUrl: " + pictureUrl);
            Log.d(TAG, "postalAddress: " + postalAddress);
            int categoryId = EventEntry.parseCategory(category);
            mEventEntities.add(new EventEntity(title, categoryId, date, venue, pictureUrl,
                    postalAddress));
        }
    }

    /**
     * Merge the incoming date into the database.
     * 1. If the data is already in database, do nothing.
     * 2. If the data is not in database, insert it later.
     */
    private void updateDatabase(SyncResult syncResult) throws RemoteException, OperationApplicationException {
        Log.i(TAG, "Fetching local events for merge");
        Uri uri = EventEntry.CONTENT_URI;
        // try with resources
        try (Cursor c = mContentResolver.query(uri, PROJECTION, null, null, null)) {
            assert c != null;
            Log.i(TAG, "Found " + c.getCount() + " local events. Computing merge solution...");

            // Find stale data
            String title;
            int category;
            String date;
            String venue;
            String pictureUrl;
            String postalCode;
            while (c.moveToNext()) {
                syncResult.stats.numEntries++;
                title = c.getString(COLUMN_TITLE);
                category = c.getInt(COLUMN_CATEGORY);
                date = c.getString(COLUMN_DATE);
                venue = c.getString(COLUMN_VENUE);
                pictureUrl = c.getString(COLUMN_PICTURE_URL);
                postalCode = c.getString(COLUMN_POSTAL_ADDRESS);
                EventEntity existingEvent = new EventEntity(title, category, date, venue,
                        pictureUrl, postalCode);
                // If the mEventEntities contains an event that is in the database
                // remove that event
                if (mEventEntities.contains(existingEvent))
                    mEventEntities.remove(existingEvent);
            }
        }
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        for (EventEntity e : mEventEntities) {
            Log.i(TAG, "Scheduling insert: " + e);
            batch.add(ContentProviderOperation.newInsert(EventEntry.CONTENT_URI)
                    .withValue(EventEntry.COLUMN_TITLE, e.getTitle())
                    .withValue(EventEntry.COLUMN_CATEGORY, e.getCategory())
                    .withValue(EventEntry.COLUMN_DATE, e.getDate())
                    .withValue(EventEntry.COLUMN_VENUE, e.getVenue())
                    .withValue(EventEntry.COLUMN_PICTURE_URL, e.getPictureUrl())
                    .withValue(EventEntry.COLUMN_POSTAL_ADDRESS, e.getPostalAddress())
                    .build());
            syncResult.stats.numInserts++;
        }

        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(EventContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(
                EventEntry.CONTENT_URI,
                null,
                false);
    }
}
