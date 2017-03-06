package com.example.brooklyn.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Brooklyn on 25-Jan-17.
 */

public class DataBaseSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Map";
    private static final int DB_VERSION = 13;
    public static final String TAG = "logz";

    public static final String TABLE_NAME = "places";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LAT = "latitude";
    public static final String COLUMN_LONG = "longtitude";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_LV = "lastVisited";
    public static final String TABLE_CREATE =
            "create table " + TABLE_NAME + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_LAT + " REAL, " +
                    COLUMN_LONG + " REAL, " +
                    COLUMN_TEXT + " text," +
                    COLUMN_IMAGE + " text," +
                    COLUMN_LV + " text" +
                    ");";

    private static DataBaseSQLiteOpenHelper sInstance;
    private static SQLiteDatabase mDB;

    public static synchronized DataBaseSQLiteOpenHelper getInstance(Context context) {

        if (sInstance == null) {
            Log.d(TAG, "getInstance: ");
            sInstance = new DataBaseSQLiteOpenHelper(context.getApplicationContext());
            mDB = sInstance.getWritableDatabase();
        }
        return sInstance;
    }

    private DataBaseSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public synchronized void close() {
        if (sInstance != null)
            mDB.close();
    }

    public boolean isEmpty () {

        try {
            Log.d(TAG, "isEmpty: " + mDB.query(TABLE_NAME, null, null, null, null, null, null).getCount());
            return !(mDB.query(TABLE_NAME, null, null, null, null, null, null).getCount() > 0);
        } catch (SQLiteException e) {
            return true;
        }
    }

    public long insert (Double latitude, Double longtitude, String text, String image, String lv) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_LAT, latitude);
        cv.put(COLUMN_LONG, longtitude);
        cv.put(COLUMN_TEXT, text);
        cv.put(COLUMN_IMAGE, image);
        cv.put(COLUMN_LV, lv);
        return mDB.insert(TABLE_NAME, null, cv);
    }

    public Cursor getAllData() {
        return mDB.query(TABLE_NAME, null, null, null, null, null, null);
    }
}