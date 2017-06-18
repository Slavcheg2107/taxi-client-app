package jdroidcoder.ua.taxi_bishkek_client.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import org.greenrobot.eventbus.EventBus;

import jdroidcoder.ua.taxi_bishkek_client.events.ChangeLocationEvent;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class LocationService extends Service implements LocationListener {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            super.onStartCommand(intent, flags, startId);
        }
        ((LocationManager) getSystemService(LOCATION_SERVICE))
                .requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLocationChanged(Location location) {
        EventBus.getDefault().post(new ChangeLocationEvent(location));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
