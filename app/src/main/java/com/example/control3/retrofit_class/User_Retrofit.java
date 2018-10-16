package com.example.control3.retrofit_class;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.control3.MainActivity;
import com.example.control3.MainActivity_;
import com.example.control3.OperateActivity_;
import com.example.control3.SignInActivity;
import com.example.control3.SignInActivity_;
import com.example.control3.pojo.RecordInformation;
import com.example.control3.pojo.UserInformation;
import com.example.control3.retrofit_interface.Record_Request;
import com.example.control3.retrofit_interface.User_Request;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

//所有Retrofit配置相关的都在这儿
public class User_Retrofit {
    public static final String URL="http://47.107.57.7:8090/mdms/";
    private static final String TAG = "User_Retrofit";

    private static final OkHttpClient client = new OkHttpClient.Builder().
            connectTimeout(30, TimeUnit.MINUTES).
            readTimeout(30, TimeUnit.MINUTES).
            writeTimeout(30, TimeUnit.MINUTES).build();

    private Retrofit retrofit;

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public void setRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    public User_Retrofit() {

        retrofit = new Retrofit.Builder()
                .baseUrl(URL) // 设置 网络请求 Url
//                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create()) //设置使用Gson解析GsonConverterFactory.create()(记得加入依赖)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()))
                .build();
    }

}
