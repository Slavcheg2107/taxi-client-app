package jdroidcoder.ua.taxi_bishkek_client.network;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.List;

import jdroidcoder.ua.taxi_bishkek_client.events.ConnectionErrorEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.ErrorMessageEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.MoveNextEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.OrderEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.TypePhoneEvent;
import jdroidcoder.ua.taxi_bishkek_client.events.UpdateAdapterEvent;
import jdroidcoder.ua.taxi_bishkek_client.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek_client.model.UserProfileDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class NetworkService {
    private RetrofitConfig retrofitConfig;

    public NetworkService() {
        retrofitConfig = new RetrofitConfig();
    }

    public void register(final String login, final String password) {
        Call<Boolean> call = retrofitConfig.getApiNetwork().register(login, password);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.body()) {
                    EventBus.getDefault().post(new TypePhoneEvent());
                } else {
                    login(login, password);
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void setDataToProfile(String email, String firstName, String lastName, String phone) {
        Call<UserProfileDto> call = retrofitConfig.getApiNetwork().setDataToProfile(email, firstName, lastName, phone);
        call.enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                UserProfileDto.User.setPhone(response.body().getPhone());
                UserProfileDto.User.setFirstName(response.body().getFirstName());
                UserProfileDto.User.setLastName(response.body().getLastName());
                UserProfileDto.User.setEmail(response.body().getEmail());
                EventBus.getDefault().post(new MoveNextEvent());
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void login(final String login, final String password) {
        Call<UserProfileDto> call = retrofitConfig.getApiNetwork().login(login, password);
        call.enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                try {
                    UserProfileDto.User.setPhone(response.body().getPhone());
                    UserProfileDto.User.setFirstName(response.body().getFirstName());
                    UserProfileDto.User.setLastName(response.body().getLastName());
                    UserProfileDto.User.setEmail(response.body().getEmail());
                    EventBus.getDefault().post(new MoveNextEvent());
                } catch (Exception e) {
                    EventBus.getDefault().post(new ErrorMessageEvent("Your phone used"));
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void makeOrder(String pointA, String pointB, Date date,
                          double[] pointACoordinate, double[] pointBCoordinate) {
        Call<OrderDto> call = retrofitConfig.getApiNetwork().makeOrder(pointA, pointB, date.getTime(),
                UserProfileDto.User.getPhone(), "new", pointACoordinate, pointBCoordinate);
        call.enqueue(new Callback<OrderDto>() {
            @Override
            public void onResponse(Call<OrderDto> call, Response<OrderDto> response) {
                try {
                    OrderDto.Oreders.add(response.body());
                    EventBus.getDefault().post(new OrderEvent());
                    EventBus.getDefault().post(new ErrorMessageEvent("Order is created"));
                }catch (Exception e){
                    EventBus.getDefault().post(new ErrorMessageEvent("You are have order"));
                }
            }

            @Override
            public void onFailure(Call<OrderDto> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void getOrders(String userPhone) {
        Call<List<OrderDto>> call = retrofitConfig.getApiNetwork().getOrders(userPhone);
        call.enqueue(new Callback<List<OrderDto>>() {
            @Override
            public void onResponse(Call<List<OrderDto>> call, Response<List<OrderDto>> response) {
                OrderDto.Oreders.setItems(response.body());
            }

            @Override
            public void onFailure(Call<List<OrderDto>> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void removeOrder(final OrderDto orderDto) {
        Call<Boolean> call = retrofitConfig.getApiNetwork().removeOrder(orderDto.getId());
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                OrderDto.Oreders.getOrders().remove(orderDto);
                EventBus.getDefault().post(new UpdateAdapterEvent());
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                EventBus.getDefault().post(new ErrorMessageEvent(t.getMessage()));
            }
        });
    }

    public void setCoordinate(Double lat, Double lng) {
        Call<Void> call = retrofitConfig.getApiNetwork().setCoordinate(UserProfileDto.User.getPhone(), lat, lng);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                EventBus.getDefault().post(new ConnectionErrorEvent());
            }
        });
    }
}
