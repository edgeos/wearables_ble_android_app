package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainTabbedActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.content.Context.BLUETOOTH_SERVICE;


public class PairingTabFragment extends Fragment {
    private static final String TAG = "Pairing Tab Fragment";

    public static final String ARG_SECTION_NUMBER = "section_number";

    public static final String TAB_NAME = "Pairing";

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

    View rootView;
    LinearLayout linLayout;
    LayoutInflater inflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_pairing, container, false);
        linLayout = rootView.findViewById(R.id.device_list);
        this.inflater = inflater;

        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        startScan();

        return rootView;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    private void connectDevice() {
        Log.d(TAG, "Attempting to connect to: " + deviceName);
        ((MainTabbedActivity)getActivity()).connectDevice(connectedDevice, deviceName);
    }

    private void disconnectDevice(CharSequence text) {
        ((MainTabbedActivity)getActivity()).disconnectDevice();
    }

    public void startScan() {
        Log.d(TAG, "StartScan called");
        //add spinner to view
        spinner = rootView.findViewById(R.id.progressBar2);
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

        mScanCallback = new BtleScanCallback(mScanResults);
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

        if(MainTabbedActivity.connectedDevice != null){
            View view = inflater.inflate(R.layout.fragment_tab_pairing_row, null);
            linLayout.addView(view);
            String objName = connectedDevice.getName() == null ? connectedDevice.getAddress() : connectedDevice.getName();
            ((TextView) view.findViewById(R.id.text)).setText(objName);
            Switch switchButton = view.findViewById(R.id.button);
            switchButton.setChecked(true);
            switchButton.setOnClickListener( v -> {
                if (switchButton.isChecked()) {
                    connectDevice();
                    Toast.makeText(this.getContext(), "connecting...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this.getContext(), "disconnecting...", Toast.LENGTH_LONG).show();
                    disconnectDevice(switchButton.getText());
                }
            });
        } else {
            Log.d(TAG, "No connected device found");
        }

        //BEGIN SIMULATOR CODE CHUNK
        View view = inflater.inflate(R.layout.fragment_tab_pairing_row, null);
        linLayout.addView(view);
        ((TextView) view.findViewById(R.id.text)).setText("Simulator");

        Switch switchButton = view.findViewById(R.id.button);
        switchButton.setChecked(false);
        switchButton.setOnClickListener( v -> {
            if (switchButton.isChecked()) {
                deviceName = "Simulator";
                connectedDevice = null;
                startSimulator();
                Toast.makeText(this.getContext(), "connecting...", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this.getContext(), "disconnecting...", Toast.LENGTH_LONG).show();
                disconnectDevice(switchButton.getText());
            }
        });
        //END SIMULATOR CODE CHUNK

        Button scanAgainButton = new Button(this.getContext());
        scanAgainButton.setText(R.string.scan_button);
        scanAgainButton.setId(R.id.scan_button);
        scanAgainButton.setOnClickListener(v -> {
            linLayout.removeAllViews();
            linLayout.addView(spinner);
            startScan();
            Log.d(TAG, "Scan again button pressed");
        });
        linLayout.addView(scanAgainButton);
    }

    //BEGIN SIMULATOR CODE CHUNK
    private void startSimulator(){
        for(int i = 0; i < 10000; i++){
            ((MainTabbedActivity)Objects.requireNonNull(getActivity())).readAvailableData(null);
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    //END SIMULATOR CODE CHUNK

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
        return  ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
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

        Boolean grey = true;

        private void addScanResult(ScanResult result) {
            BluetoothDevice obj = result.getDevice();
            String deviceAddress = obj.getAddress();
            if(!mScanResults.containsKey(deviceAddress)){
                String objName = obj.getName() == null ? obj.getAddress() : obj.getName();
                Log.d(TAG, "Found device: " + objName);

                View view = inflater.inflate(R.layout.fragment_tab_pairing_row, null);
                linLayout.addView(view);

                ((TextView) view.findViewById(R.id.text)).setText(objName);

                if(grey){
                    view.setBackgroundColor(Color.parseColor("#f5f5f5"));
                    grey = false;
                } else {
                    grey = true;
                }

                Switch switchButton = view.findViewById(R.id.button);
                switchButton.setChecked(false);
                switchButton.setOnClickListener( v -> {
                    if (switchButton.isChecked()) {
                        deviceName = objName;
                        connectedDevice = obj;
                        connectDevice();
                        Toast.makeText(rootView.getContext(), "connecting...", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(rootView.getContext(), "disconnecting...", Toast.LENGTH_LONG).show();
                        disconnectDevice(switchButton.getText());
                    }
                });

                mScanResults.put(deviceAddress, obj);
            }
        }
    }
}
