package com.example.android.nonin3230oximeter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_ENABLE_BT = 1;

    public Button btn_Scan;
    public TextView oxi_disp;
    public TextView data_disp;

    Nonin3230Oximeter noninOximeter;

    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                Bundle bundle = msg.getData();
                String textToDisplay = "";
                textToDisplay = textToDisplay + "Pulse Rate:\t" + bundle.getInt(Nonin3230Oximeter.HEART_RATE) + "\n";
                textToDisplay = textToDisplay + "SpO2:\t" + bundle.getInt(Nonin3230Oximeter.SPO2_VALUE) + "\n";
                textToDisplay = textToDisplay + "Finger Inserted Properly:\t" +
                        bundle.getString(Nonin3230Oximeter.FINGER_INSTERTED_PROPERLY) + "\n";
                data_disp.setText(textToDisplay);
            }
        };

        noninOximeter = new Nonin3230Oximeter(this, mHandler);

        btn_Scan = (Button) findViewById(R.id.btn_scan);
        btn_Scan.setOnClickListener(this);

        oxi_disp = (TextView) findViewById(R.id.oxi_name);
        data_disp = (TextView) findViewById(R.id.oxi_data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noninOximeter.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noninOximeter.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
                if(!noninOximeter.isScanning()){
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

}
