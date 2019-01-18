package com.wearables.ge.wearables_ble_receiver.activities.main.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.wearables.ge.wearables_ble_receiver.utils.MqttManager;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventsTabFragment extends Fragment {
    private static final String TAG = "Events Tab Fragment";

    public static final String TAB_NAME = "Events";

    View rootView;

    public List<VoltageEvent> voltageEvents = new ArrayList<>();

    BarChart eventGraph;

    TextView deviceName;

    File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "voltage_sensor");

    private MqttManager mMqttMgr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        rootView = inflater.inflate(R.layout.fragment_tab_events, container, false);

        refreshEventsLog();

        initializeEventChart();

        // Device name shown at the top of the page
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(MainTabbedActivity.connectedDeviceName);

        Button scanAgainButton = rootView.findViewById(R.id.button1);
        scanAgainButton.setOnClickListener(v -> {
            Log.d(TAG, "Save file to device button pressed");
            saveFile();
        });

        Button saveToCloudButton = rootView.findViewById(R.id.button2);
        saveToCloudButton.setOnClickListener(v -> {
            Log.d(TAG, "Save file to cloud button pressed");
            saveFileToCloud();
        });

        Button clearLogButton = rootView.findViewById(R.id.button3);
        clearLogButton.setOnClickListener(v -> {
            Log.d(TAG, "Clear Log button pressed");
            clearLog();
        });

        Button findFilesButton = rootView.findViewById(R.id.button4);
        findFilesButton.setOnClickListener(v -> {
            Log.d(TAG, "Find local files button pressed");
            findLocalFiles();
        });

        setRetainInstance(true);

        mMqttMgr = MqttManager.getInstance(rootView.getContext());
        mMqttMgr.connect();

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

    List<String> lines = new ArrayList<>();
    public void addEventItem(VoltageEvent voltageEvent){
        Double latitude = null;
        Double longitude = null;
        if(voltageEvent.getLocation() != null){
            latitude = voltageEvent.getLocation().getLatitude();
            longitude = voltageEvent.getLocation().getLongitude();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Date d = new Date((voltageEvent.getTime()));
        String date = dateFormat.format(d);


        String message = "Level " + voltageEvent.getVoltage() + ", lasted " + voltageEvent.getDuration() + " milliseconds";

        String coordinates = "No coordinates found for event.";
        if(latitude != null){
            coordinates = "(" + latitude.toString() + "," + longitude.toString() + ")";
        }

        String logMessage = date + " " + message + " " + coordinates;
        lines.add(logMessage);

        if(rootView != null && !viewingOldFile){
            LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
            TextView logTextView = new TextView(rootView.getContext());
            logTextView.setText(logMessage);
            logEventsList.addView(logTextView);
        }

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

    public String savedFileName;
    public void saveFile(){
        //check for file write permissions
        if(ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            return;
        }

        //if there is nothing in the current log, don't save anything
        if(lines.isEmpty()){
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
            alert.setTitle("Current Log is empty");
            alert.show();
            return;
        }

        //use time epoch ms for filename
        Long time = Calendar.getInstance().getTimeInMillis();
        savedFileName = String.valueOf(time) + "_gas_sensor_log.txt";

        //log path for debugging
        Log.d(TAG, "Files Dir: " + path.getPath());
        Boolean pathCreated = true;

        //make sure path exists
        //it should, when this main class is created it should grab the downloads directory
        if(!path.exists()){
            Log.d(TAG, "Path doesn't exist");
            pathCreated = path.mkdirs();
        }
        if(!pathCreated){
            Log.d(TAG, "Unable to create path");
            return;
        }

        //create the file to save
        File file = new File(path, savedFileName);

        //write all the lines in the saved array to the file
        try {
            FileWriter writer = new FileWriter(file);
            //head the file with some device info
            String headerLine = "Device: " + MainTabbedActivity.connectedDevice.getAddress() + " Name: " + MainTabbedActivity.connectedDeviceName + System.lineSeparator();
            writer.append(headerLine);
            for(String line : lines){
                writer.append(line);
                writer.append(System.lineSeparator());
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean viewingOldFile = false;
    File selectedFile;
    String selectedFileName;
    public void findLocalFiles(){
        //check for file read/write permissions
        if(ContextCompat.checkSelfPermission(rootView.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
            return;
        }

        //get a list of all files in the path directory
        File[] files = path.listFiles();
        List<String> optionsList = new ArrayList<>();

        //if no files were found send a message and return
        if(files == null){
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
            alert.setTitle("No log files found in " + path.toString());
            alert.show();
            return;
        }

        //add each item to a list of options
        for(File file : files){
            Log.d(TAG, "File: " + file.getName());
            optionsList.add(file.getName());
        }

        String[] optionsArray = new String[optionsList.size()];
        optionsList.toArray(optionsArray);

        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
        alert.setTitle("Select a file");
        alert.setItems(optionsArray, (dialog, which) -> {
            Log.d(TAG, "Chose option #" + which + " filename: " + optionsList.get(which));
            //grab the name of the file based o nthe index of the option selected
            selectedFileName = optionsList.get(which);
            selectedFile = new File(path, selectedFileName);
            //boolean for viewing an old file so the bluetooth service in the background doesn't update the list while you are viewing an old file
            viewingOldFile = true;
            try {
                FileInputStream is = new FileInputStream(selectedFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
                logEventsList.removeAllViews();
                lines = new ArrayList<>();
                while(line != null){
                    //Log.d(TAG, "Line read: " + line);
                    lines.add(line);
                    TextView textView = new TextView(rootView.getContext());
                    textView.setText(line);
                    textView.setGravity(Gravity.START);
                    logEventsList.addView(textView);
                    line = reader.readLine();
                }
            } catch (Exception e) {
                Log.d(TAG, "Unable to read file: " + e.getMessage());
            }
        });
        alert.show();
    }

    public void clearLog(){
        viewingOldFile = false;
        LinearLayout logEventsList = rootView.findViewById(R.id.logEventList);
        logEventsList.removeAllViews();
        lines = new ArrayList<>();
    }

    public void saveFileToCloud(){
        //if there is nothing in the current log, don't save anything
        if(lines.isEmpty()){
            AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));
            alert.setTitle("Current Log is empty");
            alert.show();
            return;
        }

        String message = "Device: " + MainTabbedActivity.connectedDevice.getAddress() + " Name: " + MainTabbedActivity.connectedDeviceName;
        for(String line : lines){
            message = message + " " + line;
        }

        if(mMqttMgr.getConnectionStatus() == MqttManager.ConnectionStatus.CONNECTED) {
            Log.d(TAG, "{ \"data\":\"" + message + "\"}");
            mMqttMgr.publish("ge/test/data", "{ \"data\":\"" + message + "\"}");
        }
    }
}
