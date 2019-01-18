
package com.wearables.ge.wearables_ble_receiver.activities.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.auth.core.StartupAuthResult;
import com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.authentication.fragments.ConfirmationDialogFragment;
import com.wearables.ge.wearables_ble_receiver.activities.authentication.fragments.ForgotPasswordDialogFragment;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainTabbedActivity;
import com.wearables.ge.wearables_ble_receiver.activities.util.fragments.ErrorDialogFragment;
import com.wearables.ge.wearables_ble_receiver.activities.util.fragments.ProgressDialogFragment;
import com.wearables.ge.wearables_ble_receiver.activities.util.fragments.SuccessDialogFragment;
import com.wearables.ge.wearables_ble_receiver.utils.ActivityUtil;

public class AuthenticatorActivity extends FragmentActivity implements ConfirmationDialogFragment.ConfirmationDialogListener {

    private static final String TAG = "AuthenticatorActivity";

    private ForgotPasswordDialogFragment mForgotPasswordFragment = new ForgotPasswordDialogFragment();
    private ConfirmationDialogFragment mConfirmationDialogFragment = new ConfirmationDialogFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bring up a loading screen
        DialogFragment progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.show(getSupportFragmentManager(), "LoginProgressFragment");

        // Initialize the AWS mobile client
        AWSMobileClient.getInstance().initialize(this, (AWSStartupResult awsStartupResult) -> {
            IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();

            // Register a signout listener to redirect the user to this page on signout
            identityManager.addSignInStateChangeListener(new SignInStateChangeListener() {
                @Override
                public void onUserSignedIn() {
                    // Do nothing here as this behavior is handled below
                }

                @Override
                public void onUserSignedOut() {
                    // Redirect to the login page
                    Intent authIntent = new Intent(getApplicationContext(), AuthenticatorActivity.class);
                    startActivity(authIntent);
                }
            });

            // Attempt to resume the session
            identityManager.resumeSession(AuthenticatorActivity.this, (StartupAuthResult authResults) -> {
                // Dismiss the loading screen
                progressDialogFragment.dismiss();

                // If the user is still signed in, redirect to the main page, otherwise show this page
                if (identityManager.isUserSignedIn()) {
                    Intent mainIntent = new Intent(getApplicationContext(), MainTabbedActivity.class);
                    startActivity(mainIntent);
                } else {
                    // If there is not identity provider set up an Identity Provider so logout will work next time
                    if (identityManager.getCurrentIdentityProvider() == null) {
                        CognitoUserPoolsSignInProvider identityProvider = new CognitoUserPoolsSignInProvider();
                        identityProvider.initialize(getApplicationContext(), identityManager.getConfiguration());
                        identityManager.federateWithProvider(identityProvider);
                    }

                    // Set up the view
                    setContentView(R.layout.activity_authenticator);

                    // Set up listeners for the different buttons
                    setupLoginListener();
                    setupSignupListener();
                    setupForgotPasswordListener();
                }
            });

        }).execute();
    }

    @Override
    public void onConfirmationCodeDialogSuccess(DialogFragment dialogFragment) {
        // Dismiss the dialog
        mConfirmationDialogFragment.dismiss();

        // Fire the login listener event again, to hopefully log the user in automatically
        final Button button = findViewById(R.id.activity_authenticator_button_login);
        button.performClick();
    }

    private void setupLoginListener() {
        final Button button = findViewById(R.id.activity_authenticator_button_login);
        button.setOnClickListener((View v) -> {
            // Get the username and password here so we can use them later when they are requested
            final EditText usernameField = findViewById(R.id.activity_authenticator_edit_username);
            final EditText passwordField = findViewById(R.id.activity_authenticator_edit_password);
            final String username = usernameField.getText().toString();
            final String password = passwordField.getText().toString();

            // Verify that the user provided all the required fields
            int[] ids = {
                    R.id.activity_authenticator_edit_username,
                    R.id.activity_authenticator_edit_password
            };
            if (!ActivityUtil.hasRequiredFields(AuthenticatorActivity.this, ids)) {
                return;
            }

            // Bring up a loading screen
            DialogFragment progressDialogFragment = new ProgressDialogFragment();
            progressDialogFragment.show(getSupportFragmentManager(), "LoginProgressFragment");

            // Get a null user from a cognito user pool
            final CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(), AWSMobileClient.getInstance().getConfiguration());
            final CognitoUser cognitoUser = userPool.getUser(username);

            // Authenticate in the background
            cognitoUser.getSessionInBackground(new AuthenticationHandler() {
                @Override
                public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                    // Switch to the main view
                    progressDialogFragment.dismiss();
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
                    progressDialogFragment.dismiss();
                    Log.i(TAG, "MFA was requested, but we have not implemented it yet");
                    String code = "";
                    continuation.setMfaCode(code);
                    continuation.continueTask();
                }

                @Override
                public void authenticationChallenge(ChallengeContinuation continuation) {
                    // TODO: Implement behavior for an authentication challenge
                    Log.d(TAG, "An authentication challenge has been received, but this behavior is not implemented");
                }

                @Override
                public void onFailure(Exception exception) {
                    // Dismiss the progress dialog
                    progressDialogFragment.dismiss();

                    // If the error is that the user is not yet confirmed, allow the user to confirm themselves
                    if (exception instanceof UserNotConfirmedException) {
                        if (mConfirmationDialogFragment.getDialog() != null && mConfirmationDialogFragment.getDialog().isShowing()) {
                            mConfirmationDialogFragment.dismiss();
                        }
                        mConfirmationDialogFragment = ConfirmationDialogFragment.showConfirmationDialog(getSupportFragmentManager(), cognitoUser);
                        return;
                    }

                    // Display the message from the exception
                    ErrorDialogFragment.showErrorDialogWithMessage(getSupportFragmentManager(), exception);
                }
            });
        });
    }

    private void setupSignupListener() {
        final Button button = findViewById(R.id.activity_authenticator_button_signup);
        button.setOnClickListener((View v) -> {
            // Switch to the signup page
            Intent signupIntent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(signupIntent);
        });
    }

    private void setupForgotPasswordListener() {
        TextView textView = findViewById(R.id.activity_authenticator_text_forgot_password);
        textView.setOnClickListener((View v) -> {
            // Get the confirmation code and password from the UI
            final EditText usernameField = findViewById(R.id.activity_authenticator_edit_username);
            final String username = usernameField.getText().toString();

            // Make sure the fields are filled out
            int[] ids = {
                    R.id.activity_authenticator_edit_username,
            };
            if (!ActivityUtil.hasRequiredFields(AuthenticatorActivity.this, ids)) {
                return;
            }

            // Show the loading dialog
            DialogFragment progressFragment = new ProgressDialogFragment();
            progressFragment.show(getSupportFragmentManager(), "ProgressResetFragment");

            // Get the user from the user pool
            final CognitoUserPool userPool = new CognitoUserPool(getApplicationContext(),
                    AWSMobileClient.getInstance().getConfiguration());
            final CognitoUser cognitoUser = userPool.getUser(username);

            // Attempt to reset the password
            cognitoUser.forgotPasswordInBackground(new ForgotPasswordHandler() {
                @Override
                public void onSuccess() {
                    // Dismiss the progress bar, and the popup
                    progressFragment.dismiss();
                    mForgotPasswordFragment.dismiss();

                    // Display a success window
                    SuccessDialogFragment.showSuccessDialogWithMessage(getSupportFragmentManager(),
                            "Password successfully reset.");
                }

                @Override
                public void getResetCode(ForgotPasswordContinuation continuation) {
                    // Open the dialog to get the reset code and new password
                    mForgotPasswordFragment = ForgotPasswordDialogFragment.showForgotPasswordDialog(getSupportFragmentManager(), continuation);
                }

                @Override
                public void onFailure(Exception exception) {
                    // Dismiss the progress dialog
                    progressFragment.dismiss();

                    // Display the message from the exception
                    ErrorDialogFragment.showErrorDialogWithMessage(getSupportFragmentManager(), exception);
                }
            });
        });
    }
}
