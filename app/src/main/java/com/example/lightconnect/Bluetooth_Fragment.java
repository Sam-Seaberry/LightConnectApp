package com.example.lightconnect;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.test.core.app.ApplicationProvider;

import com.annimon.stream.operator.IntArray;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


import kotlin.jvm.internal.markers.KMutableList;
import kotlin.jvm.internal.markers.KMutableListIterator;


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

    private int red,green,blue;

    private boolean isInUse= false;

    private static BluetoothGattCharacteristic RedCharacteristic;
    private static BluetoothGattCharacteristic GreenCharacteristic;
    private static BluetoothGattCharacteristic BlueCharacteristic;

    private RGBvalues mRGB;

    private boolean isLocationPermissionGranted= false;
    private boolean isScanning = false;
    private boolean isConnected = false;

    private ImageButton mBTButton ;
    private Button mApplyButton;
    private Spinner mBTSpinner;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner mScanner;
    private BluetoothGatt bluetoothGatt;

    private ArrayList<BluetoothGattCharacteristic> mGattCharacteristics;
    private ArrayList<BluetoothGattCharacteristic> mRGBCharacteristics;

    private ArrayAdapter<String> mBTArrayAdapter;
    private List<String> mDeviceList;
    private final Map<String, BluetoothDevice> devices = new HashMap<>();

    protected Context context;
    private LocalBroadcastManager mLocalBroadcastManager;




    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.ble_spinner_dropdown,container,false);

        mGattCharacteristics = new ArrayList<>();
        mRGBCharacteristics = new ArrayList<>();

        mRGB = new RGBvalues();

        mBTButton =view.findViewById(R.id.simpleImageButton);
        mApplyButton = view.findViewById(R.id.button3);
        mBTSpinner=view.findViewById(R.id.spinner1);

        mBTArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        mBTArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mBTSpinner.setAdapter(mBTArrayAdapter); // assign model to view

        mBTSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
        });


        return view;
    }


    private ScanSettings mScanSetting = new ScanSettings.Builder().setScanMode(
            ScanSettings.SCAN_MODE_LOW_LATENCY).build();

    private final ScanFilter mScanfilter = new ScanFilter.Builder().setServiceUuid(
            ParcelUuid.fromString("0000180a-0000-1000-8000-00805f9b34fb")
    ).build();



    @RequiresApi(21)
    private void initializeWithContext(Context context) {
        if (this.bluetoothAdapter == null || this.mLocalBroadcastManager == null) {
            BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(
                    Context.BLUETOOTH_SERVICE);
            if(bluetoothManager == null) {
                throw new NullPointerException("Cannot get BluetoothManager");
            } else {
                this.bluetoothAdapter = bluetoothManager.getAdapter();
                this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
            }
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothManager =
                (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();
        startBleScan();
    }



    @Override
    public void onResume() {
        super.onResume();
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
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
                    devices.put(address, device);
                    mDeviceList.add(name+"/"+address);

                }
            }
        };
    }


    private void startBleScan(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {

            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        else {
            mDeviceList = new ArrayList<>();
            mBTArrayAdapter.clear();
            mScanner.startScan(null, mScanSetting, scanCallback());
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
        else{


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
        return "false";
    }

    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(context,false,bluetoothGattCallback);
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address.");
            return false;
        }
        return true;
    }


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    isConnected= true;
                    bluetoothGatt.discoverServices();


                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close(); //connection failed
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
                characteristic.setValue(mRGB.tobyte(mRGB.getBLUE()));


            }else if(characteristic.getUuid().toString().equalsIgnoreCase(GreenCharacteristicUUID)){

                //characteristic.setWriteType(FORMAT_UINT8);
                characteristic.setValue(mRGB.tobyte(mRGB.getRED()));

            }else if(characteristic.getUuid().toString().equalsIgnoreCase(BlueCharacteristicUUID)) {

                //characteristic.setWriteType(FORMAT_UINT8);
                characteristic.setValue(mRGB.tobyte(mRGB.getGREEN()));

            }

            mRGBCharacteristics.remove(mRGBCharacteristics.get(mRGBCharacteristics.size() - 1));

            if (mRGBCharacteristics.size() > 0) {
                writecharacteristic();
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
        bluetoothGatt = null;
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

                mGattCharacteristics.add(RedCharacteristic);
                mGattCharacteristics.add(GreenCharacteristic);
                mGattCharacteristics.add(BlueCharacteristic);

                mRGBCharacteristics.add(RedCharacteristic);
                mRGBCharacteristics.add(GreenCharacteristic);
                mRGBCharacteristics.add(BlueCharacteristic);



                /*bluetoothGatt.setCharacteristicNotification(RedCharacteristic,true);
                bluetoothGatt.setCharacteristicNotification(GreenCharacteristic,true);
                bluetoothGatt.setCharacteristicNotification(BlueCharacteristic,true);*/
            }
        }

    }

    private void readcharacteristic(BluetoothGatt gatt)
    {
        gatt.readCharacteristic(mGattCharacteristics.get(mGattCharacteristics.size()-1));
    }

    public void writecharacteristic(){
        bluetoothGatt.writeCharacteristic(mRGBCharacteristics.get(mRGBCharacteristics.size()-1));
    }


}
