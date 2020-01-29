package com.wearables.ge.wearables_ble_receiver.activities.main.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.activities.main.MainTabbedActivity;
import com.wearables.ge.wearables_ble_receiver.services.LocationService;
import com.wearables.ge.wearables_ble_receiver.utils.GattAttributes;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageAlarmStateChar;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DeviceTabFragment extends Fragment {
    private static final String TAG = "Device Tab Fragment";

    public static final String TAB_NAME = "Device";

    private TextView logThresholdView = null;
    private TextView deviceName = null;
    private TextView locationView = null;
    private LineChart lineChart = null;
    private TextView cloud = null;

    View rootView;

    SeekBar logThresholdBar;

    public int alarmLevel = 50;
    public int chartPoints = 20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        rootView = inflater.inflate(R.layout.fragment_tab_device, container, false);

        //create the slider bar for alarm threshold
        logThresholdBar = rootView.findViewById(R.id.logThresholdBar);
        logThresholdBar.setProgress(alarmLevel);
        logThresholdView = rootView.findViewById(R.id.logThresholdView);
        logThresholdView.setText(getString(R.string.alarm_threshold, logThresholdBar.getProgress()));
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

                    //when the user accepts the dialog, write the new voltage level to the device and show the line on the graph
                    alert.setPositiveButton(R.string.dialog_accept_button_message, (dialog, whichButton) -> {
                        ((MainTabbedActivity)Objects.requireNonNull(getActivity())).mService.writeToVoltageAlarmConfigChar(GattAttributes.MESSAGE_TYPE_ALARM_THRESHOLD, String.valueOf(seekBar.getProgress()));
                        alarmLevel = seekBar.getProgress();
                        //addAlarmLevelLine();
                        updateChartThreshold();
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

        initializeLineChart();
        setConnectedMessage(isConnected);

        setRetainInstance(true);

        locationView  = rootView.findViewById(R.id.location_disp);
        cloud = rootView.findViewById(R.id.deviceConnView);
        if(null != cloud) {
            cloud.setText("\u2601");
        }

        return rootView;
    }

    public void updateAlarmLevel(int level) {
        this.alarmLevel = level;
        if (logThresholdBar != null)
            logThresholdBar.setProgress(level);
        if (logThresholdView != null)
            logThresholdView.setText(getString(R.string.alarm_threshold, level));
        updateChartThreshold();
        //addAlarmLevelLine();
    }

    // data for our three voltage channels
    List<Entry> valsCh1 = new ArrayList<Entry>();
    List<Entry> valsCh2 = new ArrayList<Entry>();
    List<Entry> valsCh3 = new ArrayList<Entry>();

    //create data set array
    List<ILineDataSet> voltData = new ArrayList<>();

    public void initializeLineChart() {
        lineChart = rootView.findViewById(R.id.sensor_log_chart);

        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawBorders(false);

        lineChart.getAxisRight().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        //show axis (time) labels on the top
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setTypeface(Typeface.SANS_SERIF);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setLabelCount(8, false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setAxisMaximum(250);

        if(voltData.size() == 0) {
            //create the data sets
            voltData.add(toDataSet(valsCh1, "Ch1", ColorTemplate.getHoloBlue()));
            voltData.add(toDataSet(valsCh2, "Ch2", ColorTemplate.COLORFUL_COLORS[2]));
            voltData.add(toDataSet(valsCh3, "Ch3", ColorTemplate.COLORFUL_COLORS[4]));
        }
        //set the graph data to the new data object
        lineChart.setData(new LineData(voltData));
        updateChartThreshold();
    }

    // update the chart's LimitLine with the alarm value
    private void updateChartThreshold() {
        if(null != lineChart) {
            lineChart.getAxisLeft().removeAllLimitLines();
            lineChart.getAxisLeft().addLimitLine(new LimitLine(alarmLevel, "Alarm Threshold"));
        }
    }

    // find the max in the 40-70Hz range of the voltage FFT data
    public static int FindMax(List<Integer> l, int nBins, int nSize) {
        //next, we will calculate the peak between 40 and 70Hz bins
        //since the bin size and number of bins may change, we will use them as variables
        //to find the range of values between 40 and 70
        int i = Math.round(40 / nSize) + 1;
        int end = Math.round(70 / nSize) + 1;
        int nmax = -1;
        for (int ie = l.size(); i < ie && i < end; ++i) {
            int tmp = l.get(i);
            if (tmp > nmax) nmax = tmp;
        }
        return nmax;
    }

    public void SetCloudConn(boolean bConnected) {
        if(null != cloud) {
            cloud.setText(bConnected ? "\u2601" : "\u20e0");
            cloud.setTextColor(bConnected ? Color.GREEN : Color.RED);
        }
    }

    // convert a channel's data to a LineDataSet
    public LineDataSet toDataSet(List<Entry> l, String lbl, int color, YAxis.AxisDependency ax) {
        //create a data set for the live values
        LineDataSet set = new LineDataSet(l, lbl);
        //set line color
        set.setColor(color);
        //don't draw numerical value on each point
        set.setDrawValues(false);
        set.setAxisDependency(ax);
        //size the circles on each individual point
        set.setCircleRadius(1);
        //set circle color to blend with color of data set
        set.setCircleHoleColor(color);
        set.setCircleColor(color);
        return set;
    }

    public LineDataSet toDataSet(List<Entry> l, String lbl, int color) { return toDataSet(l, lbl, color, YAxis.AxisDependency.LEFT); }

    // update the voltage line chart
    public void updateVoltageChart(VoltageAlarmStateChar volt) {
        // get the x value for the next entry
        int nX = valsCh1.size();
        if (nX > 0) nX = (int) valsCh1.get(nX - 1).getX() + 1;
        // add our new entries
        valsCh1.add(new Entry(nX, FindMax(volt.getCh1_fft_results(), volt.getNum_fft_bins(), volt.getFft_bin_size())));
        valsCh2.add(new Entry(nX, FindMax(volt.getCh2_fft_results(), volt.getNum_fft_bins(), volt.getFft_bin_size())));
        valsCh3.add(new Entry(nX, FindMax(volt.getCh3_fft_results(), volt.getNum_fft_bins(), volt.getFft_bin_size())));
        // remove any excess
        while (valsCh1.size() > chartPoints)
            valsCh1.remove(0);
        while (valsCh2.size() > chartPoints)
            valsCh2.remove(0);
        while (valsCh3.size() > chartPoints)
            valsCh3.remove(0);
        // notify the data sets their entries have changed -- we have to do this BEFORE notifyDataChanged
        for(ILineDataSet d : voltData)
            d.calcMinMax();
        lineChart.getData().notifyDataChanged();
        lineChart.notifyDataSetChanged();
        //refresh the graph
        lineChart.invalidate();
        // make sure our location is up-to-date
        updateLocation();
        SetCloudConn(((MainTabbedActivity)Objects.requireNonNull(getActivity())).mStoreAndForwardService.Connected());
    }

    public void displayDeviceName(String name){
        deviceName = rootView.findViewById(R.id.deviceNameView);
        if (deviceName != null) {
            deviceName.setText(name);
        }
    }

    public class DateValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis){
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date d = new Date();
            return (dateFormat.format(d));
        }
    }

    public static String DegMinSec(double d, String sPos, String sNeg) {
        double dAbs = d < 0. ? -d : d;
        int deg = (int) dAbs;
        int min = (int) ((dAbs - deg) * 60);
        double sec = ((dAbs - deg) * 60. - min) * 60.;
        return String.format("%d\u00B0%02d\u2032%02.1f\u2033%s", deg, min, sec, d < 0 ? sNeg : sPos);
    }

    public static String LatDegMinSec(double d) { return DegMinSec(d, "N", "S"); }
    public static String LongDegMinSec(double d) { return DegMinSec(d, "E", "W"); }
    public static String DegMinSec(Location l) {
        return LatDegMinSec(l.getLatitude()) + ", " + LongDegMinSec(l.getLongitude());
    }

    public void updateLocation() {
        if(isConnected && null != locationView && LocationService.locations.size() > 0) {
            Location l = LocationService.locations.get(LocationService.locations.size() - 1);
            String sB = "";
            if(l.hasBearing()) sB = String.format(", Hdg %.2f\u00B0", l.getBearing());
            locationView.setText(DegMinSec(l) + sB);
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
        updateLocation();
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
            voltageSensorStatusView.setText(getString(R.string.temperature, String.format("%.2f", temp)));
        }
    }

    public void updateHumidity(double humidity){
        TextView voltageSensorStatusView = rootView.findViewById(R.id.humidity);
        if(voltageSensorStatusView != null){
//            voltageSensorStatusView.setText(getString(R.string.humidity, String.valueOf(humidity, )));
            voltageSensorStatusView.setText(getString(R.string.humidity, String.format("%.1f", humidity)));
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

    public void updateCO2(int nCO2){
        TextView VOCSensorStatusView = rootView.findViewById(R.id.CO2);
        if(VOCSensorStatusView != null){
            VOCSensorStatusView.setText(getString(R.string.CO2, String.valueOf(nCO2)));
        }
    }
    public void updateTVOC(int nTVOC) {
        TextView VOCSensorStatusView = rootView.findViewById(R.id.VOC);
        if(VOCSensorStatusView != null){
            VOCSensorStatusView.setText(getString(R.string.VOC, String.valueOf(nTVOC)));
        }
    }
    public void updateProximity(boolean bProx) {
        TextView VOCSensorStatusView = rootView.findViewById(R.id.Proximity);
        if(VOCSensorStatusView != null){
            if(bProx)
                VOCSensorStatusView.setText(getString(R.string.proxTrue));
            else
                VOCSensorStatusView.setText(getString(R.string.proxFalse));
        }
    }
}
