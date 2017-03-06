package com.example.brooklyn.myapplication;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.brooklyn.myapplication.MainActivity.TAG;

/**
 * Created by Brooklyn on 09-Feb-17.
 */

public class PointsFromBdLoader extends AsyncTaskLoader<ArrayList<GeoPoint>>{


    public PointsFromBdLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public ArrayList<GeoPoint> loadInBackground() {
        ArrayList<GeoPoint> geoPoints = new ArrayList<>();
        Log.d(TAG, "loadInBackground: ");
        Cursor cursor = DataBaseSQLiteOpenHelper.getInstance(getContext()).getAllData();

        if (cursor.moveToFirst()) {
            do {
                try {
                    String strDate = cursor
                            .getString(cursor.getColumnIndex(DataBaseSQLiteOpenHelper.COLUMN_LV));
                    Calendar date = Calendar.getInstance();
                    date.setTime(new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
                            .parse(strDate));
                    GeoPoint geoPoint =
                            new GeoPoint(
                                    cursor.getInt(cursor
                                            .getColumnIndex(DataBaseSQLiteOpenHelper.COLUMN_ID)),
                                    cursor.getDouble(cursor
                                            .getColumnIndex(DataBaseSQLiteOpenHelper.COLUMN_LAT)),
                                    cursor.getDouble(cursor
                                            .getColumnIndex(DataBaseSQLiteOpenHelper.COLUMN_LONG)),
                                    cursor.getString(cursor
                                            .getColumnIndex(DataBaseSQLiteOpenHelper.COLUMN_TEXT)),
                                    Uri.parse(cursor.getString(cursor
                                            .getColumnIndex(DataBaseSQLiteOpenHelper.COLUMN_IMAGE))),
                            date
                    );
                    geoPoints.add(geoPoint);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        return geoPoints;
    }

}
