
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
import android.support.v7.app.AppCompatActivity;
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

public class NewAuthenticatorActivity extends FragmentActivity {
    private static final String TAG = "Authenticator Activity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_authenticator);
    }
}
