
package com.wearables.ge.wearables_ble_receiver.activities.main;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.ui.DeviceTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.ui.EventsTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.ui.HistoryTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.ui.PairingTabFragment;
import com.wearables.ge.wearables_ble_receiver.services.BluetoothService;
import com.wearables.ge.wearables_ble_receiver.services.LocationService;
import com.wearables.ge.wearables_ble_receiver.utils.AccelerometerData;
import com.wearables.ge.wearables_ble_receiver.utils.BLEQueue;
import com.wearables.ge.wearables_ble_receiver.utils.GattAttributes;
import com.wearables.ge.wearables_ble_receiver.utils.MqttManager;
import com.wearables.ge.wearables_ble_receiver.utils.TempHumidPressure;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageAlarmStateChar;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
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
    public BluetoothService mService;
    public static BluetoothDevice connectedDevice;

    public static String connectedDeviceName;

    public boolean devMode;
    public Menu menuBar;

    public int lastPeak;
    public Long lastPeakTime;

    private MqttManager mMqttMgr;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_tabbed_main);

        // Create the adapter that will return a fragment for each of the three primary sections of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }


        //bind this activity to bluetooth service
        Intent intent = new Intent(this, BluetoothService.class);
        if(!mBound){
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;
        }

        //start location service
        //Location service is not an extension of the service class and doesn't need to be bound to.
        //This is because we don't need the location service to send updates to the UI.
        //We only need to grab the latest coordinates from the location service.
        LocationService.startLocationService(this);

        //Let's connect to AWS IoT
        /*mMqttMgr = MqttManager.getInstance(this);
        mMqttMgr.connect();
        Log.i("Mqtt","Connected to AWS IoT");*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        this.menuBar = menu;
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
        registerReceiver(mGattUpdateReceiver, createIntentFilter());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mConnection != null){
            unbindService(mConnection);
            unregisterReceiver(mGattUpdateReceiver);
            mBound = false;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d(TAG, "Screen orientation is landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.d(TAG, "Screen orientation is portrait");
        }
    }

    //switch case logic for menu button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.device_id:
                Log.d(TAG, "device_id button pushed");
                showDeviceID();
                return true;
            case R.id.rename:
                Log.d(TAG, "rename button pushed");
                renameDevice();
                return true;
            case R.id.disconnect:
                //action for disconnect
                Log.d(TAG, "Disconnect button pushed");
                if(connectedDevice != null){
                    disconnectDevice();
                }
                return true;
            case R.id.logout:
                logout();
                return true;
            case R.id.dev_mode:
                Log.d(TAG, "dev_mode button pushed");
                //dev mode action
                switchModes();
                return true;
            default:
                Log.d(TAG, "No menu item found for " + item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }

    public void renameDevice(){
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));

        if(connectedDevice != null){
            alert.setMessage(R.string.rename_device_modal_message);

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setTextColor(Color.WHITE);

            alert.setView(input);

            alert.setPositiveButton(R.string.dialog_accept_button_message, (dialog, whichButton) -> {
                connectedDevice.fetchUuidsWithSdp();
                mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_RENAME, input.getText().toString());
            });

            alert.setNegativeButton(R.string.dialog_cancel_button_message, (dialog, whichButton) -> Log.d(TAG, "Rename Device dialog closed"));

        } else {
            alert.setMessage("No device Connected");
        }
        alert.show();
    }

    public void connectDevice(BluetoothDevice device, String deviceName){
        Log.d(TAG, "Attempting to connect to: " + deviceName);
        connectedDeviceName = deviceName;
        connectedDevice = device;
        mService.connectDevice(device);
        mDeviceTabFragment.displayDeviceName(deviceName);
    }

    public void disconnectDevice(){
        mService.disconnectGattServer();
        connectedDevice = null;
        connectedDeviceName = null;
        Switch button = findViewById(R.id.connected_button);
        if(button != null){
            button.setChecked(false);
        }
    }

    public void showDeviceID(){
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        if(connectedDevice != null){
            alert.setMessage(getString(R.string.show_device_id, connectedDevice.getAddress()));
        } else {
            alert.setMessage(getString(R.string.show_device_id, "No device connected"));
        }
        alert.show();
    }

    public void switchModes() {
        if(connectedDevice != null){
            MenuItem devModeItem = menuBar.findItem(R.id.dev_mode);
            if(!devMode){
                mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_MODE, Character.toString((char) 2));
                devModeItem.setTitle(R.string.normal_mode_menu_item);
                devMode = true;
            } else {
                mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_MODE, Character.toString((char) 1));
                devModeItem.setTitle(R.string.dev_mode_menu_item);
                devMode = false;
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
            alert.setMessage("No device connected");
            alert.show();
        }
    }

    public void switchDevModeMenuItems() {
        MenuItem devModeItem = menuBar.findItem(R.id.dev_mode);
        if(devMode){
            devModeItem.setTitle(R.string.normal_mode_menu_item);
        } else {
            devModeItem.setTitle(R.string.dev_mode_menu_item);
        }
    }

    public void logout(){
        IdentityManager.getDefaultIdentityManager().signOut();
        disconnectDevice();
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
            if (action != null) {
                switch (action) {
                    case BluetoothService.ACTION_GATT_SERVICES_DISCOVERED:
                        Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED broadcast received");
                        //good indication that the device is successfully connected
                        mDeviceTabFragment.setConnectedMessage(true);
                        Toast.makeText(mPairingTabFragment.getContext(), "Device Connected", Toast.LENGTH_LONG).show();
                        mService.setNotifyOnCharacteristics();
                        break;
                    case BluetoothService.ACTION_DATA_AVAILABLE:
                        int extraType = intent.getIntExtra(BluetoothService.EXTRA_TYPE, -1);
                        if (extraType == BLEQueue.ITEM_TYPE_READ) {
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

        AppSectionsPagerAdapter(FragmentManager fm){
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

        if(value != null){
            if(extraUuid.equals(GattAttributes.BATT_LEVEL_CHAR_UUID)){
                if(mDeviceTabFragment.isVisible()){
                    mDeviceTabFragment.updateBatteryLevel(extraIntData);
                }
                Log.d(TAG, "Battery level: " + extraIntData + "%");
            } else if(extraUuid.equals(GattAttributes.VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID)){
                Log.d(TAG, "VOLTAGE_ALARM_STATE value: " + value);

                //attempt to read threshold value
                /*mService.readCharacteristic(BluetoothService.connectedGatt
                        .getService(GattAttributes.VOLTAGE_WRISTBAND_SERVICE_UUID)
                        .getCharacteristic(GattAttributes.VOLTAGE_ALARM_CONFIG_CHARACTERISTIC_UUID));*/

                VoltageAlarmStateChar voltageAlarmState = new VoltageAlarmStateChar(value);
                if(voltageAlarmState.getDevMode()){
                    if(!devMode){
                        devMode = true;
                        switchDevModeMenuItems();
                    }
                    mHistoryTabFragment.updateVoltageGraph(voltageAlarmState);
                    //get peak between 40 and 70Hz bins
                    int start = Math.round(40/voltageAlarmState.getFft_bin_size()) + 1;
                    int end = Math.round(70/voltageAlarmState.getFft_bin_size()) + 1;
                    List<Integer> peakRange = new ArrayList<>();
                    for(int i = start; i <= end; i++){
                        peakRange.add(voltageAlarmState.getCh1_fft_results().get(i));
                    }
                    int peak = Collections.max(peakRange);

                    //TODO: read the alarm threshold config value to determine an event
                    int threshold = mDeviceTabFragment.alarmLevel;
                    if(peak != lastPeak && peak > threshold){
                        Long peakTime = Calendar.getInstance().getTimeInMillis();
                        Long duration;
                        if(lastPeakTime == null){
                            duration = 0L;
                        } else {
                            duration = peakTime - lastPeakTime;
                        }

                        VoltageEvent voltageEvent = new VoltageEvent(lastPeak, duration);
                        mEventsTabFragment.voltageEvents.add(voltageEvent);
                        lastPeak = peak;
                        lastPeakTime = peakTime;
                        if(mEventsTabFragment.isVisible()){
                            mEventsTabFragment.addEventItem(voltageEvent);
                        }
                    }
                    VoltageEvent voltageEvent = new VoltageEvent(peak, 0L);
                    mDeviceTabFragment.updateGraph(voltageEvent);
                    if(mDeviceTabFragment.isVisible()){
                        mDeviceTabFragment.updateVoltageLevel(peak);
                    }
                } else {
                    if(devMode){
                        devMode = false;
                        switchDevModeMenuItems();
                    }
                }
            } else if(extraUuid.equals(GattAttributes.VOLTAGE_ALARM_CONFIG_CHARACTERISTIC_UUID)){
                Log.d(TAG, "VOLTAGE_ALARM_CONFIG value: " + value);
            } else if(extraUuid.equals(GattAttributes.ACCELEROMETER_DATA_CHARACTERISTIC_UUID)){
                AccelerometerData accelerometerData = new AccelerometerData(value);
                if(accelerometerData.getDate() != null){
                    mHistoryTabFragment.updateAccelerometerGraph(accelerometerData);
                }
                Log.d(TAG, "ACCELEROMETER_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID)){
                TempHumidPressure tempHumidPressure = new TempHumidPressure(value);
                if(tempHumidPressure.getDate() != null){
                    if(mDeviceTabFragment.isVisible()){
                        mDeviceTabFragment.updateHumidity(tempHumidPressure.getHumid());
                        mDeviceTabFragment.updateTemperature(tempHumidPressure.getTemp());
                        mDeviceTabFragment.updatePressure(tempHumidPressure.getPres());
                    }
                    if(mHistoryTabFragment.isVisible()){
                        mHistoryTabFragment.updateTempHumidityPressureGraph(tempHumidPressure);
                    }
                }
                Log.d(TAG, "TEMP_HUMIDITY_PRESSURE_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.GAS_SENSOR_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "GAS_SENSOR_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.OPTICAL_SENSOR_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "OPTICAL_SENSOR_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.STREAMING_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "STREAMING_DATA value: " + value);
            } else {
                Log.d(TAG, "Received message: " + value + " with UUID: " + extraUuid);
            }

            /****
             * Send data to AWS IoT, in the next phase if real-time streaming is not required and App based storage
             * is the way to go, the data needs to be send to local storage
              */
            /*if(value != null && mMqttMgr.getConnectionStatus() == MqttManager.ConnectionStatus.CONNECTED) {
                Log.d(TAG, "{ \"data\":\"" + value + "\"}");
                mMqttMgr.publish("ge/sensor/telemetry/data", "{ \"data\":\"" + value + "\"}");
            }
            else
            {
                Log.e(TAG, "Skipping  message as we are either not Connected to AWS IoT or the message is null");
            }*/
        }
    }
}
