package com.example.lightconnect;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.Application;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


import carbon.widget.FrameLayout;


public class MainActivity extends AppCompatActivity implements CallBackFragment{



    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private RGBvalues mRGB;


    Bluetooth_Fragment mBLEFragment;
    ColorPickerFragment mColorFragment;

    private Handler mHandler;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fragmented);

        getSupportActionBar().hide();


        mBLEFragment = (Bluetooth_Fragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        mColorFragment = (ColorPickerFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView2);

        mColorFragment.setCallBackFragment(this);

        mRGB = new RGBvalues();


        mHandler = new Handler(Looper.getMainLooper()){
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    Toast.makeText(getApplicationContext(),readMessage, Toast.LENGTH_SHORT).show();
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        Toast.makeText(getApplicationContext(),"Connected To Device", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(),"Connection Failed", Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void notifyupdate(){
        assert mBLEFragment != null;
        mBLEFragment.writecharacteristic(mRGB.getRED(), mRGB.getGREEN(), mRGB.getBLUE());
    }




}