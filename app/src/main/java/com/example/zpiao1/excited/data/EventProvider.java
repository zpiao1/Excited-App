package com.example.zpiao1.excited.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.zpiao1.excited.data.EventContract.EventEntry;

public class EventProvider extends ContentProvider {
    // Query one or more values from the table
    private static final int EVENTS = 100;
    // The URI is appended with the row ID, query only that row
    private static final int EVENT_ID = 101;
    private static final UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String LOG_TAG = EventProvider.class.getSimpleName();

    // static initializer to add the match rules to the UriMatcher
    static {
        sMatcher.addURI(EventContract.CONTENT_AUTHORITY, EventContract.PATH_EVENTS, EVENTS);
        sMatcher.addURI(EventContract.CONTENT_AUTHORITY, EventContract.PATH_EVENTS + "/#", EVENT_ID);
    }

    private EventDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new EventDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        final int match = sMatcher.match(uri);
        switch (match) {
            case EVENT_ID:
                // Look for the single tuple that has the same _id as provided
                selection = EventEntry._ID + "=?";
                selectionArgs = new String[]{Long.toString(ContentUris.parseId(uri))};
            case EVENTS:
                // Look for one or more tuples that satisfy the conditions
                cursor = db.query(EventEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                Context context = getContext();
                if (context != null)
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                else
                    throw new RuntimeException("Context is null");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI to query");
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sMatcher.match(uri);
        switch (match) {
            case EVENTS:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + EventContract.CONTENT_AUTHORITY;
            case EVENT_ID:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + EventContract.CONTENT_AUTHORITY;
            default:
                throw new IllegalArgumentException("Unknown URI to get type");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sMatcher.match(uri);
        switch (match) {
            // Can only insert into the table, hence providing the URI with row ID is invalid
            case EVENTS:
                Uri returnUri = insertHelper(uri, contentValues);
                Context context = getContext();
                if (context != null)
                    context.getContentResolver().notifyChange(returnUri, null);
                else
                    throw new RuntimeException("Context is null");
                return returnUri;
            default:
                throw new IllegalArgumentException("Unknown URI to insert");
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final int match = sMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int numDeleted;
        switch (match) {
            case EVENT_ID:
                // Delete only one row in the table
                selection = EventEntry._ID + " = ?";
                selectionArgs = new String[]{Long.toString(ContentUris.parseId(uri))};
            case EVENTS:
                // Delete one or more values from the table
                numDeleted = db.delete(EventEntry.TABLE_NAME, selection, selectionArgs);
                Context context = getContext();
                if (context != null)
                    context.getContentResolver().notifyChange(uri, null);
                else
                    throw new RuntimeException("Context is null");
                return numDeleted;
            default:
                throw new IllegalArgumentException("Unknown URI to delete");
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sMatcher.match(uri);
        int numUpdated;
        switch (match) {
            case EVENT_ID:
                // Update the row itself
                selection = EventEntry._ID + " = ?";
                selectionArgs = new String[]{Long.toString(ContentUris.parseId(uri))};
            case EVENTS:
                // Update one or more tuples that satisfy the conditions
                numUpdated = updateHelper(contentValues, selection, selectionArgs);
                Context context = getContext();
                if (context != null)
                    context.getContentResolver().notifyChange(uri, null);
                else
                    throw new RuntimeException("Context is null");
                return numUpdated;
            default:
                throw new IllegalArgumentException("Unknown URI to update");
        }
    }

    private Uri insertHelper(Uri uri, ContentValues contentValues) {
        // Lots of sanity checks
        checkContainsIntegerAndNotNull(contentValues, EventEntry.COLUMN_IMAGE_ID);
        if (!EventEntry.isValidImageId(contentValues.getAsInteger(EventEntry.COLUMN_IMAGE_ID)))
            throw new IllegalArgumentException("image_id must be non-negative");

        checkContainsStringAndNotNull(contentValues, EventEntry.COLUMN_TITLE);

        checkContainsIntegerAndNotNull(contentValues, EventEntry.COLUMN_CATEGORY);
        if (!EventEntry.isValidCategory(contentValues.getAsInteger(EventEntry.COLUMN_CATEGORY)))
            throw new IllegalArgumentException("category must be in [0, 5]");

        checkContainsStringAndNotNull(contentValues, EventEntry.COLUMN_DATE);
        checkContainsStringAndNotNull(contentValues, EventEntry.COLUMN_START_TIME);
        checkContainsStringAndNotNull(contentValues, EventEntry.COLUMN_END_TIME);
        checkContainsStringAndNotNull(contentValues, EventEntry.COLUMN_VENUE);

        checkContainsIntegerAndNotNull(contentValues, EventEntry.COLUMN_IS_REMOVED);
        if (!EventEntry.isValidBoolean(contentValues.getAsInteger(EventEntry.COLUMN_IS_REMOVED)))
            throw new IllegalArgumentException("is_removed must be 0 or 1");

        checkContainsIntegerAndNotNull(contentValues, EventEntry.COLUMN_IS_STARRED);
        if (!EventEntry.isValidBoolean(contentValues.getAsInteger(EventEntry.COLUMN_IS_STARRED)))
            throw new IllegalArgumentException("is_starred must be 0 or 1");

        // Actual insertion
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(EventEntry.TABLE_NAME, null, contentValues);
        if (id != -1)
            return ContentUris.withAppendedId(uri, id);
        else
            throw new RuntimeException("Insert failed");
    }

    private int updateHelper(ContentValues contentValues, String selection, String[] selectionArgs) {
        // Sanity checks to prevent invalid non-null values
        Integer imageId = contentValues.getAsInteger(EventEntry.COLUMN_IMAGE_ID);
        if (imageId != null && !EventEntry.isValidImageId(imageId))
            throw new IllegalArgumentException("image_id must be non-negative");

        Integer category = contentValues.getAsInteger(EventEntry.COLUMN_CATEGORY);
        if (category != null && !EventEntry.isValidCategory(category))
            throw new IllegalArgumentException("category must be in [0, 5]");

        Integer isRemoved = contentValues.getAsInteger(EventEntry.COLUMN_IS_REMOVED);
        if (isRemoved != null && !EventEntry.isValidBoolean(isRemoved))
            throw new IllegalArgumentException("is_removed must be 0 or 1");

        Integer isStarred = contentValues.getAsInteger(EventEntry.COLUMN_IS_STARRED);
        if (isStarred != null && !EventEntry.isValidBoolean(isStarred))
            throw new IllegalArgumentException("is_starred must be 0 or 1");

        // Action update
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.update(EventEntry.TABLE_NAME, contentValues, selection, selectionArgs);
    }

    private void checkContainsStringAndNotNull(ContentValues contentValues, String columnName) {
        if (contentValues.getAsString(columnName) == null)
            throw new IllegalArgumentException(columnName + " cannot be NULL");
    }

    private void checkContainsIntegerAndNotNull(ContentValues contentValues, String columnName) {
        if (contentValues.getAsInteger(columnName) == null)
            throw new IllegalArgumentException(columnName + " cannot be NULL");
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final int match = sMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Context context = getContext();
        switch (match) {
            // Can only insert to the table but not a single row
            case EVENTS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long id = db.insert(EventEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            ++returnCount;
                            Log.v(LOG_TAG, "Inserted: " +
                                    value.getAsString(EventEntry.COLUMN_TITLE) + "id: " + id);
                            Uri singleUri = ContentUris.withAppendedId(uri, id);
                            if (context != null)
                                context.getContentResolver().notifyChange(singleUri, null);

                        } else
                            throw new RuntimeException("Insert failed");
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (context != null)
                    context.getContentResolver().notifyChange(uri, null);
                else
                    throw new RuntimeException("Context is null");
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
