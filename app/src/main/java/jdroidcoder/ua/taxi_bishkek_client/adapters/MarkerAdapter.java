package jdroidcoder.ua.taxi_bishkek_client.adapters;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.greenrobot.eventbus.EventBus;

import jdroidcoder.ua.taxi_bishkek_client.R;
import jdroidcoder.ua.taxi_bishkek_client.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_client.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_client.network.NetworkService;

/**
 * Created by jdroidcoder on 10.04.17.
 */
public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;
    java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("HH:mm");
    public MarkerAdapter(Context context) {
        this.context = context;
    }
    View convertView;
    OrderDto orderDto;
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        try {
            orderDto = OrderDto.Oreders.getOrders().get(OrderDto.Oreders.getOrders().size() - 1);
            convertView = LayoutInflater.from(context).inflate(R.layout.order_list_style, null);
                ((TextView) convertView.findViewById(R.id.addressTV)).setText(orderDto.getPoints());
                ((TextView) convertView.findViewById(R.id.whenTV)).setText(simpleDateFormat.format(orderDto.getTime()));
            }
            catch (Exception e) {
                Log.e("TAG", e.getMessage());
            }
            return convertView;
        }

    }

