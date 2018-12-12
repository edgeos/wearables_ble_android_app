package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainTabbedActivity;
import com.wearables.ge.wearables_ble_receiver.utils.GattAttributes;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DeviceTabFragment extends Fragment {
    private static final String TAG = "Device Tab Fragment";

    public static final String TAB_NAME = "Device";

    private TextView logThresholdView = null;
    private TextView deviceName = null;

    View rootView;

    CombinedChart logGraph;

    SeekBar logThresholdBar;

    public int alarmLevel = 50;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        rootView = inflater.inflate(R.layout.fragment_tab_device, container, false);

        logThresholdBar = rootView.findViewById(R.id.logThresholdBar);
        logThresholdView = rootView.findViewById(R.id.logThresholdView);
        logThresholdView.setText(getString(R.string.alarm_threshold, logThresholdBar.getProgress()));
        alarmLevel = logThresholdBar.getProgress();
        logThresholdBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                logThresholdView.setText(getString(R.string.alarm_threshold, progress));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(rootView.getContext(), R.style.AlertDialogCustom));

                if(MainTabbedActivity.connectedDevice != null){
                    alert.setMessage("Are you sure you would like to set the voltage alarm threshold to " + seekBar.getProgress() + "?");

                    alert.setPositiveButton(R.string.dialog_accept_button_message, (dialog, whichButton) -> {
                        ((MainTabbedActivity)Objects.requireNonNull(getActivity())).mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_ALARM_THRESHOLD, String.valueOf(seekBar.getProgress()));
                        alarmLevel = seekBar.getProgress();
                        addAlarmLevelLine();
                    });

                    alert.setNegativeButton(R.string.dialog_cancel_button_message, (dialog, whichButton) -> {
                        logThresholdBar.setProgress(alarmLevel);
                        Log.d(TAG, "Alarm Threshold dialog closed");
                    });

                } else {
                    alert.setMessage("No device connected");
                }

                alert.show();
                logThresholdView.setText(getString(R.string.alarm_threshold, seekBar.getProgress()));

            }
        });

        // Device name shown at the top of the page
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(MainTabbedActivity.connectedDeviceName);

        initializeEventChart();

        setConnectedMessage(isConnected);

        setRetainInstance(true);

        return rootView;
    }

    public void initializeEventChart(){
        logGraph = rootView.findViewById(R.id.sensor_log_graph);
        logGraph.setDrawBarShadow(false);
        logGraph.setDrawValueAboveBar(false);
        logGraph.setMaxVisibleValueCount(30);

        XAxis xAxis = logGraph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(Typeface.SANS_SERIF);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis = logGraph.getAxisLeft();
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaximum(250);

        YAxis rightAxis = logGraph.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(Typeface.SANS_SERIF);
        rightAxis.setLabelCount(8, false);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        rightAxis.setAxisMaximum(250);

    }

    int i = 0;
    List<BarEntry> entries = new ArrayList<>();
    public void updateGraph(VoltageEvent voltageEvent){
        Log.d(TAG, "Update Voltage graph called");
        if(logGraph != null ){
            i++;
            if(entries.size() == 15){
                entries.remove(0);
            }
            entries.add(new BarEntry(i, voltageEvent.getVoltage()));
            BarDataSet set = new BarDataSet(entries, "Events");
            BarData barData = new BarData(set);
            barData.setBarWidth(0.8f);
            CombinedData data = new CombinedData();
            data.setData(barData);
            data.setData(addAlarmLevelLine());

            logGraph.setData(data);

            logGraph.invalidate();


        } else {
            Log.d(TAG, "Log graph uninitialised");
        }
    }

    public LineData addAlarmLevelLine(){
        List<Entry> alarmLevelEntries = new ArrayList<>();
        alarmLevelEntries.add(new Entry(entries.get(0).getX(), logThresholdBar.getProgress()));
        alarmLevelEntries.add(new Entry(entries.get(entries.size() - 1).getX(), logThresholdBar.getProgress()));
        LineDataSet set = new LineDataSet(alarmLevelEntries, "Alarm Level");
        set.setColor(Color.RED);
        return new LineData(set);
    }

    public void displayDeviceName(String name){
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(name);
    }

    public class DateValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis){
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date d = new Date();
            return (dateFormat.format(d));
        }
    }

    boolean isConnected;
    public void setConnectedMessage(boolean status){
        if(rootView != null){
            TextView connectedStatusView = rootView.findViewById(R.id.voltage_sensor_status);
            if(connectedStatusView != null){
                String message = status ? "Connected" : "Disconnected";
                connectedStatusView.setText(getString(R.string.voltage_sensor_status, message));
            }
        }
        this.isConnected = status;
    }

    public void updateBatteryLevel(int batteryLevel){
        TextView batteryLevelView = rootView.findViewById(R.id.battery_level);
        if(batteryLevelView != null){
            batteryLevelView.setText(getString(R.string.battery_level, batteryLevel));
        }
    }

    public void updateTemperature(double temp){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.temperature);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.temperature, String.valueOf(temp)));
        }
    }

    public void updateHumidity(double humidity){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.humidity);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.humidity, String.valueOf(humidity)));
        }
    }

    public void updatePressure(double pressure){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.pressure);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.pressure, String.valueOf(pressure)));
        }
    }

    public void updateVoltageLevel(int voltageLevel){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.voltage_level);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.voltage_level, voltageLevel));
        }
    }
}
