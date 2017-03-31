package com.example.android.nonin3230oximeter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Caleb on 3/24/17.
 */

public class Nonin3230Oximeter {

    public MainActivity ma;
    public static final int REQUEST_ENABLE_BT = 1;

    public static final String HEART_RATE = "Heart Rate";
    public static final String SPO2_VALUE = "SPO2 Value";
    public static final String FINGER_INSTERTED_PROPERLY = "Finger Inserted Properly";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private BluetoothDevice oximeter;
    private Scanner_BTLE mBTLeScanner;
    private GATT_BTLE oximeterGatt;
    private Handler dataHandler;

    public Nonin3230Oximeter(MainActivity ma, Handler handler){
        this.ma = ma;
        this.dataHandler = handler;

        // Make sure that the phone supports BLE
        if(!ma.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            throw new BluetoothException("BlE not supported");
        }

        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(ma.getApplicationContext());
        mBTLeScanner = new Scanner_BTLE(this, 7500, -75);

        resetParameters();
    }

    // Force the class using Nonin3230Oximeter to handle Bluetooth Problems and such with a custom
    // exception class
    public static class BluetoothException extends RuntimeException{
        public BluetoothException(){
            super();
        }
        public BluetoothException(String message){
            super(message);
        }
    }

    // Allows another class to connect to the oximeter
    public void connect(){
        stopEverything();
        startScan();
        ma.registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    // Allows another class to disconnect from the oximeter
    public void disconnect(){
        ma.unregisterReceiver(mBTStateUpdateReceiver);
        stopEverything();
    }

    // This function is used to instantiate the oximeter
    public void addOximeter(BluetoothDevice device) {
        if(oximeter == null){
            oximeter = device;
            stopScan();
            startGatt();
        }
    }

    private void startScan() {
        stopEverything();

        mBTLeScanner.start();
    }

    public void stopScan() {
        mBTLeScanner.stop();
    }

    private void startGatt(){
        oximeterGatt = new GATT_BTLE(this, oximeter);
        oximeterGatt.execute();
    }

    private void stopGatt(){
        if(oximeterGatt != null) {
            oximeterGatt.stop();
        }
    }

    public void updateData(byte[] data){
        // Get message and bundle
        Message message = Message.obtain();
        Bundle bundle = new Bundle();

        int length = (int) data[1];
        if(length < 9) {
            bundle.putInt(HEART_RATE, -1);
            bundle.putInt(SPO2_VALUE, -1);
            bundle.putString(FINGER_INSTERTED_PROPERLY, FALSE);
            message.setData(bundle);
            dataHandler.sendMessage(message);
            return;
        }

        // Send message for heart rate
        int heartRange = (data[8] << 8) | data[9];
        if(heartRange == 511)
            bundle.putInt(HEART_RATE, -1);
        else
            bundle.putInt(HEART_RATE, heartRange);

        // Send message for spO2 value
        int spO = data[7];
        if(spO == 127)
            bundle.putInt(SPO2_VALUE, -1);
        else
            bundle.putInt(SPO2_VALUE, spO);

        // Send message for whether the finger is inserted properly
        int correctCheck = (data[1] >> 4) & 1;
        if(correctCheck == 1)
            bundle.putString(FINGER_INSTERTED_PROPERLY, TRUE);
        else
            bundle.putString(FINGER_INSTERTED_PROPERLY, FALSE);

        message.setData(bundle);
        dataHandler.sendMessage(message);
    }

    private void resetParameters(){
        oximeter = null;
        oximeterGatt = null;
    }

    public void stopEverything(){
        stopScan();
        stopGatt();
        resetParameters();
    }

    // Temperary function
    public boolean isScanning(){
        if(mBTLeScanner == null)
            return false;
        return mBTLeScanner.isScanning();
    }

}
