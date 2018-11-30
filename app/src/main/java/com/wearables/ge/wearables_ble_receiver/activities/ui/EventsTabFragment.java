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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainTabbedActivity;
import com.wearables.ge.wearables_ble_receiver.services.LocationService;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class EventsTabFragment extends Fragment {
    private static final String TAG = "Events Tab Fragment";

    public static final String TAB_NAME = "Events";

    View rootView;
    LinearLayout logEventsList;

    public List<VoltageEvent> voltageEvents = new ArrayList<>();

    GraphView eventGraph;

    TextView deviceName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_events, container, false);

        //refreshEventsLog();

        /*logEventsList = rootView.findViewById(R.id.logEventList);
        logEventsList.removeAllViews();*/
        refreshEventsLog();

        GraphView eventGraph = rootView.findViewById(R.id.event_log_graph);
        eventGraph.getGridLabelRenderer().setHumanRounding(false);
        eventGraph.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            int i = 0;
            @Override
            public String formatLabel(double value, boolean isValueX) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                if(isValueX && i == 100){
                    Date d = new Date((long) value);
                    i = 0;
                    return (dateFormat.format(d));
                } else if (isValueX){
                    i++;
                    return "";
                }
                return "" + (int) value;
            }

            @Override
            public void setViewport(Viewport viewport) {

            }
        });

        Viewport viewport1 = eventGraph.getViewport();
        viewport1.setYAxisBoundsManual(true);
        viewport1.setXAxisBoundsManual(true);
        viewport1.setMinY(0);
        viewport1.setMaxY(100);

        // Device name shown at the top of the page
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(MainTabbedActivity.connectedDeviceName);

        return rootView;
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
            //logEventsList.addView(dateTimeTextView);

            String message = "Level " + voltageEvent.getVoltage() + ", lasted " + voltageEvent.getDuration() + " milliseconds";
            TextView messageTextView = new TextView(rootView.getContext());
            messageTextView.setText(message);
            messageTextView.setGravity(Gravity.CENTER);
            //logEventsList.addView(messageTextView);

            String coordinates = "No coordinates found for event.";
            if(latitude != null){
                coordinates = "(" + latitude.toString() + "," + longitude.toString() + ")";
            }
            TextView locationTextView = new TextView(rootView.getContext());
            locationTextView.setText(coordinates);
            locationTextView.setGravity(Gravity.CENTER);
            locationTextView.setPadding(0,0,0,30);
            //logEventsList.addView(locationTextView);

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

    List<DataPoint> dataPoints = new ArrayList<>();
    double minX;
    double maxX;
    public void updateGraph(VoltageEvent voltageEvent){
        eventGraph = rootView.findViewById(R.id.event_log_graph);
        BarGraphSeries<DataPoint> logSeries = new BarGraphSeries<>();
        eventGraph.removeAllSeries();
        int dataPointsListSize = dataPoints.size();
        if(dataPointsListSize < 10){
            dataPoints.add(new DataPoint(voltageEvent.getTime(), voltageEvent.getVoltage()));
        } else {
            dataPoints.remove(0);
            dataPoints.add(new DataPoint(voltageEvent.getTime(), voltageEvent.getVoltage()));
        }
        for(DataPoint dataPoint : dataPoints){
            logSeries.appendData(dataPoint, false, 20);
        }
        dataPointsListSize = dataPoints.size();
        logSeries.setSpacing(10);
        minX = dataPoints.get(0).getX() - 500;
        maxX = dataPoints.get(dataPointsListSize - 1).getX() + 500;
        eventGraph.getViewport().setMinX(minX);
        eventGraph.getViewport().setMaxX(maxX);
        eventGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        eventGraph.addSeries(logSeries);
    }
}
