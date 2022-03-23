package com.example.lightconnect;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends ArrayAdapter<DeviceModel> {
    private Context context;
    private List<DeviceModel> mDeviceList;
    private DeviceModel device;

    private DeviceViewModel mViewModel;


    // Constructor
    public DeviceAdapter(Context context, List<DeviceModel> mDeviceList) {
        super(context, R.layout.device_list_adapter,mDeviceList);
        this.context = context;
        this.mDeviceList = mDeviceList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

       /* ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView= LayoutInflater.from(context).inflate(R.layout.device_list_adapter, parent, false);
            ViewHolder.mBLEname = convertView.findViewById(R.id.BLEname);
            viewHolder.mColorView = convertView.findViewById(R.id.colorblock);
            ViewHolder.mBright = convertView.findViewById(R.id.seekBar7);
            viewHolder.mConnect = convertView.findViewById(R.id.switch2);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        DeviceModel mDeviceLists = mDeviceList.get(position);
        ViewHolder.mBLEname.setText(mDeviceLists.getname());
        viewHolder.mColorView.setBackgroundColor(mDeviceLists.getViewColor());
        viewHolder.mBright.setProgress(mDeviceLists.getBarValue());
        viewHolder.mConnect.setChecked(mDeviceLists.getSwitchValue());*/


        return initView(position, convertView, parent);
    }


    public View initView(int position,  View convertView, ViewGroup parent) {
        //ViewHolder viewHolder;
        if(convertView==null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_list_fragment, parent, false );

        }

        device = getItem(position); //devices not linking to adapter 


        TextView mName = (TextView) convertView.findViewById(R.id.BLEname);
        View mColors = (View) convertView.findViewById(R.id.colorblock);
        SeekBar mBrightness = (SeekBar) convertView.findViewById(R.id.seekBar7);
        Switch mConnected = (Switch) convertView.findViewById(R.id.switch2);



        if(mDeviceList !=null && mDeviceList.size()>0){
            DeviceModel mDeviceLists = mDeviceList.get(position);
            mName.setText(mDeviceLists.getname());
            mColors.setBackgroundColor(mDeviceLists.getViewColor());
            mBrightness.setProgress(mDeviceLists.getBarValue());
            mConnected.setChecked(mDeviceLists.getSwitchValue());
        }else{
            mName.setText("No Devices");
            mColors.setBackgroundColor(Color.rgb(0,0,0));
            mBrightness.setProgress(0);
            mConnected.setChecked(false);
        }

        return convertView;
    }




    public static class ViewHolder {
        public static View mColorView;
        public static TextView mBLEname;
        public static SeekBar mBright;
        public static Switch mConnect;

        /*public ViewHolder(@NonNull View itemView) {
           super(itemView);
            mColorView = itemView.findViewById(R.id.colorblock);
            mBLEname = itemView.findViewById(R.id.BLEname);
            mBright = itemView.findViewById(R.id.seekBar7);
            mConnect = itemView.findViewById(R.id.switch2);
        }*/
    }


}
