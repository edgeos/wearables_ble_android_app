package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.services.LocationService;

import java.util.Random;

public class EventsTabFragment extends Fragment {
    private static final String TAG = "Events Tab Fragment";

    public static final String ARG_SECTION_NUMBER = "section_number";

    public static final String TAB_NAME = "Events";

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_events, container, false);
        Bundle args = getArguments();

        Button refreshButton = rootView.findViewById(R.id.button3);
        refreshButton.setOnClickListener(v -> {
            refreshEventsLog();
            Log.d(TAG, "Refresh button pressed");
        });

        return rootView;
    }

    public void refreshEventsLog(){
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
        logEventsList.removeAllViews();

        for(Location location : LocationService.locations){
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            //mostly just filler data for voltage events
            String date = new java.util.Date().toString();
            TextView dateTimeTextView = new TextView(rootView.getContext());
            dateTimeTextView.setText(date);
            dateTimeTextView.setGravity(Gravity.CENTER);
            logEventsList.addView(dateTimeTextView);

            Random rand = new Random();
            String message = "Level " + rand.nextInt(1000) + ", lasted " + rand.nextInt(20) + " seconds";
            TextView messageTextView = new TextView(rootView.getContext());
            messageTextView.setText(message);
            messageTextView.setGravity(Gravity.CENTER);
            logEventsList.addView(messageTextView);

            String coordinates = "(" + latitude.toString() + "," + longitude.toString() + ")";
            TextView locationTextView = new TextView(rootView.getContext());
            locationTextView.setText(coordinates);
            locationTextView.setGravity(Gravity.CENTER);
            locationTextView.setPadding(0,0,0,30);
            logEventsList.addView(locationTextView);
        }
    }
}
