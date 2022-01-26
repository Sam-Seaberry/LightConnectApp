package com.example.lightconnect;

import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConnectedThread extends Thread {
    private static final String TAG = "ERROR_LIGHT_CONNECT";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Handler handler;
    private BluetoothSocket mSocket;
    private ConnectedThread mConnectedThread;
    private BluetoothDevice mmDevice;
    private boolean connected;
    private BluetoothAdapter mAdapter;


    private final InputStream mInStream;
    private final OutputStream mOutStream;
    private StringBuffer mBuffer;
    private final Handler mHandler;


    public ConnectedThread(BluetoothSocket socket, Handler handler, BluetoothAdapter adapter, String address) {
        mSocket = socket;
        mHandler = handler;
        mAdapter = adapter;
        mmDevice = getDevice(address);

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        mBuffer = new StringBuffer();
        BluetoothSocket tmp = null;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);



        // Get the input and output streams, using temp objects because
        // member streams are final

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(BT_MODULE_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mSocket = tmp;

        try {
            tmpIn = mSocket.getInputStream();
            tmpOut = mSocket.getOutputStream();
        } catch (IOException e) { }
        mInStream = tmpIn;
        mOutStream = tmpOut;


    }


    /* Call this from the main activity to send data to the remote device */
    public void writeString(String input) {
        byte[] bytes = input.getBytes();           //converts entered String into bytes
        try {
            if(mSocket.isConnected()){
                mOutStream.write(bytes);
            }

        } catch (IOException e) { }
    }
    public void writebytes(byte[] bytes) {
        try {
            for(int i = 0;i< bytes.length;i++){
                mOutStream.write(bytes[i]);
                sleep(200);
            }
            //mmOutStream.write(bytes);

            } catch (IOException | InterruptedException e) {
            Log.e(TAG, "Error occurred when sending data", e);

        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) { }

    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (!connected) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }


    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (!connected) {

            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            write(send);

            // Reset out string buffer to zero and clear the edit text field
            mBuffer.setLength(0);

        }
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

    private BluetoothDevice getDevice(String address){
        return mAdapter.getRemoteDevice(address);
    }


    public void Connect(){
        Thread thread = new Thread(runnable);
        thread.start();
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    public void BTconnect(){
        mAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }


    }
    public boolean isConnected(){
        return mSocket.isConnected();
    }


    Runnable runnable = new Runnable(){
        public void run() {
            mAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }//some code here
        }
    };


}