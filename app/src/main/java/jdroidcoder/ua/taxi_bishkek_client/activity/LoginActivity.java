package jdroidcoder.ua.taxi_bishkek_client.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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
import jdroidcoder.ua.taxi_bishkek_client.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.MoveNextEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.TypePhoneEvent;
import jdroidcoder.ua.taxi_bishkek_client.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_client.network.NetworkService;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final static int RC_SIGN_IN = 101;
    private GoogleApiClient googleApiClient;
    private NetworkService networkService;
    private UserProfileDto userProfileDto = new UserProfileDto();
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        networkService = new NetworkService();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
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
            GoogleSignInAccount acct = result.getSignInAccount();
            userProfileDto.setFirstName(acct.getGivenName());
            userProfileDto.setLastName(acct.getFamilyName());
            email = acct.getEmail();
            networkService.register(acct.getEmail(), acct.getId());
        } else {
            Toast.makeText(this, getString(R.string.unknow_error), Toast.LENGTH_LONG).show();
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
        final View view = LayoutInflater.from(this).inflate(R.layout.alert_style, null);
//        new AlertDialog.Builder(this)
//                .setView(view)
//                .setCancelable(false)
//                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        EditText phoneET = (EditText) view.findViewById(R.id.phone);
//                        if (!TextUtils.isEmpty(phoneET.getText().toString())) {
//                            userProfileDto.setPhone(phoneET.getText().toString());
//                            networkService.setDataToProfile(email, userProfileDto.getFirstName(),
//                                    userProfileDto.getLastName(), userProfileDto.getPhone());
//                            dialog.dismiss();
//                        }
//                    }
//                }).show();
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        final EditText phoneET = (EditText) view.findViewById(R.id.phone);
        phoneET.setText(UserProfileDto.User.getPhone());
        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText phoneET = (EditText) view.findViewById(R.id.phone);
                if (!TextUtils.isEmpty(phoneET.getText().toString())) {
                    userProfileDto.setPhone(phoneET.getText().toString());
                    networkService.setDataToProfile(email, userProfileDto.getFirstName(),
                            userProfileDto.getLastName(), userProfileDto.getPhone());
                    alertDialog.dismiss();
                }
            }
        });

        alertDialog.show();
    }
}
