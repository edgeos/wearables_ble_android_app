package com.wearables.ge.wearables_ble_receiver;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int SCAN_PERIOD = 5000;

    private boolean mScanning;
    private Handler mHandler;
    private Map<String, BluetoothDevice> mScanResults;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;

    private ProgressBar spinner;

    public static String deviceName;
    public static BluetoothDevice connectedDevice;

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
        if(DisplayMessageActivity.connectedGatt != null){
            Log.d(TAG,"Main page, disconnecting from " + DisplayMessageActivity.deviceName);
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
                connectedDevice = obj;
                openConnectedPage();
                //connectDevice(obj);
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

    public void disconnectGattServer() {
        //disconnect;
        Log.d(TAG, "Attempting to disconnect " + DisplayMessageActivity.deviceName);
        if (DisplayMessageActivity.connectedGatt != null) {
            DisplayMessageActivity.connectedGatt.disconnect();
            DisplayMessageActivity.connectedGatt.close();
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
