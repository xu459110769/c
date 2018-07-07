package com.example.control3;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.control3.R;
import com.example.control3.bluetooth.BluetoothInformation;
import com.example.control3.bluetooth.InformationAdapter;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private List<BluetoothInformation> bluetoothInformationList=new ArrayList<>();

    private InformationAdapter adapter;

    @ViewById(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    private static final String TAG = "MainActivity";

    public static final String BLUETOOTH_ADDRESS="bluetooth_address";
    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.10秒后停止扫描。
    //private static final long SCAN_PERIOD = 10000;

    @ViewById(R.id.recycler_view)
    RecyclerView recyclerView;
    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    //private Handler mHandler;

    private void init()//暂时代替onCreate
    {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

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
        init();
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
}

