package com.wearables.ge.wearables_ble_receiver.activities.util.fragments;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.wearables.ge.wearables_ble_receiver.R;

import androidx.annotation.NonNull;

public class SuccessDialogFragment extends DialogFragment {

    public static final String SUCCESS_MESSAGE_BUNDLE_KEY = "SUCCESS_MESSAGE";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set up the inflater
        View thisView = inflater.inflate(R.layout.dialog_success, null);

        // remove the upper bar on the dialog
        Window window = getDialog().getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }

        // If the parent passed a custom error message, display that instead of the default text
        final Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(SUCCESS_MESSAGE_BUNDLE_KEY)) {
                final String errorMessage = arguments.getString(SUCCESS_MESSAGE_BUNDLE_KEY);

                // Set the text to the error message
                TextView textFailure = thisView.findViewById(R.id.dialog_success_text_message);
                textFailure.setText(errorMessage);
            }
        }

        // Set up a listener so when a user presses Ok the dialog will close
        final Button button = thisView.findViewById(R.id.dialog_success_button_ok);
        button.setOnClickListener((View v) -> {
            // Just dismiss
            dismiss();
        });

        return thisView;
    }

    public static SuccessDialogFragment showSuccessDialogWithMessage(FragmentManager manager, String message) {
        // Set up a bundle to pass in the message
        Bundle successBundle = new Bundle();
        successBundle.putString(SUCCESS_MESSAGE_BUNDLE_KEY, message);

        // Create the dialog and add the bundle
        SuccessDialogFragment successDialog = new SuccessDialogFragment();
        successDialog.setArguments(successBundle);
        successDialog.show(manager, "SuccessMessage");

        // Return the dialog we created so the caller can control it if they want
        return successDialog;
    }
}
