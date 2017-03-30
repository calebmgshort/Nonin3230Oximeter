package com.example.android.nonin3230oximeter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import android.os.Handler;

import java.util.UUID;

/**
 * Created by Caleb on 3/3/17. This class encapsulates the handling of the GATT connection
 */

public class GATT_BTLE {

    private BluetoothDevice oximeter;
    private BluetoothGatt oximeterGatt;
    private Nonin3230Oximeter parent;
    private Handler mHandler;

    private static final UUID OXIMETER_SERVICE_UUID = UUID.fromString("46A970E0-0D5F-11E2-8B5E-0002A5D5C51B");
    private static final UUID OXIMETER_CHARACTERISTIC_UUID = UUID.fromString("0AAD7EA0-0D60-11E2-8E3C-0002A5D5C51B");

    private final BluetoothGattCallback oximeterGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // this will get called when a device connects or disconnects
            if (status == BluetoothGatt.GATT_SUCCESS)
                Log.i("onConnectionStateChange", "GATT_SUCCESS");
            else
                Log.i("onConnectionStateChange", "GATT_FAILURE");
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.i("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("onServicesDiscovered", "" + status);
            BluetoothGattCharacteristic characteristic = gatt.getService(OXIMETER_SERVICE_UUID).getCharacteristic(OXIMETER_CHARACTERISTIC_UUID);
            Log.i("characteristic found", characteristic.getUuid().toString());
            gatt.setCharacteristicNotification(characteristic, true);
            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            String characteristicUUID = characteristic.getUuid().toString().toUpperCase();
            Log.i("onCharacteristicChanged", characteristicUUID);
            final byte[] data = characteristic.getValue();
            if(data == null)
                Log.i("onGettingData", "The data was null");
            else {
                mHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        parent.updateData(data);
                    }
                });
            }
        }
    };

    public GATT_BTLE(Nonin3230Oximeter parent, BluetoothDevice oximeter){
        this.oximeter = oximeter;
        this.parent = parent;
        this.mHandler = new Handler();
    }

    public void execute(){
        oximeterGatt = oximeter.connectGatt(parent.ma.getApplicationContext(), true, oximeterGattCallback);
    }

    public void stop(){
        if(oximeterGatt != null) {
            oximeterGatt.disconnect();
            oximeterGatt.close();
        }
    }

}
