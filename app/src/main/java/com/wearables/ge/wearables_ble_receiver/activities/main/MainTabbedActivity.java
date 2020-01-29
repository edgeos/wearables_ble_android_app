
package com.wearables.ge.wearables_ble_receiver.activities.main;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.fragments.DeviceTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.main.fragments.EventsTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.main.fragments.HistoryTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.main.fragments.PairingTabFragment;
import com.wearables.ge.wearables_ble_receiver.services.BluetoothService;
import com.wearables.ge.wearables_ble_receiver.services.LocationService;
import com.wearables.ge.wearables_ble_receiver.services.StoreAndForwardService;
import com.wearables.ge.wearables_ble_receiver.utils.AccelerometerData;
import com.wearables.ge.wearables_ble_receiver.utils.AccelerometerJsonObject;
import com.wearables.ge.wearables_ble_receiver.utils.Data;
import com.wearables.ge.wearables_ble_receiver.utils.GasSensorData;
import com.wearables.ge.wearables_ble_receiver.utils.OpticalData;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageJsonObject;
import com.wearables.ge.wearables_ble_receiver.utils.TempHumidPressureJsonObject;
import com.wearables.ge.wearables_ble_receiver.utils.BLEQueue;
import com.wearables.ge.wearables_ble_receiver.utils.GattAttributes;
import com.wearables.ge.wearables_ble_receiver.utils.TempHumidPressure;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageAlarmStateChar;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;

public class MainTabbedActivity extends FragmentActivity implements ActionBar.TabListener {
    private static final String TAG = "Main Tabbed Activity";

    private static final String BT_DEV = "BluetoothDevice";
    private static final String BT_NAME = "BluetoothDeviceName";

    /**
     * The {@link PagerAdapter} that will provide fragments for each of the
     * three primary sections of the app. We use a {@link FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a {@link FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the app, one at a
     * time.
     */
    ViewPager mViewPager;

    private String m_Text = ""; // Temporary text holder for user input for user_id


    private DeviceTabFragment mDeviceTabFragment;
    private PairingTabFragment mPairingTabFragment;
    private EventsTabFragment mEventsTabFragment;
    private HistoryTabFragment mHistoryTabFragment;

    public static String ARG_SECTION_NUMBER = "section_number";

    boolean mBound;
    public BluetoothService mService;
    public static BluetoothDevice connectedDevice;
    public static String connectedDeviceName;
    public static String connectedDeviceAddr;

    public boolean devMode;
    public Menu menuBar;

    public int lastPeak;
    public Long lastPeakTime;

    // This is a really gross way to throttle the number of messages we send to the cloud
    private long voltageMessageCount = 0;
    private long acceleromterMessageCount = 0;
    private long tempHumidPressureMessageCount = 0;

    private boolean wasAlarming = false;
    final private long voltageMaxMessageCount = 10;
    final private long acceleromterMaxMessageCount = 0;
    final private long tempHumidPressureMaxMessageCount = 2;

    final private long volt_abbreviated_message_timer_ms = 1000;
    final private long volt_full_message_timer_ms = 5000;

    // Do some more crappy throttling on the graphs
    private long voltageCount = 0;
    final private long maxVoltageCount = 10;
    private long accelerometerCount = 0;
    final private long maxAccelerometerCount = 10;
    private long tempHumidityCount = 0;
    final private long maxTempHumidityCount = 10;

    public StoreAndForwardService mStoreAndForwardService;
    // This object will be used to aggregate the data and then push it to the store and forward queue
    private VoltageJsonObject mVoltageJsonObject = new VoltageJsonObject(volt_abbreviated_message_timer_ms, volt_full_message_timer_ms);
    private AccelerometerJsonObject mAccelerometerJsonObject = new AccelerometerJsonObject();
    private TempHumidPressureJsonObject mTempHumidPressureJsonObject = new TempHumidPressureJsonObject();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences("pref", 0);
        m_Text = settings.getString("user_id", "");
        if(m_Text.equals("")){
            changeUser();
        }

        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_tabbed_main);

        // Initialize the fragments
        mDeviceTabFragment = new DeviceTabFragment();
        mPairingTabFragment = new PairingTabFragment();
        mEventsTabFragment = new EventsTabFragment();
        mHistoryTabFragment = new HistoryTabFragment();

        // Create the adapter that will return a fragment for each of the three primary sections of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), mDeviceTabFragment,
                mPairingTabFragment, mEventsTabFragment, mHistoryTabFragment);

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

        // try to connect to the last device
        SharedPreferences prefs = getPreferences(0);
        if(prefs.contains(BT_DEV) && prefs.contains(BT_NAME)) {
            BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
            if(bta != null) {
                BluetoothDevice btd = bta.getRemoteDevice(prefs.getString(BT_DEV, ""));
                if (btd != null)
                    connectDevice(btd, prefs.getString(BT_NAME, "dev"));
            }
        }
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

        // Start the storea and forward service
        final Intent storeAndForwardIntent = new Intent(this, StoreAndForwardService.class);
        startService(storeAndForwardIntent);
        bindService(storeAndForwardIntent, mStoreAndForwardConnection, Context.BIND_IMPORTANT);

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
            unbindService(mStoreAndForwardConnection);

            // Stop any running store and forward service
            final Intent storeAndForwardIntent = new Intent(this, StoreAndForwardService.class);
            stopService(storeAndForwardIntent);

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
            case R.id.change_user:
                Log.d(TAG, "Change User button pushed");
                changeUser();
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
            case R.id.clear_db:
                if(mStoreAndForwardService != null)
                    mStoreAndForwardService.ClearDB();
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
                mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_RENAME, input.getText().toString());
            });

            alert.setNegativeButton(R.string.dialog_cancel_button_message, (dialog, whichButton) -> Log.d(TAG, "Rename Device dialog closed"));

        } else {
            alert.setMessage("No device Connected");
        }
        alert.show();
    }

    public void changeUser(){

        //final View view = layoutInflater.inflate(R.layout.dialog_user_id, null);
        SharedPreferences settings = getSharedPreferences("pref", 0);
        String old_m_Text = settings.getString("user_id", "default_user");
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));

        if(m_Text.equals("")){
            m_Text = "default_user";
            settings.edit().putString("user_id", m_Text).apply();
        }

        builder.setMessage("Your current User ID is \"" + m_Text + "\". Here you can enter a new User ID:");

        //create edit text dialog
        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTextColor(Color.WHITE);
        input.setHint("User ID");

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                if(TextUtils.isEmpty(m_Text)) {
                    input.setError("Please enter a name.");
                    m_Text = settings.getString("user_id", "default_user");
                    Toast.makeText(getApplicationContext(), "User ID not changed - Please use non-empty name", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
                settings.edit().putString("user_id", m_Text).apply();
                Toast.makeText(getApplicationContext(), "User ID is now \"" + m_Text + "\"", Toast.LENGTH_SHORT).show();
                // If we're currently uploading data, we need to refresh the user_id
                if(mStoreAndForwardService != null && !m_Text.equals(old_m_Text)){
                    mStoreAndForwardService.updateUserID();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
        // there are sometimes notifications that get processed after disconnect
        // so we cache the device address so they can be processed
        connectedDeviceAddr = device.getAddress();
        mService.connectDevice(device);
        //once the device is connected, display the name in the device tab fragment
        mDeviceTabFragment.displayDeviceName(deviceName);
        Data.sm_sDeviceId = deviceName;
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
        Data.sm_sDeviceId = "Wedge XFF";
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
        // Disconnect the device
        disconnectDevice();

        if (mStoreAndForwardConnection != null) {
            unbindService(mStoreAndForwardConnection);
        }
        // Stop any running store and forward service
        final Intent storeAndForwardIntent = new Intent(this, StoreAndForwardService.class);
        stopService(storeAndForwardIntent);

        // Tell AWS to dump the credentials
        IdentityManager.getDefaultIdentityManager().signOut();
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
     * Connection callback method for the store and forward service
     */
    private ServiceConnection mStoreAndForwardConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StoreAndForwardService.StoreAndForwardBinder binder = (StoreAndForwardService.StoreAndForwardBinder) service;
            mStoreAndForwardService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Disconnnected");
        }
    };

    public void SetCloudConn(boolean b) { if(null!=mDeviceTabFragment) mDeviceTabFragment.SetCloudConn(b);}

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
                        if(mService.setNotifyOnCharacteristics()) {
                            mViewPager.setCurrentItem(1); // switch to the device tab
                            // remember our last connected device
                            SharedPreferences prefs = getPreferences(0);
                            prefs.edit().putString(BT_DEV, connectedDevice.getAddress());
                            prefs.edit().putString(BT_NAME, connectedDeviceName);
                            prefs.edit().commit();
                        } else {
                            // we didn't find our services, disconnect
                            Toast.makeText(getApplicationContext(), "Wrong Device -- Voltage Band Not Found", Toast.LENGTH_LONG).show();
                            disconnectDevice();
                        }
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

        DeviceTabFragment mDeviceTabFragment;
        PairingTabFragment mPairingTabFragment;
        EventsTabFragment mEventsTabFragment;
        HistoryTabFragment mHistoryTabFragment;

        AppSectionsPagerAdapter(FragmentManager fm, DeviceTabFragment deviceTabFragment,
                                PairingTabFragment pairingTabFragment, EventsTabFragment eventsTabFragment,
                                HistoryTabFragment historyTabFragment) {
            super(fm);

            // Save the fragments passed from the main activity
            this.mDeviceTabFragment = deviceTabFragment;
            this.mPairingTabFragment = pairingTabFragment;
            this.mEventsTabFragment = eventsTabFragment;
            this.mHistoryTabFragment = historyTabFragment;
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

    private void Enqueue(Data data) {
        if(mStoreAndForwardService != null) {
            new Thread(() -> {
                mStoreAndForwardService.enqueue((new Date()).getTime(), connectedDevice.getAddress().concat("/" + data.Type()), "", data.toJSONString());
            }).start();
        }
    }
    private boolean Enqueue(Data data, long nCnt, long nThresh) {
        if(nCnt < nThresh) return false;
        Enqueue(data);
        return true;
    }

    /**
     * Worker method for reading bluetooth data sent from a characteristic.
     * This will read any data from any characteristic and parse it to the correct format.
     * @param intent
     */
    public void readAvailableData(Intent intent) {
        //get the UUID of the incoming data
        UUID extraUuid = UUID.fromString(intent.getStringExtra(BluetoothService.EXTRA_UUID));
        //grab the raw data as a byte array
        byte[] extraData = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA);
        //sometimes the data is a single integer and we don't need to parse the byte array
        int extraIntData = intent.getIntExtra(BluetoothService.EXTRA_INT_DATA, 0);

        //stop here if there is no message to read or the device has disconnected
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
        //Log.d("DEBUG", "Received data " + value);

        if(value != null){
//            mAggregateJsonObject.setDeviceId(connectedDevice.getAddress());
            //for battery level, just show the battery level on the UI
            if(extraUuid.equals(GattAttributes.BATT_LEVEL_CHAR_UUID)){
                if(mDeviceTabFragment.isVisible()){
                    mDeviceTabFragment.updateBatteryLevel(extraIntData);
                }
                Log.d(TAG, "Battery level: " + extraIntData + "%");
            } else if(extraUuid.equals(GattAttributes.VOLTAGE_ALARM_STATE_CHARACTERISTIC_UUID)){
                mVoltageJsonObject.setDeviceId(connectedDeviceAddr);
                mVoltageJsonObject.setUserId(m_Text);

                //The voltage alarm state characteristic sends the largest messages and the most often
                Log.d(TAG, "VOLTAGE_ALARM_STATE value: " + value);

                //Create a new VoltageAlarmStateChar object with the message.
                //The VoltageAlarmStateChar class will do the heavy lifting with converting the raw message into usable data.
                VoltageAlarmStateChar voltageAlarmState = new VoltageAlarmStateChar(value);
                // Check timers to see if we should send the message (alarms excluded)
                Boolean peak_alarm = voltageAlarmState.getOverall_alarm();
                Boolean alarm_message = (peak_alarm && !wasAlarming) || (!peak_alarm && wasAlarming);

                mVoltageJsonObject.setVoltageAlarmData(voltageAlarmState);

                // Only send every maxMessagecount messages
                if (mStoreAndForwardService != null) {
                    if (alarm_message) {
                        new Thread(() -> {
                            mStoreAndForwardService.forceSend((new Date()).getTime(), connectedDeviceAddr.concat("/voltage"), "", mVoltageJsonObject.toJson(Boolean.TRUE));
                        }).start();
                    } else if (mVoltageJsonObject.timerCheck()){ // Switching to time-based check
                       //else if (voltageMessageCount++ >= voltageMaxMessageCount) {
                        new Thread(() -> {
                            mStoreAndForwardService.enqueue((new Date()).getTime(), connectedDeviceAddr.concat("/voltage"), "", mVoltageJsonObject.toJson(Boolean.FALSE));
                        }).start();
                        voltageMessageCount = 0;
                    }
                }
                wasAlarming = voltageAlarmState.getOverall_alarm();

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
                    if (voltageCount++ >= maxVoltageCount) {
                        mHistoryTabFragment.updateVoltageGraph(voltageAlarmState);
                    }

                    //next, we will calculate the peak between 40 and 70Hz bins
                    //since the bin size and number of bins may change, we will use them as variables
                    //to find the range of values between 40 and 70
                    int start = Math.round(40/voltageAlarmState.getFft_bin_size()) + 1;
                    int end = Math.round(70/voltageAlarmState.getFft_bin_size()) + 1;

                    //create a list of all the values within that range
                    List<Integer> peakRange = new ArrayList<>();
                    for(int i = start; i <= end; i++){
                        peakRange.add(voltageAlarmState.getCh1_fft_results().get(i));
                        peakRange.add(voltageAlarmState.getCh2_fft_results().get(i));
                        peakRange.add(voltageAlarmState.getCh3_fft_results().get(i));
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
                    mDeviceTabFragment.updateVoltageChart(voltageAlarmState);
                    //create a voltage event object for the device tab, this is not an alarm event so duration doesn't matter
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
                if(value.length() > 14) {
                    try {
                        // Log.d(TAG, "VOLTAGE_ALARM_CONFIG value: " + Data.parseHex(value, 1, 4));
                        mDeviceTabFragment.updateAlarmLevel(Data.parseHexInt(value, 1));
                    } catch (NumberFormatException e) {
                    }
                }
            } else if(extraUuid.equals(GattAttributes.ACCELEROMETER_DATA_CHARACTERISTIC_UUID)){
                //Get accelerometer data and send to UI
                AccelerometerData accelerometerData = new AccelerometerData(value);
                mAccelerometerJsonObject.setDeviceId(connectedDeviceAddr);
                mAccelerometerJsonObject.setUserId(m_Text);

                if(accelerometerData.getDate() != null){
                    if (accelerometerCount++ >= maxAccelerometerCount) {
                        mHistoryTabFragment.updateAccelerometerGraph(accelerometerData);
                    }
                }
                mAccelerometerJsonObject.setAccelerometerData(accelerometerData);
                Log.d(TAG, "ACCELEROMETER_DATA value: " + value);

                if(Enqueue(accelerometerData, ++acceleromterMessageCount, acceleromterMaxMessageCount)) {
                    acceleromterMessageCount = 0;
                }
                // Only send every acceleromterMaxMessageCount messages
//                if (mStoreAndForwardService != null) {
//                    if (acceleromterMessageCount++ >= acceleromterMaxMessageCount) {
//                        new Thread(() -> {
//                            mStoreAndForwardService.enqueue((new Date()).getTime(), connectedDevice.getAddress().concat("/accelerometer"), "", mAccelerometerJsonObject.toJson());
//                        }).start();
//                        acceleromterMessageCount = 0;
//                    }
//                }

            } else if(extraUuid.equals(GattAttributes.TEMP_HUMIDITY_PRESSURE_DATA_CHARACTERISTIC_UUID)){
                //display Temp/Humid/Pressure data on UI
                TempHumidPressure tempHumidPressure = new TempHumidPressure(value);
                mTempHumidPressureJsonObject.setDeviceId(connectedDeviceAddr);
                mTempHumidPressureJsonObject.setUserId(m_Text);

                if(tempHumidPressure.getDate() != null){
                    if(mDeviceTabFragment.isVisible()){
                        mDeviceTabFragment.updateHumidity(tempHumidPressure.getHumid());
                        mDeviceTabFragment.updateTemperature(tempHumidPressure.getTemp());
                        mDeviceTabFragment.updatePressure(tempHumidPressure.getPres());
                    }
                    if(mHistoryTabFragment.isVisible()){
                        if (tempHumidityCount++ >= maxTempHumidityCount) {
                            mHistoryTabFragment.updateTempHumidityPressureGraph(tempHumidPressure);
                        }
                    }
                }
                mTempHumidPressureJsonObject.setTempHumidPressureData(tempHumidPressure);
                Log.d(TAG, "TEMP_HUMIDITY_PRESSURE_DATA value: " + value);

                if(Enqueue(tempHumidPressure, ++tempHumidPressureMessageCount, tempHumidPressureMaxMessageCount))
                    tempHumidPressureMessageCount = 0;

//                // Only send every tempHumidPressureMaxMessageCount messages
//                if (mStoreAndForwardService != null) {
//                    if (tempHumidPressureMessageCount++ >= tempHumidPressureMaxMessageCount) {
//                        new Thread(() -> {
//                            mStoreAndForwardService.enqueue((new Date()).getTime(), connectedDevice.getAddress().concat("/temphumidpressure"), "", mTempHumidPressureJsonObject.toJson());
//                        }).start();
//                        tempHumidPressureMessageCount = 0;
//                    }
//                }
            } else if(extraUuid.equals(GattAttributes.GAS_SENSOR_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "GAS_SENSOR_DATA value: " + value);
                GasSensorData gsd = new GasSensorData (value);
                if(gsd.getDate() != null){
                    if(mDeviceTabFragment.isVisible()){
                        mDeviceTabFragment.updateCO2(gsd.getEquivCO2());
                        mDeviceTabFragment.updateTVOC(gsd.getTotalVOC());
                    }
                }
                Enqueue(gsd);
            } else if(extraUuid.equals(GattAttributes.OPTICAL_SENSOR_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "OPTICAL_SENSOR_DATA value: " + value);
                OpticalData od = new OpticalData(value);
                if(od.getDate() != null) {
                    if (mDeviceTabFragment.isVisible()) {
                        mDeviceTabFragment.updateProximity(od.getProximity());
                    }
                }
                Enqueue(od);
            } else if(extraUuid.equals(GattAttributes.STREAMING_DATA_CHARACTERISTIC_UUID)){
                Log.d(TAG, "STREAMING_DATA value: " + value);
            } else {
                Log.d(TAG, "Received message: " + value + " with UUID: " + extraUuid);
            }
        }
    }
}
