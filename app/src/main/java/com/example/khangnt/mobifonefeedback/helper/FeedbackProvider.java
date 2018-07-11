
package com.example.khangnt.mobifonefeedback.helper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class FeedbackProvider extends ContentProvider {

    private static final String TAG = "FeedbackProvider";

    /* Mobifone feedback DataBase */
    public static final String TABLE_NAME_USER = "user";

    public static final String TABLE_NAME_FB = "feedback";

    public static final String DATABASE_NAME = "mobifonefeedback.db";

    private SQLiteOpenHelper mOpenHelper;

    public static final String AUTHORITY = "com.example.khangnt.mobifonefeedback";

    public static final Uri CONTENT_URI_USER = Uri.parse("content://" + AUTHORITY + '/' + TABLE_NAME_USER);

    public static final Uri CONTENT_URI_FB = Uri.parse("content://" + AUTHORITY + '/' + TABLE_NAME_FB);

    /* Mobifone Feedback DataBase : Column Names of user table */
    public static final String TAG_UID = "_uid";

    public static final String TAG_EMAIL = "email";

    public static final String TAG_PERMISSION = "permission";

    public static final String TAG_DEVICEID = "deviceID";

    /* Mobifone Feedback DataBase : Column Names of feedback table */

    public static final String TAG_FID = "_fid";

    public static final String TAG_SUBJECT = "subject";

    public static final String TAG_CONTENT = "content";

    public static final String TAG_REPLY = "reply";

    public static final String TAG_DATE = "date";

    public static final String TAG_STATUS = "status";

    public static final String TAG_UFID = "_uid";

    /* Mobifone feedback DataBase : Column index of user table */
    public static final int COLUMN_UID = 0;

    public static final int COLUMN_EMAIL = 1;

    public static final int COLUMN_PERMISSION = 2;

    public static final int COLUMN_DEVICEID = 3;

    /* Mobifone feedback DataBase : Column index of feedback table */

    public static final int COLUMN_FID = 0;

    public static final int COLUMN_SUBJECT = 1;

    public static final int COLUMN_CONTENT = 2;

    public static final int COLUMN_REPLY = 3;

    public static final int COLUMN_DATE = 4;

    public static final int COLUMN_STATUS = 5;

    public static final int COLUMN_UFID = 6;


    /* Mobifone feedback DataBase : create table query */
    public final static String CREATE_TABLE_FB = "CREATE TABLE " + TABLE_NAME_FB
            + " (" + TAG_FID + " TEXT PRIMARY KEY NOT NULL," + TAG_SUBJECT
            + " TEXT NOT NULL," + TAG_CONTENT + " TEXT NOT NULL," + TAG_REPLY
            + " TEXT," + TAG_DATE + " TEXT NOT NULL," + TAG_STATUS
            + " TEXT NOT NULL," + TAG_UFID + " INTEGER NOT NULL, FOREIGN KEY (" + TAG_UFID + ") REFERENCES " + TABLE_NAME_USER + "(" + TAG_UID + "))";

    public final static String CREATE_TABLE_USER = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_USER
            + " (" + TAG_UID + " TEXT PRIMARY KEY NOT NULL," + TAG_EMAIL
            + " TEXT NOT NULL," + TAG_PERMISSION + " INTEGER NOT NULL," + TAG_DEVICEID
            + " TEXT NOT NULL, CONSTRAINT email_unq UNIQUE (" + TAG_EMAIL + "))";


    /* The default sort order for this table */
    private static final String[] QUERY_COLUMNS_USER = {
            TAG_UID, TAG_EMAIL, TAG_PERMISSION, TAG_DEVICEID};

    private static final String[] QUERY_COLUMNS_FB = {
            TAG_FID, TAG_SUBJECT, TAG_CONTENT, TAG_REPLY,
            TAG_DATE, TAG_STATUS, TAG_UFID};

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate provider");
        final Context context = getContext();
        mOpenHelper = new DatabaseHelper(context);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);
        SQLiteDatabase db;

        try {
            db = mOpenHelper.getWritableDatabase();
        } catch (Exception e) {
            db = mOpenHelper.getReadableDatabase();
            Log.d(TAG, "query Exception");
        }

        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);

        if (result != null) {
            Context context = getContext();

            if (context != null) {
                result.setNotificationUri(context.getContentResolver(), uri);
            }
        }

        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId;

        try {
            rowId = db.insert(args.table, null, initialValues);
        } catch (Exception e) {
            Log.e(TAG, "insert Exception");
            throw e;
        }

        if (rowId <= 0) {
            return null;
        }

        Uri uriResult = ContentUris.withAppendedId(uri, rowId);

        return uriResult;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;

        try {
            count = db.delete(args.table, args.where, args.args);
        } catch (Exception e) {
            Log.e(TAG, "delete Exception");
            throw e;
        }

        // finally {
        // db.close();
        // }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;

        try {
            count = db.update(args.table, values, args.where, args.args);
        } catch (Exception e) {
            Log.e(TAG, "update Exception");
            throw e;
        }

        return count;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);

        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    private void sendNotify(Uri uri) {
        Context context = getContext();

        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        }
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {

        private int DATABASE_VERSION = 0;

        private static final int DATABASE_CURRENT_VERSION = 1;

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_CURRENT_VERSION);

            DATABASE_VERSION = DATABASE_CURRENT_VERSION;
            Log.d(TAG, "DatabaseHelper: " + DATABASE_VERSION);
            Log.d(TAG, "USER: " + CREATE_TABLE_USER);
            Log.d(TAG, "FB: " + CREATE_TABLE_FB);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, "creating new mobifone feedback database");

            db.execSQL(CREATE_TABLE_USER);
            db.execSQL(CREATE_TABLE_FB);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "onUpgrade triggered");

            Log.d(TAG, "Destroying all old data.");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_FB);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER);
            onCreate(db);
        }
    }

    private static class SqlArguments {

        public final String table;

        public final String where;

        public final String[] args;

        private SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        private SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }

    public static void storeNewUserToSQLite(Context context, String uid, String email, String permission, String deviceID) {
        ContentValues values = new ContentValues();
        values.put(TAG_UID, uid);
        values.put(TAG_EMAIL, email);
        values.put(TAG_PERMISSION, permission);
        values.put(TAG_DEVICEID, deviceID);
        context.getContentResolver().insert(CONTENT_URI_USER, values);
    }

    public static void storeNewFeedbackToSQLite(Context context, String fid, String subject, String content,
                                         String reply, String date_submit, String status, String uid) {
        ContentValues values = new ContentValues();
        values.put(TAG_FID, fid);
        values.put(TAG_SUBJECT, subject);
        values.put(TAG_CONTENT, content);
        values.put(TAG_REPLY, reply);
        values.put(TAG_DATE, date_submit);
        values.put(TAG_UFID, uid);
        context.getContentResolver().insert(CONTENT_URI_FB, values);
    }

    public static String getUserID(Context context) {
        Cursor c = context.getContentResolver().query(CONTENT_URI_USER,
                new String[]{TAG_UID}, null, null, null);
        if (c != null && c.getCount() > 0 ) {
            c.moveToFirst();
            return c.getString(COLUMN_UID);
        }
        return "-1";
    }

    public static String getUserPermission(Context context) {
        Cursor c = context.getContentResolver().query(CONTENT_URI_USER,
                new String[]{TAG_PERMISSION}, null, null, null);
        if (c != null && c.getCount() > 0 ) {
            c.moveToFirst();
            return c.getString(COLUMN_PERMISSION);
        }
        return "pending";
    }

    public static void deleteUserID(Context context) {
        context.getContentResolver().delete(CONTENT_URI_USER, null, null);
    }
}
