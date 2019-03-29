package com.wearables.ge.wearables_ble_receiver.activities.authentication;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.authentication.fragments.ConfirmationDialogFragment;
import com.wearables.ge.wearables_ble_receiver.activities.util.fragments.ErrorDialogFragment;
import com.wearables.ge.wearables_ble_receiver.activities.util.fragments.ProgressDialogFragment;
import com.wearables.ge.wearables_ble_receiver.utils.ActivityUtil;

public class SignupActivity extends FragmentActivity implements ConfirmationDialogFragment.ConfirmationDialogListener {

    private static final String TAG = "SignupActivity";

    private ConfirmationDialogFragment mConfirmationDialogFragment = new ConfirmationDialogFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setupSubmitListener();
    }

    @Override
    public void onConfirmationCodeDialogSuccess(DialogFragment dialogFragment) {
        // If everything went well, dismiss the popup and redirect to the login page
        mConfirmationDialogFragment.dismiss();
        Intent loginIntent = new Intent(getApplicationContext(), AuthenticatorActivity.class);
        startActivity(loginIntent);
    }

    private void setupSubmitListener() {
        final Button button = findViewById(R.id.activity_signup_button_submit);
        button.setOnClickListener((View v) -> {
            // Get the values for each of the fields
            final EditText usernameField = findViewById(R.id.activity_signup_edit_username);
            final EditText passwordField = findViewById(R.id.activity_signup_edit_password);
            final EditText givenNameField = findViewById(R.id.activity_signup_edit_given_name);
            final EditText emailAddressField = findViewById(R.id.activity_signup_edit_email_address);
            final EditText phoneNumberField = findViewById(R.id.activity_signup_edit_phone_number);
            final String username = usernameField.getText().toString();
            final String password = passwordField.getText().toString();
            final String givenName = givenNameField.getText().toString();
            final String emailAddress = emailAddressField.getText().toString();
            final String phoneNumber = phoneNumberField.getText().toString();

            // Verify that the required fields are filled out
            int ids[] = {
                    R.id.activity_signup_edit_username,
                    R.id.activity_signup_edit_password,
                    R.id.activity_signup_edit_given_name,
                    R.id.activity_signup_edit_email_address,
                    R.id.activity_signup_edit_phone_number
            };
            if (!ActivityUtil.hasRequiredFields(SignupActivity.this, ids)) {
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
                        // Show the confirmation dialog
                        if (mConfirmationDialogFragment.getDialog() != null && mConfirmationDialogFragment.getDialog().isShowing()) {
                            mConfirmationDialogFragment.dismiss();
                        }
                        mConfirmationDialogFragment = ConfirmationDialogFragment.showConfirmationDialog(getSupportFragmentManager(), user);
                    } else {
                        // This should happen very rarely, but in the case where the user does not need confirmation redirect to login
                        Intent loginIntent = new Intent(getApplicationContext(), AuthenticatorActivity.class);
                        startActivity(loginIntent);
                    }
                }

                @Override
                public void onFailure(Exception exception) {
                    // Dismiss the spinner
                    progressDialogFragment.dismiss();

                    // Display an error popup with more information
                    ErrorDialogFragment.showErrorDialogWithMessage(getSupportFragmentManager(), exception);
                }
            });
        });
    }

}
