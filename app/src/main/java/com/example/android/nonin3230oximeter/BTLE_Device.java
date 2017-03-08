package com.example.android.nonin3230oximeter;


import android.bluetooth.BluetoothDevice;

/**
 * Created by Kelvin on 5/8/16.         I got this off the internet and tweaked it a bit.
 */


public class BTLE_Device {

    public BluetoothDevice device;     // Public is bad form but I can't extend BluetoothDevice
    private int rssi;

    public BTLE_Device(BluetoothDevice bluetoothDevice) {
        this.device = bluetoothDevice;
    }

    public String getAddress() {
        return device.getAddress();
    }

    public String getName() {
        return device.getName();
    }

    public void setRSSI(int rssi) {
        this.rssi = rssi;
    }

    public int getRSSI() {
        return rssi;
    }
}
