package jdroidcoder.ua.taxi_bishkek_client.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import jdroidcoder.ua.taxi_bishkek_client.R;
import jdroidcoder.ua.taxi_bishkek_client.adapters.MarkerAdapter;
import jdroidcoder.ua.taxi_bishkek_client.adapters.OrderAdapter;
import jdroidcoder.ua.taxi_bishkek_client.events.ChangeLocationEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.ConnectionErrorEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.OrderEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_client.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_client.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_client.network.NetworkService;
import jdroidcoder.ua.taxi_bishkek_client.service.LocationService;

import static jdroidcoder.ua.taxi_bishkek_client.R.id.button;
import static jdroidcoder.ua.taxi_bishkek_client.R.id.map;
import static jdroidcoder.ua.taxi_bishkek_client.R.id.orderView;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

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
    @BindView(R.id.orderListView)
    ListView listView;
    @BindView(R.id.buttons)
    LinearLayout buttons;
    @BindView(R.id.connection_error)
    TextView connectionError;
    private NetworkService networkService;
    private OrderAdapter orderAdapter;
    private Location location;
    private boolean isConnectionError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        ButterKnife.bind(this);
        networkService = new NetworkService();
        EventBus.getDefault().register(this);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, LocationService.class));
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        orderAdapter = new OrderAdapter(this);
        listView.setAdapter(orderAdapter);
    }

    @OnClick(R.id.makeOrder)
    public void makeOrder() {
        if (!isConnectionError) {
            if (orders.getVisibility() == View.VISIBLE) {
                orders.setVisibility(View.GONE);
            }
            if (orderView.getVisibility() == View.VISIBLE) {
                orderView.setVisibility(View.GONE);
            } else {
                orderView.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick(R.id.sendOrder)
    public void sendOrder() {
        try {
            if (!TextUtils.isEmpty(fromET.getText().toString())
                    && !TextUtils.isEmpty(toET.getText().toString())) {
                if (OrderDto.Oreders.getOrders().size() == 0) {
                    networkService.makeOrder(fromET.getText().toString(),
                            toET.getText().toString(), new Date(),
                            (location == null) ? null :
                                    new double[]{location.getLatitude(), location.getLongitude()}, null);
                    orderView.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, getString(R.string.you_are_have_order), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.empty_order), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.unknow_error), Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.doneOrder)
    public void doneOrder() {
        if (!isConnectionError) {
            if (orderView.getVisibility() == View.VISIBLE) {
                orderView.setVisibility(View.GONE);
            }
            if (orders.getVisibility() == View.GONE) {
                orders.setVisibility(View.VISIBLE);
            } else {
                orders.setVisibility(View.GONE);
            }
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
        networkService.getOrders(UserProfileDto.User.getPhone());
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
        mMap.setMyLocationEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            location = ((LocationManager) getSystemService(LOCATION_SERVICE)).
                    getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
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

    @Subscribe
    public void onConnectionErrorEvent(ConnectionErrorEvent connectionErrorEvent) {
        isConnectionError = connectionErrorEvent.isShow();
        if (connectionErrorEvent.isShow()) {
            connectionError.setVisibility(View.VISIBLE);
        } else {
            connectionError.setVisibility(View.GONE);
        }
    }
}