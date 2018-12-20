
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
import android.text.InputFilter;
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
        //when this activity is resumed, rebind the bluetooth service
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mBound = true;
        //and re-register the broadcast receiver
        registerReceiver(mGattUpdateReceiver, createIntentFilter());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //when the activity is destroyed, unbind the bluetooth service
        //and unregister the broadcast receiver
        if(mConnection != null){
            unbindService(mConnection);
            unregisterReceiver(mGattUpdateReceiver);
            mBound = false;
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

    /**
     * Opens a dialog for the user to enter a new name for the connected device.
     * Will call the writeToVoltageAlarmConfigChar method in the bluetooth service with a "rename" message type.
     */
    public void renameDevice(){
        //create alert dialog with custom alert style
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));

        //If there is no device connected, don't allow the user to enter a name
        if(connectedDevice != null){
            alert.setMessage(R.string.rename_device_modal_message);

            //create edit text dialog
            final EditText input = new EditText(this);
            //set the max length of the field to 16 characters, since the board can only take a rename up to a length of 16
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setTextColor(Color.WHITE);

            alert.setView(input);

            //when the user accepts the entered name, call the write method in the bluetooth service to make the change
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

    /**
     * Connect the selected bluetooth device and set the global variables for device name and connected device
     * @param device
     * @param deviceName
     */
    public void connectDevice(BluetoothDevice device, String deviceName){
        Log.d(TAG, "Attempting to connect to: " + deviceName);
        connectedDeviceName = deviceName;
        connectedDevice = device;
        mService.connectDevice(device);
        //once the device is connected, display the name in the device tab fragment
        mDeviceTabFragment.displayDeviceName(deviceName);
    }

    /**
     * Disconnect the connected bluetooth device
     */
    public void disconnectDevice(){
        mService.disconnectGattServer();
        connectedDevice = null;
        connectedDeviceName = null;
        //negate the switch for that device on the pairing page if it is visible
        Switch button = findViewById(R.id.connected_button);
        if(button != null){
            button.setChecked(false);
        }
    }

    /**
     * Show the MAC address of the connected device. If no device is connected, alert that to the user.
     */
    public void showDeviceID(){
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        if(connectedDevice != null){
            alert.setMessage(getString(R.string.show_device_id, connectedDevice.getAddress()));
        } else {
            alert.setMessage(getString(R.string.show_device_id, "No device connected"));
        }
        alert.show();
    }

    /**
     * This method will switch modes from Dev(engineering) mode to normal mode and chang ethe menu item accordingly.
     * First, check if a device is connected and alert the user if no device is connected.
     * Write to the VoltageAlarmConfig characteristic with a "mode" write type to change the mode type.
     * Then change the menu item to the other option.
     */
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

    /**
     * Method used for just switching the devmode menu items if the modes have been switched on their own.
     * Most common use case for this would be when the use leaves the device in engineering mode and restarts the application.
     * The default menu option is "dev mode" so this activity will automatically determine what mode is enabled based on incoming characteristics
     * and change the menu items accordingly.
     */
    public void switchDevModeMenuItems() {
        MenuItem devModeItem = menuBar.findItem(R.id.dev_mode);
        if(devMode){
            devModeItem.setTitle(R.string.normal_mode_menu_item);
        } else {
            devModeItem.setTitle(R.string.dev_mode_menu_item);
        }
    }

    /**
     * Logout of AWS instance and disconnect the device.
     */
    public void logout(){
        IdentityManager.getDefaultIdentityManager().signOut();
        disconnectDevice();
    }

    /**
     * Connection callback method for the bluetooth service
     */
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

    /**
     * Create custom intent filter for broadcasting messages from the bluetooth service to this activity
     * @return IntentFilter object
     */
    private static IntentFilter createIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     * Broadcast receiver for getting messages back from the bluetooth service.
     * When a message is received, the message type is determined and appropriate action is taken.
     */
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

    /**
     * Worker method for reading bluetooth data sent from a characteristic.
     * This will read any data from any characteristic and parse it to the correct format.
     * @param intent
     */
    public void readAvailableData(Intent intent){
        //get the UUID of the incoming data
        UUID extraUuid = UUID.fromString(intent.getStringExtra(BluetoothService.EXTRA_UUID));
        //grab the raw data as a byte array
        byte[] extraData = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);
        //sometimes the data is a single integer and we don't need to parse the byte array
        int extraIntData = intent.getIntExtra(BluetoothService.EXTRA_INT_DATA, 0);

        //stop here if there is no message to read
        if(extraData == null){
            Log.d(TAG, "No message parsed on characteristic.");
            return;
        }

        //here attempt to convert the byte array to a string
        //the incoming data from the voltage bang should be hexadecimal strings
        String value = null;
        try {
            final StringBuilder stringBuilder = new StringBuilder(extraData.length);
            for(byte byteChar : extraData){
                stringBuilder.append(String.format("%02x ", byteChar));
            }
            value = stringBuilder.toString();
        } catch (Exception e) {
            Log.e(TAG, "Unable to convert message bytes to string" + e.getMessage());
        }

        //if we were able to get a string value from the byte array message, parse it based on the UUID with it
        if(value != null){
            //for battery level, just show the battery level on the UI
            if(extraUuid.equals(GattAttributes.BATT_LEVEL_CHAR_UUID)){
                if(mDeviceTabFragment.isVisible()){
                    mDeviceTabFragment.updateBatteryLevel(extraIntData);
                }
                Log.d(TAG, "Battery level: " + extraIntData + "%");
            } else if(extraUuid.equals(GattAttributes.VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID)){
                //The voltage alarm state characteristic sends the larges messages and the most often
                Log.d(TAG, "VOLTAGE_ALARM_STATE value: " + value);

                //Create a new VoltageAlarmStateChar object with the message.
                //The VoltageAlarmStateChar class will do the heavy lifting with converting the raw message into usable data.
                VoltageAlarmStateChar voltageAlarmState = new VoltageAlarmStateChar(value);

                //From the data included in the message, VoltageAlarmStateChar determines if the device is in DevMode or not.
                if(voltageAlarmState.getDevMode()){
                    //if the message was an engineering(dev) mode message, then it will have more characteristics to send to the UI
                    //And if the UI was not already in devmode then switch it now.
                    if(!devMode){
                        devMode = true;
                        switchDevModeMenuItems();
                    }

                    //update the graphs on the voltage page
                    //if the voltage tab has not been created yet then this will not do much
                    mHistoryTabFragment.updateVoltageGraph(voltageAlarmState);

                    //next, we will calculate the peak between 40 and 70Hz bins
                    //since the bin size and number of bins may change, we will use them as variables
                    //to find the range of values between 40 and 70
                    int start = Math.round(40/voltageAlarmState.getFft_bin_size()) + 1;
                    int end = Math.round(70/voltageAlarmState.getFft_bin_size()) + 1;

                    //create a list of all the values within that range
                    List<Integer> peakRange = new ArrayList<>();
                    for(int i = start; i <= end; i++){
                        peakRange.add(voltageAlarmState.getCh1_fft_results().get(i));
                    }

                    //get the highest value in that list, that is the peak
                    int peak = Collections.max(peakRange);

                    //now determine if that peak is above the alarm threshold to log an alarm event
                    //TODO: read the alarm threshold config value to determine an event
                    int threshold = mDeviceTabFragment.alarmLevel;
                    if(peak != lastPeak && peak > threshold){
                        //we also want to determine how long the peak lasted so log the peak time at each unique peak
                        Long peakTime = Calendar.getInstance().getTimeInMillis();
                        //find the duration by subtracting the time from the last peak
                        Long duration;
                        if(lastPeakTime == null){
                            duration = 0L;
                        } else {
                            duration = peakTime - lastPeakTime;
                        }

                        //create a Voltage Event to log on the Events page
                        VoltageEvent voltageEvent = new VoltageEvent(lastPeak, duration);
                        mEventsTabFragment.voltageEvents.add(voltageEvent);
                        lastPeak = peak;
                        lastPeakTime = peakTime;
                        if(mEventsTabFragment.isVisible()){
                            mEventsTabFragment.addEventItem(voltageEvent);
                        }
                    }
                    //create a voltage event object for the device tab, this is not an alarm event so duration doesn't matter
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
                //voltage alarm config is a write characteristic but may send a notification when it is written to
                Log.d(TAG, "VOLTAGE_ALARM_CONFIG value: " + value);
            } else if(extraUuid.equals(GattAttributes.ACCELEROMETER_DATA_CHARACTERISTIC_UUID)){
                //Get accelerometer data and send to UI
                AccelerometerData accelerometerData = new AccelerometerData(value);
                if(accelerometerData.getDate() != null){
                    mHistoryTabFragment.updateAccelerometerGraph(accelerometerData);
                }
                Log.d(TAG, "ACCELEROMETER_DATA value: " + value);
            } else if(extraUuid.equals(GattAttributes.TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID)){
                //display Temp/Humid/Pressure data on UI
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
        }
    }
}
