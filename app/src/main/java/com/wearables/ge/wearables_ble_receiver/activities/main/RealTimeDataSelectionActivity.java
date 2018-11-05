/*
 * Copyright (c) 2017 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.wearables.ge.wearables_ble_receiver.activities.main;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.graphs.VoltageSensorGraphsActivity;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainActivity;

public class RealTimeDataSelectionActivity extends AppCompatActivity {
    public static String TAG = "Real Time Data selection";

    public static String deviceName = MainActivity.deviceName;
    public BluetoothDevice connectedDevice = MainActivity.connectedDevice;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_time_data_selection);
        //create custom toolbar
        Toolbar myToolbar = findViewById(R.id.display_message_toolbar);
        myToolbar.setTitle(deviceName);
        //extra logic for back button
        setSupportActionBar(myToolbar);
        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setDisplayHomeAsUpEnabled(true);
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
                //mService.disconnectGattServer();

                //unbindService(mConnection);
                //mBound = false;

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

    public void voltageSensorButton(View view){
        Intent intent = new Intent(this, VoltageSensorGraphsActivity.class);
        startActivity(intent);
    }

    public void accelerationSensorButton(View view){

    }

    public void tempHumiditySensorButton(View view){

    }

    public void spo2SensorButton(View view){

    }

    public void vocSensorButton(View view){

    }

}
