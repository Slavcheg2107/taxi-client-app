package jdroidcoder.ua.taxi_bishkek.network;

import java.util.List;

import jdroidcoder.ua.taxi_bishkek.model.OrderDto;
import jdroidcoder.ua.taxi_bishkek.model.UserProfileDto;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public interface ApiNetwork {
    @POST("register")
    @FormUrlEncoded
    Call<Boolean> register(@Field("email") String email, @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Call<UserProfileDto> login(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("setDataToProfile")
    Call<UserProfileDto> setDataToProfile(@Field("email") String email,
                                          @Field("firstName") String firstName,
                                          @Field("lastName") String lastName,
                                          @Field("phone") String phone);

    @FormUrlEncoded
    @POST("setCoordinate")
    Call<Void> setCoordinate(@Field("userPhone") String userPhone,
                             @Field("lat") Double lat,
                             @Field("lng") Double lng);

    @FormUrlEncoded
    @POST("makeOrder")
    Call<OrderDto> makeOrder(@Field("pointA") String pointA,
                             @Field("pointB") String pointB,
                             @Field("time") Long time,
                             @Field("userPhone") String userPhone,
                             @Field("status") String status,
                             @Field("pointACoordinate") double[] pointACoordinate,
                             @Field("pointBCoordinate") double[] pointBCoordinate);

    @GET("getOrders")
    Call<List<OrderDto>> getOrders(@Query("userPhone") String userPhone);

    @GET("deleteOrder")
    Call<Boolean> removeOrder(@Query("id") Long id);
}
