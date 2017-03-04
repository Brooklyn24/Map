package com.example.brooklyn.myapplication;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Brooklyn on 26-Jan-17.
 */

public class GeoPoint {

    private final LatLng latLng;
    private final String text;
    private final Uri image;
    private final Calendar lastVisited;

    public GeoPoint(Double latitude, Double longitude, String text, Uri image, Calendar lastVisited) {
        this.latLng = new LatLng(latitude,longitude);
        this.text = text;
        this.image = image;
        this.lastVisited = lastVisited;
    }

    public GeoPoint(LatLng latLng, String text, Uri image, Calendar lastVisited) {
        this.latLng = latLng;
        this.text = text;
        this.image = image;
        this.lastVisited = lastVisited;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getText() {
        return text;
    }

    public Uri getImage() {
        return image;
    }

    public Calendar getLastVisited() {
        return lastVisited;
    }


}
