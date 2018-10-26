package com.wearables.ge.wearables_ble_receiver;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class DisplayMessageActivity extends AppCompatActivity {
    private static String TAG = "Display_Message";

    public BluetoothGatt connectedGatt = MainActivity.connectedGatt;
    public String deviceName = MainActivity.deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        Toolbar myToolbar = findViewById(R.id.display_message_toolbar);
        myToolbar.setTitle(deviceName);
        setSupportActionBar(myToolbar);
        ActionBar myActionBar = getSupportActionBar();
        if(myActionBar != null){
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }
        showConnectedMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.update_rate:
                Log.d(TAG, "update_rate button pushed");
                //action for update_rate click
                return true;
            case R.id.device_id:
                Log.d(TAG, "device_id button pushed");
                //action for device_id
                return true;
            case R.id.rename:
                Log.d(TAG, "rename button pushed");
                //action for rename
                return true;
            case R.id.disconnect:
                //action for disconnect
                Log.d(TAG, "Disconnect button pushed");
                disconnectGattServer();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.dev_mode:
                Log.d(TAG, "dev_mode button pushed");
                //action for dev_mode
                return true;
            default:
                Log.d(TAG, "No menu item found for " + item.getItemId());
                return super.onOptionsItemSelected(item);
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

    public void showConnectedMessage(){
        LinearLayout linLayout = findViewById(R.id.rootContainer2);
        if(linLayout != null){
            linLayout.removeAllViews();

            TextView textView = new TextView(this);
            textView.setText("Connected to device: " + deviceName);
            textView.setGravity(Gravity.CENTER);
            linLayout.addView(textView);


            /*TextView batteryLevelView = new TextView(this);
            batteryLevelView.setText("Battery level: undefined");
            batteryLevelView.setGravity(Gravity.CENTER);
            batteryLevelView.setId(R.id.battery_level);
            linLayout.addView(batteryLevelView);*/

            /*for(BluetoothGattService obj : connectedGatt.getServices()){
                textView = new TextView(this);
                textView.setText("Found service UUID: " + obj.getUuid());
                textView.setGravity(Gravity.CENTER);
                linLayout.addView(textView);

                for(BluetoothGattCharacteristic obj2 : obj.getCharacteristics()){
                    textView = new TextView(this);
                    textView.setText("characteristic: " + obj2.getUuid() + " containing descriptor: ");
                    textView.setGravity(Gravity.CENTER);
                    linLayout.addView(textView);

                    for(BluetoothGattDescriptor obj3 : obj2.getDescriptors()){
                        textView = new TextView(this);
                        textView.setText(obj3.getUuid().toString());
                        textView.setGravity(Gravity.CENTER);
                        linLayout.addView(textView);
                    }
                }
            }*/
        }
    }

    public void setBattLevelValue(int batteryLevel){
        LinearLayout linLayout = findViewById(R.id.rootContainer2);
        TextView batteryLevelView = findViewById(R.id.battery_level);
    }
}
