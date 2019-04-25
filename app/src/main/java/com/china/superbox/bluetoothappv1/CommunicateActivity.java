package com.china.superbox.bluetoothappv1;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by Administrator on 2019/4/25.
 */

public class CommunicateActivity extends Activity {

    private static final String TAG = CommunicateActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static final String DEFAULT_DEVICE = "default_device";

    private EditText etCype;
    private EditText mEt1;
    private EditText etGui;
    private EditText etBox;

    private BluetoothLeService mBluetoothLeService;

    private BluetoothGatt mBluetoothGatt;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            Log.e(TAG, "mBluetoothLeService is okay");
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private String mDeviceName;
    private String mDeviceAddress;
    private TextView tvMsg;
    private TextView tvRecv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);
        initView();

        final Intent intent = getIntent();
//        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        //注册广播
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        //连接蓝牙设备
//        mBluetoothLeService.connect(mDeviceAddress);
    }

    private void initView() {
        tvMsg = findViewById(R.id.tv_msg);
        tvRecv = findViewById(R.id.tv_recv);
        mEt1 = findViewById(R.id.et1);
        etCype = findViewById(R.id.et_cype);
        etGui = findViewById(R.id.et_gui);
        etBox = findViewById(R.id.et_box);
    }

    public void sendData01(View view) {
        //判断et1是否为空
        if (mEt1.getText() == null || mEt1.getText().toString().length() < 1) {
            Toast.makeText(this, "请输入正确的指令", Toast.LENGTH_SHORT).show();
            return;
        }

        sendDataToBlue(mEt1.getText().toString());

        Toast.makeText(this, "成功发送数据--" + mEt1.getText().toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 发送数据到蓝牙
     *
     * @param strValue 发送的数据内容
     */
    private void sendDataToBlue(String strValue) {
        //发送数据
        mBluetoothLeService.WriteValue(strValue);
    }

    public void sendData02(View view) {
        //判断et是否为空
        if (etCype.getText() == null || etCype.getText().toString().length() < 1) {
            Toast.makeText(this, "请输入正确的指令", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etGui.getText() == null || etGui.getText().toString().length() < 1) {
            Toast.makeText(this, "请输入正确的柜号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etBox.getText() == null || etBox.getText().toString().length() < 1) {
            Toast.makeText(this, "请输入正确的箱号", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "成功发送数据--" + etCype.getText().toString() + "--" +
                etGui.getText().toString() + "--" + etBox.getText().toString() + "--", Toast.LENGTH_SHORT).show();

        //发送数据
        sendDataToBlue(etCype.getText().toString() + "--" +
                etGui.getText().toString() + "--" + etBox.getText().toString());
    }

    //蓝牙搜索广播
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothLeService.ACTION_GATT_CONNECTED://已连接
                    Log.e(TAG, "ACTION_GATT_CONNECTED");
                    tvMsg.setText("正在连接" + mDeviceAddress);
                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED://断开连接
                    Log.e(TAG, "ACTION_GATT_DISCONNECTED");
                    tvMsg.setText(mDeviceAddress + "连接已经断开");
                    Toast.makeText(context, "连接已经断开", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED://发现服务
                    Log.e(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                    tvMsg.setText(mDeviceAddress + "连接成功，可以发送信息了");

//                    mBluetoothLeService.WriteValue("0x10086");
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE://数据可用
                    Log.e(TAG, "ACTION_DATA_AVAILABLE");
                    String recv = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    tvRecv.setText("收到的数据为：" + recv);
                    break;
                case BluetoothDevice.ACTION_UUID://uuid
                    Log.e(TAG, "ACTION_UUID");
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 已连接
     * 断开连接
     * 发现服务
     * 数据可用
     * uuid
     *
     * @return
     */
    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeService != null) {
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * 设置默认的连接设备
     *
     * @param view
     */
    public void setDefault(View view) {
        SPUtil.put(this, DEFAULT_DEVICE, mDeviceAddress);
    }
}
