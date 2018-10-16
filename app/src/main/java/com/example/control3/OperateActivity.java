package com.example.control3;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;
import com.example.control3.pojo.InstrumentInformation;
import com.example.control3.pojo.Record;
import com.example.control3.pojo.UserInformation;
import com.example.control3.retrofit_class.User_Retrofit;
import com.example.control3.retrofit_interface.Instrument_Request;
import com.example.control3.retrofit_interface.Record_Request;
import com.example.control3.service.BluetoothLeService;
import com.example.control3.service.TimerService;
import com.example.control3.tools.MD5;
import com.example.control3.view.CircleRelativeLayout;
import com.example.control3.view.LEDView;
import com.example.control3.view.SmoothCheckBox;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@EActivity(R.layout.activity_operate)
public class OperateActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Context mContext;
    private OptionsPickerView pvOptions;
    private ArrayList<String> options1Items = new ArrayList<>();
    private String[] information;
    private boolean pkeIsClick = false;
    private boolean leIsClick = false;
    private boolean heIsClick = false;//按钮状态
    private boolean firstEnter=true;//第一次进入需要更新次数
    private boolean checkTimes=false;//是否检查时间
    private boolean checkInstrument=false;//是否检查机器状态
    private Integer times;
    @ViewById
    DrawerLayout drawer_layout;
    @ViewById
    NavigationView nav_view;
    @ViewById(R.id.toolbar2)
    Toolbar toolbar;
    @ViewById
    SmoothCheckBox pke;
    @ViewById
    SmoothCheckBox le;
    @ViewById
    SmoothCheckBox he;
    @ViewById
    TextView enter;

    private int strongNow;

    @ViewById(R.id.strong_one)
    RadioButton strongOne;
    @ViewById(R.id.strong_two)
    RadioButton strongTwo;
    @ViewById(R.id.strong_three)
    RadioButton strongThree;
    @ViewById(R.id.strong_four)
    RadioButton strongFour;
    @ViewById(R.id.strong_five)
    RadioButton strongFive;
    @ViewById(R.id.lin1)
    LinearLayout linearLayout1;
    @ViewById(R.id.strong_text)
    TextView strongText;
    @ViewById
    TextView time;
    @ViewById(R.id.data_times)
    LEDView dataTimes;
    @ViewById(R.id.data_date)
    LEDView dataDate;
    @ViewById(R.id.data_id)
    LEDView dataId;
    @ViewById
    RelativeLayout bottomRel;
    @ViewById
    CircleRelativeLayout circleR;

    //检测手机是否自己关了蓝牙
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF){
                    finish();
                }
            }
        }
    };

    private ProgressDialog progressDialog;
    private boolean timerWork = false;//判断服务启动状态方便返回
    private int minute = -1;
    private int second = -1;
    private TimerService.TimerBinder timerBinder;
    private CountDownTimer timer;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "onServiceConnected: ");

            timerBinder = (TimerService.TimerBinder) service;
            timer = new CountDownTimer((minute * 60 + second) * 1000 + 500, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long all = millisUntilFinished / 1000;
                    long min = all / 60;
                    long sec = all % 60;
                    //Log.e(TAG, "onTick: " + (int) min + ":" + (int) sec);
                    timerBinder.UpdateTimer((int) min, (int) sec);
                    updateCounter((int) min, (int) sec);
                }

                @Override
                public void onFinish() {
                    timerBinder.EndTimer();
                    resetUI();
                }
            };
            //运行倒计时服务
            timer.start();
            timerBinder.StartTimer(minute, second);
            timerWork = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void updateCounter(int minute, int second) {
        //Log.e(TAG, "updateCounter: " + minute + ":" + second);
        if (minute >= 0 && second >= 0) {
            String minuteString;
            String secondString;
            if (minute < 10) {
                minuteString = "0" + minute;
            } else {
                minuteString = "" + minute;
            }
            if (second < 10) {
                secondString = "0" + second;
            } else {
                secondString = "" + second;
            }
            time.setText(minuteString + ":" + secondString);
        }
    }

    //时间到了会调用这个方法
    private void resetUI() {
        enter.setText("开始");
        time.setEnabled(true);
        pke.setEnabled(true);
        le.setEnabled(true);
        he.setEnabled(true);
        time.setText("20:00");
        if (timerWork) {
            timer.cancel();
            timerBinder.CancelTimer();
            unbindService(connection);
            Intent stopIntent = new Intent(this, TimerService.class);
            stopService(stopIntent);
            timerWork = false;
        }
    }

    @AfterViews
    public void init()
    {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        //设置用户信息菜单图标
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
//        nav_view.setCheckedItem(R.id.nav_task);
//        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                //这里写逻辑
//                drawer_layout.closeDrawers();
//                return true;
//            }
//        });

        firstEnter=true;
        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(MainActivity.BLUETOOTH_ADDRESS);
        Log.e(TAG, "onCreate: " + mDeviceAddress);
        //初始化操作数
        operateNull = hex2byte("0002030405060708090A".getBytes());//空操作数
        operateLe = hex2byte("0102030405060708090A".getBytes());//生命活能
        operateHe = hex2byte("0202030405060708090A".getBytes());//健康合能
        operatePke = hex2byte("0302030405060708090A".getBytes());//光子动能
        operateEnter = hex2byte("0402030405060708090A".getBytes());//开始
        operateMinus = hex2byte("0502030405060708090A".getBytes());//减少
        operateAdd = hex2byte("0602030405060708090A".getBytes());//增加
        operateTest = hex2byte("0702030405060708090A".getBytes());//测试

        //初始化操作
        strongNow = 3;
        bottomRel.setBackgroundColor(0xFF48D1CC);
        enter.setOnClickListener(this);
        strongOne.setOnClickListener(this);
        strongTwo.setOnClickListener(this);
        strongThree.setOnClickListener(this);
        strongFour.setOnClickListener(this);
        strongFive.setOnClickListener(this);
        time.setOnClickListener(this);
        pke.setEnabled(false);
        le.setEnabled(false);
        he.setEnabled(false);

        circleR.setColor(0XFFFFFFF);//白色
        //小时选择器初始化
        pvOptions = new OptionsPickerView(this);
        for (int i = 0; i < 31; i++) {
            if (i < 10)
                options1Items.add("0" + i);
            else options1Items.add("" + i);
        }

        pvOptions.setPicker(options1Items);
        pvOptions.setLabels("分", "秒");
        pvOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                //返回的分别是三个级别的选中位置
                String tx = options1Items.get(options1) + ":00";
                time.setText(tx);
            }
        });
        //样板注释处
        progressDialog = new ProgressDialog(OperateActivity.this);
        progressDialog.setTitle("启动蓝牙服务并更新机器中，请勿关闭程序导致失败");
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        mContext=this;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //startOn();
        //上面那是是测试用的，要删掉

        pke.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void OnCheckChange(SmoothCheckBox smoothCheckBox, boolean checked) {
                if (checked)
                    pkeIsClick = true;
                else pkeIsClick = false;
            }
        });
        le.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void OnCheckChange(SmoothCheckBox smoothCheckBox, boolean checked) {
                if (checked)
                    leIsClick = true;
                else leIsClick = false;
            }
        });
        he.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void OnCheckChange(SmoothCheckBox smoothCheckBox, boolean checked) {
                //弹出强度选择框
                if (checked) {
                    linearLayout1.setVisibility(View.VISIBLE);
                    strongText.setVisibility(View.VISIBLE);
                    heIsClick = true;
                } else {
                    linearLayout1.setVisibility(View.INVISIBLE);
                    strongText.setVisibility(View.INVISIBLE);
                    heIsClick = false;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.strong_one:
                strongNow=1;
                if(timerWork)//正在工作直接修改
                {
                    progressDialog.show();
                    new Thread() {
                        @Override
                        public void run() {
                            setTimeAndStrong();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }.start();
                }
                break;
            case R.id.strong_two:
                strongNow=2;
                if(timerWork)//正在工作直接修改
                {
                    progressDialog.show();
                    new Thread() {
                        @Override
                        public void run() {
                            setTimeAndStrong();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }.start();
                }
                break;
            case R.id.strong_three:
                strongNow=3;
                if(timerWork)//正在工作直接修改
                {
                    progressDialog.show();
                    new Thread() {
                        @Override
                        public void run() {
                            setTimeAndStrong();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }.start();
                }
                break;
            case R.id.strong_four:
                strongNow=4;
                if(timerWork)//正在工作直接修改
                {
                    progressDialog.show();
                    new Thread() {
                        @Override
                        public void run() {
                            setTimeAndStrong();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }.start();
                }
                break;
            case R.id.strong_five:
                strongNow=5;
                if(timerWork)//正在工作直接修改
                {
                    progressDialog.show();
                    new Thread() {
                        @Override
                        public void run() {
                            setTimeAndStrong();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }.start();
                }
                break;

            case R.id.time:
                pvOptions.show();
                break;

            case R.id.enter:
                if (enter.getText().toString().equals("开始")) {
                    if (!(pkeIsClick || leIsClick || heIsClick)) {
                        //一个都没选中，弹出错误
                        warn("请选择至少一个功能！！",1);
                    } else {
                        userAddRecord();
//                        enter.setText("停止");
//                        time.setEnabled(false);
//                        pke.setEnabled(false);
//                        le.setEnabled(false);
//                        he.setEnabled(false);
//                        if (heIsClick) {
//                            //开启调节强度按钮
//                            linearLayout1.setVisibility(View.VISIBLE);
//                            strongText.setVisibility(View.VISIBLE);
//                        }
//                        //对蓝牙发出批量写请求，这里使用线程试试
//                        progressDialog.show();
//                        new Thread() {
//                            @Override
//                            public void run() {
//                                operateAll();
//                                if (progressDialog.isShowing())
//                                    progressDialog.dismiss();
//                            }
//                        }.start();
                    }
                } else if (enter.getText().toString().equals("停止")) {
                    progressDialog.show();
                    new Thread() {
                        @Override
                        public void run() {
                            writeOperate(operateEnter);
                            backToSelectMode(0);
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }.start();
                    resetUI();
                }
                break;
            default:
                break;
        }
    }

    private void backToSelectMode(int mode)//返回对应操作界面
    {
        writeOperate(operateTest);//获得信息
        while(!information[11].equals(mode+""))//直到在选择功能页面
        {
            writeOperate(operateEnter);
            writeOperate(operateTest);
        }
    }

    private void setTimeAndStrong()//设置时间强度
    {
        int min = Integer.parseInt(time.getText().toString().substring(0, 2));//次数
        setOperateSetTimeStrong(min);//设置好次数
        do {//检测是否成功
            writeOperate(operateSet);//设置开始
        } while(!information[6].equals(time.getText().toString().substring(0, 2)));
    }

    private void operateAll()
    {
        backToSelectMode(0);//检测页面0（设置模式）
        setOperateSetMode();//设置好模式
        do{//检测是否成功
            writeOperate(operateSet);//设置开始
        } while(!(information[8].equals(leIsClick?"1":"0")&&information[9].equals(heIsClick?"1":"0")&&information[10].equals(pkeIsClick?"1":"0")));
        writeOperate(operateEnter);
        backToSelectMode(1);//检测页面1（设置时间或强度）
        setTimeAndStrong();
        writeOperate(operateEnter);//确认后到选择时间和强度的页面
        backToSelectMode(2);//检测页面2（运行）
        //开启倒计时
        minute = Integer.parseInt(time.getText().toString().substring(0, 2));
        second = 0;
        Log.e(TAG, "onClick: " + minute + ":" + second);
        Intent timerStart = new Intent(this, TimerService.class);
        startService(timerStart);
        bindService(timerStart, connection, BIND_AUTO_CREATE);
    }

    private void sleep(int millis)//睡眠
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //state 0为退出到登录页面，1为只显示，2为退出到蓝牙扫描界面
    private void warn(String information,final int state) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(OperateActivity.this);
        dialog.setTitle("提示");
        dialog.setMessage(information);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(state==0)
                {
                    Intent intent=new Intent(mContext,SignInActivity_.class);
                    mContext.startActivity(intent);
                    finish();
                }else if(state==2){
                    Intent intent=new Intent(mContext,MainActivity_.class);
                    mContext.startActivity(intent);
                    finish();
                }
            }
        });
        dialog.show();
    }

    private void end() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(OperateActivity.this);
        dialog.setTitle("警告");
        dialog.setMessage("要断开连接并返回吗？");
        dialog.setCancelable(true);
        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (timerWork) {
                    timer.cancel();
                    timerBinder.CancelTimer();
                    timerWork = false;
                    unbindService(connection);
                    Intent stopIntent = new Intent(OperateActivity.this, TimerService.class);
                    stopService(stopIntent);
                }
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        end();
    }

    //下面开始写蓝牙相关的操作
    private String mDeviceAddress;
    private boolean mConnected = false;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    byte[] operateNull = new byte[20];
    byte[] operateEnter = new byte[20];
    byte[] operateLe = new byte[20];
    byte[] operateHe = new byte[20];
    byte[] operatePke = new byte[20];
    byte[] operateMinus = new byte[20];
    byte[] operateAdd = new byte[20];
    byte[] operateTest = new byte[20];
    byte[] operateSet = new byte[20];
    byte[] operateTimesSet = new byte[20];

    public void setOperateSetTimeStrong(int min) {//设置时间，强度
        operateSet = hex2byte(("08" + "0" + min / 10 + "0" + min % 10 + "0" + strongNow + "05060708090A").getBytes());
    }

    public void setOperateSetMode() {//设置模式
        operateSet = hex2byte(("09020304" + (leIsClick?"01":"00")  + (heIsClick?"01":"00") +  (pkeIsClick?"01":"00") + "08090A").getBytes());
    }

    public byte[] setOperateTimesSet(int times) {//设置次数
        String setTimes=null;
        if(times>=1000)
            setTimes=""+times;
        else if(times>=100)
            setTimes="0"+times;
        else if(times>=10)
            setTimes="00"+times;
        else if(times>=0)
            setTimes="000"+times;
        if(setTimes!=null)
            operateTimesSet = hex2byte(("0A0203" + setTimes +  "060708090A").getBytes());
        return operateTimesSet;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            } else
                Log.e(TAG, "onServiceConnected: good!able to initialize Bluetooth");
            // Automatically connects to the device upon successful start-up initialization.在成功启动初始化时自动连接到设备。
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.e(TAG, "Connect request result=" + result);
        } else
            Log.e(TAG, "onResume: null?");
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    /*
    /处理服务触发的各种事件。（广播接收器）
/ / action_gatt_connected：连接到GATT服务器。
/ / action_gatt_disconnected：从GATT服务器断开。
/ / action_gatt_services_discovered：关贸总协定的服务发现。
/ / action_data_available：从设备接收数据。这可能是阅读的结果。
/或通知操作。
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: 000");
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.e(TAG, "onReceive: 111");
                //这里写一个线程更新UI，用于显示状态“已连接”
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //同上，断开连接
                Log.e(TAG, "onReceive: 222");
                invalidateOptionsMenu();
                clearUI();
                //清空子列表（不需要）和数据
                finish();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.显示用户界面上所有支持的服务和特性。
                Log.e(TAG, "onReceive: 333");
                if (mBluetoothLeService.getSupportedGattServices() == null) {
                    Log.e(TAG, "onReceive: null");
                } else
                    Log.e(TAG, "onReceive: ???");
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                //具体处理连接的逻辑。目标：找到具体的BluetoothGattCharacteristic
                //封装好对应操作的WriteBytes数据，characteristic数据


                //                characteristic.setValue(value[0],
                //                        BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                //                characteristic.setValue(WriteBytes);
                //
                //                mBluetoothLeService.writeCharacteristic(characteristic);
                //用以上方法向蓝牙写数据，这里应该在按钮中写了

                startOn();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e(TAG, "onReceive: 444");
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //输出数据，括号内就是输出字符串
//                if (progressDialog.isShowing())
//                    progressDialog.cancel();
            }
        }
    };

    private void clearUI() {
//        dataTimes.setText(R.string.no_data);
//        dataDate.setText(R.string.no_data);
//        dataId.setText(R.string.no_data);
        //dataRemain.setText(R.string.no_data);
    }

    private void displayData(String data) {
        if (data != null) {
            //mDataField.setText(data);
            data = data.substring(data.indexOf("\n") + 1);
            Log.e(TAG, "displayData: length=" + data.length() + " " + data);
            dataAnalyze(data);
            //对应处理
            if (information != null&&information[5].equals("000")) {//这里做具体判断是否输出指令
                dataTimes.setLedView(70, "8888", (1000-Integer.parseInt(information[1]))+"");
                dataDate.setLedView(40, "88888", information[2] + "/" + information[3]);
                dataId.setLedView(40, "88888", information[0] + information[4]);
                dataId.setNumberColor(0xFFFFFFFF);
                dataDate.setNumberColor(0xFFFFFFFF);
                if(firstEnter)
                {
                    getInstrumentByCode();
                    firstEnter=false;
                }
                if(checkTimes)
                {
                    while(Integer.parseInt(information[1])!=times)
                    {
                        checkTimes=true;
                        writeOperate(setOperateTimesSet(times));
                        writeOperate(operateNull);
                    }
                    checkTimes=false;
                    checkInstrument=true;
                    //接下来开始检查机器的界面
                    writeOperate(operateTest);//获得信息
                }
            }else if(information != null&&information[5].equals("007")&&checkInstrument)
            {
                checkInstrument=false;
                checkAndSet();
            }
        } else
            clearUI();
    }

    private void dataAnalyze(String data) {
        if (data == null)
            return ;
        //下面分割数据返回数组
        // 0：机器型号代码  1:计数值  2:出厂年份 3：出厂月份  4：机器编号  5：返回指令
        // 发送08时候的附加值  6：时间值 7：强度值 8：生命活能 9：健康活能 10：光子动能 11：确定键状态（0：选择功能，1：设置时间，2：设置强度）
        information = new String[12];
        information[0] = data.substring(0, 2);
        //re[1] = data.substring(3, 5) + data.substring(6, 8) + data.substring(9, 11) + data.substring(12, 14);
        information[1] = data.substring(9, 11) + data.substring(12, 14);
        //先只保留四位看看
        information[2] = data.substring(15, 17);
        information[3] = data.substring(18, 20);
        information[4] = data.substring(21, 23) + data.substring(24, 25);
        information[5] = data.substring(25, 26) + data.substring(27, 29);
        information[6] = data.substring(3, 5);
        information[7] = data.substring(6, 8);
        information[8] = data.substring(9, 10);
        information[9] = data.substring(10, 11);
        information[10] = data.substring(12, 13);
        information[11] = data.substring(13, 14);
        for (int i = 0; i < information.length; i++) {
            Log.e(TAG, "dataAnalyze" + i + ": " + information[i]);
        }
    }

    //把16进制转换为10进制，两位两位的转换
    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        b = null;
        return b2;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        //原来这里是根据列表列举所有的蓝牙连接选项的，我们这里选择直接连接一个
        String uuid = null;
        BluetoothGattCharacteristic re = null;
        if (gattServices == null) {
            Log.e(TAG, "displayGattServices: gattServices == null错误，具体还需要排查（推测蓝牙自动断开的锅）");
            return;
        }
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            Log.e(TAG, "displayGattServices: S=" + uuid);
            if (uuid.equals("0000ffe0-0000-1000-8000-00805f9b34fb")) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    Log.e(TAG, "displayGattServices: C=" + uuid);
                    if (uuid.equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {
                        bluetoothGattCharacteristic = gattCharacteristic;
                    }
                }
            }
        }
    }


    private boolean writeOperate(byte[] operate) {
        if (bluetoothGattCharacteristic == null) {
            Log.e(TAG, "writeOperate: re == null错误，具体还需要排查（推测蓝牙自动断开的锅）");
            return false;
        }
        //progressDialog.show();
        final int charaProp = bluetoothGattCharacteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            byte[] value = new byte[20];
            value[0] = (byte) 0x00;
            bluetoothGattCharacteristic.setValue(value[0],
                    BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            bluetoothGattCharacteristic.setValue(operate);

            mBluetoothLeService.writeCharacteristic(bluetoothGattCharacteristic);
        } else {
            Log.e(TAG, "writeOperate: 该char不可写");
            return false;
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mBluetoothLeService.setCharacteristicNotification(
                    bluetoothGattCharacteristic, true);
        }
        sleep(500);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //样板注释处
        unbindService(mServiceConnection);

        mBluetoothLeService = null;
        if (timerWork) {
            timer.cancel();
            timerBinder.CancelTimer();
            unbindService(connection);
            Intent stopIntent = new Intent(this, TimerService.class);
            stopService(stopIntent);
            timerWork = false;
        }
    }

    //蓝牙服务初始化完毕才执行
    private void startOn() {
        //enter1.setEnabled(true);
        enter.setEnabled(true);
        le.setEnabled(true);
        he.setEnabled(true);
        pke.setEnabled(true);
        writeOperate(operateNull);//检测是否加好了数据，并向服务器更新数据

    }


    public void getInstrumentByCode()
    {
        SharedPreferences pref=SignInActivity.getSharedPrefs(this);
        String id=pref.getString("id",null);
        if(id==null)
        {
            warn("登录状态过期!请重新登录!",0);
            return;
        }else if(information[4]==null||information[4].equals("")||information[1]==null)
        {
            warn("获取仪器信息错误!",2);
            return;
        }
        Retrofit retrofit=new User_Retrofit().getRetrofit();
        Instrument_Request request = retrofit.create(Instrument_Request.class);
        Call<Integer> call = request.updateInstrument(information[4],1000-Integer.parseInt(information[1]));
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.body()==null)//操作失败，回到上一层
                {
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                    warn("仪器不存在或已关闭!",2);
                }else{//更新成功，向机器写数据
                    times=1000-response.body();
                    Log.e(TAG, "onResponse: information[1]="+Integer.parseInt(information[1])+"  times="+times );
                    checkTimes=true;
                    writeOperate(operateNull);
                    writeOperate(setOperateTimesSet(times));
                }
            }
            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                Log.e(TAG, "onFailure: 连接失败\n"+throwable.getMessage() );
                warn("连接失败!",2);
            }
        });
    }

    public void userAddRecord()
    {
        SharedPreferences pref=SignInActivity.getSharedPrefs(this);
        String id=pref.getString("id",null);
        String token=pref.getString("token",null);
        if(id==null)
        {
            warn("登录状态过期!请重新登录!",0);
            return;
        }else if(information[4]==null||information[4].equals(""))
        {
            warn("获取仪器信息错误!",2);
            return;
        }
        progressDialog.show();
        enter.setText("停止");
        time.setEnabled(false);
        pke.setEnabled(false);
        le.setEnabled(false);
        he.setEnabled(false);
        if (heIsClick) {
            //开启调节强度按钮
            linearLayout1.setVisibility(View.VISIBLE);
            strongText.setVisibility(View.VISIBLE);
        }
        Retrofit retrofit=new User_Retrofit().getRetrofit();
        Record_Request request = retrofit.create(Record_Request.class);
        Record record=new Record();
        record.setuId(Integer.parseInt(id));
        record.setToken(token);
        record.setiId(Integer.parseInt(information[4]));
        record.setrCode(MD5.md5(id+new Date()));
        record.setrStartTime(new Date());
        record.setrLastTime(Integer.parseInt(time.getText().toString().substring(0, 2)));
        record.setrStrong(strongNow);
        record.setrMode((leIsClick?"生命活能  ":"")  + (heIsClick?"健康合能  ":"") +  (pkeIsClick?"光子动能  ":""));
        Call<String> call = request.userAddRecord(record);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.body()==null)//操作失败，回到上一层
                {
                    if(progressDialog.isShowing())
                        progressDialog.dismiss();
                    warn("连接不存在!",2);
                }else if(response.body().equals("success")){//更新成功
//                    enter.setText("停止");
//                    time.setEnabled(false);
//                    pke.setEnabled(false);
//                    le.setEnabled(false);
//                    he.setEnabled(false);
//                    if (heIsClick) {
//                        //开启调节强度按钮
//                        linearLayout1.setVisibility(View.VISIBLE);
//                        strongText.setVisibility(View.VISIBLE);
//                    }
                    //对蓝牙发出批量写请求，这里使用线程试试
                    new Thread() {
                        @Override
                        public void run() {
                            operateAll();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                        }
                    }.start();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable throwable) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
                Log.e(TAG, "onFailure: 连接失败\n"+throwable.getMessage() );
                warn("连接失败!",2);
            }
        });
    }

    private void checkAndSet()//发送07并检查设置对应的界面信息
    {
        Log.e(TAG, "checkAndSet: information[11]="+information[11] );
        if(information[11].equals("2"))//0,1界面 不动即可
        {
            leIsClick=information[8].equals("1");
            heIsClick=information[9].equals("1");
            pkeIsClick=information[10].equals("1");
            strongNow=Integer.parseInt(information[7]);

            //设置界面
            pke.setChecked(pkeIsClick);
            he.setChecked(heIsClick);
            le.setChecked(leIsClick);
            switch (strongNow){
                case 1:
                    strongOne.setChecked(true);
                    break;
                case 2:
                    strongTwo.setChecked(true);
                    break;
                case 3:
                    strongThree.setChecked(true);
                    break;
                case 4:
                    strongFour.setChecked(true);
                    break;
                case 5:
                    strongFive.setChecked(true);
                    break;
                    default:break;
            }
            enter.setText("停止");
            time.setEnabled(false);
            pke.setEnabled(false);
            le.setEnabled(false);
            he.setEnabled(false);
            if (heIsClick) {
                //开启调节强度按钮
                linearLayout1.setVisibility(View.VISIBLE);
                strongText.setVisibility(View.VISIBLE);
            }
            time.setText((information[6]+":00"));
            //开启倒计时
            minute = Integer.parseInt(time.getText().toString().substring(0, 2));
            second = 0;
            Log.e(TAG, "onClick: " + minute + ":" + second);
            Intent timerStart = new Intent(this, TimerService.class);
            startService(timerStart);
            bindService(timerStart, connection, BIND_AUTO_CREATE);
        }
        if(progressDialog.isShowing())
            progressDialog.dismiss();
        progressDialog.setTitle("等待...");

    }
}
