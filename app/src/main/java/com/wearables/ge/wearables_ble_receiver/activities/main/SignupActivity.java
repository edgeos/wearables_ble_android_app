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
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.ui.ConfirmationDialogFragment;
import com.wearables.ge.wearables_ble_receiver.activities.ui.DeviceTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.ui.EventsTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.ui.HistoryTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.ui.PairingTabFragment;
import com.wearables.ge.wearables_ble_receiver.activities.ui.ProgressDialogFragment;
import com.wearables.ge.wearables_ble_receiver.services.BluetoothService;
import com.wearables.ge.wearables_ble_receiver.services.LocationService;
import com.wearables.ge.wearables_ble_receiver.utils.AccelerometerData;
import com.wearables.ge.wearables_ble_receiver.utils.BLEQueue;
import com.wearables.ge.wearables_ble_receiver.utils.GattAttributes;
import com.wearables.ge.wearables_ble_receiver.utils.MqttManager;
import com.wearables.ge.wearables_ble_receiver.utils.TempHumidPressure;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageAlarmStateChar;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SignupActivity extends FragmentActivity implements ConfirmationDialogFragment.ConfirmationDialogListener {

    private static final String TAG = "SignupActivity";

    // The key that the cognito user will have in the bundle sent to the confirmation dialog
    public static final String COGNITO_USER_BUNDLE_KEY = "COGNITO_USER";

    // This allows us to send a cognito user to the confirmation dialog
    public static class CognitoUserBundle implements Serializable {
        public CognitoUser cognitoUser;

        public CognitoUserBundle(CognitoUser cognitoUser) {
            this.cognitoUser = cognitoUser;
        }
    }

    private ConfirmationDialogFragment mConfirmationFragment = new ConfirmationDialogFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setupSubmitListener();
    }

    @Override
    public void onDialogPositivecheck(DialogFragment dialogFragment) {
        // If everything went well, dismiss the popup and redirect to the login page
        mConfirmationFragment.dismiss();
        Intent loginIntent = new Intent(getApplicationContext(), NewAuthenticatorActivity.class);
        startActivity(loginIntent);
    }

    private void setupSubmitListener() {
        final Button button = findViewById(R.id.buttonSignupSubmit);
        button.setOnClickListener((View v) -> {
            // Get the values for each of the fields
            final EditText usernameField = findViewById(R.id.editUsernameSignup);
            final EditText passwordField = findViewById(R.id.editPasswordSignup);
            final EditText givenNameField = findViewById(R.id.editGivenName);
            final EditText emailAddressField = findViewById(R.id.editEmailAddress);
            final EditText phoneNumberField = findViewById(R.id.editPhoneNumber);
            final String username = usernameField.getText().toString();
            final String password = passwordField.getText().toString();
            final String givenName = givenNameField.getText().toString();
            final String emailAddress = emailAddressField.getText().toString();
            final String phoneNumber = phoneNumberField.getText().toString();

            // Verify that the required fields are filled out
            boolean filledOut = true;
            if (username.isEmpty()) {
                usernameField.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                filledOut = false;
            }
            if (password.isEmpty()) {
                passwordField.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                filledOut = false;
            }
            if (givenName.isEmpty()) {
                givenNameField.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                filledOut = false;
            }
            if (emailAddress.isEmpty()) {
                emailAddressField.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                filledOut = false;
            }
            if (phoneNumber.isEmpty()) {
                phoneNumberField.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                filledOut = false;
            }

            // If any of the fields were not filled out, don't attempt to sign up
            if (!filledOut) {
                return;
            }

            // Show a spinner
            ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
            progressDialogFragment.show(getSupportFragmentManager(), "ProgressFragment");

            // Add the additional fields to an attributes object
            CognitoUserAttributes userAttributes = new CognitoUserAttributes();
            userAttributes.addAttribute("given_name", givenName);
            userAttributes.addAttribute("phone_number", phoneNumber);
            userAttributes.addAttribute("email", emailAddress);

            // Use the user pool to sign up the user
            final CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(), AWSMobileClient.getInstance().getConfiguration());
            userPool.signUpInBackground(username, password, userAttributes, null, new SignUpHandler() {
                @Override
                public void onSuccess(CognitoUser user, boolean signUpConfirmationState, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                    // Dismiss the spinner
                    progressDialogFragment.dismiss();

                    if (!signUpConfirmationState) {
                        // Send the user to the confirmation dialog as an argument
                        Bundle userBundle = new Bundle();
                        CognitoUserBundle cognitoUserBundle = new CognitoUserBundle(user);
                        userBundle.putSerializable(COGNITO_USER_BUNDLE_KEY, cognitoUserBundle);
                        mConfirmationFragment.setArguments(userBundle);

                        // Show the confirmation dialog
                        mConfirmationFragment.show(getSupportFragmentManager(), "ConfirmationCodeFragment");
                    } else {
                        // This should happen very rarely, but in the case where the user does not need confirmation redirect to login
                        Intent loginIntent = new Intent(getApplicationContext(), NewAuthenticatorActivity.class);
                        startActivity(loginIntent);
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    // Dismiss the spinner
                    progressDialogFragment.dismiss();
                    // TODO: Implement a pop up that shows that the signup failed
                }
            });
        });
    }

}
