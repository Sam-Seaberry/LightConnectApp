package com.example.lightconnect;

import static android.content.ContentValues.TAG;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceListFragment extends Fragment{
    private static final int VISIBLE = 0;
    private ListView mListView;

    private String device;

    private DeviceAdapter mDeviceAdapter;

    CallBackDevices callBackDevices;

    Context mContext;

    private static LiveData<DeviceModel> mDevices;
    private DeviceViewModel mViewModel;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_list_adapter, container, false);
        mContext = container.getContext();

        //mViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);

        mListView = view.findViewById(R.id.listViewProduct);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listViewProduct_onItemClick(adapterView, view, i, l);
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(@Nullable View view, Bundle savedInstanceState){
        mViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
        mViewModel.getDevice().observe(this, new Observer<List<DeviceModel>>(){
            @Override
            public void onChanged(@Nullable List<DeviceModel> devices){
                if(devices != null){
                    mDeviceAdapter = new DeviceAdapter(mContext, devices);
                    mListView.setAdapter(mDeviceAdapter);
                }
                mDeviceAdapter.notifyDataSetChanged();
            }
        } );;
    }

    private void listViewProduct_onItemClick(@NonNull AdapterView<?> adapterView, View view, int i, long l) {
        DeviceModel mDevice= (DeviceModel) adapterView.getItemAtPosition(i); //device selected
        device = mDevice.getname();
        callBackDevices.notifyswitch();
        //Toast.makeText(getApplicationContext(), mDevice.getname(), Toast.LENGTH_LONG).show();
    }

    public void setCallBackDevices(CallBackDevices callBackDevices){
        if(!(callBackDevices==null)){
            this.callBackDevices = callBackDevices;
        }

    }

    public String getSelectedDevice(){
        return device;
    }

}
