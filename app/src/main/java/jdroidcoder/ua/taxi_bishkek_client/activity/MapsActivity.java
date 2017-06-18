package jdroidcoder.ua.taxi_bishkek_client.activity;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.vision.text.Text;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
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
import jdroidcoder.ua.taxi_bishkek_client.events.OrderAccepted;
import jdroidcoder.ua.taxi_bishkek_client.events.OrderEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_client.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_client.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_client.network.NetworkService;
import jdroidcoder.ua.taxi_bishkek_client.service.LocationService;
import jdroidcoder.ua.taxi_bishkek_client.service.NotificationService;
import jdroidcoder.ua.taxi_bishkek_client.service.UpdateOrdersService;

import static jdroidcoder.ua.taxi_bishkek_client.R.id.button;
import static jdroidcoder.ua.taxi_bishkek_client.R.id.map;
import static jdroidcoder.ua.taxi_bishkek_client.R.id.orderStatus;
import static jdroidcoder.ua.taxi_bishkek_client.R.id.orderView;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private MarkerOptions markerOptions;
    @BindView(R.id.orderView)
    LinearLayout orderView;
    @BindView(R.id.orderStatus)
    TextView orderStatus;
    @BindView(R.id.fromET)
    EditText fromET;
    @BindView(R.id.toET)
    EditText toET;

    @BindView(R.id.buttons)
    LinearLayout buttons;
    @BindView(R.id.connection_error)
    TextView connectionError;
    private NetworkService networkService;
    private Location location;
    private boolean isConnectionError = false;
    List<OrderDto> orderDtos;
    public static OrderDto orderDto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, LocationService.class));
        }
        networkService = new NetworkService();
        startService(new Intent(this, UpdateOrdersService.class));
        startService(new Intent(this, NotificationService.class));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onResume(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startService(new Intent(this, LocationService.class));
        }
        startService(new Intent(this, NotificationService.class));
        startService(new Intent(this, UpdateOrdersService.class));
        super.onResume();
    }

    @OnClick(R.id.cancel_order)
    public void makeOrder() {
        if (!isConnectionError) {
            if (orderDto != null) {
                if (!orderDto.getStatus().equals("accepted")) {
                    networkService.removeOrder(orderDto);
                    Toast.makeText(getApplicationContext(), "Ваш заказ отменен", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Заказ нельзя отменить пока он принят водителем", Toast.LENGTH_SHORT).show();
                }
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
                    orderStatus.setText("Ищем водителя");
//                    orderView.setVisibility(View.GONE);
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
            if(orderDto!=null){
                networkService.removeOrder(orderDto);
                Toast.makeText(getApplicationContext(), "Ваш заказ завершен", Toast.LENGTH_SHORT).show();
                orderStatus.setText("Создайте заказ");
            }
            else{
                Toast.makeText(getApplicationContext(), "Вы не делали заказ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        UpdateOrdersService.isRun = false;
        stopService(new Intent(this, NotificationService.class));
        stopService(new Intent(this, UpdateOrdersService.class));
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

        if(!OrderDto.Oreders.getOrders().isEmpty()) {
            orderDtos = OrderDto.Oreders.getOrders();
            orderDto = orderDtos.get(0);
            if(orderDto.getStatus().equals("accepted")){
                orderStatus.setText("Ваш заказ принят водителем");
            }
            else if(orderDto.getStatus().equals("new")){
                orderStatus.setText("Ишем водителя");
            }
        }
        else{
            orderStatus.setText("Создайте заказ");
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.contact) {

            final AlertDialog.Builder phoneInput = new AlertDialog.Builder(this);

            phoneInput.setPositiveButton("Whatsapp для красоток", null);
            phoneInput.setNegativeButton("Я не красотка", null);
            phoneInput.setTitle("Макс Плэйбой");

            final AlertDialog mAlertDialog = phoneInput.create();

            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {


                    Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String smsNumber = "0555488488";
                            try {
                                Intent contactIntent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,ContactsContract.Contacts.CONTENT_URI);
                                contactIntent.setData(Uri.parse("tel:0555488488"));//Add the mobile number here
                                contactIntent.putExtra(ContactsContract.Intents.Insert.NAME, "Макс Плэйбой"); //ADD contact name here
                                startActivity(contactIntent);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                            mAlertDialog.dismiss();
                        }
                    });

                    Button n = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    n.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAlertDialog.dismiss();
                        }
                    });
                }
            });
            mAlertDialog.show();
//            startActivity(new Intent(this, RuleActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}