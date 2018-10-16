package com.example.control3;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.control3.pojo.User;
import com.example.control3.pojo.UserInformation;
import com.example.control3.retrofit_class.User_Retrofit;
import com.example.control3.retrofit_interface.User_Request;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@EActivity(R.layout.activity_log_in)
public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "LogInActivity";
    public ProgressDialog progressDialog;

    @ViewById
    TextView resetpwd_edit_number;
    @ViewById
    TextView resetpwd_edit_name;
    @ViewById
    TextView resetpwd_edit_pwd_old;
    @ViewById
    TextView resetpwd_edit_pwd_new;
    @ViewById
    RadioGroup radioGroup;

    @AfterViews
    void init()
    {
        //设置等待框
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("请稍等");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);

        radioGroup.check(R.id.boy);
    }

    @Click
    void register_btn_sure(){
        if(!resetpwd_edit_name.getText().toString().equals("")&&!resetpwd_edit_number.getText().toString().equals("")
                &&!resetpwd_edit_pwd_old.getText().toString().equals("")&&resetpwd_edit_pwd_old.getText().toString().equals(resetpwd_edit_pwd_new.getText().toString())){
            progressDialog.show();
            userLogin();
        }else {
            warn("输入数据有误！请检查！",1);
        }
    }

    @Click
    void register_btn_cancel(){
        finish();
    }

    public void warn(String information, final int state) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("提示");
        dialog.setMessage(information);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(state==0)
                {
                    finish();
                }
            }
        });
        dialog.show();
    }

    public void userLogin()
    {
        Retrofit retrofit=new User_Retrofit().getRetrofit();
        User_Request request = retrofit.create(User_Request.class);
        User user=new User();
        user.setuId(-1);
        user.setuName(resetpwd_edit_name.getText().toString());
        user.setuAccount(resetpwd_edit_number.getText().toString());
        user.setuPassword(resetpwd_edit_pwd_new.getText().toString());
        RadioButton checkRadioButton = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
        user.setuSex(checkRadioButton.getText().toString());
        user.setuDel("0");
        user.setuDate(new Date());
        Call<String> call = request.userLogIn(user);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String information=response.body();
                Log.e(TAG, "onResponse: "+information );
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                if(information.equals("success"))
                {
                    warn("注册成功!请返回重新登录!",0);
                }
                else warn("注册失败!",1);
            }
            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                Log.e(TAG, "onFailure: 连接失败\n"+throwable.getMessage() );
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                warn("连接失败!",1);
            }
        });
    }
}
