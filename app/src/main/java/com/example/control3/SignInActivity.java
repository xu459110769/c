package com.example.control3;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.example.control3.pojo.UserInformation;
import com.example.control3.retrofit_class.User_Retrofit;
import com.example.control3.retrofit_interface.User_Request;
import com.example.control3.tools.MD5;
import com.example.control3.tools.SecurePreferences;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@EActivity(R.layout.activity_sign_in)
public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    public ProgressDialog progressDialog;
    private Context mContext;
    @ViewById
    public EditText login_edit_account;
    @ViewById
    public EditText login_edit_pwd;

    @AfterViews
    protected void init() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("登录中");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);

        mContext=this;
        SharedPreferences pref=getSharedPrefs(this);
        String id=pref.getString("id",null);
        String token=pref.getString("token",null);
        Log.e(TAG, "onCreate: id="+ id+"   token="+token);
        if(id!=null&&token!=null)
        {
            progressDialog.show();
            userInformation(id,token);
        }
    }

    public static SharedPreferences getSharedPrefs(Context context) {
            return new SecurePreferences(context, "wgy", "pref-file");//获取加密的SharedPreferences
    }

    @Click
    public void login_btn_login() {
        if(login_edit_account.getText().toString().equals("")||login_edit_pwd.getText().toString().equals(""))
        {
            warn("请输入您的账号或密码!");
            return;
        }
        progressDialog.show();

        userSignIn(login_edit_account.getText().toString(), MD5.md5(login_edit_pwd.getText().toString()));
//        Intent intent=new Intent(this,MainActivity_.class);//测试用
//        startActivity(intent);
    }

    @Click
    public void login_btn_register() {
        Intent intent=new Intent(this,LogInActivity_.class);
        startActivity(intent);
    }


    public void warn(String information) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("警告");
        dialog.setMessage(information);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    public void userInformation(String id,String token)
    {
        Retrofit retrofit=new User_Retrofit().getRetrofit();
        // 步骤5:创建 网络请求接口 的实例
        User_Request request = retrofit.create(User_Request.class);
        //对 发送请求 进行封装
        Call<UserInformation> call = request.userInformation(token,id);
        //步骤6:发送网络请求(异步)
        call.enqueue(new Callback<UserInformation>() {
            //请求成功时回调
            @Override
            public void onResponse(Call<UserInformation> call, Response<UserInformation> response) {
                // 步骤7：处理返回的数据结果
                UserInformation userInformation=response.body();
//                Log.e(TAG, "onResponse: " +userInformation.toString());
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                if(userInformation.getCode()==1)//验证成功，得到数据后跳转蓝牙
                {
                    Intent intent=new Intent(mContext,MainActivity_.class);
                    //记得加入数据
                    intent.putExtra("userInformation",userInformation);
                    startActivity(intent);
                    finish();
                }else {
                    SharedPreferences.Editor editor=SignInActivity.getSharedPrefs(mContext).edit();//清除token和id
                    editor.remove("id");
                    editor.remove("token");
                    editor.apply();
                    warn("登录状态过期!请重新登录!");
                }
            }
            //请求失败时回调
            @Override
            public void onFailure(Call<UserInformation> call, Throwable throwable) {
                Log.e(TAG, "onFailure: 连接失败\n"+throwable.getMessage() );
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                warn("连接失败!请稍后重试!");
            }
        });
    }

    public void userSignIn(String account,String password)
    {
        Retrofit retrofit=new User_Retrofit().getRetrofit();
        User_Request request = retrofit.create(User_Request.class);
        Call<UserInformation> call = request.userSignIn(account,password);
        call.enqueue(new Callback<UserInformation>() {
            @Override
            public void onResponse(Call<UserInformation> call, Response<UserInformation> response) {
                UserInformation userInformation=response.body();
                Log.e(TAG, "onResponse: " +userInformation.toString());
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                if(userInformation.getCode()==1)//验证成功，得到数据后跳转蓝牙
                {
                    SharedPreferences.Editor editor= SignInActivity.getSharedPrefs(mContext).edit();
                    editor.putString("id",userInformation.getId().toString());
                    editor.putString("token",userInformation.getToken());
                    editor.apply();
                    Intent intent=new Intent(mContext,MainActivity_.class);
                    intent.putExtra("userInformation",userInformation);
                    startActivity(intent);
                    finish();
                }else if(userInformation.getCode()==2)//密码错误
                {
                    warn("账号名或者密码错误!");
                }
            }
            @Override
            public void onFailure(Call<UserInformation> call, Throwable throwable) {
                Log.e(TAG, "onFailure: 连接失败\n"+throwable.getMessage() );
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                warn("连接失败!");
            }
        });
    }
}
