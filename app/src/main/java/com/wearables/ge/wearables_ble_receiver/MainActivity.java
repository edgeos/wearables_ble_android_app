package com.wearables.ge.wearables_ble_receiver;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wearables.ge.wearables_ble_receiver.utils.BLEQueue;
import com.wearables.ge.wearables_ble_receiver.utils.GattAttributes;
import com.wearables.ge.wearables_ble_receiver.utils.QueueItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int SCAN_PERIOD = 3000;

    private boolean mScanning;
    private Handler mHandler;
    private Map<String, BluetoothDevice> mScanResults;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;

    private ProgressBar spinner;

    public static BluetoothGatt connectedGatt;
    public static String deviceName;

    private BLEQueue bleQueue = new BLEQueue();
    private boolean bleQueueIsFree = true;

    public static UUID batteryCharacteristicUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static UUID batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create custom toolbar
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //initialize spinner
        spinner = findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);

        //grab layout and clear it (useful when returning to main page)
        LinearLayout linLayout = findViewById(R.id.rootContainer);
        linLayout.removeAllViews();

        //disconnect if returning to main page while connected to a BT device
        if(connectedGatt != null){
            Log.d(TAG,"Main page, disconnecting from " + deviceName);
            disconnectGattServer();
        }

        //get bluetooth object
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void openConnectedPage() {
        //move to the DisplayMessageActivity page to show device info
        //TODO: rename the DisplayMessageActivity to something more helpful
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        startActivity(intent);
    }

    protected void onResume() {
        super.onResume();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
        }
    }

    public void startScan(View view) {
        //scan button pushed, start scanning
        //add spinner to view
        LinearLayout linLayout = findViewById(R.id.rootContainer);
        linLayout.removeAllViews();
        linLayout.addView(spinner);
        spinner.setVisibility(View.VISIBLE);

        //if the user hasn't allowed BT scanning  or if a scan is currently happening then stop here
        if (!hasPermissions() || mScanning) {
            return;
        }

        //initialize result set
        mScanResults = new HashMap<>();

        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        mScanCallback = new MainActivity.BtleScanCallback(mScanResults);
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        mScanning = true;
        mHandler = new Handler();
        mHandler.postDelayed(this::stopScan, SCAN_PERIOD);
    }

    private void stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;
    }

    private void scanComplete() {
        //on completed scan, remove spinner
        spinner.setVisibility(View.GONE);

        //if no results, stop here
        if (mScanResults.isEmpty()) {
            return;
        }

        //add elements to view, first grab layout container
        LinearLayout linLayout = findViewById(R.id.rootContainer);
        Boolean grey = true;
        for (BluetoothDevice obj : mScanResults.values()){
            //create a text view for the name and a connect button for each result
            TextView textView = new TextView(this);
            Button btnShow = new Button(this);

            //grab device name or address if it doesn't have a defined name
            String deviceName = obj.getName() == null ? obj.getAddress() : obj.getName();
            Log.d(TAG, "Found device: " + deviceName);
            btnShow.setText(R.string.connect_button);
            textView.setText(deviceName);

            //set onclick event for connect button
            btnShow.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "connecting...", Toast.LENGTH_LONG).show();
                connectDevice(obj);
            });

            // Add Button and text to linear layout
            // do it by adding both with constraints to a constraint layout then adding that to the linear layout
            if (linLayout != null) {
                ConstraintLayout cl = new ConstraintLayout(this);
                ConstraintLayout.LayoutParams clParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                cl.setLayoutParams(clParams);

                // give the displayed results a light striped effect
                if(grey){
                    cl.setBackgroundColor(Color.parseColor("#f5f5f5"));
                    grey = false;
                } else {
                    grey = true;
                }

                ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                buttonParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                buttonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                buttonParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                btnShow.setLayoutParams(buttonParams);

                ConstraintLayout.LayoutParams textParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                textParams.setMarginStart(16);
                textParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                textParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                textParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                textParams.endToStart = btnShow.getId();
                textView.setLayoutParams(textParams);

                cl.addView(textView);
                cl.addView(btnShow);
                linLayout.addView(cl);
            }
        }

    }

    private class BtleScanCallback extends ScanCallback {

        private Map<String, BluetoothDevice> mScanResults;

        BtleScanCallback(Map<String, BluetoothDevice> scanResults) {
            mScanResults = scanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            mScanResults.put(deviceAddress, device);
        }
    }

    private void connectDevice(BluetoothDevice device) {
        MainActivity.GattClientCallback gattClientCallback = new MainActivity.GattClientCallback();
        connectedGatt = device.connectGatt(this, false, gattClientCallback);
    }

    private void enableCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        //set characteristics and descriptors to notify for constant updates whenever values are changed
        if(characteristic.getUuid().equals(batteryCharacteristicUuid)){
            boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
            if (characteristicWriteSuccess) {
                Log.d(TAG,"Characteristic notification set successfully for " + characteristic.getUuid().toString());
                for(BluetoothGattDescriptor descriptor : characteristic.getDescriptors()){
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    boolean notifySuccess = gatt.writeDescriptor(descriptor);
                    if(notifySuccess){
                        Log.d(TAG, "Successfully set notify on descriptor: " + descriptor.getUuid() + " for characteristic: " + characteristic.getUuid());
                    } else {
                        Log.d(TAG, "Unable to set notify on descriptor: " + descriptor.getUuid() + " for characteristic: " + characteristic.getUuid());
                    }
                }
            } else {
                Log.d(TAG,"Characteristic notification set failure for " + characteristic.getUuid().toString());
            }
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

            runOnUiThread(MainActivity.this::openConnectedPage);

            for(BluetoothGattService service : connectedGatt.getServices()){
                for(BluetoothGattCharacteristic gattChar : service.getCharacteristics()){
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(gattChar.getUuid());
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    enableCharacteristicNotification(gatt, characteristic);
                    onCharacteristicChanged(gatt, characteristic);
                }
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            super.onCharacteristicChanged(gatt, characteristic);
            byte[] messageBytes = characteristic.getValue();
            //byte[] messageBytes;
            int messageInt;

            if(characteristic.getUuid().equals(batteryCharacteristicUuid)){
                Log.d(TAG, "attempting to parse battery level");
                BluetoothGattService batteryServ = gatt.getService(batteryServiceUuid);
                BluetoothGattCharacteristic battLevel = batteryServ.getCharacteristic(batteryCharacteristicUuid);
                bleQueue.addRead(battLevel);
                processQueue();
                //messageBytes = battLevel.getValue();
                messageInt = battLevel.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);


                runOnUiThread(() -> {
                    setContentView(R.layout.activity_display_message);
                    TextView batteryLevelView = findViewById(R.id.battery_level);
                    if(batteryLevelView == null){
                        Log.d(TAG, "NULL BATT LEVEL VIEW");
                    } else {
                        Log.d(TAG, "NOT NULL BATT LEVEL VIEW");
                    }
                });

                Log.d(TAG, "Battery level: " + messageInt + "%");
                return;
            }

            if(messageBytes == null){
                Log.d(TAG, "No message parsed on characteristic.");
                return;
            }

            try {
                final StringBuilder stringBuilder = new StringBuilder(messageBytes.length);
                for(byte byteChar : messageBytes){
                    stringBuilder.append(String.format("%02x ", byteChar));
                }
                Log.d(TAG, "Char UUID: " + characteristic.getUuid());
                Log.d(TAG, "Received message: " + new String(messageBytes));
                Log.d(TAG, "Raw: " + stringBuilder.toString());
                //Log.d(TAG, "Formatted: " + messageInt);
            } catch (Exception e) {
                Log.e(TAG, "Unable to convert message bytes to string" + e.getMessage());
            }
            Log.d(TAG, "Received message (string value): " + characteristic.getStringValue(0));
        }
    }

    private void processQueue() {
        if (bleQueueIsFree) {
            bleQueueIsFree = false;
            QueueItem queueItem = bleQueue.getNextItem();
            if (queueItem == null) {
                bleQueueIsFree = true;
                return;
            } else {
                boolean status = false;
                switch (queueItem.itemType) {
                    case BLEQueue.ITEM_TYPE_READ:
                        status = connectedGatt.readCharacteristic(queueItem.characteristic);
                        break;
                    case BLEQueue.ITEM_TYPE_WRITE:
                        status = connectedGatt.writeCharacteristic(queueItem.characteristic);
                        break;
                    case BLEQueue.ITEM_TYPE_NOTIFICATION:
                        connectedGatt.setCharacteristicNotification(queueItem.characteristic, true);
                        BluetoothGattDescriptor descriptor = queueItem.characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            status = connectedGatt.writeDescriptor(descriptor);
                        } else {
                            status = false;
                        }
                        break;
                }
                if (!status) {
                    bleQueueIsFree = true;
                }
            }
        }
    }

    public void disconnectGattServer() {
        //disconnect;
        Log.d(TAG, "Attempting to disconnect " + deviceName);
        if (connectedGatt != null) {
            connectedGatt.disconnect();
            connectedGatt.close();
        }
    }

    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Log.d(TAG, "Requested user enables Bluetooth. Try starting the scan again.");
    }

    private boolean hasLocationPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }
}
