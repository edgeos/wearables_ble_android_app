package com.wearables.ge.wearables_ble_receiver.activities.util.fragments;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.wearables.ge.wearables_ble_receiver.R;

import androidx.annotation.NonNull;

public class ProgressDialogFragment extends DialogFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Make the background transparent
        Window window = getDialog().getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Make it so that the user cannot cancel the progress bar
        setCancelable(false);

        // Set up the inflater and builder
        return inflater.inflate(R.layout.dialog_progress, null);
    }
}
