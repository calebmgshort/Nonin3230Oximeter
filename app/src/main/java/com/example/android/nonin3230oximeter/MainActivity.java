package com.example.android.nonin3230oximeter;

import android.bluetooth.BluetoothGatt;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import static android.R.attr.data;
import static android.text.TextUtils.concat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 1;

    public Button btn_Scan;
    public TextView oxi_disp;
    public TextView data_disp;

    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private Scanner_BTLE mBTLeScanner;

    private BTLE_Device oximeter;

    private GATT_BTLE oximeterGatt;

    private byte[] oximeterData;

    Nonin3230Oximeter noninOximeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            throw new Nonin3230Oximeter.BluetoothException("BlE not supported");
        }

        noninOximeter = new Nonin3230Oximeter(this);

        //mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getApplicationContext());
        //mBTLeScanner = new Scanner_BTLE(this, 7500, -75);

        //resetParameters();

        btn_Scan = (Button) findViewById(R.id.btn_scan);
        btn_Scan.setOnClickListener(this);

        oxi_disp = (TextView) findViewById(R.id.oxi_name);
        data_disp = (TextView) findViewById(R.id.oxi_data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noninOximeter.connect();

        //startScan();
        //registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        //startScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopEverything();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noninOximeter.disconnect();
        //unregisterReceiver(mBTStateUpdateReceiver);
        //stopEverything();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override       // TODO: figure out how to convert this
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_CANCELED)
                Log.i("BluetoothConnection", "Please turn on Bluetooth");
                //Utils.toast(getApplicationContext(), "Please turn on Bluetooth");
            else if (resultCode == RESULT_OK)
                Log.i("BluetoothConnection", "Bluetooth is connected");
            //else if (resultCode == RESULT_OK)
            //    Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
        }
    }

    /**
     * Called when an item in the ListView is clicked.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Unused
    }

    /**     TODO: Probably remove this
     * Called when the scan button is clicked.
     * @param v The view that was clicked
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_scan:
                Utils.toast(getApplicationContext(), "Scan Button Pressed");

                if(!mBTLeScanner.isScanning()){
                     noninOximeter.connect();
                }
                else{
                     noninOximeter.disconnect();
                }

                break;
            default:
                break;
        }
    }

    /*
    // I copied the above function and tweaked it for just the Oximeter
    public void addOximeter(BluetoothDevice device, int new_rssi) {
        String address = device.getAddress();
        if(oximeter == null){
            oximeter = new BTLE_Device(device);
            oximeter.setRSSI(new_rssi);

            oxi_disp.setText(oximeter.getName());

            stopScan();
            startGatt();
        }
    }

    private void startScan() {
        btn_Scan.setText("Scanning...");

        stopEverything();

        mBTLeScanner.start();
    }

    public void stopScan() {
        btn_Scan.setText("Re-start Scan");
        if(oximeter == null) {
            oxi_disp.setText("Oximeter not found");
            data_disp.setText("");
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
            data_disp.setText("Insufficient Data");
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

        data_disp.setText(textToDisplay);
        //data_disp.setText("Pulse Rate: " + heartRange + "\n SpO2: " + data[7]);
    }

    public void clearData(){
        this.oximeterData = null;
        data_disp.setText("No data recieved at this time");
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
        oxi_disp.setText("Oximeter not found");
        data_disp.setText("");
    }
    */
}
