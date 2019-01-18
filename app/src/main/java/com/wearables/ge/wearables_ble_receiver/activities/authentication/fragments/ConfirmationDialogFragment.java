package com.wearables.ge.wearables_ble_receiver.activities.authentication.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.util.fragments.ErrorDialogFragment;
import com.wearables.ge.wearables_ble_receiver.activities.util.fragments.ProgressDialogFragment;
import com.wearables.ge.wearables_ble_receiver.utils.ActivityUtil;
import com.wearables.ge.wearables_ble_receiver.utils.CognitoUserBundle;

import androidx.annotation.NonNull;

public class ConfirmationDialogFragment extends DialogFragment {

    private static final String TAG = "ConfirmationDialog";

    // The parent activity MUST implement this interface in order to get data back from this dialog
    public interface ConfirmationDialogListener {
        void onConfirmationCodeDialogSuccess(DialogFragment dialogFragment);
    }

    // We will use this instance to deliver events to the parent
    private ConfirmationDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Make sure the host implemented the listener
        try {
            mListener = (ConfirmationDialogListener) context;
        } catch (ClassCastException c) {
            // If the parent did not implement the listener, throw an exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement ConfirmationDialogListener");
        }
    }

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
        final CognitoUserBundle cognitoUserBundle = (CognitoUserBundle) arguments.getSerializable(CognitoUserBundle.COGNITO_USER_BUNDLE_KEY);
        if (cognitoUserBundle == null) {
            return builder.create();
        }
        final Activity activity = getActivity();
        if (activity == null) {
            return builder.create();
        }

        // Now that we know we have everything we need, pull out the user and set up the inflater
        final CognitoUser cognitoUser = cognitoUserBundle.cognitoUser;
        final LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_confirmation_code, null);

        // Add the submit listener
        Button submitButton = view.findViewById(R.id.dialog_confirmation_code_button_submit);
        submitButton.setOnClickListener((View v) -> {
            // Get the confirmation code from the UI
            final EditText confirmationCodeField = view.findViewById(R.id.dialog_confirmation_code_edit_code);
            final String confirmationCode = confirmationCodeField.getText().toString();

            // Make sure the field was filled out
            int[] ids = {
                    R.id.dialog_confirmation_code_edit_code
            };
            if (!ActivityUtil.hasRequiredFields(ConfirmationDialogFragment.this, view, ids)) {
                return;
            }

            // Show the loading dialog if we can
            final FragmentManager fragmentManager = getFragmentManager();
            DialogFragment progressFragment = new ProgressDialogFragment();
            if (fragmentManager != null) {
                progressFragment.show(fragmentManager, "ProgressConfirmationFragment");
            }

            // Attempt to confirm given the user
            cognitoUser.confirmSignUpInBackground(confirmationCode, false, new GenericHandler() {
                @Override
                public void onSuccess() {
                    // Send control back to the signup dialog
                    progressFragment.dismiss();
                    mListener.onConfirmationCodeDialogSuccess(ConfirmationDialogFragment.this);
                }

                @Override
                public void onFailure(Exception exception) {
                    // Dismiss the progress bar, and this dialog
                    progressFragment.dismiss();

                    // Show an error dialog
                    ErrorDialogFragment.showErrorDialogWithMessage(getFragmentManager(), exception);
                }
            });
        });

        // Set the view and return the builder
        builder.setView(view);
        return builder.create();
    }

    public static ConfirmationDialogFragment showConfirmationDialog(FragmentManager manager, CognitoUser cognitoUser) {
        // Create an instance of this dialog fragment
        ConfirmationDialogFragment confirmationDialogFragment = new ConfirmationDialogFragment();

        // Send the user to the confirmation dialog as an argument
        Bundle userBundle = new Bundle();
        CognitoUserBundle cognitoUserBundle = new CognitoUserBundle(cognitoUser);
        userBundle.putSerializable(CognitoUserBundle.COGNITO_USER_BUNDLE_KEY, cognitoUserBundle);
        confirmationDialogFragment.setArguments(userBundle);

        // Show the confirmation dialog
        confirmationDialogFragment.show(manager, "ConfirmationCodeFragment");
        return confirmationDialogFragment;
    }
}
