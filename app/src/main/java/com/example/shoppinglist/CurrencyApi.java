package com.example.shoppinglist;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CurrencyApi {
    @GET("USD")
    Call<CurrencyModel> getData(@Query("ParamMode") int mode);
}
