package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageAlarmStateChar;


public class HistoryTabFragment extends Fragment {
    private static final String TAG = "History Tab Fragment";

    public static final String TAB_NAME = "History";

    private ScaleAnimation expandAnimation = new ScaleAnimation(1, 1, 0, 1);
    private ScaleAnimation collapseAnimation = new ScaleAnimation(1, 1, 1, 0);

    private LineGraphSeries<DataPoint> series1;
    private LineGraphSeries<DataPoint> series2;
    private LineGraphSeries<DataPoint> series3;
    GraphView graph1;
    GraphView graph2;
    GraphView graph3;
    private int lastX = 0;

    View rootView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "History Tab opened");
        rootView = inflater.inflate(R.layout.fragment_tab_history, container, false);

        LinearLayout expandableLayout1 = rootView.findViewById(R.id.collapsibleContainer1);
        Switch switchButton1 = rootView.findViewById(R.id.expand1);
        switchButton1.setChecked(true);
        switchButton1.setOnClickListener( v -> {
            if (switchButton1.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_LONG).show();
                expandView(expandableLayout1, 500);

            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_LONG).show();
                collapseView(expandableLayout1, 500);
            }
        });

        LinearLayout expandableLayout2 = rootView.findViewById(R.id.collapsibleContainer2);
        Switch switchButton2 = rootView.findViewById(R.id.expand2);
        switchButton2.setChecked(true);
        switchButton2.setOnClickListener( v -> {
            if (switchButton2.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_LONG).show();
                expandView(expandableLayout2, 500);
            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_LONG).show();
                collapseView(expandableLayout2, 500 );
            }
        });

        // get graph view instance
        Log.d(TAG, "Getting graph objects");
        graph1 = rootView.findViewById(R.id.voltage_sensor_graph_1);
        Viewport viewport1 = graph1.getViewport();
        viewport1.setYAxisBoundsManual(true);
        viewport1.setXAxisBoundsManual(true);
        viewport1.setMinY(0);
        GridLabelRenderer gridLabel1 = graph1.getGridLabelRenderer();
        gridLabel1.setHorizontalAxisTitle(getString(R.string.voltage_channel_graph_x_axis_label));
        gridLabel1.setVerticalAxisTitle(getString(R.string.voltage_channel_graph_y_axis_label));

        // second graph
        graph2 = rootView.findViewById(R.id.voltage_sensor_graph_2);
        Viewport viewport2 = graph2.getViewport();
        viewport2.setYAxisBoundsManual(true);
        viewport2.setXAxisBoundsManual(true);
        viewport2.setMinY(0);
        GridLabelRenderer gridLabel2 = graph2.getGridLabelRenderer();
        gridLabel2.setHorizontalAxisTitle(getString(R.string.voltage_channel_graph_x_axis_label));
        gridLabel2.setVerticalAxisTitle(getString(R.string.voltage_channel_graph_y_axis_label));

        // third graph
        graph3 = rootView.findViewById(R.id.voltage_sensor_graph_3);
        Viewport viewport3 = graph3.getViewport();
        viewport3.setYAxisBoundsManual(true);
        viewport3.setXAxisBoundsManual(true);
        viewport3.setMinY(0);
        GridLabelRenderer gridLabel3 = graph3.getGridLabelRenderer();
        gridLabel3.setHorizontalAxisTitle(getString(R.string.voltage_channel_graph_x_axis_label));
        gridLabel3.setVerticalAxisTitle(getString(R.string.voltage_channel_graph_y_axis_label));

        if(maxXvalue > 0 && maxYvalue > 0){
            updateBounds();
        }
        return rootView;
    }


    private void expandView(View view, long duration) {
        View parentContainer = view.getRootView();
        expandAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
                parentContainer.requestLayout();
            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        expandAnimation.setDuration(duration);

        view.startAnimation(expandAnimation);
    }


    private void collapseView(View view, long duration) {
        View parentContainer = view.getRootView();
        collapseAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = 0;
                view.setLayoutParams(params);
                parentContainer.requestLayout();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        collapseAnimation.setDuration(duration);
        view.startAnimation(collapseAnimation);
    }

    public int y1;
    public int y2;
    public int y3;
    public int increment;
    public int maxYvalue;
    public int maxXvalue;

    public void updateGraph(VoltageAlarmStateChar voltageAlarmState) {
        Log.d(TAG, "UpdateGraph called");
        super.onResume();

        if(graph1 == null || graph2 == null || graph3 == null){
            return;
        }

        int maxXvalueCheck = ((voltageAlarmState.getNum_fft_bins() - 1) * voltageAlarmState.getFft_bin_size()) + 20;

        series1 = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
        series3 = new LineGraphSeries<>();

        increment = voltageAlarmState.getFft_bin_size();

        for (int i = 0; i < voltageAlarmState.getNum_fft_bins(); i++) {
            y1 = voltageAlarmState.getCh1_fft_results().get(i);
            y2 = voltageAlarmState.getCh2_fft_results().get(i);
            y3 = voltageAlarmState.getCh3_fft_results().get(i);
            if(y1 > maxYvalue){
                roundMaxY(y1);
            }
            if(y2 > maxYvalue){
                roundMaxY(y2);
            }
            if(y3 > maxYvalue){
                roundMaxY(y3);
            }
            addEntry();
        }
        if(maxXvalueCheck != maxXvalue){
            maxXvalue = maxXvalueCheck;
            updateBounds();
        }
        addGraphSeries();
    }

    private void addEntry() {
        lastX = lastX + increment;
        series1.appendData(new DataPoint(lastX - increment, y1), false, 70);
        series2.appendData(new DataPoint(lastX - increment, y2), false, 70);
        series3.appendData(new DataPoint(lastX - increment, y3), false, 70);
    }

    private void addGraphSeries(){
        graph1.removeAllSeries();
        graph2.removeAllSeries();
        graph3.removeAllSeries();

        LineGraphSeries<DataPoint>  series = new LineGraphSeries<>();
        series.appendData(new DataPoint(60, 0), false, 100);
        series.appendData(new DataPoint(60, maxYvalue), false, 100);
        series.setThickness(2);
        series.setColor(Color.parseColor("#808080"));
        graph1.addSeries(series);
        graph2.addSeries(series);
        graph3.addSeries(series);

        graph1.addSeries(series1);
        graph2.addSeries(series2);
        graph3.addSeries(series3);
        lastX = 0;
    }

    private void roundMaxY(int num){
        double value = num;
        double roundTo = 10;
        value = roundTo * Math.round(value / roundTo);
        if(value < num){
            maxYvalue = (int) value + 10;
        } else {
            maxYvalue = (int) value;
        }
        Log.d(TAG, "Rounded: " + num + " to: " + maxYvalue);
    }

    private int round(int num){
        double value = num;
        double roundTo = 50;
        value = roundTo * Math.round(value / roundTo);
        if(value < num){
            return (int) (value + 50);
        } else {
            return (int) value;
        }
    }

    private void updateBounds(){
        Log.d(TAG, "Max Y value: " + maxYvalue);
        Log.d(TAG, "Max X value: " + maxXvalue);

        graph1.getViewport().setMaxY(maxYvalue);
        graph1.getViewport().setMaxX(maxXvalue);

        graph2.getViewport().setMaxY(maxYvalue);
        graph2.getViewport().setMaxX(maxXvalue);

        graph3.getViewport().setMaxY(maxYvalue);
        graph3.getViewport().setMaxX(maxXvalue);

        graph1.getGridLabelRenderer().setNumHorizontalLabels(round(maxXvalue)/50);
        graph2.getGridLabelRenderer().setNumHorizontalLabels(round(maxXvalue)/50);
        graph3.getGridLabelRenderer().setNumHorizontalLabels(round(maxXvalue)/50);
    }
}
