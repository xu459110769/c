package com.example.control3;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.TimePickerView;
import com.example.control3.adapter.InformationAdapter;
import com.example.control3.adapter.RecordAdapter;
import com.example.control3.pojo.Record;
import com.example.control3.pojo.RecordInformation;
import com.example.control3.pojo.RecordItem;
import com.example.control3.retrofit_class.User_Retrofit;
import com.example.control3.retrofit_interface.Record_Request;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


@EActivity(R.layout.activity_record)
public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RecordActivity";
    private Context mContext;
    private RecordActivity recordActivity;
    private RecordInformation recordInformation;
    public ProgressDialog progressDialog;

    @ViewById
    TextView time_start;
    @ViewById
    TextView time_end;
    @ViewById
    Toolbar toolbar;
    @ViewById
    RecyclerView recycler_view;
    @ViewById
    SwipeRefreshLayout swipe_refresh;

    private TimePickerView timeStart;
    private TimePickerView timeEnd;
    private Date startDate;
    private Date endDate;
    private List<RecordItem> recordItems=new ArrayList<>();
    private RecordAdapter adapter;
    private boolean isFinding=false;

    @AfterViews
    void init()
    {
        //设置标题栏
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        startDate=new Date();
        endDate=new Date();

        //设置列表
        GridLayoutManager layoutManager=new GridLayoutManager(this,1);
        recycler_view.setLayoutManager(layoutManager);
        adapter=new RecordAdapter(recordItems);
        recycler_view.setAdapter(adapter);
        ((DefaultItemAnimator) recycler_view.getItemAnimator()).setSupportsChangeAnimations(false);//刷新不闪屏
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        //设置进度框
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("请稍等");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);

        //界面设置完毕
        mContext=this;
        recordActivity=this;

        //设置时间开始选择器
        timeStart=new TimePickerView(this,TimePickerView.Type.YEAR_MONTH_DAY);
        timeStart.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = formatter.format(date);
                time_start.setText(dateString);
                timeEnd.setTime(date);
                startDate=date;
                //筛选
                setRecordAdapter();
            }
        });
        timeStart.setTitle("请选择开始时间");
        timeStart.setCyclic(true);
        time_start.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        //设置时间结束选择器
        timeEnd=new TimePickerView(this,TimePickerView.Type.YEAR_MONTH_DAY);
        timeEnd.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String dateString = formatter.format(date);
                time_end.setText(dateString);
                endDate=date;
                //筛选
                setRecordAdapter();
            }
        });
        timeEnd.setTitle("请选择结束时间");
        timeEnd.setCyclic(true);
        time_end.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //获取所有的记录便于筛选
        if(!isFinding)
        {
            isFinding=true;
            userRecord();
        }else {
            Toast.makeText(mContext, "已在查找，请稍等一会儿哦~", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Click
    void time_start()
    {
        timeStart.show();
    }

    @Click
    void time_end()
    {
        timeEnd.show();
    }

    public void userRecord(){
        progressDialog.show();
        SharedPreferences pref=SignInActivity.getSharedPrefs(this);
        String id=pref.getString("id",null);
        String token=pref.getString("token",null);
        Retrofit retrofit=new User_Retrofit().getRetrofit();
        Record_Request request = retrofit.create(Record_Request.class);
        Call<RecordInformation> call = request.userRecord(token,id);
        call.enqueue(new Callback<RecordInformation>() {
            @Override
            public void onResponse(Call<RecordInformation> call, Response<RecordInformation> response) {
                recordInformation=response.body();
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                //Log.e(TAG, "onResponse: " +recordInformation.toString());
                if(recordInformation.getCode()==1)//验证成功，显示数据
                {
                    Log.e(TAG, "onResponse: " +recordInformation.toString());
                    startDate=recordInformation.getDate();
                    time_start.setText(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
                    setRecordAdapter();
                }else if(recordInformation.getCode()==0)//验证失败，回到登录界面
                {
                    warn("登录状态过期!请重新登录!",true);
                }
                swipe_refresh.setRefreshing(false);
                isFinding=false;
            }
            @Override
            public void onFailure(Call<RecordInformation> call, Throwable throwable) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                Log.e(TAG, "onFailure: 连接失败\n"+throwable.getMessage() );
                warn("连接失败!",false);
                swipe_refresh.setRefreshing(false);
                isFinding=false;
            }
        });
    }

    public void warn(String information, final boolean isToSignIn) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("警告");
        dialog.setMessage(information);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(isToSignIn){
                    Intent intent=new Intent(mContext,SignInActivity_.class);
                    mContext.startActivity(intent);
                    recordActivity.finish();
                }
            }
        });
        dialog.show();
    }

    public void setRecordAdapter()
    {
        adapter.clear();
        recordItems.clear();
        RecordItem firstLine=new RecordItem();
        firstLine.setrMode("第一行");
        recordItems.add(firstLine);
        if(recordInformation.getRecords()!=null)
        {
            for(RecordItem recordItem:recordInformation.getRecords())
            {
                if(recordItem.getrStartTime().getTime()>=startDate.getTime()&&recordItem.getrStartTime().getTime()<=endDate.getTime()){
                    recordItems.add(recordItem);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void refresh()
    {
        if(!isFinding)
        {
            isFinding=true;
            userRecord();
        }else {
            Toast.makeText(mContext, "已在查找，请稍等一会儿哦~", Toast.LENGTH_SHORT).show();
        }

    }
}
