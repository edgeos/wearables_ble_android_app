package com.wearables.ge.wearables_ble_receiver;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wearables.ge.wearables_ble_receiver.res.gattAttributes;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class DisplayMessageActivity extends AppCompatActivity {
    private static String TAG = "Display_Message";

    public BluetoothGatt connectedGatt;
    public String deviceName = MainActivity.deviceName;
    public BluetoothDevice connectedDevice = MainActivity.connectedDevice;

    public int battLevelValue;



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
            textView.setText("Connected to device: " + deviceName);
            textView.setGravity(Gravity.CENTER);
            linLayout.addView(textView);


            TextView batteryLevelView = new TextView(this);
            batteryLevelView.setText("Battery level: undefined");
            batteryLevelView.setGravity(Gravity.CENTER);
            batteryLevelView.setId(R.id.battery_level);
            linLayout.addView(batteryLevelView);

            for(BluetoothGattService obj : connectedGatt.getServices()){
                textView = new TextView(this);
                textView.setText("Found service UUID: " + obj.getUuid());
                textView.setGravity(Gravity.CENTER);
                linLayout.addView(textView);

                for(BluetoothGattCharacteristic obj2 : obj.getCharacteristics()){
                    textView = new TextView(this);
                    textView.setText("characteristic: " + obj2.getUuid() + " containing descriptor: ");
                    textView.setGravity(Gravity.CENTER);
                    linLayout.addView(textView);

                    for(BluetoothGattDescriptor obj3 : obj2.getDescriptors()){
                        textView = new TextView(this);
                        textView.setText(obj3.getUuid().toString());
                        textView.setGravity(Gravity.CENTER);
                        linLayout.addView(textView);
                    }
                }
            }
        }
    }

    public void updateBatteryLevel(){
        TextView batteryLevelView = findViewById(R.id.battery_level);
        batteryLevelView.setText("Battery level: " + battLevelValue + "%");
    }

    private Queue<BluetoothGattDescriptor> descriptorWriteQueue = new LinkedList<BluetoothGattDescriptor>();
    private Queue<BluetoothGattCharacteristic> characteristicReadQueue = new LinkedList<BluetoothGattCharacteristic>();

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

            runOnUiThread(DisplayMessageActivity.this::showConnectedMessage);

            for(BluetoothGattService service : connectedGatt.getServices()){
                for(BluetoothGattCharacteristic gattChar : service.getCharacteristics()){
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(gattChar.getUuid());
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    enableCharacteristicNotification(gatt, characteristic);
                }
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            super.onCharacteristicChanged(gatt, characteristic);

            int messageInt;

            if(characteristic.getUuid().equals(gattAttributes.BATT_LEVEL_CHAR_UUID)){
                Log.d(TAG, "attempting to parse battery level");
                BluetoothGattService batteryServ = gatt.getService(gattAttributes.BATT_SERVICE_UUID);
                BluetoothGattCharacteristic battLevel = batteryServ.getCharacteristic(gattAttributes.BATT_LEVEL_CHAR_UUID);
                characteristicReadQueue.add(characteristic);
                if((characteristicReadQueue.size() == 1) && (descriptorWriteQueue.size() == 0)){
                    gatt.readCharacteristic(characteristic);
                }
                messageInt = battLevel.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                battLevelValue = messageInt;
                runOnUiThread(DisplayMessageActivity.this::updateBatteryLevel);

                Log.d(TAG, "Battery level: " + messageInt + "%");
                return;
            }

            characteristicReadQueue.add(characteristic);
            if((characteristicReadQueue.size() == 1) && (descriptorWriteQueue.size() == 0)){
                gatt.readCharacteristic(characteristic);
            }

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
                //Log.d(TAG, "Char UUID: " + characteristic.getUuid());
                //Log.d(TAG, "Received message: " + new String(messageBytes));
                Log.d(TAG, "Received message: " + stringBuilder.toString());
            } catch (Exception e) {
                Log.e(TAG, "Unable to convert message bytes to string" + e.getMessage());
            }
            Log.d(TAG, "Received message (string value): " + characteristic.getStringValue(0));
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
