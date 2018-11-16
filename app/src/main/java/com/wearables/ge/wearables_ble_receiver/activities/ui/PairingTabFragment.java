package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wearables.ge.wearables_ble_receiver.R;


public class PairingTabFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    public static final String TAB_NAME = "Pairing";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_pairing, container, false);
        Bundle args = getArguments();
        /*
        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
        */

        //TODO: obtain this from bluetooth, etc....
        String blueToothDevices[] = {"Device1", "Device2", "Device3"};

        LinearLayout linLayout = rootView.findViewById(R.id.device_list);
        for (String deviceName: blueToothDevices) {
            View view = inflater.inflate(R.layout.fragment_tab_pairing_row, null);
            linLayout.addView(view);

            ((TextView) view.findViewById(R.id.text)).setText(deviceName);

            Switch switchButton = (Switch) view.findViewById(R.id.button);
            switchButton.setChecked(false);
            switchButton.setOnClickListener( v -> {
                if (switchButton.isChecked()) {
                    Toast.makeText(this.getContext(), "connecting...", Toast.LENGTH_LONG).show();
                    connectDevice(switchButton.getText());
                } else {
                    Toast.makeText(this.getContext(), "disconnecting...", Toast.LENGTH_LONG).show();
                    disconnectDevice(switchButton.getText());
                }
            });
        }

        return rootView;
    }

    private void connectDevice(CharSequence text) {
        //TODO: also disconnect other devices...
    }

    private void disconnectDevice(CharSequence text) {

    }
}
