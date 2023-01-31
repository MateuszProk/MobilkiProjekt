package com.martynaroj.pokedex.retrofit;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Rest {
    private static RestInterface serviceRest;
    private static final String BASE_URL = "https://pokeapi.co/api/v2/";

    private Rest() {
    }

//=========================================

    public static RestInterface getRest() {
        return serviceRest;
    }

//=========================================

    public static void init() {
        Gson gson = new GsonBuilder().create();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        serviceRest = retrofit.create(RestInterface.class);
    }


}
