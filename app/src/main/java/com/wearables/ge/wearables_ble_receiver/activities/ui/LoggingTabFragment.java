package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wearables.ge.wearables_ble_receiver.R;

import java.util.Arrays;
import java.util.List;

public class LoggingTabFragment extends Fragment {
    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_tab_logging, container, false);

        showFileText();

        return rootView;
    }

    public void showFileText(){
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
        List<String> lines = Arrays.asList("Test Data Line 1",
                "Test Data Line 2",
                "Test Data Line 3",
                "Test Data Line 4",
                "Test Data Line 5",
                "Test Data Line 6",
                "Test Data Line 7");

        for(String line : lines){
            TextView textView = new TextView(rootView.getContext());
            textView.setText(line);
            textView.setGravity(Gravity.START);
            logEventsList.addView(textView);
        }

    }
}
