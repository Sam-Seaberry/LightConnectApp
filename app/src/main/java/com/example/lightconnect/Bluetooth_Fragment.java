package com.example.lightconnect;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RequiresApi(api = Build.VERSION_CODES.N)
public class Bluetooth_Fragment extends Fragment {
    private static final String TAG = "BluetoothFrag";

    private final static int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 4;

    private static final String BleServiceUUID = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String RedCharacteristicUUID = "00000001-0000-1000-8000-00805f9b34fb";
    private static final String GreenCharacteristicUUID = "00000002-0000-1000-8000-00805f9b34fb";
    private static final String BlueCharacteristicUUID = "00000003-0000-1000-8000-00805f9b34fb";
    private static final String AlphaCharacteristicUUID = "00000004-0000-1000-8000-00805f9b34fb";

    private int mRED, mGREEN, mBLUE, mBRIGHT;

    private boolean isInUse= false;

    private static BluetoothGattCharacteristic RedCharacteristic;
    private static BluetoothGattCharacteristic GreenCharacteristic;
    private static BluetoothGattCharacteristic BlueCharacteristic;
    private static BluetoothGattCharacteristic AlphaCharacteristic;

    private RGBvalues mRGB;

    private boolean isLocationPermissionGranted;
    private boolean isScanning;
    private static boolean isConnected;

    private Button mBTButton ;
    private Spinner mBTSpinner;

    private ProgressBar mProgress;

    private float centerX;
    private float centerY;

    private ImageView mThumb;
    private View mColorView;
    private View mReference;
    private View mNoDevices;
    private TextView mDeviceName;
    private int progress = 0;
    private boolean touching = false;


    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothManager bluetoothManager;
    private static BluetoothLeScanner mScanner;
    private static BluetoothGatt bluetoothGatt;

    private static ArrayList<BluetoothGattCharacteristic> mGattCharacteristics;
    private static ArrayList<BluetoothGattCharacteristic> mRGBCharacteristics;

    private ArrayAdapter<String> mBTArrayAdapter;
    private List<String> mDeviceList;
    private final Map<String, BluetoothDevice> devices = new HashMap<>();

    protected Context context;
    private LocalBroadcastManager mLocalBroadcastManager;

    private DeviceModel mModel;
    ArrayList<DeviceModel> mDeviceModelList;

    private DeviceViewModel mViewModel;



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.ble_fragment,container,false);

        mModel = new DeviceModel();

        mGattCharacteristics = new ArrayList<>();
        mRGBCharacteristics = new ArrayList<>();

        mDeviceModelList = new ArrayList<>();

        context = getContext();

        mRGB = new RGBvalues();

        mProgress = view.findViewById(R.id.progress_bar);
        mColorView = view.findViewById(R.id.color_view);
        mNoDevices = view.findViewById(R.id.no_device_view);
        mDeviceName = view.findViewById(R.id.devicename);

        mThumb = view.findViewById(R.id.imageView);
        mThumb.setRotation((float)(-50*0.705)); //init the thumb to bottom of the progressbar

        centerX= mThumb.getWidth()/2+mThumb.getX();
        centerY= mThumb.getHeight()/2+mThumb.getY(); //finding center of the progressbar

        mReference = view.findViewById(R.id.referenceView);

        mBTButton = view.findViewById(R.id.add_device_button);
        mBTSpinner =view.findViewById(R.id.add_device_spinner);//ble spinner

        mBTArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        mBTArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBTSpinner.setAdapter(mBTArrayAdapter);
         // assign model to view
        mBTSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //this could be turned into a button
            boolean initalized = false;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!initalized ){initalized =true; ConnectAll(); return;}
                    if(isScanning){
                        stopBleScan();

                    }

                    String fail = "false";
                    String info = ((TextView) view).getText().toString();
                    if(getAddress(info).equals(fail)){
                        return;
                    }

                    String address = getAddress(info);


                    connect(address);



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(!isScanning){
                    startBleScan();

                }
            }
        });

        mBTButton.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                    if(!isScanning){
                        devices.clear();
                        startBleScan();
                    }
                    isScanning = false;
                    mBTSpinner.performClick();

                }
        });



       /* mBTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isScanning){
                    //bluetoothOn();
                    devices.clear();
                    startBleScan();
                    mBTButton.setBackgroundResource(R.drawable.style_ble_spinner1);

                }else{
                    //bluetoothOff();
                    stopBleScan();
                    mBTButton.setBackgroundResource(R.drawable.style_ble_spinner_red);

                }
            }
        });*/
        mReference.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(centerX == 0 || centerY==0){
                    centerX= mThumb.getWidth()/2+mThumb.getX();
                    centerY= mThumb.getHeight()/2+mThumb.getY();
                }

                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    if(!(event.getX()<centerX && event.getY()>(centerY*1.6))) { //clicks under the progressbar are ignored
                        getprogressvalue(event.getX(), event.getY());
                    }


                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    touching = false;
                }else if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(!(event.getX()<centerX && event.getY()>centerY)) { //clicks under the progressbar are ignored
                        getprogressvalue(event.getX(), event.getY());
                    }

                }
                return true;


            }
        });

        return view;
    }




    private ScanSettings mScanSetting = new ScanSettings.Builder().setScanMode(
            ScanSettings.SCAN_MODE_LOW_LATENCY).build();

    private final ScanFilter mScanfilter = new ScanFilter.Builder().setServiceUuid(
            ParcelUuid.fromString("0000180a-0000-1000-8000-00805f9b34fb")
    ).build();

    @Override
    public void onViewCreated(@Nullable View view, Bundle savedInstanceState){
        mViewModel = new ViewModelProvider(requireActivity()).get(DeviceViewModel.class);
    }

    @Override
    public void onStart() {

        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        setup();

        startBleScan();



    }

    private void setup(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothManager =
                (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    @RequiresApi(21)
    private void initializeWithContext(Context context) {
        if (this.bluetoothAdapter == null || this.mLocalBroadcastManager == null) {
            BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(
                    Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null) {
                throw new NullPointerException("Cannot get BluetoothManager");
            } else {
                this.bluetoothAdapter = bluetoothManager.getAdapter();
               // mScanner = this.bluetoothAdapter.getBluetoothLeScanner();
                this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
            }
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        initializeWithContext(context);
        if (!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
        }
    }

    private void promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, ENABLE_BLUETOOTH_REQUEST_CODE);
            // Otherwise, setup the chat session
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ENABLE_BLUETOOTH_REQUEST_CODE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    promptEnableBluetooth();
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {

                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                }
        }
    }

    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            isLocationPermissionGranted = true;
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            isLocationPermissionGranted = true;
                        } else {
                            isLocationPermissionGranted = false;
                        }
                    }
            );

    public ActivityResultLauncher<String[]> getLocationPermissionRequest() {
        return locationPermissionRequest;
    }

    public void setLocationPermissionRequest(ActivityResultLauncher<String[]> locationPermissionRequest) {
        this.locationPermissionRequest = locationPermissionRequest;
    }
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    isLocationPermissionGranted = true;
                } else {

                    new Thread(new Runnable() {
                        public void run() {
                            ActivityCompat.requestPermissions(
                                    (Activity) context,
                                    new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.BLUETOOTH,
                                            android.Manifest.permission.BLUETOOTH_ADMIN,
                                    },
                                    LOCATION_PERMISSION_REQUEST_CODE
                            );
                            }
                        }).start();
                }
            });
    private ScanCallback scanCallback() {
        return new ScanCallback() {

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                //super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();

                if (devices.values().contains(device)) {
                    return;
                }
                if (result == null || result.getDevice() == null || device.getName()== null) {
                    // no devices found


                    return;
                }else{
                    String address = device.getAddress();
                    String name = device.getName();
                    mBTArrayAdapter.add(name);
                    mBTArrayAdapter.notifyDataSetChanged();
                    devices.put(address, device);
                    mDeviceList.add(name+"/"+address);

                }

            }
        };
    }


    private void startBleScan(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {

            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }else{
            mDeviceList = new ArrayList<>();
            mBTArrayAdapter.clear();
            mScanner.startScan(Collections.singletonList(mScanfilter), mScanSetting, scanCallback());//set filters to above to only find device with specified BLE services
            mBTArrayAdapter.notifyDataSetChanged();
            isScanning = true;
        }
    }
    private void stopBleScan() {
        mScanner.stopScan(scanCallback());
        isScanning = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    startBleScan();
                }

        }
    }

    private void bluetoothOn(){
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        }

    }

    private void bluetoothOff(){
        bluetoothAdapter.disable(); // turn off
        //mBluetoothStatus.setText("Bluetooth disabled");

    }

    private String getAddress(String name){
        for(int i=0;i< mDeviceList.size();i++){
          String[] inter = mDeviceList.get(i).split("/");
            if(inter[0].equals(name)){
              return inter[1];
          }
        }
        return null;
    }

    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(context,false,bluetoothGattCallback); //change to true to auto connect

            //setdevice(device);


        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address.");
            return false;
        }
        return true;
    }

    private void ConnectAll(){
        if(!(mDeviceList==null)){
            for(int i=0;i<mDeviceList.size();i++){
                String[] inter = mDeviceList.get(i).split("/");
                String address = inter[1];

                try {
                    final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    //bluetoothGatt = device.connectGatt(context,false,bluetoothGattCallback);
                    setdevice(device);

                } catch (IllegalArgumentException exception) {
                }
            }
        }else{

        }


    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    isConnected= true;
                    bluetoothGatt.discoverServices();
                    setupFrag(bluetoothGatt.getDevice());


                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close(); //connection failed
                    noDevices();
                    isConnected = false;
                }
            }else{
                gatt.close(); //gatt failed
                isConnected = false;
            }

        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt,int status){
            FindGattCharacteristics(getSupportedGattServices());
            bluetoothGatt = gatt;
            readcharacteristic(gatt);

        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status){

            if(characteristic.getUuid().toString().equalsIgnoreCase(RedCharacteristicUUID)){

                mRGB.setRED(characteristic.getIntValue(FORMAT_UINT8,0));


            }else if(characteristic.getUuid().toString().equalsIgnoreCase(GreenCharacteristicUUID)){

                mRGB.setGREEN(characteristic.getIntValue(FORMAT_UINT8,0));


            }else if(characteristic.getUuid().toString().equalsIgnoreCase(BlueCharacteristicUUID)) {

                mRGB.setBLUE(characteristic.getIntValue(FORMAT_UINT8,0));

            }else if(characteristic.getUuid().toString().equalsIgnoreCase(AlphaCharacteristicUUID)) {

                mRGB.setBRIGHT(characteristic.getIntValue(FORMAT_UINT8,0));

            }

            mGattCharacteristics.remove(mGattCharacteristics.get(mGattCharacteristics.size() - 1));

            if (mGattCharacteristics.size() > 0) {
                readcharacteristic(gatt);
            }


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            if(characteristic.getUuid().toString().equalsIgnoreCase(RedCharacteristicUUID)){

               // characteristic.setWriteType(FORMAT_UINT8);
                characteristic.setValue(mRGB.tobyte(mBRIGHT));


            }else if(characteristic.getUuid().toString().equalsIgnoreCase(GreenCharacteristicUUID)){

                //characteristic.setWriteType(FORMAT_UINT8);
                characteristic.setValue(mRGB.tobyte(mRED));

            }else if(characteristic.getUuid().toString().equalsIgnoreCase(BlueCharacteristicUUID)) {

                //characteristic.setWriteType(FORMAT_UINT8);
                characteristic.setValue(mRGB.tobyte(mGREEN));

            }else if(characteristic.getUuid().toString().equalsIgnoreCase(AlphaCharacteristicUUID)){
                characteristic.setValue(mRGB.tobyte(mBLUE));
            }

            mRGBCharacteristics.remove(mRGBCharacteristics.get(mRGBCharacteristics.size() - 1));

            if (mRGBCharacteristics.size() > 0) {
                writecharacteristic(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE(),mRGB.getBRIGHT());
            }else{
                FindGattCharacteristics(getSupportedGattServices());
            }
        }

    };


    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        mGattCharacteristics.clear(); //cleared so that next device can be written and read from
        mRGBCharacteristics.clear();
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;

        return bluetoothGatt.getServices();
    }


    private void FindGattCharacteristics(List<BluetoothGattService> service){
        for (int i = 0; i < service.size(); i++) {
            BluetoothGattService gattService = service.get(i);
            if(gattService.getUuid().toString().equalsIgnoreCase(BleServiceUUID)) {

                RedCharacteristic = gattService.getCharacteristic(UUID.fromString(RedCharacteristicUUID));
                GreenCharacteristic = gattService.getCharacteristic(UUID.fromString(GreenCharacteristicUUID));
                BlueCharacteristic = gattService.getCharacteristic(UUID.fromString(BlueCharacteristicUUID));
                AlphaCharacteristic = gattService.getCharacteristic(UUID.fromString(AlphaCharacteristicUUID));

                mGattCharacteristics.add(RedCharacteristic);
                mGattCharacteristics.add(GreenCharacteristic);
                mGattCharacteristics.add(BlueCharacteristic);
                mGattCharacteristics.add(AlphaCharacteristic);

                mRGBCharacteristics.add(RedCharacteristic);
                mRGBCharacteristics.add(GreenCharacteristic);
                mRGBCharacteristics.add(BlueCharacteristic);
                mRGBCharacteristics.add(AlphaCharacteristic);




            }
        }

    }

    public void readcharacteristic(BluetoothGatt gatt)
    {
        gatt.readCharacteristic(mGattCharacteristics.get(mGattCharacteristics.size()-1));
    }

    public void writecharacteristic(int red, int green, int blue, int bright){

        mRED = red;
        mGREEN = green;
        mBLUE = blue;
        mBRIGHT = bright;
        if(mRGBCharacteristics.size()>0){
            bluetoothGatt.writeCharacteristic(mRGBCharacteristics.get(mRGBCharacteristics.size()-1));
        }
        if(mColorView!=null){
            mColorView.setBackgroundColor(Color.rgb(mRED,mGREEN,mBLUE));
        }




    }

    private void updateprogressbar(int value) {
        mProgress.setProgress(value);
        setAngle((float)value);
    }

    private void getprogressvalue(float touchx, float touchy){

        double angle = Math.atan((touchy-(centerY))/(touchx-centerX));
        double total = (angle*(180/Math.PI)*1.4);
        if(touchx > centerX){
            angle = angle+1.5;
            total = (angle*(180/Math.PI)*1.4)+195;
            if(total<375) {
                mProgress.setProgress((int)total);
                setAngle((float)total);
                mRGB.setBRIGHT(((int)total)/2);
                writecharacteristic(mRGB.getRED(), mRGB.getGREEN(), mRGB.getBLUE(), mRGB.getBRIGHT());
            }


        }else{

            mProgress.setProgress((int)total+65);
            setAngle((float)total+65);
            mRGB.setBRIGHT(((int)total+70)/2);
            writecharacteristic(mRGB.getRED(), mRGB.getGREEN(), mRGB.getBLUE(), mRGB.getBRIGHT());
        }



    }

    private void setAngle(float value){
        double i = (value-65)*0.72; //offset by 65 as the progressbar starts at 225 degrees and below inital thumb position
        if(i<225 && i>-45) {
            mThumb.setRotation((float) i);

        }
    }

    private void noDevices(){
        mDeviceName.setText("No Device Connected");
        mNoDevices.setVisibility(View.VISIBLE);
    }

    private void setupFrag(BluetoothDevice device){

        new Thread() {
            public void run() {

                try {
                    ((Activity)context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            String name = device.getName();
                            mDeviceName.setText(name);

                            updateprogressbar(mRGB.getBRIGHT());

                            mColorView.setBackgroundColor(Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE()));

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }
        }.start();





    }

    private static boolean isConnected(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
            boolean connected = (boolean) m.invoke(device, (Object[]) null);
            return connected;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void setdevice(BluetoothDevice device){
        String name  = device.getName();
        int colour = Color.rgb(mRGB.getRED(),mRGB.getGREEN(),mRGB.getBLUE());
        int bright = mRGB.getBRIGHT();
        boolean connected = isConnected(device);

        DeviceModel model = new DeviceModel(name,colour,bright,connected);
        mViewModel.addDevice(model);

    }

    public void switchDevices(String deviceName){
        close();
        setup();
        String address = getAddress(deviceName);
        connect(address);
    }





}


