package jdroidcoder.ua.taxi_bishkek.activity;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jdroidcoder.ua.taxi_bishkek.R;
import jdroidcoder.ua.taxi_bishkek.adapters.MarkerAdapter;
import jdroidcoder.ua.taxi_bishkek.adapters.OrderAdapter;
import jdroidcoder.ua.taxi_bishkek.events.ChangeLocationEvent;
import jdroidcoder.ua.taxi_bishkek.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek.events.OrderEvent;
import jdroidcoder.ua.taxi_bishkek.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek.network.NetworkService;
import jdroidcoder.ua.taxi_bishkek.service.LocationService;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private MarkerOptions markerOptions;
    @BindView(R.id.orderView)
    LinearLayout orderView;
    @BindView(R.id.orders)
    LinearLayout orders;
    @BindView(R.id.fromET)
    EditText fromET;
    @BindView(R.id.toET)
    EditText toET;
    @BindView(R.id.timeTV)
    EditText timeTV;
    @BindView(R.id.orderListView)
    ListView listView;
    private NetworkService networkService;
    private Calendar mcurrentTime = Calendar.getInstance();
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        ButterKnife.bind(this);
        networkService = new NetworkService();
        EventBus.getDefault().register(this);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        }

        startService(new Intent(this, LocationService.class));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        orderAdapter = new OrderAdapter(this);
        listView.setAdapter(orderAdapter);
    }

    @OnClick(R.id.makeOrder)
    public void makeOrder() {
        if (orders.getVisibility() == View.VISIBLE) {
            orders.setVisibility(View.GONE);
        }
        if (orderView.getVisibility() == View.VISIBLE) {
            orderView.setVisibility(View.GONE);
        } else {
            orderView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.sendOrder)
    public void sendOrder() {
        if (!TextUtils.isEmpty(fromET.getText().toString())
                && !TextUtils.isEmpty(toET.getText().toString())
                && !TextUtils.isEmpty(timeTV.getText().toString())) {
            networkService.makeOrder(fromET.getText().toString(), toET.getText().toString(), mcurrentTime.getTime());
            orderView.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, getString(R.string.empty_order), Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.timeTV)
    public void selectTime() {
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mcurrentTime.set(mcurrentTime.get(Calendar.YEAR), mcurrentTime.get(Calendar.MONTH)
                        , mcurrentTime.get(Calendar.DATE), hourOfDay, minute);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

                timeTV.setText(simpleDateFormat.format(mcurrentTime.getTime()));
            }
        }, hour, minute, true).show();
    }

    @OnClick(R.id.doneOrder)
    public void doneOrder() {
        if (orderView.getVisibility() == View.VISIBLE) {
            orderView.setVisibility(View.GONE);
        }
        if (orders.getVisibility() == View.GONE) {
            orders.setVisibility(View.VISIBLE);
        } else {
            orders.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, LocationService.class));
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onChangeLocationEvent(ChangeLocationEvent changeLocationEvent) {
        networkService.setCoordinate(changeLocationEvent.getLocation().getLatitude(),
                changeLocationEvent.getLocation().getLongitude());
        LatLng sydney = new LatLng(changeLocationEvent.getLocation().getLatitude(),
                changeLocationEvent.getLocation().getLongitude());
        markerOptions.position(sydney);
    }

    @Subscribe
    public void onUpdateAdapterEvent(UpdateAdapterEvent updateAdapterEvent) {
        orderAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (orderView.getVisibility() == View.VISIBLE) {
            orderView.setVisibility(View.GONE);
            return;
        }
        if (orders.getVisibility() == View.VISIBLE) {
            orders.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }

    @Subscribe
    public void onMessageEvent(ErrorMessageEvent event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onNewOrderEvent(OrderEvent event) {
        mMap.setInfoWindowAdapter(new MarkerAdapter(this));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            Location location = ((LocationManager) getSystemService(LOCATION_SERVICE)).
                    getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            LatLng sydney = new LatLng(location.getLatitude(),
                    location.getLongitude());
            markerOptions = new MarkerOptions().position(sydney);
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(sydney, 17f, 0f, 0f)));
            mMap.setInfoWindowAdapter(new MarkerAdapter(this));
            networkService.setCoordinate(location.getLatitude(), location.getLongitude());
            mMap.setOnMarkerClickListener(this);
        } catch (Exception e) {

        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }
}