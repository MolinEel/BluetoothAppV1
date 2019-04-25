package com.china.superbox.bluetoothappv1;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2019/4/24.
 */

public class LeDeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater inflater;

    public LeDeviceListAdapter(LayoutInflater inflater) {
        this.mLeDevices = new ArrayList<>();
        this.inflater = inflater;
    }

    public void clear() {
        mLeDevices.clear();
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.itemview, null);
            holder.deviceName = view.findViewById(R.id.name);
            holder.deviceAddress = view.findViewById(R.id.address);
            holder.deviceUUID = view.findViewById(R.id.uuid);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        String name = mLeDevices.get(i).getName();
        if (name != null && name.length() > 0) {
            holder.deviceName.setText(name);
        } else {
            holder.deviceName.setText("UnKnown device");
        }
        holder.deviceAddress.setText(mLeDevices.get(i).getAddress());
        holder.deviceUUID.setText(mLeDevices.get(i).getUuids()+"");
        return view;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceUUID;
    }
}
