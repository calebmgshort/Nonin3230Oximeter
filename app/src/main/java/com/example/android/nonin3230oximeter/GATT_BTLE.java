package com.example.android.nonin3230oximeter;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.List;

import static android.R.attr.data;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by Caleb on 3/3/17. This class encapsulates the handling of the GATT connection
 */

public class GATT_BTLE {

    private BTLE_Device oximeter;
    private BluetoothGatt oximeterGatt;
    private MainActivity ma;

    private final String OXIMETER_SERVICE_UUID = "46A970E0-0D5F-11E2-8B5E-0002A5D5C51B";
    private final String OXIMETER_CHARACTERISTIC_UUID = "0AAD7EA0-0D60-11E2-8E3C-0002A5D5C51B";

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // this will get called when a device connects or disconnects
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    //Utils.toast(ma.getApplicationContext(), "GATT connected successfully");
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
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            List<BluetoothGattService> services = gatt.getServices();
            displayGattServices(services);
            Log.i("onServicesDiscovered", services.toString());
            //Utils.toast(ma.getApplicationContext(), "Services discovered successfully");
            //gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String characteristicUUID = characteristic.getUuid().toString().toUpperCase();
            Log.i("onCharacteristicRead", characteristicUUID);
            byte[] data = characteristic.getValue();
            if(data == null)
                Log.i("onGettingData", "The data was null");
            else
                ma.updateData(data);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            String characteristicUUID = characteristic.getUuid().toString().toUpperCase();
            Log.i("onCharacteristicChanged", characteristicUUID);
            if(characteristicUUID.equals(OXIMETER_CHARACTERISTIC_UUID)) {
                gatt.readCharacteristic(characteristic);
            }
        }
    };

    public GATT_BTLE(MainActivity ma, BTLE_Device oximeter){
        this.oximeter = oximeter;
        this.ma = ma;
    }

    public void execute(){
        oximeterGatt = oximeter.device.connectGatt(ma.getApplicationContext(), true, btleGattCallback);

    }

    private void displayGattServices(List<BluetoothGattService> services){
        if(services == null)
            return;
        for (BluetoothGattService service : services) {
            String serviceUUID = service.getUuid().toString().toUpperCase();
            Log.i("Service UUID", serviceUUID);
            if(serviceUUID.equals(OXIMETER_SERVICE_UUID)) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUUID = characteristic.getUuid().toString().toUpperCase();
                    Log.i("Characteristic UUID", characteristicUUID);
                    if(characteristicUUID.equals(OXIMETER_CHARACTERISTIC_UUID)) {
                        oximeterGatt.readCharacteristic(characteristic);
                        /*
                        byte[] data = characteristic.getValue();
                        if(data == null)
                            Log.i("onGettingData", "The data was null");
                        else
                            ma.updateData(data);
                        break;
                        */
                    }
                    /*
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        //find descriptor UUID that matches Client Characteristic Configuration (0x2902)
                        // and then call setValue on that descriptor

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        oximeterGatt.writeDescriptor(descriptor);
                    }*/
                }
                break;
            }
        }
    }

    public void stop(){
        if(oximeterGatt != null) {
            oximeterGatt.disconnect();
            oximeterGatt.close();
        }
    }

}
