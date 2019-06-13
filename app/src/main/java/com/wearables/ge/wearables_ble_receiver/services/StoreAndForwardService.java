package com.wearables.ge.wearables_ble_receiver.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.room.Room;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttMessageDeliveryCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.wearables.ge.wearables_ble_receiver.persistence.StoreAndForwardData;
import com.wearables.ge.wearables_ble_receiver.persistence.StoreAndForwardDatabase;

import org.json.JSONException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class StoreAndForwardService extends Service {

    public static final String DATA_EXTRA_NAME = "STORE_AND_FORWARD_DATA";
    public static final String ENTRY_LIMIT_EXTRA_NAME = "STORE_AND_FORWARD_ENTRY_LIMIT";
    public static final String SAVE_WAIT_TIME_MS_EXTRA_NAME = "STORE_AND_FORWARD_SAVE_WAIT_TIME_MS";
    public static final String SYNC_WAIT_TIME_MS_EXTRA_NAME = "STORE_AND_FORWARD_SYNC_WAIT_TIME_MS";

    private static final String TAG = "StoreAndForwardService";
    private static final String MQTT_CLIENT_ID_PREPEND = "wearables_volt_app/";
    private static final String MQTT_IOT_CONFIG_KEY = "IoTConfig";
    private static final String MQTT_IOT_ENDPOINT_KEY = "IoTEndpoint";
    private static final String MQTT_IOT_BASE_TOPIC_KEY = "IoTBaseTopic";
    private static String USER_ID = "user_1";
    private static String MQTT_IOT_BASE_TOPIC_DEFAULT = "wearables/raw/".concat(USER_ID);
    private static final String DATABASE_NAME = "store-and-forward";
    private static final long DEFAULT_ENTRY_LIMIT = 1024;
    private static final long DEFAULT_SAVE_WAIT_TIME_MS = 1000;
    private static final long DEFAULT_SYNC_WAIT_TIME_MS = 60000;

    private StoreAndForwardBinder mBinder;

    private AtomicLong mEntryLimit;

    private List<StoreAndForwardData> mDataList;
    private StoreAndForwardDatabase mDatabase;

    private AtomicLong mSaveWaitTimeMs;
    private AtomicLong mSyncWaitTimeMs;

    private Thread mSendThread;
    private Thread mSyncThread;

    private AtomicBoolean mRunning;
    private AtomicBoolean mInitialized;
    private AtomicBoolean mConnected;

    private AWSIotMqttManager mMqttManager;
    private String mMqttTopic;

    // This binder will be used to communicate with the parent activity
    public class StoreAndForwardBinder extends Binder {
        StoreAndForwardService mService;

        private StoreAndForwardBinder(final StoreAndForwardService service) {
            super();
            this.mService = service;
        }

        public StoreAndForwardService getService() {
            return mService;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Set all the config options
        mEntryLimit = new AtomicLong(DEFAULT_ENTRY_LIMIT);
        mSaveWaitTimeMs = new AtomicLong(DEFAULT_SAVE_WAIT_TIME_MS);
        mSyncWaitTimeMs = new AtomicLong(DEFAULT_SYNC_WAIT_TIME_MS);
        if (intent != null) {
            // If configuration options have been sent for any of the limits set them
            if (intent.hasExtra(ENTRY_LIMIT_EXTRA_NAME)) {
                mEntryLimit.set(intent.getLongExtra(ENTRY_LIMIT_EXTRA_NAME, DEFAULT_ENTRY_LIMIT));
            }
            if (intent.hasExtra(SAVE_WAIT_TIME_MS_EXTRA_NAME)) {
                mSaveWaitTimeMs.set(intent.getLongExtra(SAVE_WAIT_TIME_MS_EXTRA_NAME, DEFAULT_SAVE_WAIT_TIME_MS));
            }
            if (intent.hasExtra(SYNC_WAIT_TIME_MS_EXTRA_NAME)) {
                mSyncWaitTimeMs.set(intent.getLongExtra(SYNC_WAIT_TIME_MS_EXTRA_NAME, DEFAULT_SYNC_WAIT_TIME_MS));
            }
        }

        // Set up the database
        mDatabase = Room.databaseBuilder(getApplicationContext(), StoreAndForwardDatabase.class, DATABASE_NAME).build();

        // Attempt to start the MQTT connection
        connect();

        // Start a thread to do the initial database stuff
        mInitialized = new AtomicBoolean(false);
        new Thread(() -> {
            synchronized (this) {
                // Delete all of the sent entries on disk
                mDatabase.storeAndForwardDataDao().deleteAllSent();

                // Get a chunk of the files on disk and load them into memory
                mDataList = mDatabase.storeAndForwardDataDao().getNotSent(mEntryLimit.get());
                mInitialized.set(true);
            }
        }).start();

        // Let the threads know that it is okay to start
        mRunning = new AtomicBoolean(true);

        // This is a simple thread that will wait for a configurable time before attempting to send all available data
        mSendThread = new Thread(() -> {
            while (mRunning.get()) {
                if (mInitialized.get()) {
                    // Send the data to the cloud
                    sendData();
                }

                // Wait for a configurable amount of time
                try {
                    synchronized (mSendThread) {
                        mSendThread.wait(mSaveWaitTimeMs.get());
                    }
                } catch (InterruptedException i) {
                    // Do nothing
                }
            }
        });
        mSendThread.start();

        // This is a simple thread that will wait for a configurable amount of time before attempting to flush all data to the disk mDatabase as well as clean the mDatabase
        mSyncThread = new Thread(() -> {
            while (mRunning.get()) {
                if (mInitialized.get()) {
                    // Flush the data from the in memory mDatabase to the disk
                    syncData();
                }

                // Wait for a configurable amount of time
                try {
                    synchronized (mSyncThread) {
                        mSyncThread.wait(mSyncWaitTimeMs.get());
                    }
                } catch (InterruptedException i) {
                    // Do nothing
                }
            }
        });
        mSyncThread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Tell the threads it is time to stop
        mRunning.set(false);
        synchronized (mSendThread) {
            mSendThread.notify();
        }
        synchronized (mSyncThread) {
            mSyncThread.notify();
        }

        // Attempt to save and close the database gracefully (we cannot do this on the main thread, so start a thread to do it)
        new Thread(() -> {
            syncData();
            synchronized (mDatabase) {
                mDatabase.close();
            }
        }).start();
    }

    @Override
    public IBinder onBind(@Nullable Intent intent) {
        // Set up the binder
        mBinder = new StoreAndForwardBinder(StoreAndForwardService.this);
        return mBinder;
    }

    public void enqueue(long timestamp, String topic_suffix, String headerLine, String dataLine) {
        // Create an instance of the table data
        StoreAndForwardData data = new StoreAndForwardData();
        data.timestamp = timestamp;
        data.deviceId = topic_suffix;
        data.sent = false;
        data.dataLine = dataLine;

        // Save the data either in memory or to the database
        if (mInitialized.get()) {
            saveData(data);
        }
    }

    private void connect() {
        // Pull the IoT config out of the configuration
        String iotEndpoint = "";
        mMqttTopic = "";
        final AWSConfiguration configuration = IdentityManager.getDefaultIdentityManager().getConfiguration();
        try {
            iotEndpoint = configuration.optJsonObject(MQTT_IOT_CONFIG_KEY).getString(MQTT_IOT_ENDPOINT_KEY);
            mMqttTopic = configuration.optJsonObject(MQTT_IOT_CONFIG_KEY).getString(MQTT_IOT_BASE_TOPIC_KEY);
        } catch (JSONException j) {
            // Do nothing for now
        }

        // If the topic wasn't set to anything use the default topic
        if (mMqttTopic.isEmpty()) {
            mMqttTopic = MQTT_IOT_BASE_TOPIC_DEFAULT;
        }


        // Start the connection, this will also handle states where we are reconnecting, disconnecting, etc.
        mConnected = new AtomicBoolean(false);
        mMqttManager = new AWSIotMqttManager(MQTT_CLIENT_ID_PREPEND + UUID.randomUUID().toString(), iotEndpoint);
        final AWSCredentialsProvider provider = IdentityManager.getDefaultIdentityManager().getCredentialsProvider();
        mMqttManager.connect(provider, (status, throwable) -> {
            switch (status) {
                case Connected:
                    mConnected.set(true);
                    break;
                case Connecting:
                case Reconnecting:
                case ConnectionLost:
                    // TODO: See if we want to do anything more specific for these cases
                    mConnected.set(false);
            }
        });
    }

    synchronized private void sendData() {
        // Loop through all not sent data and send it
        if (mConnected.get()) {
            for (final StoreAndForwardData data : mDataList) {
                // If the entry has not yet been sent, send it
                if (!data.sent) {
                    try {
                        final String topic = (mMqttTopic.endsWith("/")) ?
                                (mMqttTopic + data.deviceId) : (mMqttTopic + "/" + data.deviceId);
                        mMqttManager.publishString(data.toString(), topic, AWSIotMqttQos.QOS0, (status, userData) -> {
                            if (status == AWSIotMqttMessageDeliveryCallback.MessageDeliveryStatus.Success) {
                                // If we were able to publish, set sent to true
                                synchronized (data) {
                                    data.sent = true;
                                }
                            }
                        }, null);
                    } catch (Exception e) {
                        Log.d(TAG, "Unable to publish data: " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    public void forceSend(long timestamp, String topic_suffix, String headerLine, String dataLine) {
        // Create an instance of the table data
        StoreAndForwardData data = new StoreAndForwardData();
        data.timestamp = timestamp;
        data.deviceId = topic_suffix;
        data.sent = false;
        data.dataLine = dataLine;

        // Save the data either in memory or to the database
        if (mInitialized.get()) {
            try {
                final String topic = (mMqttTopic.endsWith("/")) ?
                        (mMqttTopic + data.deviceId) : (mMqttTopic + "/" + data.deviceId);
                mMqttManager.publishString(data.toString(), topic, AWSIotMqttQos.QOS0, (status, userData) -> {}, null);
            } catch (Exception e) {
                Log.d(TAG, "Unable to publish data: " + e.getLocalizedMessage());
            }
        }
    }

    synchronized private void saveData(StoreAndForwardData data) {
        // If the mDataList is already too full, save the data to the mDatabase
        if (mDataList.size() < mEntryLimit.get()) {
            mDataList.add(data);
        } else {
            mDatabase.storeAndForwardDataDao().insert(data);
        }

        // Notify the sending thread that it should check again
        synchronized (mSendThread) {
            mSendThread.notify();
        }
    }

    synchronized private void syncData() {
        // Save all entries in the mDataList to the mDatabase
        if (!mDataList.isEmpty()) {
            mDatabase.storeAndForwardDataDao().insertAll(mDataList.toArray(new StoreAndForwardData[0]));
        }

        // Delete all sent entries from the database
        mDatabase.storeAndForwardDataDao().deleteAllSent();

        // Refresh the mDataList so it is synced with the database
        if (mRunning.get()) {
            mDataList.clear();
            mDataList = mDatabase.storeAndForwardDataDao().getNotSent(mEntryLimit.get());
        }
    }

    public void updateUserID(){
        SharedPreferences settings = getSharedPreferences("pref", 0);
        USER_ID = settings.getString("user_id", "user_1");
        MQTT_IOT_BASE_TOPIC_DEFAULT = "wearables/raw/".concat(USER_ID);

        try{
            mMqttManager.disconnect();
            connect();
        }
        catch (Exception e){
            Log.d(TAG, "Error updating user ID");
        }
    }
}
