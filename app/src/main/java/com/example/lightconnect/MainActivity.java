package com.example.lightconnect;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


import carbon.widget.FrameLayout;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private final String TAG = MainActivity.class.getSimpleName();

    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

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


    //new instances
    private ImageButton mBLEButton;
    private Button mSaveButton;

    private Button mApplyButton;

    //sql instances
    private SQL_base mSQL;
    private FrameLayout mFrame;

    //for ble spinner
    private AdapterData mBLEData;
    private Spinner mSpinnerList;
    private BleAdapter mBLEAdapter;

    //BLE and other instances
    private Button mDiscoverBtn;
    private ListView mDevicesListView;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;


    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket=null; // bi-directional client-to-client data path

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //bluetooth connection checking
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

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

        mSpinnerList = findViewById(R.id.spinner1);
        mSQL = new SQL_base(MainActivity.this);

        mBLEButton = findViewById(R.id.simpleImageButton);
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


        mBLEData = new AdapterData();
        mBLEData.loadAdapterData();
        //bluetooth spinner and setup

        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        listPairedDevices();
        mBTArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //mBLEAdapter = new BleAdapter(this,mBLEData.devices,mBLEData.addresses);


        mSpinnerList.setAdapter(mBTArrayAdapter); // assign model to view



        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        //what happens on device selection from drop down
        mSpinnerList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if(!mBTAdapter.isEnabled()) {
                    Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                    return;
                }
                //mConnectedThread.cancel();
                String info = ((TextView) view).getText().toString();//reading device name from sql spinner
                if(info.isEmpty()){
                    Toast.makeText(getBaseContext(), "Bluetooth Device Not Selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String address = getBLEaddress(info);

                mConnectedThread = new ConnectedThread(mBTSocket,mHandler,mBTAdapter,address);


                mConnectedThread.BTconnect();


                if(mConnectedThread.isConnected()){
                    Toast.makeText(getBaseContext(), "Bluetooth Device Connected", Toast.LENGTH_SHORT).show();
                    mSpinnerList.setBackgroundResource(R.drawable.style_green_ble_icon);
                }else{
                    Toast.makeText(getBaseContext(), "Bluetooth Device Not Connected", Toast.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                listPairedDevices();
            }
        });


        //ontouchlistener for the colorwheel so users can select a colour if their choice from the color wheel image
        mColourWheel.setOnTouchListener((v, event) -> {
            if(event.getAction()== MotionEvent.ACTION_DOWN || event.getAction()== MotionEvent.ACTION_MOVE){
                bitmap = mColourWheel.getDrawingCache();
                int pixels = bitmap.getPixel((int)event.getX(), (int)event.getY());

                red = Color.red(pixels);
                green = Color.green(pixels);
                blue = Color.blue(pixels);

                setBarValue();

                mColorView.setBackgroundColor(Color.rgb(red,green,blue));
                mFrame.setElevationShadowColor(Color.rgb(red,green,blue));
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

        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            //mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {

            mApplyButton.setOnClickListener(v -> {
                String send = "1-"+red+"-2-"+green+"-3-"+blue+"-4-"+brightness;
                mConnectedThread.writeString(send);
            });


            mBLEButton.setOnClickListener(v->{
                if(!mBTAdapter.isEnabled()){
                    bluetoothOn();
                    mBLEButton.setBackgroundResource(R.drawable.style_ble_spinner1);
                }else{
                    bluetoothOff();
                    mBLEButton.setBackgroundResource(R.drawable.style_ble_spinner_red);

                }
            });

            //mSaveButton.setOnClickListener(v -> bluetoothOff());

            mSaveButton.setOnClickListener(v -> newpreset());


        }

    }



    private void setRGB(){

        mColorView.setBackgroundColor(Color.rgb(red,green,blue));
        mFrame.setElevationShadowColor(Color.rgb(red,green,blue));


    }

    private void bluetoothOn(){
        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();

        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                Toast.makeText(getApplicationContext(),"Bluetooth turned ON", Toast.LENGTH_SHORT).show();
                updateDeviceList();
            } else
                Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
        }
    }

    private void bluetoothOff(){
        mBTAdapter.disable(); // turn off
        //mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }
    private void updateDeviceList(){
        listPairedDevices();
        mSpinnerList.setAdapter(mBTArrayAdapter);
    }


    private void listPairedDevices(){
        mBTArrayAdapter.clear();
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                //mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.add(device.getName());
            Toast.makeText(getApplicationContext(), "Show Paired Devices", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();

    }
    private String getBLEaddress(String name){

        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                if(Objects.equals(device.getName(),name)){
                    return device.getAddress();
                }


        }
        return "fail";
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch(seekBar.getId()){
            case R.id.seekBar:
                red=progress;
                break;
            case R.id.seekBar2:
                green = progress;
                break;
            case R.id.seekBar3:
                blue = progress;
                break;
            case R.id.seekBar4:
                brightness = progress;
                break;


        }
        mColorView.setBackgroundColor(Color.rgb(red,green,blue));
        mFrame.setElevationShadowColor(Color.rgb(red,green,blue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    private void addpresettolist(String name, int r, int g, int b){
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
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String presetname = mInputName.getText().toString();
                addpresettolist(presetname,red, green, blue);

                mSQL.addNewPreset(presetname,red, green, blue,brightness);

                dialog.dismiss();
            }
        });

        ImageView btnclose = dialog.findViewById(R.id.close_btn);
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    //sets the seek bar values
    private void setBarValue(){
        mRed.setProgress(red);
        mGreen.setProgress(green);
        mBlue.setProgress(blue);
        mBrightness.setProgress(brightness);
    }


    //gets selected preset and extracts rgb values
    private void getSQLdata(String selected){
        SQL_base mSQL = new SQL_base(this);

        //query PRESET_COL for presets matching "selected"
        String queryString = "SELECT * FROM " + "presets" + " WHERE " + "preset" + " = ?";
        Cursor cursor = mSQL.getData(selected);
        cursor.moveToFirst();
        if(cursor!=null && cursor.getCount()>0){
            red = cursor.getInt(1);
            green = cursor.getInt(2);
            blue = cursor.getInt(3);
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
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, labels);

        mAdapter = new PresetsAdapter(this, labels);

        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data to spinner
        mPresets.setAdapter(dataAdapter);
    }



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))  {
                connected = true;
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                connected = false;
            }
        }
    };



}
