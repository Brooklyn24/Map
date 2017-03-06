package com.example.brooklyn.myapplication;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class MyMapFragment extends Fragment implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<HashMap<Integer, GeoPoint>> {

    private GoogleMap map;
    private boolean isMapEditable;
    private Snackbar addNewPlaceSnackbar;
    private Context context;
    private ServiceResultReceiver serviceResultReceiver;
    private HashMap<Integer, GeoPoint> geoPoints;

    @BindView(R.id.map) MapView mapView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.coordinator_Layout) CoordinatorLayout coordinatorLayout;

    public MyMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startServiceOrLoadData();
        isMapEditable = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_map, container, false);
        ButterKnife.bind(this, view);
        mapView.onCreate(savedInstanceState);
        return view;
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.getMapAsync(this);
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

                View v = getActivity().getLayoutInflater().inflate(R.layout.costom_map_window, null);
                TextView text = (TextView) v.findViewById(R.id.textView);
                TextView lastVisited = (TextView) v.findViewById(R.id.textView2);
                ImageView image = (ImageView) v.findViewById(R.id.imageView);
                String formatedDate = new SimpleDateFormat("EEE, d MMM yyyy K:mm a", Locale.ENGLISH)
                        .format(markerPoint.getLastVisited().getTime());
                text.setText(marker.getTitle());
                lastVisited.setText("Last visited" + formatedDate);
                Uri imageUrl = markerPoint.getImage();

                Picasso.with(context).load(imageUrl).resize(0, 300)
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

    @OnClick(R.id.fab)
    public void fabClick() {
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

    private void initMapIfExist() {
        if (geoPoints != null && geoPoints.size() != 0 && map != null) {
            for (Map.Entry<Integer, GeoPoint> geoPointEntry : geoPoints.entrySet()) {
                Integer key = geoPointEntry.getKey();
                GeoPoint geoPoint = geoPointEntry.getValue();
                map.addMarker(new MarkerOptions()
                        .position(geoPoint.getLatLng())
                        .snippet(String.valueOf(key))
                        .title(geoPoint.getText()));
            }
        }
    }

    @Override
    public Loader<HashMap<Integer, GeoPoint>> onCreateLoader(int id, Bundle args) {
        return new PointsFromBdLoader(context);
    }

    @Override
    public void onLoadFinished(Loader<HashMap<Integer, GeoPoint>> loader, HashMap<Integer, GeoPoint> data) {
        geoPoints = data;
        initMapIfExist();
    }

    @Override
    public void onLoaderReset(Loader<HashMap<Integer, GeoPoint>> loader) {

    }

    private void setupServiceReceiver() {
        serviceResultReceiver = new ServiceResultReceiver(new Handler());
        serviceResultReceiver.setReceiver(new ServiceResultReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    getLoaderManager().initLoader(0, null, MyMapFragment.this);
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

    public void addNewPlace(GeoPoint geoPoint) {
        AddPointToBdAndMap addPointToBdAndMap = new AddPointToBdAndMap();
        addPointToBdAndMap.execute(geoPoint);
        getFragmentManager().popBackStack();
    }

    private void startServiceOrLoadData() {
        if (DataBaseSQLiteOpenHelper.getInstance(context).isEmpty()) {
            Intent i = new Intent(context, LoadFromURLService.class);
            setupServiceReceiver();
            i.putExtra("receiver", serviceResultReceiver);
            getActivity().startService(i);
        } else {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    class AddPointToBdAndMap extends AsyncTask<GeoPoint, Void, Integer> {

        GeoPoint geoPoint;

        @Override
        protected Integer doInBackground(GeoPoint... params) {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
            geoPoint = params[0];
            int id = (int) DataBaseSQLiteOpenHelper.getInstance(context).insert(
                    geoPoint.getLatLng().latitude,
                    geoPoint.getLatLng().longitude,
                    geoPoint.getText(),
                    geoPoint.getImage().toString(),
                    sdf.format(geoPoint.getLastVisited().getTime())
            );
            geoPoints.put(id, geoPoint);
            return id;
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
}