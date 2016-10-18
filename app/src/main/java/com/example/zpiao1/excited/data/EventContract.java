package com.example.zpiao1.excited.data;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public class EventContract {
    static final String CONTENT_AUTHORITY = "com.example.zpiao1.excited";
    static final String PATH_EVENTS = "events";
    // "content://com.example.zpiao1.excited"
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private EventContract() {
    }

    public static final class EventEntry implements BaseColumns {

        // "content://com.example.zpiao1.excited/events"
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EVENTS);
        public static final String COLUMN_IMAGE_ID = "image_id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_IS_STARRED = "is_starred";
        public static final String COLUMN_IS_REMOVED = "is_removed";
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_END_TIME = "end_time";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_VENUE = "venue";

        public static final int CATEGORY_MOVIE = 0;
        public static final int CATEGORY_ART = 1;
        public static final int CATEGORY_SPORTS = 2;
        public static final int CATEGORY_NIGHTLIFE = 3;
        public static final int CATEGORY_KIDS = 4;
        public static final int CATEGORY_EXPO = 5;

        public static final int BOOLEAN_TRUE = 1;
        public static final int BOOLEAN_FALSE = 0;

        static final String TABLE_NAME = "events";

        static boolean isValidBoolean(int booleanValue) {
            return booleanValue == BOOLEAN_TRUE || booleanValue == BOOLEAN_FALSE;
        }

        static boolean isValidCategory(int categoryValue) {
            return categoryValue >= 0 && categoryValue <= 6;
        }

        static boolean isValidImageId(int imageIdValue) {
            return imageIdValue >= 0;
        }

        public static ContentValues buildContentValues(
                int imageId,
                String date,
                String title,
                String startTime,
                String endTime,
                int category,
                String venue) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IMAGE_ID, imageId);
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_TITLE, title);
            values.put(COLUMN_START_TIME, startTime);
            values.put(COLUMN_END_TIME, endTime);
            values.put(COLUMN_CATEGORY, category);
            values.put(COLUMN_VENUE, venue);
            return values;
        }

        public static String getCatetoryFromId(int categoryId) {
            switch (categoryId) {
                case CATEGORY_MOVIE:
                    return "Movie";
                case CATEGORY_ART:
                    return "Art";
                case CATEGORY_SPORTS:
                    return "Sports";
                case CATEGORY_NIGHTLIFE:
                    return "NightLife";
                case CATEGORY_KIDS:
                    return "Kids";
                case CATEGORY_EXPO:
                    return "Expo";
                default:
                    throw new IllegalArgumentException("Invalid category ID");
            }
        }
    }
}
