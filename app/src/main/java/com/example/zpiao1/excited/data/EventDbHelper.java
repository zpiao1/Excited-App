package com.example.zpiao1.excited.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.zpiao1.excited.data.EventContract.EventEntry;

public class EventDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = EventDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "events.db";
    // Update the DATABASE_VERSION to reflect the change in database;
    private static final int DATABASE_VERSION = 2;

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String DEFAULT = " DEFAULT ";
    private static final String NOT_NULL = " NOT NULL";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +
                    EventEntry._ID + TYPE_INTEGER + " PRIMARY KEY" + COMMA_SEP +
                    EventEntry.COLUMN_TITLE + TYPE_TEXT + COMMA_SEP +
                    EventEntry.COLUMN_CATEGORY + TYPE_INTEGER + COMMA_SEP +
                    EventEntry.COLUMN_DATE + TYPE_TEXT + COMMA_SEP +
                    EventEntry.COLUMN_VENUE + TYPE_TEXT + COMMA_SEP +
                    EventEntry.COLUMN_PICTURE_URL + TYPE_TEXT + COMMA_SEP +
                    EventEntry.COLUMN_POSTAL_ADDRESS + TYPE_TEXT + COMMA_SEP +
                    EventEntry.COLUMN_IS_STARRED + TYPE_INTEGER + NOT_NULL + DEFAULT + EventEntry.BOOLEAN_FALSE + COMMA_SEP +
                    EventEntry.COLUMN_IS_REMOVED + TYPE_INTEGER + NOT_NULL + DEFAULT + EventEntry.BOOLEAN_FALSE + ")";

    private static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME;

    public EventDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // "CREATE TABLE events (
        //     _id INTEGER PRIMARY KEY,
        //     title TEXT,
        //     category TEXT,
        //     date TEXT,
        //     venue TEXT,
        //     picture_url TEXT,
        //     postal_address TEXT,
        //     is_removed INTEGER NOT NULL DEFAULT 0,
        //     is_starred INTEGER NOT NULL DEFAULT 0);"
        Log.d(LOG_TAG, "Create table statement: " + SQL_CREATE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(LOG_TAG, "Drop table statement: " + SQL_DROP_TABLE);
        // "DROP TABLE events;"
        sqLiteDatabase.execSQL(SQL_DROP_TABLE);
        onCreate(sqLiteDatabase);
    }
}
