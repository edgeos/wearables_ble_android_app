package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wearables.ge.wearables_ble_receiver.R;

public class EventsTabFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    public static final String TAB_NAME = "Events";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_events, container, false);
        Bundle args = getArguments();

        /*
        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
        */

        // programmatically ade events to the list...
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
        String[] eventsArray = {
                "-- 10/10/2018 3:57 PM \n Level 1024, lasted 5 seconds \n (42.830507, -73.880557)",
                "-- 10/10/2018 3:42 PM \n Level 352, lasted 2 seconds \n (42.830468, -73.879822)",
                "-- 10/10/2018 3:34 PM \n Level 452, lasted 6 seconds \n (45.836478, -76.879822)"
        };
        for (String event: eventsArray) {
            TextView textView = new TextView(this.getContext());
            textView.setText(event);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(5,5,5,5);

            textView.setLayoutParams(params);
            logEventsList.addView(textView);

        }


        return rootView;
    }
}
