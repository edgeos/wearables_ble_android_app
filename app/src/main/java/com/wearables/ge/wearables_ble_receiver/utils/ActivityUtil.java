package com.wearables.ge.wearables_ble_receiver.utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.View;
import android.widget.EditText;

import com.wearables.ge.wearables_ble_receiver.activities.util.fragments.ErrorDialogFragment;

public class ActivityUtil {

    public static boolean hasRequiredFields(Activity activity, int[] ids) {
        // For each of the requested ids, check if the field is populated
        boolean hasRequiredFields = true;
        for (final int id : ids) {
            EditText editText = activity.findViewById(id);
            final String val = editText.getText().toString();

            // If the field is not populated, highlight the field red, otherwise clear any red we might have put on it
            if (val.isEmpty()) {
                editText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                hasRequiredFields = false;
            } else {
                editText.getBackground().clearColorFilter();
            }
        }

        // Show a popup with an error if we are missing fields and the activity supports it
        if (!hasRequiredFields && activity instanceof FragmentActivity) {
            final FragmentActivity fragmentActivity = (FragmentActivity) activity;
            ErrorDialogFragment.showErrorDialogWithMessage(fragmentActivity.getSupportFragmentManager(),
                    "Missing required fields, please fill in the fields highlighted in red.");
        }

        return hasRequiredFields;
    }

    public static boolean hasRequiredFields(Fragment fragment, View view, int[] ids) {
        // For each of the requested ids, check if the field is populated
        boolean hasRequiredFields = true;
        for (final int id : ids) {
            EditText editText = view.findViewById(id);
            final String val = editText.getText().toString();

            // If the field is not populated, highlight the field red, otherwise clear any red we might have put on it
            if (val.isEmpty()) {
                editText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                hasRequiredFields = false;
            } else {
                editText.getBackground().clearColorFilter();
            }
        }

        // Show a popup with an error if we are missing fields and the activity supports it
        if (!hasRequiredFields) {
            ErrorDialogFragment.showErrorDialogWithMessage(fragment.getFragmentManager(),
                    "Missing required fields, please fill in the fields highlighted in red.");
        }

        return hasRequiredFields;
    }
}
