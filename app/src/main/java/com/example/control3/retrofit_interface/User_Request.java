package com.example.control3.retrofit_interface;

import com.example.control3.pojo.User;
import com.example.control3.pojo.UserInformation;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface User_Request {
    @POST("userInformation")
    @FormUrlEncoded
    Call<UserInformation> userInformation(@Field("token") String token, @Field("id") String id);

    @POST("userSignIn")
    @FormUrlEncoded
    Call<UserInformation> userSignIn(@Field("account") String account, @Field("password") String password);

    @POST("userNote")
    Call<UserInformation> userNote(@Body UserInformation userInformation);

    @POST("IUUser")
    Call<String> userLogIn(@Body User user);
}
