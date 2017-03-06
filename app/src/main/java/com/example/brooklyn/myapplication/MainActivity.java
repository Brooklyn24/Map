package com.example.brooklyn.myapplication;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import permissions.dispatcher.RuntimePermissions;

public class MainActivity extends AppCompatActivity implements AddNewPlaceFragment.INewPlaceAdded {
    public static final String MAP_FRAGMENT_TAG = "MAP_FRAGMENT_TAG";
    public static final String TAG = "logz";

    private MyMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        if (savedInstanceState == null) {
            Picasso.Builder picassoBuilder = new Picasso.Builder(this);
            picassoBuilder.downloader(new OkHttp3Downloader(new OkHttpClient()));
            Picasso.setSingletonInstance(picassoBuilder.build());
            mapFragment = new MyMapFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.frameLayoutForFragments, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        } else {
            mapFragment = (MyMapFragment) getFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        }
    }

    @Override
    public void addNewPlace(GeoPoint geoPoint) {
        mapFragment.addNewPlace(geoPoint);
    }
}
