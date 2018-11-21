package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainTabbedActivity;

import java.util.Objects;

public class DeviceTabFragment extends Fragment {

    public static final String TAB_NAME = "Device";

    private TextView sampleRateView = null;
    private TextView logThresholdView = null;
    private TextView deviceName = null;


    private GraphView logGraph = null;

    View rootView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_device, container, false);

        int sampleRateStepSize = 25; // from 0 - 100 with increments of 25 points each

        SeekBar sampleRateBar = rootView.findViewById(R.id.sampleRateBar);
        sampleRateView = rootView.findViewById(R.id.sampleRateView);
        sampleRateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
                int stepProgress = ((int)Math.round(progress/sampleRateStepSize))*sampleRateStepSize;
                seekBar.setProgress(stepProgress);
                if (seekBar.getProgress() > 0) {
                    sampleRateView.setText("Sample Rate: " + seekBar.getProgress() + " ms");
                } else {
                    sampleRateView.setText("Auto Sampling OFF");
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

        SeekBar logThresholdBar = rootView.findViewById(R.id.logThresholdBar);
        logThresholdView = rootView.findViewById(R.id.logThresholdView);
        logThresholdBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // updated continuously as the user slides the thumb
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // called when the user first touches the SeekBar
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // called after the user finishes moving the SeekBar
                ((MainTabbedActivity)Objects.requireNonNull(getActivity())).mService.sendAlarmThresholdMessage(String.valueOf(seekBar.getProgress()));
                logThresholdView.setText("Event Log Threshold: " + seekBar.getProgress());

                //TODO: change yellow and red areas of speedometer to reflect change
                // depends on the min and max readings for the sensor, normal, elevated and high values...

            }
        });

        // Device name shown at the top of the page
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(MainTabbedActivity.connectedDeviceName);

        // log graphic
        GraphView logGraph = rootView.findViewById(R.id.sensor_log_graph);
        LineGraphSeries<DataPoint> logSeries = new LineGraphSeries<>();
        // data
        logGraph.addSeries(logSeries);
        // customize viewport
        Viewport viewport1 = logGraph.getViewport();
        viewport1.setYAxisBoundsManual(true);
        viewport1.setMinY(0);
        viewport1.setMaxY(100);
        viewport1.setScrollable(true);

        return rootView;
    }

    public void displayDeviceName(String name){
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(name);
    }

    public void updateVoltageSensorStatus(String sensorStatus){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.voltage_sensor_status);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.voltage_sensor_status, sensorStatus));
        }
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

    public void updateVOC(int VOC){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.VOC);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.VOC, String.valueOf(VOC)));
        }
    }

    public void updateSpo2Sensor(String spo2SensorStatus){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.spo2_sensor);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.spo2, spo2SensorStatus));
        }
    }

    public void updateVoltageLevel(int voltageLevel){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.voltage_level);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.voltage_level, voltageLevel));
        }
    }

    public void updateAlarmThreshold(int threshold){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.alarm_threshold);
        if(voltageSensorStatusView != null){
            voltageSensorStatusView.setText(getString(R.string.alarm_threshold, threshold));
        }
    }
}
