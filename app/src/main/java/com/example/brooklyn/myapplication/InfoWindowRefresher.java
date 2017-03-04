package com.example.brooklyn.myapplication;

import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;

public class InfoWindowRefresher implements Callback {

    private Marker marker;

    public InfoWindowRefresher(Marker marker) {
        this.marker = marker;
    }

    @Override
    public void onSuccess() {
        if (marker != null && marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
            marker.showInfoWindow();
        }
    }

    @Override
    public void onError() {

    }
}
