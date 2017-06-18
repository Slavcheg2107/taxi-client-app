package jdroidcoder.ua.taxi_bishkek_client.service;

import android.app.IntentService;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import jdroidcoder.ua.taxi_bishkek_client.activity.MapsActivity;
import jdroidcoder.ua.taxi_bishkek_client.events.OrderAccepted;
import jdroidcoder.ua.taxi_bishkek_client.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_client.model.UserProfileDto;
import jdroidcoder.ua.taxi_bishkek_client.network.NetworkService;

/**
 * Created by jdroidcoder on 02.05.17.
 */

public class UpdateOrdersService extends IntentService {
    public static boolean isRun = true;

    public UpdateOrdersService() {
        super("OrdersService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRun) {
                    new NetworkService().getOrders(UserProfileDto.User.getPhone());
                    EventBus.getDefault().post(new UpdateAdapterEvent());
                    if(MapsActivity.orderDto!=null){
                    if(MapsActivity.orderDto.getStatus().equals("accepted")){
                        EventBus.getDefault().post(new OrderAccepted());
                    }}
                    try {
                        TimeUnit.SECONDS.sleep(8);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}