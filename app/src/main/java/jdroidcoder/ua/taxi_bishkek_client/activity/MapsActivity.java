package jdroidcoder.ua.taxi_bishkek_client.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import jdroidcoder.ua.taxi_bishkek_client.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.OrderEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_client.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_client.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_client.network.NetworkService;
import jdroidcoder.ua.taxi_bishkek_client.service.LocationService;

import static jdroidcoder.ua.taxi_bishkek_client.R.id.map;

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
    private NetworkService networkService;
    private OrderAdapter orderAdapter;
    private Location location;

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
                .findFragmentById(map);
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
        double[] pointACoordinate = getAddressLocation(getAddressLocation(location.getLatitude(), location.getLongitude())
                + " " + fromET.getText().toString());
        double[] pointBCoordinate = getAddressLocation(toET.getText().toString());
        if (pointACoordinate != null && pointBCoordinate != null) {
            if (!TextUtils.isEmpty(fromET.getText().toString())
                    && !TextUtils.isEmpty(toET.getText().toString())) {
                if (OrderDto.Oreders.getOrders().size() == 0) {
                    networkService.makeOrder(fromET.getText().toString(),
                            toET.getText().toString(), new Date(),
                            pointACoordinate, pointBCoordinate);
                    orderView.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, getString(R.string.you_are_have_order), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.empty_order), Toast.LENGTH_LONG).show();
            }
        }
    }

    private double[] getAddressLocation(String address) {
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 5);
            if (addresses.size() > 0) {
                Double lat = addresses.get(0).getLatitude();
                Double lon = addresses.get(0).getLongitude();
                return new double[]{lat, lon};
            } else {
                EventBus.getDefault().post(new ErrorMessageEvent("Address not found"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new ErrorMessageEvent("Address not found"));
        }
        return null;
    }

    private String getAddressLocation(double lat, double lng) {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        String countryName = "";
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses.size() > 0) {
            countryName = addresses.get(0).getCountryName() + " " + addresses.get(0).getLocality();
        }

        return countryName;
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
            location = ((LocationManager) getSystemService(LOCATION_SERVICE)).
                    getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            LatLng sydney = new LatLng(location.getLatitude(),
                    location.getLongitude());
            markerOptions = new MarkerOptions().position(sydney);
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(sydney, 17f, 0f, 0f)));
            mMap.setInfoWindowAdapter(new MarkerAdapter(this));
            networkService.setCoordinate(location.getLatitude(), location.getLongitude());
            mMap.setOnMarkerClickListener(this);
//            fromET.setText(getAddressLocation(location.getLatitude(), location.getLongitude()));
        } catch (Exception e) {

        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final View view = LayoutInflater.from(this).inflate(R.layout.alert_style, null);
        if (item.getItemId() == R.id.changeNumber) {
            final AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .create();

            final EditText phoneET = (EditText) view.findViewById(R.id.phone);
            phoneET.setText(UserProfileDto.User.getPhone());
            view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(phoneET.getText().toString())) {
                        UserProfileDto.User.setPhone(phoneET.getText().toString());
                        networkService.setDataToProfile(UserProfileDto.User.getEmail(),
                                UserProfileDto.User.getFirstName(),
                                UserProfileDto.User.getLastName(),
                                UserProfileDto.User.getPhone());
                        alertDialog.dismiss();
                    }
                }
            });

            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.toET)
    public void streets() {

    }
}