package com.example.brooklyn.myapplication;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.brooklyn.myapplication.MainActivity.TAG;

public class LoadFromURLService extends IntentService {

    public static final String JSON_URL = "http://interesnee.ru/files/android-middle-level-data.json";
    public static final  String JSON_OBJECT_NAME = "places";

    String resultJson = "";
    ResultReceiver rec;

    public LoadFromURLService() {
        super("LoadFromUrl");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "Service started");
        Request request = new Request.Builder()
                .url(JSON_URL)
                .build();
        OkHttpClient client = new OkHttpClient();
        rec = intent.getParcelableExtra("receiver");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                rec.send(Activity.RESULT_CANCELED, null);
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    resultJson = response.body().string();
                    JSONObject dataJsonObj = new JSONObject(resultJson);
                    JSONArray placesJson = dataJsonObj.getJSONArray(JSON_OBJECT_NAME);
                    for (int i = 0; i < placesJson.length(); i++) {
                        JSONObject place = placesJson.getJSONObject(i);

                        DataBaseSQLiteOpenHelper.getInstance(getApplicationContext())
                                .insert(place.getDouble(DataBaseSQLiteOpenHelper.COLUMN_LAT),
                                        place.getDouble(DataBaseSQLiteOpenHelper.COLUMN_LONG),
                                        place.getString(DataBaseSQLiteOpenHelper.COLUMN_TEXT),
                                        place.getString(DataBaseSQLiteOpenHelper.COLUMN_IMAGE),
                                        place.getString(DataBaseSQLiteOpenHelper.COLUMN_LV));
                    }
                    rec.send(Activity.RESULT_OK, null);
                    stopSelf();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
