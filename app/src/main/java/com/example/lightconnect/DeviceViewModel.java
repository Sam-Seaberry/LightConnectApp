package com.example.lightconnect;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;


public class DeviceViewModel extends ViewModel {
    private final MutableLiveData<List<DeviceModel>> devices = new MutableLiveData<List<DeviceModel>>();
    private static ArrayList<DeviceModel> DeviceList = new ArrayList<DeviceModel>();

    public void addDevice(DeviceModel device){
        DeviceList.add(device);
        devices.setValue(DeviceList);
    }


    public LiveData<List<DeviceModel>> getDevice(){
        return devices;
    }
}
