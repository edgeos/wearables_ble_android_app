
package com.wearables.ge.wearables_ble_receiver.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.ui.ForgotPasswordDialogFragment;

public class NewAuthenticatorActivity extends FragmentActivity implements ForgotPasswordDialogFragment.ForgotPasswordListener {

    private static final String TAG = "AuthenticatorActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the AWS mobile client
        AWSMobileClient.getInstance().initialize(this, (AWSStartupResult awsStartupResult) ->
                Log.i(TAG, "AWSMobileclient is instantiated and you are connected to AWS")).execute();

        // Only render the view if we are not logged in
        setContentView(R.layout.activity_authenticator);

        // Set up listeners for the different buttons
        setupLoginListener();
        setupSignupListener();
        setupForgotPasswordListener();
    }

    @Override
    public void onDialogPositivecheck(DialogFragment dialogFragment) {
        Log.d(TAG, "test");
    }

    private void setupLoginListener() {
        final Button button = findViewById(R.id.buttonLogin);
        button.setOnClickListener((View v) -> {
            // Get the username and password here so we can use them later when they are requested
            final EditText usernameField = findViewById(R.id.editUsername);
            final EditText passwordField = findViewById(R.id.editPassword);
            final String username = usernameField.getText().toString();
            final String password = passwordField.getText().toString();

            // Get a null user from a cognito user pool
            final CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(), AWSMobileClient.getInstance().getConfiguration());
            final CognitoUser cognitoUser = userPool.getUser(username);

            // Authenticate in the background
            cognitoUser.getSessionInBackground(new AuthenticationHandler() {
                @Override
                public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                    // Switch to the main view
                    Intent mainIntent = new Intent(getApplicationContext(), MainTabbedActivity.class);
                    startActivity(mainIntent);
                }

                @Override
                public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                    // Fill out the authentication details so we can continue logging in
                    final AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, password, null);
                    authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                    authenticationContinuation.continueTask();
                }

                @Override
                public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                    // TODO: Set up a pop-up box that will ask for MFA credentials
                    Log.i(TAG, "MFA was requested, but we have not implemented it yet");
                    String code = "";
                    continuation.setMfaCode(code);
                    continuation.continueTask();
                }

                @Override
                public void authenticationChallenge(ChallengeContinuation continuation) {
                    Log.d(TAG, "An authentication challenge has been received");
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.d(TAG, "test");
                }
            });
        });
    }

    private void setupSignupListener() {
        final Button button = findViewById(R.id.buttonSignup);
        button.setOnClickListener((View v) -> {
            // Switch to the signup page
            Intent signupIntent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(signupIntent);
        });
    }

    private void setupForgotPasswordListener() {
        final TextView textView = findViewById(R.id.textForgotPassword);
        textView.setOnClickListener((View v) -> {
            // Open the forgot password dialog
            DialogFragment resetPasswordFragment = new ForgotPasswordDialogFragment();
            resetPasswordFragment.show(getSupportFragmentManager(), "ResetPasswordFragment");
        });
    }
}
