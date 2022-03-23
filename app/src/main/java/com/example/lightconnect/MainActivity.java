package com.example.lightconnect;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.nio.charset.StandardCharsets;



public class MainActivity extends AppCompatActivity implements CallBackFragment, FragmentChangeListener, CallBackDevices{



    // #defines for identifying shared types between calling functions

    public final static int MESSAGE_READ = 7; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private final static int DEVICE_FRAG_CODE = 1;
    private final static int COLOR_FRAG_CODE = 2;

    private RGBvalues mRGB;

    private Button mColorButton;
    private Button mDeviceButton;

    private FragmentTransaction mFragmentTrans;

    private DeviceModel mDeviceList;


    private Bluetooth_Fragment mBLEFragment;
    private ColorPickerFragment mColorFragment;
    private DeviceListFragment mDeviceFragment;
    private FragmentManager mFragmentManager;
    private CallBackFragment mCallback;
    private CallBackDevices mCallbackDevices;

    private Handler mHandler;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fragmented);

        getSupportActionBar().hide();

        mColorButton = findViewById(R.id.color_picker_button);
        mDeviceButton = findViewById(R.id.device_frag_button);

        mDeviceList = new DeviceModel();

        mCallbackDevices = this;
        mCallback = this;

        setup();

        mDeviceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDeviceFragment = new DeviceListFragment();
                FragmentChangeListener fc=(FragmentChangeListener)MainActivity.this;
                fc.replaceFragment(mDeviceFragment,"DeviceFragment");
                mDeviceFragment.setCallBackDevices(mCallbackDevices);
                //ChangeFrags(DEVICE_FRAG_CODE);

            }
        });

        mColorButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                mColorFragment = new ColorPickerFragment();
                FragmentChangeListener fc=(FragmentChangeListener)MainActivity.this;
                fc.replaceFragment(mColorFragment,"ColorFragment");
                mColorFragment.setCallBackFragment(mCallback);
                //ChangeFrags(COLOR_FRAG_CODE);

            }
        });

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
        mBLEFragment.writecharacteristic(mRGB.getRED(), mRGB.getGREEN(), mRGB.getBLUE(), mRGB.getBRIGHT());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void notifyswitch() {
        assert mBLEFragment !=null;
        mBLEFragment.switchDevices(mDeviceFragment.getSelectedDevice());
    }


    @Override
    public void replaceFragment(Fragment fragment, String id) {
        mFragmentTrans = getSupportFragmentManager().beginTransaction();
        mFragmentTrans.replace(R.id.fragmentContainerView2,fragment,id);
        mFragmentTrans.addToBackStack(id);
        mFragmentTrans.commit();

    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setup(){
        mBLEFragment = new Bluetooth_Fragment();
        mFragmentTrans = getSupportFragmentManager().beginTransaction();
        mFragmentTrans.replace(R.id.fragmentContainerView,mBLEFragment,"BLEFragment");
        mFragmentTrans.addToBackStack("BLEFragment");
        mFragmentTrans.commit();

        mColorFragment = new ColorPickerFragment();
        mFragmentTrans = getSupportFragmentManager().beginTransaction();
        mFragmentTrans.replace(R.id.fragmentContainerView2,mColorFragment,"ColorsFragment");
        mFragmentTrans.addToBackStack("ColorsFragment");
        mFragmentTrans.commit();

        mDeviceFragment = new DeviceListFragment();
        mFragmentTrans = getSupportFragmentManager().beginTransaction();
        mFragmentTrans.replace(R.id.fragmentContainerView2,mDeviceFragment,"DevicesFragment");
        mDeviceFragment.setCallBackDevices(mCallbackDevices);
        mFragmentTrans.addToBackStack("DevicesFragment");
        mFragmentTrans.commit();
    }



}