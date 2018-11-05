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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.services.BluetoothService;
import com.wearables.ge.wearables_ble_receiver.services.LocationService;

import java.util.Random;

public class DisplayMessageActivity extends AppCompatActivity {
    private static String TAG = "Display_Message";

    public static String deviceName = MainActivity.deviceName;
    public BluetoothDevice connectedDevice = MainActivity.connectedDevice;

    BluetoothService mService;
    boolean mBound;

    boolean shouldDisconnect = true;
    boolean devMode = false;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_display_message);

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
        mBound = true;

        //start location service
        //Location service is not an extension of the service class and doesn't need to be bound to.
        //This is because we don't need the location service to send updates to the UI.
        //We only need to grab the latest coordinates from the location service.
        LocationService.startLocationService(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        showConnectedMessage();
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
                //mService.close();
            } catch (Exception e){
                Log.d(TAG, "Couldn't disconnect bluetooth device: " + e.getMessage());
            }
            unbindBluetoothService();
        }
        unregisterReceiver(mGattUpdateReceiver);
    }

    public void unbindBluetoothService(){
        if(mBound){
            Log.d(TAG, "Unbinding service");
            unbindService(mConnection);
            mBound = false;
        }
    }

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
                        showConnectedMessage();
                        break;
                    case BluetoothService.ACTION_UPDATE_VOLTAGE_SENSOR_STATUS:
                        updateVoltageSensorStatus();
                        break;
                    case BluetoothService.ACTION_UPDATE_BATTERY_LEVEL:
                        updateBatteryLevel();
                        break;
                    case BluetoothService.ACTION_UPDATE_TEMPERATURE:
                        updateTemperature();
                        break;
                    case BluetoothService.ACTION_UPDATE_HUMIDITY:
                        updateHumidity();
                        break;
                    case BluetoothService.ACTION_UPDATE_VOC:
                        updateVOC();
                        break;
                    case BluetoothService.ACTION_UPDATE_SPO2_SENSOR_STATUS:
                        updateSpo2Sensor();
                        break;
                    case BluetoothService.ACTION_UPDATE_VOLTAGE_LEVEL:
                        updateVoltageLevel();
                        break;
                    case BluetoothService.ACTION_UPDATE_ALARM_THRESHOLD:
                        updateAlarmThreshold();
                        break;
                }
            }
        }
    };

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

                unbindService(mConnection);
                mBound = false;
                shouldDisconnect = true;

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.dev_mode:
                Log.d(TAG, "dev_mode button pushed");
                //dev mode is not a new activity, just a change to the UI
                devMode = !devMode;
                switchModes();
                return true;
            default:
                Log.d(TAG, "No menu item found for " + item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }

    public void switchModes(){
        //just change the buttons and one of the menu items
        //if devmode gets more complex we may want to create a separate activity for it
        LinearLayout linLayout = findViewById(R.id.rootContainer2);
        if(devMode){
            linLayout.removeView(findViewById(R.id.alarm_threshold_button));

            Button showRealTimeDataButton = new Button(this);
            showRealTimeDataButton.setText(R.string.show_real_time_data_button);
            showRealTimeDataButton.setId(R.id.real_time_data_button);
            showRealTimeDataButton.setOnClickListener(v -> {
                showRealTimeData();
                Log.d(TAG, "Show real time data button pressed");
            });
            linLayout.addView(showRealTimeDataButton);

            MenuItem devModeItem = menu.findItem(R.id.dev_mode);
            devModeItem.setTitle(R.string.normal_mode_menu_item);
        } else {
            //when returning to normal mode, remove the two buttons and add them back in the right order
            linLayout.removeView(findViewById(R.id.real_time_data_button));
            linLayout.removeView(findViewById(R.id.voltage_events_button));
            addNormalModeButtons();
            MenuItem devModeItem = menu.findItem(R.id.dev_mode);
            devModeItem.setTitle(R.string.dev_mode_menu_item);
        }
    }

    //method for adding the buttons that show up in normal mode
    private void addNormalModeButtons(){
        LinearLayout linLayout = findViewById(R.id.rootContainer2);
        Button alarmThresholdButton = new Button(this);
        alarmThresholdButton.setText(R.string.alarm_threshold_button);
        alarmThresholdButton.setId(R.id.alarm_threshold_button);
        alarmThresholdButton.setOnClickListener(v -> {
            showAlarmThresholdDialog();
            Log.d(TAG, "Alarm Threshold button pressed");
        });
        linLayout.addView(alarmThresholdButton);

        Button voltageEventsButton = new Button(this);
        voltageEventsButton.setText(R.string.voltage_events_button);
        voltageEventsButton.setId(R.id.voltage_events_button);
        voltageEventsButton.setOnClickListener(v -> {
            showVoltageEventsDialog();
            Log.d(TAG, "Voltage Events button pressed");
        });
        linLayout.addView(voltageEventsButton);
    }

    private void showRealTimeData(){
        shouldDisconnect = true;
        Intent intent = new Intent(this, RealTimeDataSelectionActivity.class);
        startActivity(intent);
    }

    public void showConnectedMessage() {
        LinearLayout linLayout = findViewById(R.id.rootContainer2);
        if (linLayout != null) {
            linLayout.removeAllViews();

            TextView textView = new TextView(this);
            textView.setText(getString(R.string.connected_device_message, deviceName));
            textView.setGravity(Gravity.CENTER);
            linLayout.addView(textView);


            TextView voltageSensorStatusView = new TextView(this);
            voltageSensorStatusView.setText(getString(R.string.voltage_sensor_status, "undefined"));
            voltageSensorStatusView.setGravity(Gravity.CENTER);
            voltageSensorStatusView.setId(R.id.voltage_sensor_status);
            linLayout.addView(voltageSensorStatusView);

            TextView batteryLevelView = new TextView(this);
            batteryLevelView.setText(getString(R.string.battery_level, 0));
            batteryLevelView.setGravity(Gravity.CENTER);
            batteryLevelView.setId(R.id.battery_level);
            linLayout.addView(batteryLevelView);

            TextView temperatureView = new TextView(this);
            temperatureView.setText(getString(R.string.temperature, "undefined"));
            temperatureView.setGravity(Gravity.CENTER);
            temperatureView.setId(R.id.temperature);
            linLayout.addView(temperatureView);

            TextView humidityView = new TextView(this);
            humidityView.setText(getString(R.string.humidity, "undefined"));
            humidityView.setGravity(Gravity.CENTER);
            humidityView.setId(R.id.humidity);
            linLayout.addView(humidityView);

            TextView VOCView = new TextView(this);
            VOCView.setText(getString(R.string.VOC, "undefined"));
            VOCView.setGravity(Gravity.CENTER);
            VOCView.setId(R.id.VOC);
            linLayout.addView(VOCView);

            TextView spo2SensorView = new TextView(this);
            spo2SensorView.setText(getString(R.string.spo2, "undefined"));
            spo2SensorView.setGravity(Gravity.CENTER);
            spo2SensorView.setId(R.id.spo2_sensor);
            linLayout.addView(spo2SensorView);

            TextView voltageLevelView = new TextView(this);
            voltageLevelView.setText(getString(R.string.voltage_level, 0));
            voltageLevelView.setGravity(Gravity.CENTER);
            voltageLevelView.setId(R.id.voltage_level);
            linLayout.addView(voltageLevelView);

            TextView alarmThresholdView = new TextView(this);
            alarmThresholdView.setText(getString(R.string.alarm_threshold, 0));
            alarmThresholdView.setGravity(Gravity.CENTER);
            alarmThresholdView.setId(R.id.alarm_threshold);
            linLayout.addView(alarmThresholdView);

            addNormalModeButtons();
        }
    }

    //alarm threshold button opens up a modal dialog that allows the user to enter a number
    private void showAlarmThresholdDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage(R.string.alert_threshold_dialog_message);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        alert.setView(input);

        alert.setPositiveButton(R.string.dialog_accept_button_message, (dialog, whichButton) -> {
            int value = Integer.parseInt(input.getText().toString());
            //TODO: send this new threshold to the device
            Log.d(TAG, "Alarm Threshold set to: " + value);
        });

        alert.setNegativeButton(R.string.dialog_cancel_button_message, (dialog, whichButton) -> Log.d(TAG, "Alarm Threshold dialog closed"));

        alert.show();
    }

    private void showVoltageEventsDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage(R.string.voltage_event_dialog_message);

        LinearLayout voltageLogLinearLayout = new LinearLayout(this);
        voltageLogLinearLayout.setOrientation(LinearLayout.VERTICAL);

        for(Location location : LocationService.locations){
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            //mostly just filler data for voltage events
            String date = new java.util.Date().toString();
            TextView dateTimeTextView = new TextView(this);
            dateTimeTextView.setText(date);
            dateTimeTextView.setGravity(Gravity.CENTER);
            voltageLogLinearLayout.addView(dateTimeTextView);

            Random rand = new Random();
            String message = "Level " + rand.nextInt(1000) + ", lasted " + rand.nextInt(20) + " seconds";
            TextView messageTextView = new TextView(this);
            messageTextView.setText(message);
            messageTextView.setGravity(Gravity.CENTER);
            voltageLogLinearLayout.addView(messageTextView);

            String coordinates = "(" + latitude.toString() + "," + longitude.toString() + ")";
            TextView locationTextView = new TextView(this);
            locationTextView.setText(coordinates);
            locationTextView.setGravity(Gravity.CENTER);
            locationTextView.setPadding(0,0,0,30);
            voltageLogLinearLayout.addView(locationTextView);
        }

        alert.setView(voltageLogLinearLayout);

        alert.setNegativeButton(R.string.dialog_cancel_button_message, (dialog, whichButton) -> {
            voltageLogLinearLayout.removeAllViews();
            Log.d(TAG, "Voltage Log dialog closed");
        });

        alert.show();
    }

    public void updateVoltageSensorStatus(){
        TextView voltageSensorStatusView = findViewById(R.id.voltage_sensor_status);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.voltage_sensor_status, mService.voltageSensorStatus));
        }
    }

    public void updateBatteryLevel(){
        TextView batteryLevelView = findViewById(R.id.battery_level);
        if(batteryLevelView != null){
            batteryLevelView.setText(getString(R.string.battery_level, mService.batteryLevel));
        }
    }

    public void updateTemperature(){
        TextView voltageSensorStatusView = findViewById(R.id.temperature);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.temperature, mService.temperature));
        }
    }

    public void updateHumidity(){
        TextView voltageSensorStatusView = findViewById(R.id.humidity);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.humidity, mService.humidity));
        }
    }

    public void updateVOC(){
        TextView voltageSensorStatusView = findViewById(R.id.VOC);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.VOC, mService.VOC));
        }
    }

    public void updateSpo2Sensor(){
        TextView voltageSensorStatusView = findViewById(R.id.spo2_sensor);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.spo2, mService.spo2_sensor));
        }
    }

    public void updateVoltageLevel(){
        TextView voltageSensorStatusView = findViewById(R.id.voltage_level);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.voltage_level, mService.voltageLevel));
        }
    }


    public void updateAlarmThreshold(){
        TextView voltageSensorStatusView = findViewById(R.id.alarm_threshold);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.alarm_threshold, mService.alarmThreshold));
        }
    }
}
