package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.graphics.Color;
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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainTabbedActivity;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DeviceTabFragment extends Fragment {
    private static final String TAG = "Device Tab Fragment";

    public static final String TAB_NAME = "Device";

    private TextView sampleRateView = null;
    private TextView logThresholdView = null;
    private TextView deviceName = null;

    View rootView;

    LineGraphSeries<DataPoint> alarmLevelSeries;
    GraphView logGraph;

    SeekBar logThresholdBar;

    public int alarmLevel;
    private static Double minX;
    private static Double maxX;
    List<DataPoint> dataPoints = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_device, container, false);

        int sampleRateStepSize = 25; // from 0 - 100 with increments of 25 points each

        SeekBar sampleRateBar = rootView.findViewById(R.id.sampleRateBar);
        sampleRateView = rootView.findViewById(R.id.sampleRateView);
        sampleRateView.setText(getString(R.string.update_rate_value, sampleRateBar.getProgress()));
        sampleRateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                int stepProgress = ((int)Math.round(progress/sampleRateStepSize))*sampleRateStepSize;
                seekBar.setProgress(stepProgress);
                if (seekBar.getProgress() > 0) {
                    sampleRateView.setText(getString(R.string.update_rate_value, seekBar.getProgress()));
                } else {
                    sampleRateView.setText(getString(R.string.auto_sample_off_message));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                //sampleRateView.setText("Sample Rate: " + seekBar.getProgress());
            }
        });

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
                        ((MainTabbedActivity)Objects.requireNonNull(getActivity())).mService.writeToVoltageAlarmConfigChar(String.valueOf(seekBar.getProgress()));
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

        // log graphic
        GraphView logGraph = rootView.findViewById(R.id.sensor_log_graph);
        logGraph.getGridLabelRenderer().setHumanRounding(false);
        logGraph.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
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

        Viewport viewport1 = logGraph.getViewport();
        viewport1.setYAxisBoundsManual(true);
        viewport1.setXAxisBoundsManual(true);
        viewport1.setMinY(0);
        viewport1.setMaxY(100);

        return rootView;
    }

    public void updateGraph(VoltageEvent voltageEvent){
        logGraph = rootView.findViewById(R.id.sensor_log_graph);
        BarGraphSeries<DataPoint> logSeries = new BarGraphSeries<>();
        logGraph.removeAllSeries();
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
        logGraph.getViewport().setMinX(minX);
        logGraph.getViewport().setMaxX(maxX);
        logGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        logGraph.addSeries(logSeries);

        addAlarmLevelLine();
    }

    public void addAlarmLevelLine(){
        logGraph.removeSeries(alarmLevelSeries);
        alarmLevelSeries = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(minX, logThresholdBar.getProgress()),
                new DataPoint(maxX, logThresholdBar.getProgress())
        });
        logGraph.addSeries(alarmLevelSeries);
        alarmLevelSeries.setColor(Color.RED);
    }

    public void displayDeviceName(String name){
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(name);
    }

    public void updateBatteryLevel(int batteryLevel){
        TextView batteryLevelView = rootView.findViewById(R.id.battery_level);
        if(batteryLevelView != null){
            batteryLevelView.setText(getString(R.string.battery_level, batteryLevel));
        }
    }

    public void updateTemperature(int temp){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.temperature);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.temperature, String.valueOf(temp)));
        }
    }

    public void updateHumidity(int humidity){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.humidity);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.humidity, String.valueOf(humidity)));
        }
    }

    public void updateVoltageLevel(int voltageLevel){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.voltage_level);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.voltage_level, voltageLevel));
        }
    }
}
