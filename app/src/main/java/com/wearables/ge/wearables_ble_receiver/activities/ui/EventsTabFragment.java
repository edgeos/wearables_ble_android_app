package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainTabbedActivity;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventsTabFragment extends Fragment {
    private static final String TAG = "Events Tab Fragment";

    public static final String TAB_NAME = "Events";

    View rootView;

    public List<VoltageEvent> voltageEvents = new ArrayList<>();

    BarChart eventGraph;

    TextView deviceName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_events, container, false);

        refreshEventsLog();

        initializeEventChart();

        // Device name shown at the top of the page
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(MainTabbedActivity.connectedDeviceName);

        return rootView;
    }

    public void initializeEventChart(){
        eventGraph = rootView.findViewById(R.id.event_log_graph);
        eventGraph.setDrawBarShadow(false);
        eventGraph.setDrawValueAboveBar(false);
        eventGraph.setMaxVisibleValueCount(30);

        XAxis xAxis = eventGraph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(Typeface.SANS_SERIF);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis = eventGraph.getAxisLeft();
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaximum(250);

        YAxis rightAxis = eventGraph.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(Typeface.SANS_SERIF);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        rightAxis.setAxisMaximum(250);

    }

    public class DateValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis){
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date d = new Date();
            return (dateFormat.format(d));
        }
    }

    public void refreshEventsLog(){
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
        logEventsList.removeAllViews();

        for(VoltageEvent voltageEvent : voltageEvents){
            Double latitude = null;
            Double longitude = null;
            if(voltageEvent.getLocation() != null){
                latitude = voltageEvent.getLocation().getLatitude();
                longitude = voltageEvent.getLocation().getLongitude();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            Date d = new Date((voltageEvent.getTime()));
            String date = dateFormat.format(d);
            TextView dateTimeTextView = new TextView(rootView.getContext());
            dateTimeTextView.setText(date);
            dateTimeTextView.setGravity(Gravity.CENTER);

            String message = "Level " + voltageEvent.getVoltage() + ", lasted " + voltageEvent.getDuration() + " milliseconds";
            TextView messageTextView = new TextView(rootView.getContext());
            messageTextView.setText(message);
            messageTextView.setGravity(Gravity.CENTER);

            String coordinates = "No coordinates found for event.";
            if(latitude != null){
                coordinates = "(" + latitude.toString() + "," + longitude.toString() + ")";
            }
            TextView locationTextView = new TextView(rootView.getContext());
            locationTextView.setText(coordinates);
            locationTextView.setGravity(Gravity.CENTER);
            locationTextView.setPadding(0,0,0,30);

            logEventsList.addView(locationTextView, 0);
            logEventsList.addView(messageTextView, 0);
            logEventsList.addView(dateTimeTextView, 0);

            updateGraph(voltageEvent);
        }
    }

    public void addEventItem(VoltageEvent voltageEvent){
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);

        Double latitude = null;
        Double longitude = null;
        if(voltageEvent.getLocation() != null){
            latitude = voltageEvent.getLocation().getLatitude();
            longitude = voltageEvent.getLocation().getLongitude();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Date d = new Date((voltageEvent.getTime()));
        String date = dateFormat.format(d);
        TextView dateTimeTextView = new TextView(rootView.getContext());
        dateTimeTextView.setText(date);
        dateTimeTextView.setGravity(Gravity.CENTER);

        String message = "Level " + voltageEvent.getVoltage() + ", lasted " + voltageEvent.getDuration() + " milliseconds";
        TextView messageTextView = new TextView(rootView.getContext());
        messageTextView.setText(message);
        messageTextView.setGravity(Gravity.CENTER);

        String coordinates = "No coordinates found for event.";
        if(latitude != null){
            coordinates = "(" + latitude.toString() + "," + longitude.toString() + ")";
        }
        TextView locationTextView = new TextView(rootView.getContext());
        locationTextView.setText(coordinates);
        locationTextView.setGravity(Gravity.CENTER);
        locationTextView.setPadding(0,0,0,30);

        logEventsList.addView(locationTextView, 0);
        logEventsList.addView(messageTextView, 0);
        logEventsList.addView(dateTimeTextView, 0);

        updateGraph(voltageEvent);
    }

    List<BarEntry> entries = new ArrayList<>();
    int i =0;
    public void updateGraph(VoltageEvent voltageEvent){
        if(eventGraph != null ){
            i++;
            if(entries.size() == 15){
                entries.remove(0);
            }
            entries.add(new BarEntry(i, voltageEvent.getVoltage()));
            BarDataSet set = new BarDataSet(entries, "Events");
            BarData barData = new BarData(set);
            barData.setBarWidth(0.8f);

            eventGraph.setData(barData);
            eventGraph.setFitBars(true);

            eventGraph.invalidate();
        } else {
            Log.d(TAG, "Log graph uninitialised");
        }
    }
}
