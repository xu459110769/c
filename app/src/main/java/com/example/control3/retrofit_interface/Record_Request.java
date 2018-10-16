package com.example.control3.retrofit_interface;

import com.example.control3.pojo.Record;
import com.example.control3.pojo.RecordInformation;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Record_Request {
    @POST("userRecord")
    @FormUrlEncoded
    Call<RecordInformation> userRecord(@Field("token") String token, @Field("id") String id);

    @POST("userAddRecord")
    Call<String> userAddRecord(@Body Record record);
}
