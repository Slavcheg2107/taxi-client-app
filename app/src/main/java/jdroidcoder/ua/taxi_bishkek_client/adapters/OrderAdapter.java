package jdroidcoder.ua.taxi_bishkek_client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import jdroidcoder.ua.taxi_bishkek_client.R;
import jdroidcoder.ua.taxi_bishkek_client.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_client.network.NetworkService;

/**
 * Created by jdroidcoder on 10.04.17.
 */
public class OrderAdapter extends BaseAdapter {
    private Context context;

    public OrderAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return OrderDto.Oreders.getOrders().size();
    }

    @Override
    public Object getItem(int position) {
        return OrderDto.Oreders.getOrders().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final OrderDto orderDto = OrderDto.Oreders.getOrders().get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.order_list_style, parent, false);
        try {
            ((TextView) convertView.findViewById(R.id.addressTV)).setText(orderDto.getPoints());
            ((TextView) convertView.findViewById(R.id.whenTV)).setText(orderDto.getTime());
            convertView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new NetworkService().removeOrder(orderDto);
                }
            });
        } catch (Exception e) {
            convertView.setVisibility(View.GONE);
        }
        return convertView;
    }
}
