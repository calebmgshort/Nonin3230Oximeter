package com.example.android.nonin3230oximeter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

/**
 * Created by Caleb on 3/24/17.
 */

public class Nonin3230Oximeter {

    public MainActivity ma;
    public static final int REQUEST_ENABLE_BT = 1;

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private BTLE_Device oximeter;
    private Scanner_BTLE mBTLeScanner;
    private GATT_BTLE oximeterGatt;
    private byte[] oximeterData;

    public Nonin3230Oximeter(MainActivity ma){
        this.ma = ma;

        // Make sure that the phone supports BLE
        if(!ma.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            throw new BluetoothException("BlE not supported");
        }

        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(ma.getApplicationContext());
        mBTLeScanner = new Scanner_BTLE(this, 7500, -75);

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
        disconnect();
        startScan();
        ma.registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    // Allows another class to disconnect from the oximeter
    public void disconnect(){
        ma.unregisterReceiver(mBTStateUpdateReceiver);
        stopEverything();
    }

    // Allows another class to instantiate a Handler through which messages about the data will be sent
    public void registerMHandler(Handler mHandler){

    }

    // This function is used to instantiate the oximeter
    public void addOximeter(BluetoothDevice device, int new_rssi) {
        String address = device.getAddress();
        if(oximeter == null){
            oximeter = new BTLE_Device(device);
            oximeter.setRSSI(new_rssi);

            ma.oxi_disp.setText(oximeter.getName());

            stopScan();
            startGatt();
        }
    }

    private void startScan() {
        ma.btn_Scan.setText("Scanning...");

        stopEverything();

        mBTLeScanner.start();
    }

    public void stopScan() {
        ma.btn_Scan.setText("Re-start Scan");
        if(oximeter == null) {
            ma.oxi_disp.setText("Oximeter not found");
            ma.data_disp.setText("");
        }
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
        clearData();
    }

    public void updateData(byte[] data){
        this.oximeterData = data;
        int length = (int) data[1];
        if(length < 9) {
            ma.data_disp.setText("Insufficient Data");
            return;
        }

        String textToDisplay = "";

        int heartRange = (data[8] << 8) | data[9];
        if(heartRange == 511)
            textToDisplay = textToDisplay + "Pulse Rate data missing\n";
        else
            textToDisplay = textToDisplay + "Pulse Rate:\t" + heartRange + "\n";

        int spO = data[7];
        if(spO == 127)
            textToDisplay = textToDisplay + "SpO2 data missing\n";
        else
            textToDisplay = textToDisplay + "SpO2:\t" + data[7] + "\n";

        textToDisplay = textToDisplay + "\n";

        int correctCheck = (data[1] >> 4) & 1;
        if(correctCheck == 1)
            textToDisplay = textToDisplay + "Finger inserted properly\n";
        else
            textToDisplay = textToDisplay + "Slide finger further into device\n";

        int lowBattery = (data[1] >> 5) & 1;
        if(lowBattery == 1)
            textToDisplay = textToDisplay + "Batteries are low. Change batteries.\n";
        else
            textToDisplay = textToDisplay + "Battery status is good.\n";

        ma.data_disp.setText(textToDisplay);
    }

    public void clearData(){
        this.oximeterData = null;
        ma.data_disp.setText("No data recieved at this time");
    }

    private void resetParameters(){
        oximeter = null;
        oximeterGatt = null;
        oximeterData = null;
    }

    public void stopEverything(){
        stopScan();
        stopGatt();
        resetParameters();
        ma.oxi_disp.setText("Oximeter not found");
        ma.data_disp.setText("");
    }

}
