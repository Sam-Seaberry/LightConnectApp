package com.example.lightconnect;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;



import java.util.Objects;
import java.util.Set;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
class BluetoothFrag extends Activity {

    private static final String TAG = "BluetoothFrag";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;



    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private Set<BluetoothDevice> mPairedDevices;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBTAdapter = null;

    private ArrayAdapter<String> mBTArrayAdapter;

    /**
     * Member object for the chat services
     */
    private BluetoothConnect mBTService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        // Get local Bluetooth adapter
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported

        if (mBTAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();

        }

    }



    @Override
    public void onStart() {
        super.onStart();
        if (mBTAdapter == null) {
            return;
        }
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBTAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mBTService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBTService != null) {
            mBTService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTService.getState() == BluetoothConnect.STATE_NONE) {
                // Start the Bluetooth chat services
                mBTService.start();
            }
        }
    }


    /**
     * Set up the UI and background operations for chat.
     */


    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBTAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBTService.getState() != BluetoothConnect.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(),"Not Connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBTService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);

        }
    }


    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case BluetoothConnect.Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnect.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(),"Connected to "+mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                            mBTArrayAdapter.clear();
                            break;
                        case BluetoothConnect.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(),"Connecting...", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothConnect.STATE_LISTEN:
                        case BluetoothConnect.STATE_NONE:
                            Toast.makeText(getApplicationContext(),"Not Connected ", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case BluetoothConnect.Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mBTArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case BluetoothConnect.Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mBTArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case BluetoothConnect.Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothConnect.Constants.DEVICE_NAME);

                    Toast.makeText(getApplicationContext(), "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                    break;
                case BluetoothConnect.Constants.MESSAGE_TOAST:

                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothConnect.Constants.TOAST),
                            Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };

    public void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mBTArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        mBTArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        Bundle extras = data.getExtras();
        if (extras == null) {
            return;
        }
        String address = extras.getString(ListDevices.EXTRA_DEVICE_ADDRESS);

        BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

        mBTService.connect(device, secure);
    }







}