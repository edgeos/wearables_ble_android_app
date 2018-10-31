package com.wearables.ge.wearables_ble_receiver;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.wearables.ge.wearables_ble_receiver.res.gattAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DisplayMessageActivity extends AppCompatActivity {
    private static String TAG = "Display_Message";

    public static BluetoothGatt connectedGatt;
    public static String deviceName = MainActivity.deviceName;
    public BluetoothDevice connectedDevice = MainActivity.connectedDevice;

    public int batteryLevel;
    public String voltageSensorStatus;
    public String temperature;
    public String humidity;
    public String VOC;
    public String spo2_sensor;
    public int voltageLevel;
    public int alarmThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        Toolbar myToolbar = findViewById(R.id.display_message_toolbar);
        myToolbar.setTitle(deviceName);
        setSupportActionBar(myToolbar);
        ActionBar myActionBar = getSupportActionBar();
        if(myActionBar != null){
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }
        connectDevice(connectedDevice);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
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
                disconnectGattServer();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.dev_mode:
                Log.d(TAG, "dev_mode button pushed");
                //action for dev_mode
                return true;
            default:
                Log.d(TAG, "No menu item found for " + item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }

    private void connectDevice(BluetoothDevice device) {
        DisplayMessageActivity.GattClientCallback gattClientCallback = new DisplayMessageActivity.GattClientCallback();
        connectedGatt = device.connectGatt(this, false, gattClientCallback);
    }

    public void disconnectGattServer() {
        //disconnect;
        Log.d(TAG, "Attempting to disconnect " + deviceName);
        if (connectedGatt != null) {
            connectedGatt.disconnect();
            connectedGatt.close();
        }
    }

    public void showConnectedMessage(){
        LinearLayout linLayout = findViewById(R.id.rootContainer2);
        if(linLayout != null){
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

    private void showAlarmThresholdDialog(){
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

    private void showVoltageEventsDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage(R.string.voltage_event_dialog_message);

        LinearLayout voltageLogLinearLayout = new LinearLayout(this);
        voltageLogLinearLayout.setOrientation(LinearLayout.VERTICAL);

        List<String> testList = new ArrayList<>(Arrays.asList("one", "two", "three"));

        for(String obj : testList){
            TextView textView = new TextView(this);
            textView.setText(obj);
            textView.setGravity(Gravity.CENTER);
            voltageLogLinearLayout.addView(textView);
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


    public void updateVoltageSensorStatus(){
        TextView voltageSensorStatusView = findViewById(R.id.voltage_sensor_status);
        voltageSensorStatusView.setText(getString(R.string.voltage_sensor_status, voltageSensorStatus));
        //voltageSensorStatusView.setText("Status: " + voltageSensorStatus);
    }

    public void updateBatteryLevel(){
        TextView batteryLevelView = findViewById(R.id.battery_level);
        batteryLevelView.setText(getString(R.string.battery_level, batteryLevel));
        //batteryLevelView.setText("Battery level: " + batteryLevel + "%");
    }

    public void updateTemperature(){
        TextView voltageSensorStatusView = findViewById(R.id.temperature);
        voltageSensorStatusView.setText(getString(R.string.temperature, temperature));
        //voltageSensorStatusView.setText("Temperature: " + temperature + "\u00b0C");
    }

    public void updateHumidity(){
        TextView voltageSensorStatusView = findViewById(R.id.humidity);
        voltageSensorStatusView.setText(getString(R.string.humidity, humidity));
        //voltageSensorStatusView.setText("Humidity: " + humidity + "%");
    }

    public void updateVOC(){
        TextView voltageSensorStatusView = findViewById(R.id.VOC);
        voltageSensorStatusView.setText(getString(R.string.VOC, VOC));
        //voltageSensorStatusView.setText("VOC: " + VOC + "ppm");
    }

    public void updateSpo2Sensor(){
        TextView voltageSensorStatusView = findViewById(R.id.spo2_sensor);
        voltageSensorStatusView.setText(getString(R.string.spo2, spo2_sensor));
        //voltageSensorStatusView.setText("SpO2 Sensor " + spo2_sensor);
    }

    public void updateVoltageLevel(){
        TextView voltageSensorStatusView = findViewById(R.id.voltage_level);
        voltageSensorStatusView.setText(getString(R.string.voltage_level, voltageLevel));
        //voltageSensorStatusView.setText("Voltage Level: " + voltageLevel);
    }

    public void updateAlarmThreshold(){
        TextView voltageSensorStatusView = findViewById(R.id.alarm_threshold);
        voltageSensorStatusView.setText(getString(R.string.alarm_threshold, alarmThreshold));
        //voltageSensorStatusView.setText("Alarm Threshold: " + alarmThreshold);
    }

    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<>();
    private Queue<BluetoothGattCharacteristic> characteristicReadQueue = new LinkedList<>();

    private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //set characteristics and descriptors to notify for constant updates whenever values are changed
        boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
        if (characteristicWriteSuccess) {
            Log.d(TAG, "Characteristic notification set successfully for " + characteristic.getUuid().toString());
            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                descriptorWriteQueue.add(descriptor);
                if (descriptorWriteQueue.size() == 1) {
                    gatt.writeDescriptor(descriptor);
                }
            }
        } else {
            Log.d(TAG, "Characteristic notification set failure for " + characteristic.getUuid().toString());
        }
    }

    private class GattClientCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer();
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //triggered when a device is connected

                //set global variables for connected device and device name
                connectedGatt = gatt;
                deviceName = gatt.getDevice().getName() == null ? gatt.getDevice().getAddress() : gatt.getDevice().getName();
                Log.d(TAG, "Device connected: " + deviceName);

                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer();
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            //UI changes need to be run on the original UI thread
            runOnUiThread(DisplayMessageActivity.this::showConnectedMessage);

            //Enable notifications for all characteristics found
            //we can do this dynamically later
            for(BluetoothGattService service : connectedGatt.getServices()){
                for(BluetoothGattCharacteristic gattChar : service.getCharacteristics()){
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(gattChar.getUuid());
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    enableCharacteristicNotification(gatt, characteristic);
                }
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //this method is triggered when the process is notified that a characteristic has changed

            super.onCharacteristicChanged(gatt, characteristic);

            //add the characteristic to the queue to be read, if queue is empty, read it.
            characteristicReadQueue.add(characteristic);
            if((characteristicReadQueue.size() == 1) && (descriptorWriteQueue.size() == 0)){
                gatt.readCharacteristic(characteristic);
            }

            //handle battery level case
            if(characteristic.getUuid().equals(gattAttributes.BATT_LEVEL_CHAR_UUID)){
                batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                runOnUiThread(DisplayMessageActivity.this::updateBatteryLevel);
                Log.d(TAG, "Battery level: " + batteryLevel + "%");
                return;
            }

            //if none of the cases above, parse message as a normal byte array
            byte[] messageBytes = characteristic.getValue();
            if(messageBytes == null){
                Log.d(TAG, "No message parsed on characteristic.");
                return;
            }

            try {
                final StringBuilder stringBuilder = new StringBuilder(messageBytes.length);
                for(byte byteChar : messageBytes){
                    stringBuilder.append(String.format("%02x ", byteChar));
                }

                //TODO: send this data to AWS for storage
                String value = stringBuilder.toString();

                if(characteristic.getUuid().equals(gattAttributes.VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID)){
                    Log.d(TAG, "VOLTAGE_ALARM_STATE value: " + value);
                } else if(characteristic.getUuid().equals(gattAttributes.VOLTAGE_ALARM_CONFIG_CHARACTERISTIC_UUID)){
                    Log.d(TAG, "VOLTAGE_ALARM_CONFIG value: " + value);
                } else if(characteristic.getUuid().equals(gattAttributes.ACCELEROMETER_DATA_CHARACTERISTIC_UUID)){
                    Log.d(TAG, "ACCELEROMETER_DATA value: " + value);
                } else if(characteristic.getUuid().equals(gattAttributes.TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID)){
                    temperature = value;
                    humidity = value;
                    VOC = value;
                    runOnUiThread(DisplayMessageActivity.this::updateHumidity);
                    runOnUiThread(DisplayMessageActivity.this::updateTemperature);
                    runOnUiThread(DisplayMessageActivity.this::updateVOC);
                    Log.d(TAG, "TEMP_HUMIDITY_PRESSURE_DATA value: " + value);
                } else if(characteristic.getUuid().equals(gattAttributes.GAS_SENSOR_DATA_CHARACTERISTIC_UUID)){
                    Log.d(TAG, "GAS_SENSOR_DATA value: " + value);
                } else if(characteristic.getUuid().equals(gattAttributes.OPTICAL_SENSOR_DATA_CHARACTERISTIC_UUID)){
                    Log.d(TAG, "OPTICAL_SENSOR_DATA value: " + value);
                } else if(characteristic.getUuid().equals(gattAttributes.STREAMING_DATA_CHARACTERISTIC_UUID)){
                    Log.d(TAG, "STREAMING_DATA value: " + value);
                } else {
                    Log.d(TAG, "Received message: " + value + " with UUID: " + characteristic.getUuid());
                }

            } catch (Exception e) {
                Log.e(TAG, "Unable to convert message bytes to string" + e.getMessage());
            }
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Callback: Wrote GATT Descriptor successfully.");
            }
            else{
                Log.d(TAG, "Callback: Error writing GATT Descriptor: "+ status);
            }
            descriptorWriteQueue.remove();  //pop the item that we just finishing writing
            //if there is more to write, do it!
            if(descriptorWriteQueue.size() > 0)
                connectedGatt.writeDescriptor(descriptorWriteQueue.element());
            else if(characteristicReadQueue.size() > 0)
                connectedGatt.readCharacteristic(characteristicReadQueue.element());
        }

        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            characteristicReadQueue.remove();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead success: " + status);
            }
            else{
                Log.d(TAG, "onCharacteristicRead error: " + status);
            }

            if(characteristicReadQueue.size() > 0)
                connectedGatt.readCharacteristic(characteristicReadQueue.element());
        }
    }
}
