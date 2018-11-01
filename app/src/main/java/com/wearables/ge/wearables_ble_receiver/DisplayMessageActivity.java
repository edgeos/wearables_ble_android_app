package com.wearables.ge.wearables_ble_receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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

import com.wearables.ge.wearables_ble_receiver.services.BluetoothService;
import com.wearables.ge.wearables_ble_receiver.services.LocationService;

import java.util.Random;

public class DisplayMessageActivity extends AppCompatActivity {
    private static String TAG = "Display_Message";

    public static String deviceName = MainActivity.deviceName;
    public BluetoothDevice connectedDevice = MainActivity.connectedDevice;

    BluetoothService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        Toolbar myToolbar = findViewById(R.id.display_message_toolbar);
        myToolbar.setTitle(deviceName);
        setSupportActionBar(myToolbar);
        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        LocationService.startLocationService(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Activity paused");
        //Some activities may not want to disconnect the device.
        // we will want to handle this scenario
        try {
            mService.disconnectGattServer();
            mService.close();
        } catch (Exception e){
            Log.d(TAG, "Couldn't disconnect bluetooth device: " + e.getMessage());
        }
        Log.d(TAG, "Unregistering update receiver and unbinding service");
        if(mBound){
            unbindService(mConnection);
            unregisterReceiver(mGattUpdateReceiver);
        }
    }


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

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "attempting bind to bluetooth service");
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mService = binder.getService();
            registerReceiver(mGattUpdateReceiver, createIntentFilter());
            mService.connectDevice(connectedDevice);
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
        return true;
    }

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
                mService.disconnectGattServer();

                unbindService(mConnection);
                mBound = false;

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.dev_mode:
                Log.d(TAG, "dev_mode button pushed");
                openDevMode();
                return true;
            default:
                Log.d(TAG, "No menu item found for " + item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }

    public void openDevMode(){
        Intent intent = new Intent(this, DeveloperModeActivity.class);
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

            Button alarmThresholdButton = new Button(this);
            alarmThresholdButton.setText(R.string.alarm_threshold_button);
            alarmThresholdButton.setOnClickListener(v -> {
                showAlarmThresholdDialog();
                Log.d(TAG, "Alarm Threshold button pressed");
            });
            linLayout.addView(alarmThresholdButton);

            Button voltageEventsButton = new Button(this);
            voltageEventsButton.setText(R.string.voltage_events_button);
            voltageEventsButton.setOnClickListener(v -> {
                showVoltageEventsDialog();
                Log.d(TAG, "Voltage Events button pressed");
            });
            linLayout.addView(voltageEventsButton);
        }
    }

    private void showAlarmThresholdDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage(R.string.alert_threshold_dialog_message);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        alert.setView(input);

        alert.setPositiveButton(R.string.dialog_accept_button_message, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int value = Integer.parseInt(input.getText().toString());
                //TODO: send this new threshold to the device
                Log.d(TAG, "Alarm Threshold set to: " + value);
            }
        });

        alert.setNegativeButton(R.string.dialog_cancel_button_message, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, "Alarm Threshold dialog closed");
            }
        });

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

            //just filler data for voltage events
            String date = new java.util.Date().toString();
            TextView dateTimeTextView = new TextView(DisplayMessageActivity.this);
            dateTimeTextView.setText(date);
            dateTimeTextView.setGravity(Gravity.CENTER);
            voltageLogLinearLayout.addView(dateTimeTextView);

            Random rand = new Random();
            String message = "Level " + rand.nextInt(1000) + ", lasted " + rand.nextInt(20) + " seconds";
            TextView messageTextView = new TextView(DisplayMessageActivity.this);
            messageTextView.setText(message);
            messageTextView.setGravity(Gravity.CENTER);
            voltageLogLinearLayout.addView(messageTextView);

            String coordinates = "(" + latitude.toString() + "," + longitude.toString() + ")";
            TextView locationTextView = new TextView(DisplayMessageActivity.this);
            locationTextView.setText(coordinates);
            locationTextView.setGravity(Gravity.CENTER);
            locationTextView.setPadding(0,0,0,30);
            voltageLogLinearLayout.addView(locationTextView);
        }

        alert.setView(voltageLogLinearLayout);

        alert.setNegativeButton(R.string.dialog_cancel_button_message, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                voltageLogLinearLayout.removeAllViews();
                Log.d(TAG, "Voltage Log dialog closed");
            }
        });

        alert.show();
    }

    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
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
    };


    public void updateVoltageSensorStatus(){
        TextView voltageSensorStatusView = findViewById(R.id.voltage_sensor_status);
        voltageSensorStatusView.setText(getString(R.string.voltage_sensor_status, mService.voltageSensorStatus));
    }

    public void updateBatteryLevel(){
        TextView batteryLevelView = findViewById(R.id.battery_level);
        batteryLevelView.setText(getString(R.string.battery_level, mService.batteryLevel));
    }

    public void updateTemperature(){
        TextView voltageSensorStatusView = findViewById(R.id.temperature);
        voltageSensorStatusView.setText(getString(R.string.temperature, mService.temperature));
    }

    public void updateHumidity(){
        TextView voltageSensorStatusView = findViewById(R.id.humidity);
        voltageSensorStatusView.setText(getString(R.string.humidity, mService.humidity));
    }

    public void updateVOC(){
        TextView voltageSensorStatusView = findViewById(R.id.VOC);
        voltageSensorStatusView.setText(getString(R.string.VOC, mService.VOC));
    }

    public void updateSpo2Sensor(){
        TextView voltageSensorStatusView = findViewById(R.id.spo2_sensor);
        voltageSensorStatusView.setText(getString(R.string.spo2, mService.spo2_sensor));
    }

    public void updateVoltageLevel(){
        TextView voltageSensorStatusView = findViewById(R.id.voltage_level);
        voltageSensorStatusView.setText(getString(R.string.voltage_level, mService.voltageLevel));
    }

    public void updateAlarmThreshold(){
        TextView voltageSensorStatusView = findViewById(R.id.alarm_threshold);
        voltageSensorStatusView.setText(getString(R.string.alarm_threshold, mService.alarmThreshold));
    }
}
