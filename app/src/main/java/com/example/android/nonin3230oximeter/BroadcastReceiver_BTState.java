package com.example.android.nonin3230oximeter;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Kelvin on 4/18/16.            I copied this from the internet
 */
public class BroadcastReceiver_BTState extends BroadcastReceiver {

    Context activityContext;

    public BroadcastReceiver_BTState(Context activityContext) {
        this.activityContext = activityContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    //Log.i("BluetoothState", "Bluetooth is off");
                    throw new Nonin3230Oximeter.BluetoothException("Bluetooth is off");
                    //Utils.toast(activityContext, "Bluetooth is off");
                    //break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.i("BluetoothState", "Bluetooth is turning off...");
                    //Utils.toast(activityContext, "Bluetooth is turning off...");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.i("BluetoothState", "Bluetooth is on");
                    //Utils.toast(activityContext, "Bluetooth is on");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.i("BluetoothState", "Bluetooth is turning on...");
                    //Utils.toast(activityContext, "Bluetooth is turning on...");
                    break;
            }
        }
    }
}