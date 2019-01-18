package com.wearables.ge.wearables_ble_receiver.activities.util.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.amazonaws.mobile.auth.core.signin.ui.BackgroundDrawable;
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
