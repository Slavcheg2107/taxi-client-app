package jdroidcoder.ua.taxi_bishkek_client.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.InputType;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jdroidcoder.ua.taxi_bishkek_client.R;
import jdroidcoder.ua.taxi_bishkek_client.events.ConnectionErrorEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.MoveNextEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.SimChangedEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.TypePhoneEvent;
import jdroidcoder.ua.taxi_bishkek_client.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_client.network.NetworkService;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final static int RC_SIGN_IN = 101;
    private GoogleApiClient googleApiClient;
    private NetworkService networkService;
    private UserProfileDto userProfileDto = new UserProfileDto();
    private String email;
    private boolean isSend = false;
    public static SharedPreferences settings;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_SIMID = "";
    public static int count = 1;
    SharedPreferences.Editor editor;
    String say;
    public static TelephonyManager m;
    GoogleSignInAccount acct;
    public static String prevGEmail;
    int count1 = 0;
    Snackbar snackbar;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        networkService = new NetworkService();
        settings = this.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        editor = settings.edit();
        view = findViewById(R.id.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            m = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if(m.getSimSerialNumber()==null||m.getSimSerialNumber().equals("null")){
                Toast.makeText(getApplicationContext(), R.string.sim_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }else {
                enter();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.READ_PHONE_STATE},
                    123);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            m = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if(m.getSimSerialNumber()==null||m.getSimSerialNumber().equals("null")){
                Toast.makeText(getApplicationContext(), R.string.check_sim, Toast.LENGTH_LONG).show();
                finish();
            }else {
                snackbar = Snackbar.make( view, R.string.sb_add_acc, Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
                CountDownTimer timer = new CountDownTimer(5000,4000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        snackbar.dismiss();
                        enter();
                    }
                };
                timer.start();
            }
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @OnClick(R.id.sign_in_button)
    public void enter() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            if (!isSend) {
                acct = result.getSignInAccount();
                userProfileDto.setFirstName(acct.getGivenName());
                userProfileDto.setLastName(acct.getFamilyName());
                email = acct.getEmail();
                networkService.register(acct.getEmail(), acct.getId());
            } else {
                isSend = false;
            }
        } else {
            Toast.makeText(this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void onMessageEvent(ErrorMessageEvent event) {
        Toast.makeText(this, event.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onMoveToNext(MoveNextEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                networkService.getOrders(UserProfileDto.User.getPhone());
            }
        }).start();
        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }

    @Subscribe
    public void onTypeEvent(TypePhoneEvent event) {
        showPhoneNumberInput();
    }

    public void showPhoneNumberInput() {
        AlertDialog.Builder phoneInput = new AlertDialog.Builder(this);
        final EditText phoneNumber = new EditText(this);
        final TextView code = new TextView(this);

        LinearLayout rel = new LinearLayout(this);
        rel.setOrientation(LinearLayout.HORIZONTAL);
        rel.addView(code);
        rel.addView(phoneNumber);

        code.setText("+996");
        code.setTextSize(18);
        code.setTextColor(Color.rgb(0, 0, 0));

        phoneNumber.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneNumber.setHint("Например: 701222333");
        phoneNumber.setMaxLines(1);
        int maxLength = 9;
        phoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        phoneInput.setPositiveButton("Подтвердить", null);
        phoneInput.setView(rel);

        phoneInput.setTitle("Введите номер телефона");

        final AlertDialog mAlertDialog = phoneInput.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (phoneNumber.getText().toString().length() == 9 && phoneNumber.getText().charAt(0) != '0') {

                            if (count1 == 0) {
                                mAlertDialog.setTitle("Сохранить данный номер?");
                                mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Сохранить");
                                count1 = 1;
                            } else if (count1 == 1) {
                                say = code.getText().toString() + phoneNumber.getText().toString();
                                userProfileDto.setPhone(say);
                                networkService.setDataToProfile(email, userProfileDto.getFirstName(),
                                        userProfileDto.getLastName(), userProfileDto.getPhone());
                                mAlertDialog.dismiss();
                                count1 = 0;
                            }
                        } else if (phoneNumber.getText().toString().length() != 9) {
                            phoneNumber.setError("Допишите цифры");
                        } else if (phoneNumber.getText().charAt(0) == '0') {
                            phoneNumber.setError("Уберите ноль в начале");
                        }
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    @Subscribe
    public void OnSimChangedEvent(SimChangedEvent simChangedEvent) {

        if (settings.getString(APP_PREFERENCES_SIMID, "").isEmpty() || settings.getString(APP_PREFERENCES_SIMID, "").equals("")) {
            editor.putString(APP_PREFERENCES_SIMID, m.getSimSerialNumber());
            editor.apply();
            enter();
        }
        else
        if (!settings.getString(APP_PREFERENCES_SIMID, "").equals(m.getSimSerialNumber())) {
            if(acct.getEmail().equals(prevGEmail)){
                count++;
                Toast.makeText(getApplicationContext(), "Если вы сменили номер телефона нажмите " +  "\"Другой аккаунт\"", Toast.LENGTH_LONG).show();
                googleApiClient.clearDefaultAccountAndReconnect();
                enter();
            }
            else{
                count = 0;
                editor.putString(APP_PREFERENCES_SIMID, m.getSimSerialNumber());
                editor.apply();
                enter();
            }
        }
    }

    @Subscribe
    public void onConnectionErrorEvent(ConnectionErrorEvent connectionErrorEvent) {
        Snackbar.make(findViewById(R.id.sign_in_button),
                getResources().getString(R.string.connection_error), Snackbar.LENGTH_INDEFINITE).show();
    }
}
