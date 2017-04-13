package jdroidcoder.ua.taxi_bishkek.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by jdroidcoder on 07.04.17.
 */
public class RetrofitConfig {
    private final static String BASE_URL = "http://192.168.1.101:4547/";
    private Retrofit retrofit = new Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build();

    private ApiNetwork apiNetwork = retrofit.create(ApiNetwork.class);

    public ApiNetwork getApiNetwork() {
        return apiNetwork;
    }
}
