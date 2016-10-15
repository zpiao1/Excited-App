package com.example.zpiao1.excited.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.zpiao1.excited.data.EventContract.EventEntry;

public class EventDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "events.db";
    private static final int DATABASE_VERSION = 1;

    public EventDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // "CREATE TABLE events (
        //     _id INTEGER PRIMARY KEY,
        //     image_id INTEGER NOT NULL,
        //     title TEXT NOT NULL,
        //     category TEXT NOT NULL,
        //     date TEXT NOT NULL,
        //     time TEXT NOT NULL,
        //     venue TEXT NOT NULL,
        //     start_time TEXT NOT NULL,
        //     end_time TEXT NOT NULL,
        //     is_removed INTEGER NOT NULL DEFAULT 0,
        //     is_starred INTEGER NOT NULL DEFAULT 0);"
        String SQL_DROP_EVENTS_TABLE = "DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME + ";";
        String SQL_CREATE_EVENTS_TABLE = "CREATE TABLE " + EventEntry.TABLE_NAME + "( "
                + EventEntry._ID + " INTEGER PRIMARY KEY, "
                + EventEntry.COLUMN_IMAGE_ID + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_TITLE + " TEXT NOT NULL, "
                + EventEntry.COLUMN_CATEGORY + " INTEGER NOT NULL, "
                + EventEntry.COLUMN_DATE + " TEXT NOT NULL, "
                + EventEntry.COLUMN_START_TIME + " TEXT NOT NULL, "
                + EventEntry.COLUMN_END_TIME + " TEXT NOT NULL, "
                + EventEntry.COLUMN_VENUE + " TEXT NOT NULL, "
                + EventEntry.COLUMN_IS_REMOVED + " INTEGER NOT NULL DEFAULT 0, "
                + EventEntry.COLUMN_IS_STARRED + " INTEGER NOT NULL DEFAULT 0);";
        sqLiteDatabase.execSQL(SQL_DROP_EVENTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // "DROP TABLE events;"
        String SQL_DROP_EVENTS_TABLE = "DROP TABLE " + EventEntry.TABLE_NAME + ";";
        sqLiteDatabase.execSQL(SQL_DROP_EVENTS_TABLE);
        onCreate(sqLiteDatabase);
    }
}
