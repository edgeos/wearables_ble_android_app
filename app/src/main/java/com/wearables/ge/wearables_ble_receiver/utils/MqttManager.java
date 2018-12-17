package com.wearables.ge.wearables_ble_receiver.utils;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;

import java.util.UUID;

/**
 * Created by simithn on 23/11/18.
 */

public class MqttManager {

   String mClientId;
   Context mCtxt;
   String mAwsIoTEndpoint;

    AWSCredentialsProvider mCredentialsProvider;
    // Customer specific IoT endpoint
    //private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a1bb7j6i5uiivh-ats.iot.us-west-2.amazonaws.com";
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a3bx6ijnr5v29n-ats.iot.us-east-1.amazonaws.com";

    private static AWSIotMqttManager mMqttMgr;

   //TODO : modularise with endpoint etc.
   public MqttManager(Context pCtxt){

       mCtxt = pCtxt;
       mClientId = UUID.randomUUID().toString();
       mMqttMgr = new AWSIotMqttManager(mClientId, CUSTOMER_SPECIFIC_ENDPOINT);
       mCredentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();

   }

   public void connect(){

       try {
           mMqttMgr.connect(mCredentialsProvider, new AWSIotMqttClientStatusCallback() {
               @Override
               public void onStatusChanged(final AWSIotMqttClientStatus status,
                                           final Throwable throwable) {
                   Log.d("D","Status"+status);
               }
           });
       }
       catch(Exception e){
           e.printStackTrace();
       }
   }



   //TODO: can add supported QoS as input parameter
   public static void publish(String pMqttTopic,String pMsg){
       try {
           mMqttMgr.publishString( pMsg,pMqttTopic, AWSIotMqttQos.QOS0);
       }
       catch(Exception e){
           e.printStackTrace();
       }
   }

}
