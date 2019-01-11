package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainTabbedActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static android.content.Context.BLUETOOTH_SERVICE;


public class PairingTabFragment extends Fragment {
    private static final String TAG = "Pairing Tab Fragment";

    public static final String TAB_NAME = "Pairing";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;
    private static final int SCAN_PERIOD = 10000;

    private boolean mScanning;
    private Handler mHandler;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScannerCompat mBluetoothLeScanner;
    private ScanCallback mScanCallback;

    private ProgressBar spinner;

    public static String deviceName;
    public static BluetoothDevice connectedDevice;

    View rootView;
    LinearLayout linLayout;
    LayoutInflater inflater;

    Boolean checkedWhileScanning = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        rootView = inflater.inflate(R.layout.fragment_tab_pairing, container, false);
        linLayout = rootView.findViewById(R.id.device_list);
        this.inflater = inflater;

        BluetoothManager bluetoothManager = (BluetoothManager) Objects.requireNonNull(getActivity()).getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        startScan();

        setRetainInstance(true);

        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    private void connectDevice() {
        Log.d(TAG, "Attempting to connect to: " + deviceName);
        ((MainTabbedActivity)Objects.requireNonNull(getActivity())).connectDevice(connectedDevice, deviceName);
    }

    private void disconnectDevice() {
        ((MainTabbedActivity)Objects.requireNonNull(getActivity())).disconnectDevice();
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

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        mScanCallback = new BtleScanCallback();
        mBluetoothLeScanner = BluetoothLeScannerCompat.getScanner();
        mBluetoothLeScanner.startScan(null, settings, mScanCallback);
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

        if(MainTabbedActivity.connectedDevice != null && !checkedWhileScanning){
            View view = inflater.inflate(R.layout.fragment_tab_pairing_row, null);
            linLayout.addView(view, 0);
            String objName = connectedDevice.getName() == null ? connectedDevice.getAddress() : connectedDevice.getName();
            ((TextView) view.findViewById(R.id.text)).setText(objName);
            Switch switchButton = view.findViewById(R.id.button);
            switchButton.setChecked(true);
            switchButton.setOnClickListener( v -> {
                if (switchButton.isChecked()) {
                    connectDevice();
                    switchButton.setId(R.id.connected_button);
                    Toast.makeText(this.getContext(), "connecting...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this.getContext(), "disconnecting...", Toast.LENGTH_LONG).show();
                    disconnectDevice();
                }
            });
        } else if(checkedWhileScanning) {
            checkedWhileScanning = false;
        } else {
            Log.d(TAG, "No connected device found");
        }

        Button scanAgainButton = new Button(rootView.getContext());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScan();
                } else {
                    Log.d(TAG, "Location permission denied");
                }
            }
        }
    }

    private class BtleScanCallback extends no.nordicsemi.android.support.v18.scanner.ScanCallback {

        private Map<String, BluetoothDevice> scanResults;

        BtleScanCallback() {
            scanResults = new HashMap<>();
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
            String objName = obj.getName() == null ? deviceAddress : obj.getName();
            Log.d(TAG, "Found device: " + objName);
            if(!scanResults.containsKey(deviceAddress)){
                View view = inflater.inflate(R.layout.fragment_tab_pairing_row, null);
                linLayout.addView(view, linLayout.indexOfChild(spinner));

                String displayName;
                if(objName.equals(deviceAddress)){
                    displayName = "Unknown";
                } else {
                    displayName = objName;
                }

                ((TextView) view.findViewById(R.id.text)).setText(displayName);
                ((TextView) view.findViewById(R.id.address)).setText(deviceAddress);

                if(grey){
                    view.setBackgroundColor(Color.parseColor("#e0e0e0"));
                    grey = false;
                } else {
                    grey = true;
                }

                Switch switchButton = view.findViewById(R.id.button);
                switchButton.setChecked(false);
                switchButton.setOnClickListener( v -> {
                    if (switchButton.isChecked()) {
                        if(mScanning){
                            checkedWhileScanning = true;
                        }
                        deviceName = objName;
                        connectedDevice = obj;
                        switchButton.setId(R.id.connected_button);
                        connectDevice();
                        Toast.makeText(rootView.getContext(), "connecting...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(rootView.getContext(), "disconnecting...", Toast.LENGTH_LONG).show();
                        disconnectDevice();
                    }
                });

                scanResults.put(deviceAddress, obj);
            }
        }
    }
}
