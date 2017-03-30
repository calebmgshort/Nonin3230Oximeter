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

    private Nonin3230Oximeter parent;

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private long scanPeriod;
    private int signalStrength;

    public Scanner_BTLE(Nonin3230Oximeter parentClass, long scanPeriod, int signalStrength){
        parent = parentClass;

        mHandler = new Handler();

        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) parent.ma.getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isScanning(){
        return mScanning;
    }

    public void start(){
        if (mBluetoothAdapter == null){
            parent.stopScan();
            throw new Nonin3230Oximeter.BluetoothException("Bluetooth is not available.");
        }
        else if(!mBluetoothAdapter.isEnabled()){
            parent.stopScan();
            throw new Nonin3230Oximeter.BluetoothException("Bluetooth is not enabled.");
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
            mHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    parent.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback(){
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord){

                    final int new_rssi = rssi;
                    String deviceName = device.getName();
                    if(rssi > signalStrength && deviceName != null && deviceName.contains("Nonin3230")){
                        mHandler.post(new Runnable(){
                            @Override
                            public void run(){
                                parent.addOximeter(device, new_rssi);
                            }
                        });
                    }
                }
            };
}
