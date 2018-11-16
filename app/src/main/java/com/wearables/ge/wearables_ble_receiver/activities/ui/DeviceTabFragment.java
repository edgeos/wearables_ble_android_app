package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab_device, container, false);
        Bundle args = getArguments();

        int sampleRateStepSize = 25; // from 0 - 100 with increments of 25 points each

        /*
        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
        */

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
            }
        });


        // Initial gauge config.
        //TODO: add Bluetooth listeners and update these dynamically.
        speedometer = rootView.findViewById(R.id.speedView);
        speedometer.speedTo(75);
        speedometer.setMaxSpeed(100);
        speedometer.setMinSpeed(0);
        speedometer.setUnit(" ppm");


        // Device name shown at the top of the page
        deviceName = rootView.findViewById(R.id.deviceNameView);
        deviceName.setText("Selected Device Name");


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
}