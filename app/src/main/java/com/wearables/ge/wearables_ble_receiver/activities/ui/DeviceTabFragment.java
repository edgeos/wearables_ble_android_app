package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.anastr.speedviewlib.SpeedView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.wearables.ge.wearables_ble_receiver.R;

public class DeviceTabFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    public static final String TAB_NAME = "Device";

    private SeekBar sampleRateBar = null;
    private TextView sampleRateView = null;
    private SeekBar logThresholdBar = null;
    private TextView logThresholdView = null;
    private SpeedView speedometer = null;
    private TextView deviceName = null;


    private GraphView logGraph = null;
    private LineGraphSeries<DataPoint> logSeries = null;

    public int fragmentId;

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tab_device, container, false);
        Bundle args = getArguments();

        showConnectedMessage();

        /*// Initial gauge config using https://github.com/anastr/SpeedView
        //TODO: add Bluetooth listeners and update these dynamically.
        speedometer = rootView.findViewById(R.id.speedView);
        speedometer.setWithTremble(false);
        speedometer.speedTo(71);
        speedometer.setMaxSpeed(100);
        speedometer.setMinSpeed(0);
        speedometer.setUnit(" ppm");

        //TODO: change the yellow and red areas of the speedometer according to user threshold e.g. yellow event, red event...
        speedometer.setLowSpeedPercent(25);
        speedometer.setMediumSpeedPercent(75);*/

        int sampleRateStepSize = 25; // from 0 - 100 with increments of 25 points each

        sampleRateBar = rootView.findViewById(R.id.sampleRateBar);
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

        logThresholdBar = rootView.findViewById(R.id.logThresholdBar);
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
        logSeries = new LineGraphSeries<>();
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

    public void showConnectedMessage() {
        LinearLayout linLayout = rootView.findViewById(R.id.main_information_display);
        if (linLayout != null) {
            linLayout.removeAllViews();

            TextView textView = new TextView(rootView.getContext());
            textView.setText(getString(R.string.connected_device_message, deviceName));
            textView.setGravity(Gravity.CENTER);
            textView.setId(R.id.connected_message);
            linLayout.addView(textView);


            TextView voltageSensorStatusView = new TextView(rootView.getContext());
            voltageSensorStatusView.setText(getString(R.string.voltage_sensor_status, "undefined"));
            voltageSensorStatusView.setGravity(Gravity.CENTER);
            voltageSensorStatusView.setId(R.id.voltage_sensor_status);
            linLayout.addView(voltageSensorStatusView);

            TextView batteryLevelView = new TextView(rootView.getContext());
            batteryLevelView.setText(getString(R.string.battery_level, 0));
            batteryLevelView.setGravity(Gravity.CENTER);
            batteryLevelView.setId(R.id.battery_level);
            linLayout.addView(batteryLevelView);

            TextView temperatureView = new TextView(rootView.getContext());
            temperatureView.setText(getString(R.string.temperature, "undefined"));
            temperatureView.setGravity(Gravity.CENTER);
            temperatureView.setId(R.id.temperature);
            linLayout.addView(temperatureView);

            TextView humidityView = new TextView(rootView.getContext());
            humidityView.setText(getString(R.string.humidity, "undefined"));
            humidityView.setGravity(Gravity.CENTER);
            humidityView.setId(R.id.humidity);
            linLayout.addView(humidityView);

            TextView VOCView = new TextView(rootView.getContext());
            VOCView.setText(getString(R.string.VOC, "undefined"));
            VOCView.setGravity(Gravity.CENTER);
            VOCView.setId(R.id.VOC);
            linLayout.addView(VOCView);

            TextView spo2SensorView = new TextView(rootView.getContext());
            spo2SensorView.setText(getString(R.string.spo2, "undefined"));
            spo2SensorView.setGravity(Gravity.CENTER);
            spo2SensorView.setId(R.id.spo2_sensor);
            linLayout.addView(spo2SensorView);

            TextView voltageLevelView = new TextView(rootView.getContext());
            voltageLevelView.setText(getString(R.string.voltage_level, 0));
            voltageLevelView.setGravity(Gravity.CENTER);
            voltageLevelView.setId(R.id.voltage_level);
            linLayout.addView(voltageLevelView);

            TextView alarmThresholdView = new TextView(rootView.getContext());
            alarmThresholdView.setText(getString(R.string.alarm_threshold, 0));
            alarmThresholdView.setGravity(Gravity.CENTER);
            alarmThresholdView.setId(R.id.alarm_threshold);
            linLayout.addView(alarmThresholdView);
        }
    }

    public void displayDeviceName(String name){
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText(name);

        TextView connectedMessage = rootView.findViewById(R.id.connected_message);
        if(connectedMessage != null){
            connectedMessage.setText(getString(R.string.connected_device_message, name));
        }
    }

    /*public void updateVOCGauge(int value){
        speedometer.speedTo(value);
    }*/

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
