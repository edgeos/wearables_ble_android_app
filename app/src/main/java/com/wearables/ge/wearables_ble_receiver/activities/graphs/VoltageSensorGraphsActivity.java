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
import android.os.Handler;
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
import com.wearables.ge.wearables_ble_receiver.activities.main.DisplayMessageActivity;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainActivity;
import com.wearables.ge.wearables_ble_receiver.services.BluetoothService;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class VoltageSensorGraphsActivity extends AppCompatActivity {
    public static String TAG = "Voltage sensor graphs";

    public static String deviceName = MainActivity.deviceName;
    public BluetoothDevice connectedDevice = MainActivity.connectedDevice;

    private Menu menu;

    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
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
        GraphView graph = findViewById(R.id.voltage_sensor_graph_1);
        // data
        series = new LineGraphSeries<>();
        graph.addSeries(series);
        // customize viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(100);
        viewport.setScrollable(true);
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

    //create custom intent filter for broadcasting messages from the bluetooth service to this activity
    private static IntentFilter createIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_SHOW_CONNECTED_MESSAGE);
        intentFilter.addAction(BluetoothService.ACTION_UPDATE_ALARM_THRESHOLD);
        intentFilter.addAction(BluetoothService.ACTION_UPDATE_BATTERY_LEVEL);
        intentFilter.addAction(BluetoothService.ACTION_UPDATE_HUMIDITY);
        intentFilter.addAction(BluetoothService.ACTION_UPDATE_SPO2_SENSOR_STATUS);
        intentFilter.addAction(BluetoothService.ACTION_UPDATE_TEMPERATURE);
        intentFilter.addAction(BluetoothService.ACTION_UPDATE_VOC);
        intentFilter.addAction(BluetoothService.ACTION_UPDATE_VOLTAGE_LEVEL);
        intentFilter.addAction(BluetoothService.ACTION_UPDATE_VOLTAGE_SENSOR_STATUS);
        return intentFilter;
    }

    //this method handles broadcasts sent from the bluetooth service
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action != null){
                switch(action){
                    case BluetoothService.ACTION_SHOW_CONNECTED_MESSAGE:

                        break;
                    case BluetoothService.ACTION_UPDATE_VOLTAGE_SENSOR_STATUS:

                        break;
                    case BluetoothService.ACTION_UPDATE_BATTERY_LEVEL:
                        updateGraph();
                        break;
                    case BluetoothService.ACTION_UPDATE_TEMPERATURE:

                        break;
                    case BluetoothService.ACTION_UPDATE_HUMIDITY:

                        break;
                    case BluetoothService.ACTION_UPDATE_VOC:

                        break;
                    case BluetoothService.ACTION_UPDATE_SPO2_SENSOR_STATUS:

                        break;
                    case BluetoothService.ACTION_UPDATE_VOLTAGE_LEVEL:

                        break;
                    case BluetoothService.ACTION_UPDATE_ALARM_THRESHOLD:

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
        series.appendData(new DataPoint(lastX++, mService.batteryLevel), false, 10);
    }

}
