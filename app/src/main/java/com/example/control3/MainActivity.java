package com.example.control3;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.control3.adapter.BluetoothInformation;
import com.example.control3.adapter.InformationAdapter;
import com.example.control3.pojo.RecordInformation;
import com.example.control3.pojo.UserInformation;
import com.example.control3.retrofit_class.User_Retrofit;
import com.example.control3.retrofit_interface.Record_Request;
import com.example.control3.retrofit_interface.User_Request;
import com.example.control3.service.TimerService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private List<BluetoothInformation> bluetoothInformationList=new ArrayList<>();
    private EditText edit;
    private InformationAdapter adapter;
    public ProgressDialog progressDialog;

    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    @ViewById
    DrawerLayout drawer_layout;

    private static final String TAG = "MainActivity";
    private Context mContext=this;
    public static final String BLUETOOTH_ADDRESS="bluetooth_address";
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.10秒后停止扫描。
    //private static final long SCAN_PERIOD = 10000;

    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;
    @ViewById(R.id.toolbar)
    Toolbar toolbar;
    @ViewById
    NavigationView nav_view;

    TextView name_information;

    TextView account_information;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    //private Handler mHandler;

    @AfterViews
    public void init()//暂时代替onCreate
    {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        UserInformation userInformation = (UserInformation) getIntent().getSerializableExtra("userInformation");
//        Log.e(TAG, "init: "+userInformation.toString() );

        //设置等待框
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("请稍等");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);

        //设置用户信息菜单图标
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_task:
                        Intent intent=new Intent(mContext,RecordActivity_.class);
                        mContext.startActivity(intent);
                        break;
                    case R.id.nav_back:
                        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                        dialog.setTitle("提示");
                        dialog.setMessage("要注销您的账号并返回吗？");
                        dialog.setCancelable(true);
                        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor editor=SignInActivity.getSharedPrefs(mContext).edit();//清除token和id
                                editor.remove("id");
                                editor.remove("token");
                                editor.apply();
                                Intent intent=new Intent(mContext,SignInActivity_.class);
                                mContext.startActivity(intent);
                                finish();
                            }
                        });
                        dialog.show();
                        break;
                    case R.id.nav_mail:
                        //设置反馈框
                        edit = new EditText(mContext);
                        edit.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
                        edit.setMinLines(5);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle("请输入您的反馈");    //设置对话框标题
                        builder.setIcon(android.R.drawable.btn_star);   //设置对话框标题前的图标
                        builder.setView(edit);
                        builder.setPositiveButton("提交", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!edit.getText().toString().equals(""))
                                {
                                    progressDialog.show();
                                    userRecord();
                                }
                                //提交请求更新
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                        break;
                        default:break;
                }
                return true;
            }
        });
        View headerView = nav_view.getHeaderView(0);
        name_information = headerView.findViewById(R.id.name_information);
        account_information= headerView.findViewById(R.id.account_information);//必须从上到下逐级获取
        name_information.setText(userInformation.getuName());
        account_information.setText(userInformation.getuAccount());
//        注释测试

        initBluetooth();
        GridLayoutManager layoutManager=new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new InformationAdapter(bluetoothInformationList);
        recyclerView.setAdapter(adapter);
        ((DefaultItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);//刷新不闪屏
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        getBlePermissionFromSys();

        //mHandler=new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        else
        {
            Log.e(TAG, "onCreate:正常 " );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        //没有连接就不显示stop按钮
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_scan:
                //scanLeDevice(true);
                refresh();
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case android.R.id.home:
                drawer_layout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    // Device scan callback.设备扫描回调。
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.e(TAG, "run: 设备回调" );
                            //initBluetooth();
                            boolean flag=true;
                            for(BluetoothInformation b:bluetoothInformationList)
                            {
                                if(b.getAddress().equals(device.getAddress()))
                                {
                                    flag=false;
                                    break;
                                }
                            }
                            if(flag)
                            {
                                bluetoothInformationList.add(new BluetoothInformation(device.getName(),device.getAddress()));
                                adapter.notifyDataSetChanged();
                            }
                            //刷新！！
                        }
                    });
                }
            };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.在预定的扫描周期之后停止扫描。
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //Log.e(TAG, "run: " );
//                    mScanning = false;
//                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                    invalidateOptionsMenu();
//                }
//            }, SCAN_PERIOD);//10s后停止扫描
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }


    private void initBluetooth()
    {
        if(adapter!=null)
        {
            adapter.clear();
        }
        bluetoothInformationList.clear();
        //暂且试试加入数据，正式会删掉
        //bluetoothInformationList.add(new BluetoothInformation("测试数据-这里是名称","测试数据-这里是地址"));
    }

    private void refresh()
    {
        if(mScanning)
        {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initBluetooth();
                        scanLeDevice(true);
                        //具体刷新逻辑
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
//        init();
        super.onResume();
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.确保设备上启用了蓝牙。如果当前没有启用蓝牙，则触发一个对话框，显示请求用户授予其启用权限的对话框。
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                Log.e(TAG, "onResume: !mBluetoothAdapter.isEnabled()" );
            }
        }
        Log.e(TAG, "onResume: mBluetoothAdapter.isEnabled()" );
        scanLeDevice(true);
        refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        initBluetooth();
    }

    @Override
    protected void onDestroy() {
        scanLeDevice(false);
        initBluetooth();
        super.onDestroy();
    }

    //SDK高一定要申请这个获得位置的权限，不然蓝牙还没法回调
    public void getBlePermissionFromSys() {
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 102;
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }
    }

    //提交反馈
    public void userRecord(){
        progressDialog.show();
        SharedPreferences pref=SignInActivity.getSharedPrefs(this);
        String id=pref.getString("id",null);
        String token=pref.getString("token",null);
        Retrofit retrofit=new User_Retrofit().getRetrofit();
        User_Request request = retrofit.create(User_Request.class);
        UserInformation userInformation=new UserInformation();
        userInformation.setuWords(edit.getText().toString());
        userInformation.setId(Integer.parseInt(id));
        userInformation.setToken(token);
        Call<UserInformation> call = request.userNote(userInformation);
        call.enqueue(new Callback<UserInformation>() {
            @Override
            public void onResponse(Call<UserInformation> call, Response<UserInformation> response) {
                UserInformation userInformation=response.body();
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                //Log.e(TAG, "onResponse: " +recordInformation.toString());
                if(userInformation.getCode()==1)//验证成功
                {
                    warn("反馈成功!",1);
                }else if(userInformation.getCode()==0)//验证失败，回到登录界面
                {
                    warn("登录状态过期!请重新登录!",0);
                }
            }
            @Override
            public void onFailure(Call<UserInformation> call, Throwable throwable) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                Log.e(TAG, "onFailure: 连接失败\n"+throwable.getMessage() );
                warn("连接失败!",2);
            }
        });
    }

    public void warn(String information, final int isToSignIn) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        if(isToSignIn==1)
            dialog.setTitle("提示");
        else dialog.setTitle("警告");
        dialog.setMessage(information);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(isToSignIn==0){
                    Intent intent=new Intent(mContext,SignInActivity_.class);
                    mContext.startActivity(intent);
                    finish();
                }
            }
        });
        dialog.show();
    }
}

