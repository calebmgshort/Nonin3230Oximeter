package com.example.android.nonin3230oximeter;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import android.os.Handler;

import java.util.List;
import java.util.UUID;

import static android.R.attr.data;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by Caleb on 3/3/17. This class encapsulates the handling of the GATT connection
 */

public class GATT_BTLE {

    private BTLE_Device oximeter;
    private BluetoothGatt oximeterGatt;
    private MainActivity ma;
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
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", "" + status);
            BluetoothGattCharacteristic characteristic = gatt.getService(OXIMETER_SERVICE_UUID).getCharacteristic(OXIMETER_CHARACTERISTIC_UUID);
            Log.i("characteristic found", characteristic.getUuid().toString());
            gatt.setCharacteristicNotification(characteristic, true);
            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
            boolean val = gatt.readCharacteristic(characteristic);
            if(val == false){
                Log.i("onReadCharacteristic", "Read characteristic failed");
            }
            //findCharacteristic(services);

            //Utils.toast(ma.getApplicationContext(), "Services discovered successfully");
            //gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String characteristicUUID = characteristic.getUuid().toString().toUpperCase();
            Log.i("onCharacteristicRead", characteristicUUID);
            final byte[] data = characteristic.getValue();
            if(data == null)
                Log.i("onGettingData", "The data was null");
            else {
                Log.i("onGettingData", "The data was not null");
                mHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        ma.updateData(data);
                        //Utils.toast(ma.getApplicationContext(), "A device was added successfully");
                    }
                });
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
                Log.i("onGettingData", "The data was not null");
                mHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        ma.updateData(data);
                        //Utils.toast(ma.getApplicationContext(), "A device was added successfully");
                    }
                });
            }
        }
    };

    public GATT_BTLE(MainActivity ma, BTLE_Device oximeter){
        this.oximeter = oximeter;
        this.ma = ma;
        this.mHandler = new Handler();
    }

    public void execute(){
        oximeterGatt = oximeter.device.connectGatt(ma.getApplicationContext(), true, oximeterGattCallback);

    }

    private void findCharacteristic(List<BluetoothGattService> services){
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
                        //oximeterGatt.readCharacteristic(characteristic);
                        /*
                        byte[] data = characteristic.getValue();
                        if(data == null)
                            Log.i("onGettingData", "The data was null");
                        else
                            ma.updateData(data);
                        */


                        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                            //find descriptor UUID that matches Client Characteristic Configuration (0x2902)
                            // and then call setValue on that descriptor
                            //if(descriptor.)
                            byte[] value = descriptor.getValue();
                            if(value != null)
                                Log.i("Descriptor value", value.toString());
                            else
                                Log.i("Descriptor value", "null");
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            oximeterGatt.writeDescriptor(descriptor);
                        }
                        break;
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
