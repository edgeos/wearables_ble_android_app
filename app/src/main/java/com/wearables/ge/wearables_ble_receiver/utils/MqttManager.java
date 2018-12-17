/*-------------------------------------------------------------------------------
# Copyright (c) 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
#
# This source code is subject to the terms found in the AWS Enterprise Customer Agreement.
#-------------------------------------------------------------------------------*/
package com.wearables.ge.wearables_ble_receiver.utils;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import org.json.JSONObject;

import java.util.UUID;


public class MqttManager {

   String mClientId;
   Context mCtxt;
   String mAwsIoTEndpoint;
   AWSCredentialsProvider mCredentialsProvider;
   private static AWSIotMqttManager mMqttMgr;
   private static MqttManager mInstance;

   public enum ConnectionStatus{
        NOT_CONNECTED,CONNECTING,CONNECTED,RECONNECTING,DISCONNECTED
    };

   ConnectionStatus mConnectionStatus = ConnectionStatus.NOT_CONNECTED;

   private final static String TAG = "MqttManager";

   private MqttManager(Context pCtxt){
       mCtxt = pCtxt;
       mAwsIoTEndpoint = getConfig("IoTEndpoint");
       mClientId = UUID.randomUUID().toString();
       mMqttMgr = new AWSIotMqttManager(mClientId, mAwsIoTEndpoint);
       mCredentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
   }

    public static MqttManager getInstance(Context pCtxt){

        if(mInstance == null){
            mInstance = new MqttManager(pCtxt);
        }
        return mInstance;
    }


    private String getConfig(String pKey){

       String value = null;
       AWSConfiguration configuration = IdentityManager.getDefaultIdentityManager().getConfiguration();
       JSONObject object = configuration.optJsonObject("IoTConfig");
       try {
           value = (String) object.get(pKey);
       }
       catch(Exception e){
           e.printStackTrace();
       }

       return value;
   }

    public void connect(){

        //TODO: maintain status of the Connection and provide it to the Application if required

        try {
            mMqttMgr.connect(mCredentialsProvider, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(TAG,"Status"+status);

                    switch(status){

                        case Connecting:{

                            mConnectionStatus = ConnectionStatus.CONNECTING;
                        }
                        break;

                        case Connected:{
                            mConnectionStatus = ConnectionStatus.CONNECTED;

                        }
                        break;

                        case Reconnecting:{
                            mConnectionStatus = ConnectionStatus.RECONNECTING;
                        }
                        break;

                        case ConnectionLost:{
                            mConnectionStatus = ConnectionStatus.DISCONNECTED;
                        }
                        break;
                    }
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public ConnectionStatus getConnectionStatus(){

        return mConnectionStatus;
    }

    //TODO for the next phase and provide callbacks for Message delivery notifications:
    // can add supported QoS as input parameter, callback function
    public  void publish(String pMqttTopic,String pMsg){
        try {
            mMqttMgr.publishString( pMsg,pMqttTopic, AWSIotMqttQos.QOS0);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
