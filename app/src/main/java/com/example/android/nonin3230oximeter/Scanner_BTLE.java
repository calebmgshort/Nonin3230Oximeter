package com.example.android.nonin3230oximeter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;

/**
 * Created by Caleb on 2/25/17. Copied originally from the internet but then changed for this project
 */

public class Scanner_BTLE {

    private MainActivity ma;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private long scanPeriod;
    private int signalStrength;

    public Scanner_BTLE(MainActivity mainActivity, long scanPeriod, int signalStrength){
        ma = mainActivity;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) ma.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isScanning(){
        return mScanning;
    }

    public void start(){
        if(!Utils.checkBluetooth(mBluetoothAdapter)){
            Utils.requestUserBluetooth(ma);
            ma.stopScan();
            Utils.toast(ma.getApplicationContext(), "Scan did not start because of mBluetoothAdapter");
        }
        else{
            scanLeDevice(true);
        }
    }

    public void stop(){
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable){
        if(enable && !mScanning) {
            Utils.toast(ma.getApplication(), "Starting BLE scan...");

            mHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    Utils.toast(ma.getApplicationContext(), "Stopping BLE scan...");

                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    ma.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            //Utils.toast(ma.getApplicationContext(), "mBluetoothAdapter.startLeScan(mLeScanCallback) ran");
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback(){
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord){

                    final int new_rssi = rssi;
                    String deviceName = device.getName();
                    //Utils.toast(ma.getApplicationContext(), deviceName);
                    if(rssi > signalStrength && deviceName != null && deviceName.contains("Nonin3230")){
                    //if(rssi > signalStrength){
                        mHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                ma.addDevice(device, new_rssi);
                                //Utils.toast(ma.getApplicationContext(), "A device was added successfully");
                            }
                        });
                    }
                }
            };
}
