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


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{



    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private boolean connected;
    // GUI Components

    // For color selector
    private ImageView mColourWheel;
    private View mColorView;
    Bitmap bitmap;
    private int mColorVal;
    private int red,blue,green,brightness;
    private SeekBar mRed, mGreen, mBlue, mBrightness;

    //for presets
    private Spinner mPresets;
    private ArrayList<presets_items> mPresetList;
    private PresetsAdapter mAdapter;

    private RGBvalues mRGB;
    //new instances
    private Button mSaveButton;
    private Button mApplyButton;

    //sql instances
    private SQL_base mSQL;
    private FrameLayout mFrame;

    private Handler mHandler;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRGB = new RGBvalues();

        red = mRGB.getRED();
        blue = mRGB.getBLUE();
        green = mRGB.getGREEN();

        Bluetooth_Fragment fragment = (Bluetooth_Fragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);

        //colorwheel
        mColourWheel = findViewById(R.id.imageView);
        mColorView = findViewById(R.id.display_colors);
        mColourWheel.setDrawingCacheEnabled(true);
        mColourWheel.buildDrawingCache(true);

        //seekbars
        mRed = findViewById(R.id.seekBar);
        mGreen = findViewById(R.id.seekBar2);
        mBlue = findViewById(R.id.seekBar3);
        mBrightness = findViewById(R.id.seekBar4);

        mFrame = findViewById(R.id.framelay);

        //mSpinnerList = findViewById(R.id.spinner1);
        mSQL = new SQL_base(MainActivity.this);


        mSaveButton = findViewById(R.id.button4);
        mApplyButton = findViewById(R.id.button3);

        //setting up preset spinner dropdown
        mPresetList = new ArrayList<>();
        mPresets = findViewById(R.id.spinner2);


        //loading data to spinner on create
        loadSpinnerData();

        mPresets.setAdapter(mAdapter);


        // Presets dropdown menu selection listener
        mPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String label="";

                try {
                    label = parent.getItemAtPosition(position).toString();
                } catch (Exception e) {
                    System.out.println("Error " + e.getMessage());

                }
                Toast.makeText(parent.getContext(), "You selected: " + label, Toast.LENGTH_LONG).show();

                getSQLdata(label);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //colourwheel listener so users can select RGB colour option from image
        mColourWheel.setOnTouchListener((v, event) -> {
            if(event.getAction()== MotionEvent.ACTION_DOWN || event.getAction()== MotionEvent.ACTION_MOVE){
                bitmap = mColourWheel.getDrawingCache();
                int pixels = bitmap.getPixel((int)event.getX(), (int)event.getY());

                mRGB.setALL(Color.red(pixels), Color.green(pixels), Color.blue(pixels));

                setBarValue();

                mColorView.setBackgroundColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));
                mFrame.setElevationShadowColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));
            }
            return true;
        });


        //seekbar listeners
        mRed.setOnSeekBarChangeListener(this);
        mGreen.setOnSeekBarChangeListener(this);
        mBlue.setOnSeekBarChangeListener(this);
        mBrightness.setOnSeekBarChangeListener(this);


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



        mApplyButton.setOnClickListener(v -> {
            assert fragment != null;
            fragment.writecharacteristic();
        });




            //mSaveButton.setOnClickListener(v -> bluetoothOff());

        mSaveButton.setOnClickListener(v -> newpreset());




    }



    private void setRGB(){
        //setting the colour preview
        mColorView.setBackgroundColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));
        mFrame.setElevationShadowColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));


    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(seekBar.getId()){
            case R.id.seekBar:
                mRGB.setRED(progress);
                break;
            case R.id.seekBar2:
                mRGB.setGREEN(progress);
                break;
            case R.id.seekBar3:
                mRGB.setBLUE(progress);
                break;
            case R.id.seekBar4:
                brightness = progress;
                break;


        }
        mColorView.setBackgroundColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));
        mFrame.setElevationShadowColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    private void addpresettolist(String name, int r, int g, int b){ //saves colour preset to SQLite
        mPresetList.add(new presets_items(name, r, g, b));

    }

    //for opening save preset dialog
    private void newpreset(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.preset_dialog);



        EditText mInputName = dialog.findViewById(R.id.textinput);
        String presetname = mInputName.getText().toString();


        Button savebtn = dialog.findViewById(R.id.savebtn);
        Log.v("EditText", mInputName.getText().toString());
        savebtn.setOnClickListener(v -> {
            String presetname1 = mInputName.getText().toString();
            addpresettolist(presetname1,mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE());

            mSQL.addNewPreset(presetname1,mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE(),brightness);

            dialog.dismiss();
        });

        ImageView btnclose = dialog.findViewById(R.id.close_btn);
        btnclose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
        loadSpinnerData();

    }

    //sets the seek bar values
    private void setBarValue(){
        mRed.setProgress(mRGB.getRED());
        mGreen.setProgress(mRGB.getGREEN());
        mBlue.setProgress(mRGB.getBLUE());
        mBrightness.setProgress(brightness);
    }


    //gets selected preset and extracts rgb values
    private void getSQLdata(String selected){
        SQL_base mSQL = new SQL_base(this);

        //query PRESET_COL for presets matching "selected"
        String queryString = "SELECT * FROM " + "presets" + " WHERE " + "preset" + " = ?";
        Cursor cursor = mSQL.getData(selected);
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            mRGB.setRED(cursor.getInt(1));
            mRGB.setGREEN(cursor.getInt(2));
            mRGB.setBLUE(cursor.getInt(3));
            brightness = cursor.getInt(4);
        }
        setRGB();
        setBarValue();


    }


    // updating and loading spinner data
    private void loadSpinnerData() {
        SQL_base db = new SQL_base(getApplicationContext());
        List<String> labels = db.getPresetLabels();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);

        mAdapter = new PresetsAdapter(this, labels);

        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data to spinner
        mPresets.setAdapter(dataAdapter);
    }




}