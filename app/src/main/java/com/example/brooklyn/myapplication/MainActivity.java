package com.example.brooklyn.myapplication;

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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<ArrayList<GeoPoint>>, AddNewPlaceFragment.INewPlaceAdded {
    public static final String MAP_FRAGMENT_TAG = "MAP_FRAGMENT_TAG";
    public static final String ADD_PLACE_FRAGMENT_TAG = "ADD_PLACE_FRAGMENT_TAG";
    public static final String TAG = "logz";

    private ServiceResultReceiver serviceResultReceiver;
    private GoogleMap map;
    private Snackbar addNewPlaceSnackbar;
    private MapFragment mapFragment;
    private ArrayList<GeoPoint> geoPoints;
    private boolean isMapEditable;

    @BindView(R.id.activity_main) CoordinatorLayout coordinatorLayout;

    @BindView(R.id.fab) FloatingActionButton fab;
    @OnClick(R.id.fab)
    public void fabClick (){
        isMapEditable = true;
        fab.hide();
        addNewPlaceSnackbar = Snackbar.make(coordinatorLayout,
                R.string.snaclbar_alert_add_new_place, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_alert_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isMapEditable = false;
                        fab.show();
                    }
                });
        addNewPlaceSnackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        isMapEditable = false;
        if (savedInstanceState == null) {
            Picasso.Builder picassoBuilder = new Picasso.Builder(this);
            picassoBuilder.downloader(new OkHttp3Downloader(new OkHttpClient()));
            Picasso.setSingletonInstance(picassoBuilder.build());
            mapFragment = MapFragment.newInstance();
            getFragmentManager().beginTransaction()
                    .add(R.id.frameLayoutForFragments, mapFragment, MAP_FRAGMENT_TAG)
                    .commit();
        } else {
            mapFragment = (MapFragment) getFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        }
        startServiceOrLoadData();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                GeoPoint markerPoint = geoPoints.get(Integer.parseInt(marker.getSnippet()));

                View v = getLayoutInflater().inflate(R.layout.costom_map_window, null);
                TextView text = (TextView) v.findViewById(R.id.textView);
                TextView lastVisited = (TextView) v.findViewById(R.id.textView2);
                ImageView image = (ImageView) v.findViewById(R.id.imageView);
                String formatedDate = new SimpleDateFormat("EEE, d MMM yyyy K:mm a", Locale.ENGLISH)
                        .format(markerPoint.getLastVisited().getTime());
                text.setText(marker.getTitle());
                lastVisited.setText("Last visited" + formatedDate);
                Uri imageUrl = markerPoint.getImage();

                Picasso.with(MainActivity.this).load(imageUrl).resize(0, 300)
                        .into(image, new InfoWindowRefresher(marker));

                return v;
            }
        });
        initMapIfExist();

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isMapEditable) {
                    addNewPlaceSnackbar.dismiss();
                    AddNewPlaceFragment addNewPlaceFragment =
                            AddNewPlaceFragment.newInstance(latLng);
                    fab.show();
                    isMapEditable = false;
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameLayoutForFragments, addNewPlaceFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
    }

    @Override
    public Loader<ArrayList<GeoPoint>> onCreateLoader(int id, Bundle args) {
        return new PointsFromBdLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<GeoPoint>> loader, ArrayList<GeoPoint> data) {
        geoPoints = data;
        initMapIfExist();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<GeoPoint>> loader) {

    }

    private void setupServiceReceiver() {
        serviceResultReceiver = new ServiceResultReceiver(new Handler());
        serviceResultReceiver.setReceiver(new ServiceResultReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    getLoaderManager().initLoader(0, null, MainActivity.this);
                }
                if (resultCode == RESULT_CANCELED) {
                    Snackbar.make(coordinatorLayout,
                            R.string.Snackbar_alert_no_internet, Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.Snackbar_alert_try_again, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startServiceOrLoadData();
                                }
                            }).show();
                }
            }
        });
    }

    private void startServiceOrLoadData() {
        if (DataBaseSQLiteOpenHelper.getInstance(getApplicationContext()).isEmpty()) {
            Intent i = new Intent(this, LoadFromURLService.class);
            setupServiceReceiver();
            i.putExtra("receiver", serviceResultReceiver);
            startService(i);
        } else {
            getLoaderManager().initLoader(0, null, MainActivity.this);
        }
    }

    private void initMapIfExist() {
        if (geoPoints != null && geoPoints.size() != 0 && map != null) {
            for (int i = 0; i < geoPoints.size(); i++) {
                GeoPoint geoPoint = geoPoints.get(i);
                map.addMarker(new MarkerOptions()
                        .position(geoPoint.getLatLng())
                        .snippet(String.valueOf(i))
                        .title(geoPoint.getText()));
            }
        }
    }

    @Override
    public void addNewPlace(GeoPoint geoPoint) {
        AddPointToBdAndMap addPointToBdAndMap = new AddPointToBdAndMap();
        addPointToBdAndMap.execute(geoPoint);
        getFragmentManager().popBackStack();
    }

    class AddPointToBdAndMap extends AsyncTask<GeoPoint, Void, Integer> {

        GeoPoint geoPoint;

        @Override
        protected Integer doInBackground(GeoPoint... params) {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
            geoPoint = params[0];
            DataBaseSQLiteOpenHelper.getInstance(getApplicationContext()).insert(
                    geoPoint.getLatLng().latitude,
                    geoPoint.getLatLng().longitude,
                    geoPoint.getText(),
                    geoPoint.getImage().toString(),
                    sdf.format(geoPoint.getLastVisited().getTime())

            );
            geoPoints.add(geoPoint);
            return geoPoints.size() - 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (map != null) {
                map.addMarker(new MarkerOptions()
                        .position(geoPoint.getLatLng())
                        .snippet(String.valueOf(result))
                        .title(geoPoint.getText()));
            }
        }
    }

    /*TODO
    * add google image for empty url
    * add image from gallery
    * press + before loaded
    * load from url only if empty and load other every time
    * */


}
