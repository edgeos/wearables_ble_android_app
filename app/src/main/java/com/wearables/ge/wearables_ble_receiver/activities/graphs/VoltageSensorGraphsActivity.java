/*
 * Copyright (c) 2017 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.wearables.ge.wearables_ble_receiver.activities.graphs;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainActivity;
import com.wearables.ge.wearables_ble_receiver.services.BluetoothService;
import com.wearables.ge.wearables_ble_receiver.utils.BLEQueue;
import com.wearables.ge.wearables_ble_receiver.utils.GattAttributes;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageAlarmStateChar;

import java.util.UUID;

public class VoltageSensorGraphsActivity extends AppCompatActivity {
    public static String TAG = "Voltage sensor graphs";

    public static String deviceName = MainActivity.deviceName;
    public BluetoothDevice connectedDevice = MainActivity.connectedDevice;

    private LineGraphSeries<DataPoint> series1;
    private LineGraphSeries<DataPoint> series2;
    private LineGraphSeries<DataPoint> series3;
    GraphView graph1;
    GraphView graph2;
    GraphView graph3;
    private int lastX = 0;

    BluetoothService mService;
    boolean mBound = false;

    boolean shouldDisconnect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voltage_sensor_graphs);
        //create custom toolbar
        Toolbar myToolbar = findViewById(R.id.display_message_toolbar);
        myToolbar.setTitle(deviceName);
        //extra logic for back button
        setSupportActionBar(myToolbar);
        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        //bind this activity to bluetooth service
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // get graph view instance
        graph1 = findViewById(R.id.voltage_sensor_graph_1);
        graph1.setTitle(getString(R.string.voltage_channel_one_graph_title));
        Viewport viewport1 = graph1.getViewport();
        viewport1.setYAxisBoundsManual(true);
        viewport1.setXAxisBoundsManual(true);
        viewport1.setMinY(0);
        viewport1.setScrollable(true);
        viewport1.setScrollableY(true);
        viewport1.setScalable(true);
        viewport1.setScalableY(true);
        GridLabelRenderer gridLabel1 = graph1.getGridLabelRenderer();
        gridLabel1.setHorizontalAxisTitle(getString(R.string.voltage_channel_graph_x_axis_label));
        gridLabel1.setVerticalAxisTitle(getString(R.string.voltage_channel_graph_y_axis_label));

        // second graph
        graph2 = findViewById(R.id.voltage_sensor_graph_2);
        graph2.setTitle(getString(R.string.voltage_channel_two_graph_title));
        Viewport viewport2 = graph2.getViewport();
        viewport2.setYAxisBoundsManual(true);
        viewport2.setXAxisBoundsManual(true);
        viewport2.setMinY(0);
        viewport2.setScrollable(true);
        viewport2.setScrollableY(true);
        viewport2.setScalable(true);
        viewport2.setScalableY(true);
        GridLabelRenderer gridLabel2 = graph2.getGridLabelRenderer();
        gridLabel2.setHorizontalAxisTitle(getString(R.string.voltage_channel_graph_x_axis_label));
        gridLabel2.setVerticalAxisTitle(getString(R.string.voltage_channel_graph_y_axis_label));

        // third graph
        graph3 = findViewById(R.id.voltage_sensor_graph_3);
        graph3.setTitle(getString(R.string.voltage_channel_three_graph_title));
        Viewport viewport3 = graph3.getViewport();
        viewport3.setYAxisBoundsManual(true);
        viewport3.setXAxisBoundsManual(true);
        viewport3.setMinY(0);
        viewport3.setScrollable(true);
        viewport3.setScrollableY(true);
        viewport3.setScalable(true);
        viewport3.setScalableY(true);
        GridLabelRenderer gridLabel3 = graph3.getGridLabelRenderer();
        gridLabel3.setHorizontalAxisTitle(getString(R.string.voltage_channel_graph_x_axis_label));
        gridLabel3.setVerticalAxisTitle(getString(R.string.voltage_channel_graph_y_axis_label));
    }

    @Override
    public void onPause() {
        //action for pressing the back button (or any time we leave this activity)
        super.onPause();
        Log.d(TAG, "Activity paused");
        //Some activities may not want to disconnect the device.
        if(shouldDisconnect){
            try {
                Log.d(TAG,"Disconnecting bluetooth device");
                mService.disconnectGattServer();
            } catch (Exception e){
                Log.d(TAG, "Couldn't disconnect bluetooth device: " + e.getMessage());
            }
        }
        Log.d(TAG, "Unregistering update receiver and unbinding service");
        unregisterReceiver(mGattUpdateReceiver);
        unbindBluetoothService();
    }

    public void unbindBluetoothService(){
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        //Menu menu1 = menu;
        return true;
    }

    //switch case logic for menu button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_rate:
                Log.d(TAG, "update_rate button pushed");
                //action for update_rate click
                return true;
            case R.id.device_id:
                Log.d(TAG, "device_id button pushed");
                //action for device_id
                return true;
            case R.id.rename:
                Log.d(TAG, "rename button pushed");
                //action for rename
                return true;
            case R.id.disconnect:
                //action for disconnect
                Log.d(TAG, "Disconnect button pushed");
                shouldDisconnect = true;

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.dev_mode:
                Log.d(TAG, "dev_mode button pushed");
                //dev mode is not a new activity, just a change to the UI
                //devMode = !devMode;
                //switchModes();
                return true;
            default:
                Log.d(TAG, "No menu item found for " + item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }

    //connection callback for bluetooth service
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "attempting bind to bluetooth service");
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mService = binder.getService();

            //register the broadcast receiver
            registerReceiver(mGattUpdateReceiver, createIntentFilter());

            //connect the bluetooth device
            if(BluetoothService.connectedGatt == null){
                mService.connectDevice(connectedDevice);
            }

            Log.d(TAG, "Bluetooth service bound successfully");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "Bluetooth service disconnected");
            mBound = false;
        }
    };

    //create custom intent filter for broadcasting messages from the bluetooth service to this activity
    private static IntentFilter createIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    //this method handles broadcasts sent from the bluetooth service
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action != null){
                switch(action){
                    case BluetoothService.ACTION_GATT_SERVICES_DISCOVERED:
                        Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED broadcast received");
                        mService.setNotifyOnCharacteristics();
                        break;
                    case BluetoothService.ACTION_DATA_AVAILABLE:
                        int extraType = intent.getIntExtra(BluetoothService.EXTRA_TYPE, -1);
                        if(extraType == BLEQueue.ITEM_TYPE_READ){
                            readAvailableData(intent);
                        }
                        break;
                }
            }
        }
    };

    public void readAvailableData(Intent intent){
        UUID extraUuid = UUID.fromString(intent.getStringExtra(BluetoothService.EXTRA_UUID));
        byte[] extraData = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);

        if(extraData == null){
            Log.d(TAG, "No message parsed on characteristic.");
            return;
        }
        String value = null;
        try {
            final StringBuilder stringBuilder = new StringBuilder(extraData.length);
            for(byte byteChar : extraData){
                stringBuilder.append(String.format("%02x ", byteChar));
            }
            //TODO: send this data to AWS for storage
            value = stringBuilder.toString();
        } catch (Exception e) {
            Log.e(TAG, "Unable to convert message bytes to string" + e.getMessage());
        }

        if(extraUuid.equals(GattAttributes.VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID)){
            Log.d(TAG, "VOLTAGE_ALARM_STATE value: " + value);
            if(value != null){
                VoltageAlarmStateChar voltageAlarmState = new VoltageAlarmStateChar(value);
                updateGraph(voltageAlarmState);
            } else {
                Log.d(TAG, "Parsed message value was null");
            }
        }

    }

    public int y1;
    public int y2;
    public int y3;
    public int increment;
    public int maxYvalue = 0;
    public int maxXvalue;

    protected void updateGraph(VoltageAlarmStateChar voltageAlarmState) {
        super.onResume();

        int maxXvalueCheck = ((voltageAlarmState.getNum_fft_bins() - 1) * voltageAlarmState.getFft_bin_size()) + 20;

        series1 = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
        series3 = new LineGraphSeries<>();

        increment = voltageAlarmState.getFft_bin_size();

        for (int i = 0; i < voltageAlarmState.getNum_fft_bins(); i++) {
            y1 = voltageAlarmState.getCh1_fft_results().get(i);
            y2 = voltageAlarmState.getCh2_fft_results().get(i);
            y3 = voltageAlarmState.getCh3_fft_results().get(i);
            if(y1 > maxYvalue){
                roundMaxY(y1);
            }
            if(y2 > maxYvalue){
                roundMaxY(y2);
            }
            if(y3 > maxYvalue){
                roundMaxY(y3);
            }
            addEntry();
        }
        if(maxXvalueCheck != maxXvalue){
            maxXvalue = maxXvalueCheck;
            updateBounds();
        }
        addGraphSeries();
    }

    private void addEntry() {
        lastX = lastX + increment;
        series1.appendData(new DataPoint(lastX, y1), false, 70);
        series2.appendData(new DataPoint(lastX, y2), false, 70);
        series3.appendData(new DataPoint(lastX, y3), false, 70);
    }

    private void addGraphSeries(){
        graph1.removeAllSeries();
        graph2.removeAllSeries();
        graph3.removeAllSeries();

        graph1.addSeries(series1);
        graph2.addSeries(series2);
        graph3.addSeries(series3);
        lastX = 0;
    }

    private void roundMaxY(int num){
        int newNum = ((num + 5) / 10) * 10;
        if(newNum < num){
            maxYvalue = newNum + 10;
        } else {
            maxYvalue = newNum;
        }
        Log.d(TAG, "Rounded: " + num + " to: " + maxYvalue);
    }

    private void updateBounds(){
        Log.d(TAG, "Max Y value: " + maxYvalue);
        Log.d(TAG, "Max X value: " + maxXvalue);

        graph1.getViewport().setMaxY(maxYvalue);
        graph1.getViewport().setMaxX(maxXvalue);

        graph2.getViewport().setMaxY(maxYvalue);
        graph2.getViewport().setMaxX(maxXvalue);

        graph3.getViewport().setMaxY(maxYvalue);
        graph3.getViewport().setMaxX(maxXvalue);
    }

}
