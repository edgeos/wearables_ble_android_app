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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainActivity;
import com.wearables.ge.wearables_ble_receiver.services.BluetoothService;
import com.wearables.ge.wearables_ble_receiver.utils.BLEQueue;

public class TempHumiditySensorGraph extends AppCompatActivity {

    public static String TAG = "Acceleration sensor graphs";

    public static String deviceName = MainActivity.deviceName;
    public BluetoothDevice connectedDevice = MainActivity.connectedDevice;

    private Menu menu;

    private LineGraphSeries<DataPoint> series1;
    private LineGraphSeries<DataPoint> series2;
    private int lastX = 0;

    BluetoothService mService;
    boolean mBound = false;

    boolean shouldDisconnect = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_humidity_sensor_graph);
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
        GraphView graph1 = findViewById(R.id.temp_humid_sensor_graph_1);
        // data
        series1 = new LineGraphSeries<>();
        graph1.addSeries(series1);
        // customize viewport
        Viewport viewport1 = graph1.getViewport();
        viewport1.setYAxisBoundsManual(true);
        viewport1.setMinY(0);
        viewport1.setMaxY(100);
        viewport1.setScrollable(true);

        // second graph
        GraphView graph2 = findViewById(R.id.temp_humid_sensor_graph_2);
        series2 = new LineGraphSeries<>();
        graph2.addSeries(series2);
        Viewport viewport2 = graph2.getViewport();
        viewport2.setYAxisBoundsManual(true);
        viewport2.setMinY(0);
        viewport2.setMaxY(100);
        viewport2.setScrollable(true);
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
        this.menu = menu;
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

    public int batteryLevel;
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
                            batteryLevel = intent.getIntExtra(BluetoothService.EXTRA_INT_DATA, 0);
                            updateGraph();
                        }
                        break;
                }
            }
        }
    };

    protected void updateGraph() {
        super.onResume();
        // simulate real time with thread that appends data to the graph
        new Thread(() -> {
            // add 100 new entries
            for (int i = 0; i < 100; i++) {
                runOnUiThread(this::addEntry);

                // sleep to slow down the add of entries
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    // manage error ...
                }
            }
        }).start();
    }

    // add random data to graph
    private void addEntry() {
        series1.appendData(new DataPoint(lastX++, batteryLevel), false, 100);
        series2.appendData(new DataPoint(lastX++, batteryLevel), false, 100);
    }
}
