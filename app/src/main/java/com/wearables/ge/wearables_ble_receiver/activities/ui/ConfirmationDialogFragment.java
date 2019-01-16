package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.signin.ui.BackgroundDrawable;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.SignupActivity;

import androidx.annotation.NonNull;

public class ConfirmationDialogFragment extends DialogFragment {

    private static final String TAG = "ConfigmrationDialog";

    // The parent activity MUST implement this interface in order to get data back from this dialog
    public interface ConfirmationDialogListener {
        void onDialogPositivecheck(DialogFragment dialogFragment);
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
        // Pull out the user from the bundle
        final SignupActivity.CognitoUserBundle cognitoUserBundle = (SignupActivity.CognitoUserBundle)
                getArguments().getSerializable(SignupActivity.COGNITO_USER_BUNDLE_KEY);
        final CognitoUser cognitoUser = cognitoUserBundle.cognitoUser;

        // Set up the inflater and builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_confirmation_code, null);

        // Add the submit listener
        Button submitButton = view.findViewById(R.id.buttonConfirmationCodeSubmit);
        submitButton.setOnClickListener((View v) -> {
            // Get the confirmation code from the UI
            EditText confirmationCodeField = view.findViewById(R.id.editConfirmationCode);
            final String confirmationCode = confirmationCodeField.getText().toString();

            // If the code was not given, highlight the field red
            if (confirmationCode.isEmpty()) {
                confirmationCodeField.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                return;
            }

            // Show the loading dialog
            DialogFragment progressFragment = new ProgressDialogFragment();
            progressFragment.show(getFragmentManager(), "ProgressConfirmationFragment");

            // Attempt to confirm given the user
            cognitoUser.confirmSignUpInBackground(confirmationCode, false, new GenericHandler() {
                @Override
                public void onSuccess() {
                    // Send control back to the signup dialog
                    progressFragment.dismiss();
                    mListener.onDialogPositivecheck(ConfirmationDialogFragment.this);
                }

                @Override
                public void onFailure(Exception exception) {
                    progressFragment.dismiss();
                    // TODO: Show a failure dialog in this case
                }
            });
        });

        // Set the view and return the builder
        builder.setView(view);
        return builder.create();
    }
}
