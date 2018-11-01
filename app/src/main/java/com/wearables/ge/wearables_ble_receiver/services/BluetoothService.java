package com.wearables.ge.wearables_ble_receiver.services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.wearables.ge.wearables_ble_receiver.res.gattAttributes;

import java.util.LinkedList;
import java.util.Queue;

public class BluetoothService extends Service {
    private static String TAG = "Bluetooth Service";

    public static BluetoothGatt connectedGatt;
    public static String deviceName;

    public int batteryLevel;
    public String voltageSensorStatus;
    public String temperature;
    public String humidity;
    public String VOC;
    public String spo2_sensor;
    public int voltageLevel;
    public int alarmThreshold;

    private final IBinder mBinder = new LocalBinder();

    public final static String ACTION_SHOW_CONNECTED_MESSAGE = "com.wearables.ge.ACTION_SHOW_CONNECTED_MESSAGE";
    public final static String ACTION_UPDATE_VOLTAGE_SENSOR_STATUS = "com.wearables.ge.ACTION_UPDATE_VOLTAGE_SENSOR_STATUS";
    public final static String ACTION_UPDATE_BATTERY_LEVEL = "com.wearables.ge.ACTION_UPDATE_BATTERY_LEVEL";
    public final static String ACTION_UPDATE_TEMPERATURE = "com.wearables.ge.ACTION_UPDATE_TEMPERATURE";
    public final static String ACTION_UPDATE_HUMIDITY = "com.wearables.ge.ACTION_UPDATE_HUMIDITY";
    public final static String ACTION_UPDATE_VOC = "com.wearables.ge.ACTION_UPDATE_VOC";
    public final static String ACTION_UPDATE_SPO2_SENSOR_STATUS = "com.wearables.ge.ACTION_UPDATE_SPO2_SENSOR_STATUS";
    public final static String ACTION_UPDATE_VOLTAGE_LEVEL = "com.wearables.ge.ACTION_UPDATE_VOLTAGE_LEVEL";
    public final static String ACTION_UPDATE_ALARM_THRESHOLD = "com.wearables.ge.ACTION_UPDATE_ALARM_THRESHOLD";


    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public void connectDevice(BluetoothDevice device) {
        BluetoothService.GattClientCallback gattClientCallback = new BluetoothService.GattClientCallback();
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

    @Override
    public boolean onUnbind(Intent intent) {
        // Close to connection
        close();
        return super.onUnbind(intent);
    }

    public void close() {
        if (connectedGatt == null) {
            return;
        }
        connectedGatt.close();
        connectedGatt = null;
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
            broadcastUpdate(ACTION_SHOW_CONNECTED_MESSAGE);

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
                //runOnUiThread(DisplayMessageActivity.this::updateBatteryLevel);
                broadcastUpdate(ACTION_UPDATE_BATTERY_LEVEL);
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
                    broadcastUpdate(ACTION_UPDATE_HUMIDITY);
                    broadcastUpdate(ACTION_UPDATE_TEMPERATURE);
                    broadcastUpdate(ACTION_UPDATE_VOC);
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
