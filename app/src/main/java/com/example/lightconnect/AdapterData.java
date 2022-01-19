package com.example.lightconnect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.util.Log;

import java.util.Set;

public class AdapterData {
    private static final String TAG ="Error" ;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;




    String[] addresses = new String[0];
    String[] devices = new String[0];


    public void loadAdapterData() {
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairedDevices = mBTAdapter.getBondedDevices();
        if (mBTAdapter.isEnabled()) {
            // put it's one to the adapter

            for (BluetoothDevice device : mPairedDevices) {
                addresses.toString().concat(device.getAddress());
                devices.toString().concat(device.getName());
            }
        }else{
            Log.e(TAG, "Could not find paired devices");
        }

    }

    public String[] Devices(){
        return devices;
    }
    public String[] addresses(){
        return addresses;
    }
}
