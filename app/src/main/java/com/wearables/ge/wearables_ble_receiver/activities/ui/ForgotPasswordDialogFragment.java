package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.wearables.ge.wearables_ble_receiver.R;

import androidx.annotation.NonNull;

public class ForgotPasswordDialogFragment extends DialogFragment {

    private static final String TAG = "ConfigmrationDialog";

    // The parent activity MUST implement this interface in order to get data back from this dialog
    public interface ForgotPasswordListener {
        void onDialogPositivecheck(DialogFragment dialogFragment);
    }

    // We will use this instance to deliver events to the parent
    private ForgotPasswordListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Make sure the host implemented the listener
        try {
            mListener = (ForgotPasswordListener) context;
        } catch (ClassCastException c) {
            // If the parent did not implement the listener, throw an exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement ConfirmationDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Set up the inflater and builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_forgot_password, null);

        // Add the submit and cancel button
        /*
        builder.setPositiveButton(R.string.dialog_accept_button_message, (DialogInterface dialog, int which) -> {
            mListener.onDialogPositivecheck(ConfirmationDialogFragment.this);
        });
        builder.setNegativeButton(R.string.dialog_cancel_button_message, (DialogInterface dialog, int which) -> {
            mListener.onDialogNegativeClick(ConfirmationDialogFragment.this);
        });
        */

        // set the view and return the builder
        builder.setView(view);
        return builder.create();
    }
}
