/*
 * Copyright (c) 2017 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.wearables.ge.wearables_ble_receiver.activities.main;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.wearables.ge.wearables_ble_receiver.R;

import org.json.JSONObject;

public class AuthenticatorActivity extends AppCompatActivity {

    private final static String TAG = "AuthenticatorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);


        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler(){
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.i(TAG, "AWSMobileClient is instantiated and you are connected to AWS!");

            }
        }).execute();

        // Sign-in listener
        IdentityManager.getDefaultIdentityManager().addSignInStateChangeListener(new SignInStateChangeListener() {
            @Override
            public void onUserSignedIn() {

                Log.i(TAG, "User Signed In");
                try {
                    CognitoCachingCredentialsProvider credentialsProvider =
                            (CognitoCachingCredentialsProvider)AWSMobileClient.getInstance().getCredentialsProvider();
                    /*Attach an IoT Policy to the Cognito Identity, so that we can then have access to
                    * AWS IoT Service for streaming messages via MQTT over websockets, all the configuration is in
                    * awsconfiguration.json in res/raw folder*/
                    AWSConfiguration configuration = IdentityManager.getDefaultIdentityManager().getConfiguration();
                    JSONObject object = configuration.optJsonObject("IoTConfig");
                    String iotRegion = (String) object.get("IoTRegion");
                    String iotPolicy = (String) object.get("IoTPolicy");

                    AWSIotClient awsIotClient = new AWSIotClient(credentialsProvider);
                    awsIotClient.setRegion(Region.getRegion(iotRegion));
                    String principalId = credentialsProvider.getIdentityId();
                    AttachPrincipalPolicyRequest policyAttachRequest =
                            new AttachPrincipalPolicyRequest().withPolicyName(iotPolicy)
                                    .withPrincipal(principalId);
                    awsIotClient.attachPrincipalPolicy(policyAttachRequest);
                    Log.i(TAG, "Iot policy attached successfully to Cognito identity.");
                } catch (Exception e) {
                    Log.e(TAG, "Exception caught: ", e);
                }
            }

            // Sign-out listener
            @Override
            public void onUserSignedOut() {
                Log.i(TAG, "User Signed Out");
                showSignIn();
            }
        });

        showSignIn();
    }

    /*
     * Display the AWS SDK sign-in/sign-up UI
     */
    private void showSignIn() {

        Log.d(TAG, "showSignInActivity");

        AuthUIConfiguration config =
                new AuthUIConfiguration.Builder()
                        .userPools(true)  // true? show the Email and Password UI
                        .logoResId(R.drawable.logo) // Change the logo
                        .backgroundColor(Color.WHITE) // Change the backgroundColor
                        .isBackgroundColorFullScreen(true) // Full screen backgroundColor the backgroundColor full screenff
                        .fontFamily("sans-serif-light") // Apply sans-serif-light as the global font
                        .canCancel(true)
                        .build();

        SignInUI signin = (SignInUI) AWSMobileClient.getInstance()
                    .getClient(AuthenticatorActivity.this, SignInUI.class);
        signin.login(AuthenticatorActivity.this, MainTabbedActivity.class).authUIConfiguration(config).execute();
        }
    }

