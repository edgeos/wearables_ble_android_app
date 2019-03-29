package com.wearables.ge.wearables_ble_receiver.activities.authentication.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.utils.ActivityUtil;
import com.wearables.ge.wearables_ble_receiver.utils.ForgotPasswordContinuationBundle;

import androidx.annotation.NonNull;

public class ForgotPasswordDialogFragment extends DialogFragment {

    private static final String TAG = "ForgotPasswordDialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Set up the builder early, so we can return early if there is an error
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Make sure the dialog was passed everything it needs, otherwise return early
        final Bundle arguments = getArguments();
        if (arguments == null) {
            return builder.create();
        }
        final ForgotPasswordContinuationBundle forgotPasswordContinuationBundle = (ForgotPasswordContinuationBundle)
                arguments.getSerializable(ForgotPasswordContinuationBundle.FORGOT_PASSWORD_CONTINUATION_BUNDLE_KEY);
        if (forgotPasswordContinuationBundle == null) {
            return builder.create();
        }
        final Activity activity = getActivity();
        if (activity == null) {
            return builder.create();
        }

        // Now that we know we have everything we need, pull out the continuation and set up the inflater
        final ForgotPasswordContinuation forgotPasswordContinuation = forgotPasswordContinuationBundle.forgotPasswordContinuation;
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_forgot_password, null);

        Button submitButton = view.findViewById(R.id.dialog_reset_password_button_submit);
        submitButton.setOnClickListener((View v) -> {
            // Get the confirmation code and password from the UI
            final EditText confirmationCodeField = view.findViewById(R.id.dialog_reset_password_edit_code);
            final EditText passwordField = view.findViewById(R.id.dialog_reset_password_edit_password);
            final String confirmationCode = confirmationCodeField.getText().toString();
            final String password = passwordField.getText().toString();

            // Make sure the fields are filled out
            int[] ids = {
                    R.id.dialog_reset_password_edit_code,
                    R.id.dialog_reset_password_edit_password
            };
            if (!ActivityUtil.hasRequiredFields(ForgotPasswordDialogFragment.this, view, ids)) {
                return;
            }

            // Finish the password reset by continuing the task
            forgotPasswordContinuation.setPassword(password);
            forgotPasswordContinuation.setVerificationCode(confirmationCode);
            forgotPasswordContinuation.continueTask();
        });

        // set the view and return the builder
        builder.setView(view);
        return builder.create();
    }

    public static ForgotPasswordDialogFragment showForgotPasswordDialog(FragmentManager manager,
                                                                        ForgotPasswordContinuation forgotPasswordContinuation) {
        // Create an instance of this dialog fragment
        ForgotPasswordDialogFragment forgotPasswordDialogFragment = new ForgotPasswordDialogFragment();

        // Send the continuation to the fragment as a bundle
        Bundle continuationBundle = new Bundle();
        ForgotPasswordContinuationBundle forgotPasswordContinuationBundle = new ForgotPasswordContinuationBundle(forgotPasswordContinuation);
        continuationBundle.putSerializable(ForgotPasswordContinuationBundle.FORGOT_PASSWORD_CONTINUATION_BUNDLE_KEY, forgotPasswordContinuationBundle);
        forgotPasswordDialogFragment.setArguments(continuationBundle);

        // Show the confirmation dialog
        forgotPasswordDialogFragment.show(manager, "ForgotPasswordFragment");
        return forgotPasswordDialogFragment;
    }
}
