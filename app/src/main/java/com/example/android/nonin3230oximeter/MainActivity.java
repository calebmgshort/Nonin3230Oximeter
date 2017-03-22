package com.example.android.nonin3230oximeter;

import android.bluetooth.BluetoothGatt;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 1;

    //private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    //private ArrayList<BTLE_Device> mBTDevicesArrayList;
    //private ListAdapter_BTLE_Devices adapter;

    private Button btn_Scan;
    private TextView oxi_disp;
    private TextView data_disp;

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private Scanner_BTLE mBTLeScanner;

    private BTLE_Device oximeter;

    private GATT_BTLE oximeterGatt;

    private byte[] oximeterData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make sure that the phone supports BLE
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Utils.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        mBTLeScanner = new Scanner_BTLE(this, 7500, -75);

        resetParameters();

        //mBTDevicesHashMap = new HashMap<>();
        //mBTDevicesArrayList = new ArrayList<>();

        //adapter = new ListAdapter_BTLE_Devices(this, R.layout.btle_device_list_item, mBTDevicesArrayList);

        //ListView listView = new ListView(this);
        //listView.setAdapter(adapter);
        //listView.setOnItemClickListener(this);
        //((ScrollView) findViewById(R.id.scrollView)).addView(listView);

        btn_Scan = (Button) findViewById(R.id.btn_scan);
        findViewById(R.id.btn_scan).setOnClickListener(this);

        oxi_disp = (TextView) findViewById(R.id.oxi_name);
        data_disp = (TextView) findViewById(R.id.oxi_data);

        startScan();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
        stopGatt();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBTStateUpdateReceiver);
        stopScan();
        stopGatt();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            }
            else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
    }

    /**
     * Called when an item in the ListView is clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Used in future BLE tutorials
    }

    /**
     * Called when the scan button is clicked.
     * @param v The view that was clicked
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_scan:
                Utils.toast(getApplicationContext(), "Scan Button Pressed");

                if(!mBTLeScanner.isScanning()){
                     startScan();
                }
                else{
                     stopScan();
                }

                break;
            default:
                break;
        }
    }

    /*
    public void addDevice(BluetoothDevice device, int new_rssi) {
        String address = device.getAddress();

        if(!mBTDevicesHashMap.containsKey(address)){
            BTLE_Device btle_device = new BTLE_Device(device);
            btle_device.setRSSI(new_rssi);

            mBTDevicesHashMap.put(address, btle_device);
            mBTDevicesArrayList.add(btle_device);
            Utils.toast(getApplicationContext(), "A device was added successfully");
        }
        else{
            mBTDevicesHashMap.get(address).setRSSI(new_rssi);
        }

        adapter.notifyDataSetChanged();
    }
    */

    // I copied the above function and tweaked it for just the Oximeter
    public void addDevice(BluetoothDevice device, int new_rssi) {
        String address = device.getAddress();

        if(oximeter == null){
            oximeter = new BTLE_Device(device);
            oximeter.setRSSI(new_rssi);

            oxi_disp.setText(oximeter.getName());

            //Utils.toast(getApplicationContext(), "A device was added successfully");

            stopScan();
            startGatt();
        }
        /*else{
            mBTDevicesHashMap.get(address).setRSSI(new_rssi);
        }

        adapter.notifyDataSetChanged();*/

    }

    public void startScan() {
        btn_Scan.setText("Scanning...");
        oxi_disp.setText("Oximeter not found");

        //mBTDevicesArrayList.clear();
        //mBTDevicesHashMap.clear();

        //adapter.notifyDataSetChanged();
        //oximeter.device.
        stopScan();
        stopGatt();
        resetParameters();
        //oxi_disp.setText("");

        mBTLeScanner.start();
    }

    public void stopScan() {
        btn_Scan.setText("Re-start Scan");
        if(oximeter == null)
            oxi_disp.setText("Oximeter not found");
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
            Utils.toast(getApplicationContext(), "Insufficient Data");
            return;
        }
        int heartRange = (data[8] << 8) | data[9];
        int spO = data[7];
        data_disp.setText("Pulse Rate: " + heartRange + "\n SpO2: " + data[7]);
        //Utils.toast(getApplicationContext(), "The updateData function was called");
    }

    public void clearData(){
        this.oximeterData = null;
        data_disp.setText("No data recieved at this time");
    }

    public void resetParameters(){
        oximeter = null;
        oximeterGatt = null;
        oximeterData = null;
    }
}
