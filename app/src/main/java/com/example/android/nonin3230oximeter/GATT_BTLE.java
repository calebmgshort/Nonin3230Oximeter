package com.example.android.nonin3230oximeter;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.List;

/**
 * Created by Caleb on 3/3/17. This class encapsulates the handling of the GATT connection
 */

public class GATT_BTLE {

    private BTLE_Device oximeter;
    private BluetoothGatt oximeterGatt;
    private MainActivity ma;

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            byte[] data = characteristic.getValue();
            ma.updateData(data);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            // this will get called when a device connects or disconnects
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            gatt.readCharacteristic(services.get(1).getCharacteristics().get
                    (0));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            //gatt.disconnect();
        }
    };

    public GATT_BTLE(MainActivity ma, BTLE_Device oximeter){
        this.oximeter = oximeter;
        this.ma = ma;
    }

    public void execute(){
        oximeterGatt = oximeter.device.connectGatt(ma.getApplicationContext(), false, btleGattCallback);
        oximeterGatt.discoverServices();
        List<BluetoothGattService> services = oximeterGatt.getServices();
        displayGattServices(services);
        //stop();
        Utils.toast(ma.getApplicationContext(), "The  execute function in GATT_BTLE ran successfully");
    }

    private void displayGattServices(List<BluetoothGattService> services){
        if(services == null)
            return;
        for (BluetoothGattService service : services) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    //find descriptor UUID that matches Client Characteristic Configuration (0x2902)
                    // and then call setValue on that descriptor

                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    oximeterGatt.writeDescriptor(descriptor);
                }
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
