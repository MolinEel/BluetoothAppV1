package com.china.superbox.bluetoothappv1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button controlBluetooth;
    private Button aboutUs;
    private Button findBluetooth;
    private Button updateVersion;
    private ListView mlistview;
    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mListViewAdapter;
    private BluetoothDevice mDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        //检查是否支持蓝牙
        checkBluetooth();
        //注册广播
        registerReceiver(mGattReceiver, makeFilter());

        //自动操作
        initAction();
    }

    private void initAction() {
        controlBluetooth();
    }


    private void initData() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mListViewAdapter = new LeDeviceListAdapter(getLayoutInflater());
        mlistview.setAdapter(mListViewAdapter);

        //设置item点击事件
        mlistview.setOnItemClickListener(this);
        controlBluetooth.setOnClickListener(this);
        aboutUs.setOnClickListener(this);
        findBluetooth.setOnClickListener(this);
        updateVersion.setOnClickListener(this);
    }

    private void initView() {
        controlBluetooth = findViewById(R.id.control_bluetooth);
        aboutUs = findViewById(R.id.aboutus);
        findBluetooth = findViewById(R.id.find_bluetooth);
        updateVersion = findViewById(R.id.update_version);
        mlistview = findViewById(R.id.listview);
    }

    /**
     * 检查是否支持蓝牙
     */
    private void checkBluetooth() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "您的设备不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is supported on the device", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //这里连接指定的蓝牙设备
        Toast.makeText(this, mListViewAdapter.getDevice(i).getAddress(), Toast.LENGTH_SHORT).show();
        //这个方法需要三个参数：一个Context对象，自动连接（boolean值,表示只要BLE设备可用是否自动连接它），和BluetoothGattCallback调用。
        mDevice = mListViewAdapter.getDevice(i);
//        mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);
//        Log.e(TAG, mBluetoothGatt.toString());
//        mBluetoothLeService.connect(mDevice.getAddress());

        //跳转到连接通信的页面
        jumpConnect(mDevice.getAddress());

    }

    /**
     * 跳转到连接通信页面
     */
    private void jumpConnect(String address){
        Intent intent = new Intent(this, CommunicateActivity.class);
//        intent.putExtra(CommunicateActivity.EXTRAS_DEVICE_NAME, mDevice.getName());
        intent.putExtra(CommunicateActivity.EXTRAS_DEVICE_ADDRESS, address);
        startActivity(intent);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.control_bluetooth:
                controlBluetooth();
                break;
            case R.id.find_bluetooth:
                findBluetooth();
                break;
            case R.id.update_version:
                Toast.makeText(this, "版本更新", Toast.LENGTH_SHORT).show();
                break;
            case R.id.aboutus:
                Toast.makeText(this, "关于我们", Toast.LENGTH_SHORT).show();
//                mBluetoothLeService.WriteValue("{\"ctype\":1,\"gui\":2,\"box\":10}");
                break;
            default:
                break;
        }
    }

    /**
     * 搜索蓝牙
     */
    private void findBluetooth() {
        //将原列表中的数据清除
        mListViewAdapter.clear();
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * 蓝牙开关
     */
    public void controlBluetooth() {
        //是否已经开启蓝牙
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.disable();
            Toast.makeText(this, "关闭蓝牙", Toast.LENGTH_SHORT).show();
        } else {
            mBluetoothAdapter.enable();
            findBluetooth();
            Toast.makeText(this, "打开蓝牙", Toast.LENGTH_SHORT).show();
        }
    }


    //蓝牙搜索广播
    private final BroadcastReceiver mGattReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED://蓝牙状态改变的广播
                    Log.e(TAG, "ACTION_STATE_CHANGED");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED://开始扫描的广播
                    Log.e(TAG, "ACTION_DISCOVERY_STARTED");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED://搜索完成的广播
                    Log.e(TAG, "ACTION_DISCOVERY_FINISHED");
                    mBluetoothAdapter.cancelDiscovery();
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED://状态改变
                    Log.e(TAG, "ACTION_BOND_STATE_CHANGED");
                    break;
                case BluetoothDevice.ACTION_FOUND://找到设备的广播
                    Log.e(TAG, "ACTION_FOUND");
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    //找到默认的设备
                    if(SPUtil.getString(MainActivity.this,CommunicateActivity.DEFAULT_DEVICE,"")!=null
                            &&!SPUtil.getString(MainActivity.this,CommunicateActivity.DEFAULT_DEVICE,"").trim().equals("")){
                        if(SPUtil.getString(MainActivity.this,CommunicateActivity.DEFAULT_DEVICE,"").equals(device.getAddress())){
                            mBluetoothAdapter.cancelDiscovery();
                            jumpConnect(device.getAddress());
                        }
                    }

                    mListViewAdapter.addDevice(device);
                    mListViewAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }

        }
    };

    /**
     * 蓝牙广播过滤器
     * 蓝牙状态改变
     * 找到设备
     * 搜索完成
     * 开始扫描
     * 状态改变
     *
     * @return
     */
    public IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙状态改变的广播
        filter.addAction(BluetoothDevice.ACTION_FOUND);//找到设备的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//搜索完成的广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始扫描的广播
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
        return filter;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattReceiver);
    }
}
