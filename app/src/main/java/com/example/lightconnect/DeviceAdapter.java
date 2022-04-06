package com.example.lightconnect;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
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

import carbon.widget.CheckBox;

public class DeviceAdapter extends ArrayAdapter<DeviceModel> {
    private Context context;
    private List<DeviceModel> mDeviceList;
    private DeviceModel device;

    private RGBvalues2 mRGB;

    private DeviceViewModel mViewModel;

    private boolean checked = false;

    private TextView mName;
    private SeekBar mBrightness;
    private View mColors;
    private Switch mConnected;


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

        mRGB = new RGBvalues2();

        device = getItem(position);


        mName = (TextView) convertView.findViewById(R.id.BLEname);
        mColors = (View) convertView.findViewById(R.id.colorblock);
        mBrightness = (SeekBar) convertView.findViewById(R.id.seekBar7);
        mConnected = (Switch) convertView.findViewById(R.id.switch2);

        mBrightness.setMax(255);



        if(mDeviceList !=null && mDeviceList.size()>0){
            DeviceModel mDeviceLists = mDeviceList.get(position);
            mName.setText(mDeviceLists.getname());
            mColors.setBackgroundColor(mDeviceLists.getViewColor());

            mRGB.setRED(Color.red(mDeviceLists.getViewColor()));
            mRGB.setGREEN(Color.green(mDeviceLists.getViewColor()));
            mRGB.setBLUE(Color.blue(mDeviceLists.getViewColor()));

            mBrightness.setProgress(mDeviceLists.getBarValue());

            mRGB.setBRIGHT(mDeviceLists.getBarValue());

            mConnected.setChecked(mDeviceLists.getSwitchValue());
        }else{
            mName.setText("No Devices");
            mColors.setBackgroundColor(Color.rgb(0,0,0));
            mBrightness.setProgress(0);
            mConnected.setChecked(false);
        }

        mBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRGB.setBRIGHT(progress);

                //notifyadapter(device.getname(),progress,checked);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mConnected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mRGB.setConnect(true);
                    checked = true;
                    //notifyadapter(device.getname(),progress,checked);
                } else {
                    mRGB.setConnect(false);
                    checked = false;
                    //notifyadapter(device.getname(),progress,checked);
                }
            }
        });



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

    public void changecolor(int red, int blue, int green){


        mRGB.setALL(red,blue,green);
        if(mColors != null ){
            mColors.setBackgroundColor(Color.rgb(red,blue,green));
        }
    }


}
