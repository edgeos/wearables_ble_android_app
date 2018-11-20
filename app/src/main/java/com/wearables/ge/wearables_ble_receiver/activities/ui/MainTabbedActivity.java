
package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.services.BluetoothService;
import com.wearables.ge.wearables_ble_receiver.utils.BLEQueue;
import com.wearables.ge.wearables_ble_receiver.utils.GattAttributes;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageAlarmStateChar;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainTabbedActivity extends FragmentActivity implements ActionBar.TabListener {
    private static final String TAG = "Main Tabbed Activity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;

    static DeviceTabFragment mDeviceTabFragment = new DeviceTabFragment();
    static PairingTabFragment mPairingTabFragment = new PairingTabFragment();
    static EventsTabFragment mEventsTabFragment = new EventsTabFragment();
    static HistoryTabFragment mHistoryTabFragment = new HistoryTabFragment();

    public static String ARG_SECTION_NUMBER = "section_number";

    boolean mBound;
    BluetoothService mService;
    public BluetoothDevice connectedDevice;

    public static String connectedDeviceName;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed_main);

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        //bind this activity to bluetooth service
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
    }

    public void connectDevice(BluetoothDevice device, String deviceName){
        Log.d(TAG, "Attempting to connect to: " + deviceName);
        connectedDeviceName = deviceName;
        mService.connectDevice(device);
        mDeviceTabFragment.displayDeviceName(deviceName);
    }

    public void disconnectDevice(){
        mService.disconnectGattServer();
    }


    //connection callback for bluetooth service
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "attempting bind to bluetooth service");
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mService = binder.getService();

            //register the broadcast receiver
            registerReceiver(mGattUpdateReceiver, createIntentFilter());

            Log.d(TAG, "Bluetooth service bound successfully");
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "Bluetooth service disconnected");
            mBound = false;
        }
    };

    //create custom intent filter for broadcasting messages from the bluetooth service to this activity
    private static IntentFilter createIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    //this method handles broadcasts sent from the bluetooth service
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action != null){
                switch(action){
                    case BluetoothService.ACTION_GATT_SERVICES_DISCOVERED:
                        Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED broadcast received");
                        //showConnectedMessage();
                        mService.setNotifyOnCharacteristics();
                        break;
                    case BluetoothService.ACTION_DATA_AVAILABLE:
                        int extraType = intent.getIntExtra(BluetoothService.EXTRA_TYPE, -1);
                        if(extraType == BLEQueue.ITEM_TYPE_READ){
                            readAvailableData(intent);
                        }
                        break;
                }
            }
        }
    };

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {


        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, i + 1);

            switch (i) {
                case 0:
                    mPairingTabFragment.setArguments(args);
                    return mPairingTabFragment;
                case 1:
                    mDeviceTabFragment.setArguments(args);
                    return mDeviceTabFragment;
                case 2:
                    mEventsTabFragment.setArguments(args);
                    return mEventsTabFragment;
                case 3:
                    mHistoryTabFragment.setArguments(args);
                    return mHistoryTabFragment;

                default:

                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return PairingTabFragment.TAB_NAME;
                case 1:
                    return DeviceTabFragment.TAB_NAME;
                case 2:
                    return EventsTabFragment.TAB_NAME;
                case 3:
                    return HistoryTabFragment.TAB_NAME;

                default:
                    return "Section " + (position + 1);
            }
        }
    }

    public boolean voltage;
    public void readAvailableData(Intent intent){
        //BEGIN SIMULATOR CODE CHUNK
        UUID extraUuid;
        byte[] extraData = null;
        int extraIntData = 0;
        if(intent == null){
            //switch between voltage and temp/humid/pressure for simulator
            if(voltage){
                extraUuid = GattAttributes.VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID;
                voltage = false;
            } else {
                extraUuid = GattAttributes.TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID;
                voltage = true;
            }

        } else {
            extraUuid = UUID.fromString(intent.getStringExtra(BluetoothService.EXTRA_UUID));
            extraData = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);
            extraIntData = intent.getIntExtra(BluetoothService.EXTRA_INT_DATA, 0);
        }
        //END SIMULATOR CODE CHUNK

        //CODE COMMENTED FOR SIMULATOR, REPLACED BY ABOVE CODE CHUNK
        /*UUID extraUuid = UUID.fromString(intent.getStringExtra(BluetoothService.EXTRA_UUID));
        byte[] extraData = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);
        int extraIntData = intent.getIntExtra(BluetoothService.EXTRA_INT_DATA, 0);*/

        //BEGIN SIMULATOR CODE CHUNK
        String value = null;
        if(intent == null){
            if(extraUuid.equals(GattAttributes.VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID)){
                value = "00 00 00 00 40 08 ff 07 07 05 05 04 03 03 03 02 02 02 02 02 01 01 01 01 01 01 01 01 01 01 01 01 01 01 01 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 c5 07 06 05 04 03 02 03 03 01 01 01 01 01 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 5d 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ";
            } else if(extraUuid.equals(GattAttributes.TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID)){
                extraIntData = 45;
            }
        } else {
            if(extraData == null){
                Log.d(TAG, "No message parsed on characteristic.");
                return;
            }
            value = null;
            try {
                final StringBuilder stringBuilder = new StringBuilder(extraData.length);
                for(byte byteChar : extraData){
                    stringBuilder.append(String.format("%02x ", byteChar));
                }
                //TODO: send this data to AWS for storage
                value = stringBuilder.toString();
            } catch (Exception e) {
                Log.e(TAG, "Unable to convert message bytes to string" + e.getMessage());
            }
        }
        //END SIMULATOR CODE CHUNK

        //CODE COMMENTED FOR SIMULATOR, REPLACED BY ABOVE CHUNK
        /*if(extraData == null){
            Log.d(TAG, "No message parsed on characteristic.");
            return;
        }
        String value = null;
        try {
            final StringBuilder stringBuilder = new StringBuilder(extraData.length);
            for(byte byteChar : extraData){
                stringBuilder.append(String.format("%02x ", byteChar));
            }
            //TODO: send this data to AWS for storage
            value = stringBuilder.toString();
        } catch (Exception e) {
            Log.e(TAG, "Unable to convert message bytes to string" + e.getMessage());
        }*/

        if(extraUuid.equals(GattAttributes.BATT_LEVEL_CHAR_UUID)){
            mDeviceTabFragment.updateBatteryLevel(extraIntData);
            Log.d(TAG, "Battery level: " + extraIntData + "%");
        } else if(extraUuid.equals(GattAttributes.VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID)){
            Log.d(TAG, "VOLTAGE_ALARM_STATE value: " + value);
            VoltageAlarmStateChar voltageAlarmState = new VoltageAlarmStateChar(value);
            mHistoryTabFragment.updateGraph(voltageAlarmState);
        } else if(extraUuid.equals(GattAttributes.VOLTAGE_ALARM_CONFIG_CHARACTERISTIC_UUID)){
            Log.d(TAG, "VOLTAGE_ALARM_CONFIG value: " + value);
        } else if(extraUuid.equals(GattAttributes.ACCELEROMETER_DATA_CHARACTERISTIC_UUID)){
            mDeviceTabFragment.updateVoltageSensorStatus(String.valueOf(extraIntData));
            Log.d(TAG, "ACCELEROMETER_DATA value: " + value);
        } else if(extraUuid.equals(GattAttributes.TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID)){
            mDeviceTabFragment.updateHumidity(extraIntData);
            mDeviceTabFragment.updateTemperature(extraIntData);
            mDeviceTabFragment.updateVOC(extraIntData);
            //mDeviceTabFragment.updateVOCGauge(extraIntData);
            Log.d(TAG, "TEMP_HUMIDITY_PRESSURE_DATA value: " + value);
        } else if(extraUuid.equals(GattAttributes.GAS_SENSOR_DATA_CHARACTERISTIC_UUID)){
            mDeviceTabFragment.updateSpo2Sensor(value);
            Log.d(TAG, "GAS_SENSOR_DATA value: " + value);
        } else if(extraUuid.equals(GattAttributes.OPTICAL_SENSOR_DATA_CHARACTERISTIC_UUID)){
            Log.d(TAG, "OPTICAL_SENSOR_DATA value: " + value);
        } else if(extraUuid.equals(GattAttributes.STREAMING_DATA_CHARACTERISTIC_UUID)){
            Log.d(TAG, "STREAMING_DATA value: " + value);
        } else {
            Log.d(TAG, "Received message: " + value + " with UUID: " + extraUuid);
        }

    }

}
