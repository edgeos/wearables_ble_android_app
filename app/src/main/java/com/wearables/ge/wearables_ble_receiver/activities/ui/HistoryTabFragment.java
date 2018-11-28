package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.graphics.Color;
import android.os.Bundle;
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
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.utils.AccelerometerData;
import com.wearables.ge.wearables_ble_receiver.utils.TempHumidPressure;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageAlarmStateChar;

import java.text.SimpleDateFormat;
import java.util.Date;


public class HistoryTabFragment extends Fragment {
    private static final String TAG = "History Tab Fragment";

    public static final String TAB_NAME = "History";

    private ScaleAnimation expandAnimation = new ScaleAnimation(1, 1, 0, 1);
    private ScaleAnimation collapseAnimation = new ScaleAnimation(1, 1, 1, 0);

    private LineGraphSeries<DataPoint> series1;
    private LineGraphSeries<DataPoint> series2;
    private LineGraphSeries<DataPoint> series3;
    GraphView voltageGraph1;
    GraphView voltageGraph2;
    GraphView voltageGraph3;
    GraphView accelerationGraph1;
    GraphView accelerationGraph2;
    GraphView accelerationGraph3;
    GraphView tempGraph;
    GraphView humidityGraph;
    GraphView pressureGraph;
    private int lastX = 0;

    View rootView;

    LineGraphSeries<DataPoint>  accelerometerXseries = new LineGraphSeries<>();
    LineGraphSeries<DataPoint>  accelerometerYseries = new LineGraphSeries<>();
    LineGraphSeries<DataPoint>  accelerometerZseries = new LineGraphSeries<>();

    LineGraphSeries<DataPoint>  temperatureSeries = new LineGraphSeries<>();
    LineGraphSeries<DataPoint>  humiditySeries = new LineGraphSeries<>();
    LineGraphSeries<DataPoint>  pressureSeries = new LineGraphSeries<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

        LinearLayout expandableLayout3 = rootView.findViewById(R.id.collapsibleContainer3);
        Switch switchButton3 = rootView.findViewById(R.id.expand3);
        switchButton3.setChecked(true);
        switchButton3.setOnClickListener( v -> {
            if (switchButton3.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_LONG).show();
                expandView(expandableLayout3, 500);
            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_LONG).show();
                collapseView(expandableLayout3, 500 );
            }
        });

        // get graph view instance
        Log.d(TAG, "Getting graph objects");
        voltageGraph1 = rootView.findViewById(R.id.voltage_sensor_graph_1);
        Viewport voltageGraphViewport1 = voltageGraph1.getViewport();
        voltageGraphViewport1.setYAxisBoundsManual(true);
        voltageGraphViewport1.setXAxisBoundsManual(true);
        voltageGraphViewport1.setMinY(0);
        GridLabelRenderer gridLabel1 = voltageGraph1.getGridLabelRenderer();
        gridLabel1.setHorizontalAxisTitle(getString(R.string.voltage_channel_graph_x_axis_label));
        gridLabel1.setVerticalAxisTitle(getString(R.string.voltage_channel_graph_y_axis_label));

        // second graph
        voltageGraph2 = rootView.findViewById(R.id.voltage_sensor_graph_2);
        Viewport voltageGraphViewport2 = voltageGraph2.getViewport();
        voltageGraphViewport2.setYAxisBoundsManual(true);
        voltageGraphViewport2.setXAxisBoundsManual(true);
        voltageGraphViewport2.setMinY(0);
        GridLabelRenderer gridLabel2 = voltageGraph2.getGridLabelRenderer();
        gridLabel2.setHorizontalAxisTitle(getString(R.string.voltage_channel_graph_x_axis_label));
        gridLabel2.setVerticalAxisTitle(getString(R.string.voltage_channel_graph_y_axis_label));

        // third graph
        voltageGraph3 = rootView.findViewById(R.id.voltage_sensor_graph_3);
        Viewport voltageGraphViewport3 = voltageGraph3.getViewport();
        voltageGraphViewport3.setYAxisBoundsManual(true);
        voltageGraphViewport3.setXAxisBoundsManual(true);
        voltageGraphViewport3.setMinY(0);
        GridLabelRenderer gridLabel3 = voltageGraph3.getGridLabelRenderer();
        gridLabel3.setHorizontalAxisTitle(getString(R.string.voltage_channel_graph_x_axis_label));
        gridLabel3.setVerticalAxisTitle(getString(R.string.voltage_channel_graph_y_axis_label));

        //Acceleration Graphs
        accelerationGraph1 = rootView.findViewById(R.id.acceleration_sensor_graph_1);
        Viewport accelerationGraphViewport1 = accelerationGraph1.getViewport();
        accelerationGraphViewport1.setYAxisBoundsManual(true);
        accelerationGraphViewport1.setXAxisBoundsManual(true);
        accelerationGraph1.addSeries(accelerometerXseries);
        GridLabelRenderer accelerationGridLabel1 = accelerationGraph1.getGridLabelRenderer();
        accelerationGridLabel1.setHorizontalAxisTitle(getString(R.string.acceleration_graph_x_axis_label));
        accelerationGridLabel1.setVerticalAxisTitle(getString(R.string.acceleration_graph_y_axis_label));
        accelerationGraph1.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                if(isValueX){
                    Date d = new Date((long) value);
                    return (dateFormat.format(d));
                }
                return "" + (int) value;
            }

            @Override
            public void setViewport(Viewport viewport) {

            }
        });

        // second acceleration graph
        accelerationGraph2 = rootView.findViewById(R.id.acceleration_sensor_graph_2);
        Viewport accelerationGraphViewport2 = accelerationGraph2.getViewport();
        accelerationGraphViewport2.setYAxisBoundsManual(true);
        accelerationGraphViewport2.setXAxisBoundsManual(true);
        accelerationGraph2.addSeries(accelerometerYseries);
        GridLabelRenderer accelerationGridLabel2 = accelerationGraph2.getGridLabelRenderer();
        accelerationGridLabel2.setHorizontalAxisTitle(getString(R.string.acceleration_graph_x_axis_label));
        accelerationGridLabel2.setVerticalAxisTitle(getString(R.string.acceleration_graph_y_axis_label));
        accelerationGraph2.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                if(isValueX){
                    Date d = new Date((long) value);
                    return (dateFormat.format(d));
                }
                return "" + (int) value;
            }

            @Override
            public void setViewport(Viewport viewport) {

            }
        });

        // third acceleration graph
        accelerationGraph3 = rootView.findViewById(R.id.acceleration_sensor_graph_3);
        Viewport accelerationGraphViewport3 = accelerationGraph3.getViewport();
        accelerationGraphViewport3.setYAxisBoundsManual(true);
        accelerationGraphViewport3.setXAxisBoundsManual(true);
        accelerationGraph3.addSeries(accelerometerZseries);
        GridLabelRenderer accelerationGridLabel3 = accelerationGraph3.getGridLabelRenderer();
        accelerationGridLabel3.setHorizontalAxisTitle(getString(R.string.acceleration_graph_x_axis_label));
        accelerationGridLabel3.setVerticalAxisTitle(getString(R.string.acceleration_graph_y_axis_label));
        accelerationGraph3.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                if(isValueX){
                    Date d = new Date((long) value);
                    return (dateFormat.format(d));
                }
                return "" + (int) value;
            }

            @Override
            public void setViewport(Viewport viewport) {

            }
        });

        tempGraph = rootView.findViewById(R.id.temperature_graph);
        Viewport tempGraphViewport = tempGraph.getViewport();
        tempGraphViewport.setYAxisBoundsManual(true);
        tempGraphViewport.setXAxisBoundsManual(true);
        tempGraph.addSeries(temperatureSeries);
        GridLabelRenderer tempGridLabel = tempGraph.getGridLabelRenderer();
        tempGridLabel.setHorizontalAxisTitle(getString(R.string.temperature_graph_x_axis_label));
        tempGridLabel.setVerticalAxisTitle(getString(R.string.temperature_graph_y_axis_label));
        tempGraph.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                if(isValueX){
                    Date d = new Date((long) value);
                    return (dateFormat.format(d));
                }
                return "" + (int) value;
            }

            @Override
            public void setViewport(Viewport viewport) {

            }
        });

        humidityGraph = rootView.findViewById(R.id.humidity_graph);
        Viewport humidityGraphViewport = humidityGraph.getViewport();
        humidityGraphViewport.setYAxisBoundsManual(true);
        humidityGraphViewport.setXAxisBoundsManual(true);
        humidityGraph.addSeries(humiditySeries);
        GridLabelRenderer humidityGridLabel = humidityGraph.getGridLabelRenderer();
        humidityGridLabel.setHorizontalAxisTitle(getString(R.string.humidity_graph_x_axis_label));
        humidityGridLabel.setVerticalAxisTitle(getString(R.string.humidity_graph_y_axis_label));
        humidityGraph.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                if(isValueX){
                    Date d = new Date((long) value);
                    return (dateFormat.format(d));
                }
                return "" + (int) value;
            }

            @Override
            public void setViewport(Viewport viewport) {

            }
        });

        pressureGraph = rootView.findViewById(R.id.pressure_graph);
        Viewport pressureGraphViewport = pressureGraph.getViewport();
        pressureGraphViewport.setYAxisBoundsManual(true);
        pressureGraphViewport.setXAxisBoundsManual(true);
        pressureGraph.addSeries(pressureSeries);
        GridLabelRenderer pressureGridLabel = pressureGraph.getGridLabelRenderer();
        pressureGridLabel.setHorizontalAxisTitle(getString(R.string.pressure_graph_x_axis_label));
        pressureGridLabel.setVerticalAxisTitle(getString(R.string.pressure_graph_y_axis_label));
        pressureGraph.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                if(isValueX){
                    Date d = new Date((long) value);
                    return (dateFormat.format(d));
                }
                return "" + (int) value;
            }

            @Override
            public void setViewport(Viewport viewport) {

            }
        });

        if(maxXvalue > 0 && maxYvalue > 0){
            updateVoltageGraphBounds();
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

    public void updateVoltageGraph(VoltageAlarmStateChar voltageAlarmState) {
        Log.d(TAG, "UpdateGraph called");
        super.onResume();

        if(voltageGraph1 == null || voltageGraph2 == null || voltageGraph3 == null){
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
            addVoltageEntry();
        }
        if(maxXvalueCheck != maxXvalue){
            maxXvalue = maxXvalueCheck;
            updateVoltageGraphBounds();
        }
        addVoltageGraphSeries();
    }

    private void addVoltageEntry() {
        lastX = lastX + increment;
        series1.appendData(new DataPoint(lastX - increment, y1), false, 70);
        series2.appendData(new DataPoint(lastX - increment, y2), false, 70);
        series3.appendData(new DataPoint(lastX - increment, y3), false, 70);
    }

    private void addVoltageGraphSeries(){
        voltageGraph1.removeAllSeries();
        voltageGraph2.removeAllSeries();
        voltageGraph3.removeAllSeries();

        LineGraphSeries<DataPoint>  series = new LineGraphSeries<>();
        series.appendData(new DataPoint(60, 0), false, 100);
        series.appendData(new DataPoint(60, maxYvalue), false, 100);
        series.setThickness(2);
        series.setColor(Color.parseColor("#808080"));
        voltageGraph1.addSeries(series);
        voltageGraph2.addSeries(series);
        voltageGraph3.addSeries(series);

        voltageGraph1.addSeries(series1);
        voltageGraph2.addSeries(series2);
        voltageGraph3.addSeries(series3);
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

    private int roundX(int num){
        double value = num;
        double roundTo = 50;
        value = roundTo * Math.round(value / roundTo);
        if(value < num){
            return (int) (value + 50);
        } else {
            return (int) value;
        }
    }

    private void updateVoltageGraphBounds(){
        Log.d(TAG, "Max Y value: " + maxYvalue);
        Log.d(TAG, "Max X value: " + maxXvalue);

        voltageGraph1.getViewport().setMaxY(maxYvalue);
        voltageGraph1.getViewport().setMaxX(maxXvalue);

        voltageGraph2.getViewport().setMaxY(maxYvalue);
        voltageGraph2.getViewport().setMaxX(maxXvalue);

        voltageGraph3.getViewport().setMaxY(maxYvalue);
        voltageGraph3.getViewport().setMaxX(maxXvalue);

        voltageGraph1.getGridLabelRenderer().setNumHorizontalLabels(roundX(maxXvalue)/50);
        voltageGraph2.getGridLabelRenderer().setNumHorizontalLabels(roundX(maxXvalue)/50);
        voltageGraph3.getGridLabelRenderer().setNumHorizontalLabels(roundX(maxXvalue)/50);
    }

    public void updateAccelerometerGraph(AccelerometerData accelerometerData){
        accelerometerXseries.appendData(new DataPoint(accelerometerData.getDate(), accelerometerData.getxValue()), false, 300);
        accelerometerYseries.appendData(new DataPoint(accelerometerData.getDate(), accelerometerData.getyValue()), false, 300);
        accelerometerZseries.appendData(new DataPoint(accelerometerData.getDate(), accelerometerData.getzValue()), false, 300);

        if(accelerationGraph1 != null && accelerationGraph2 != null && accelerationGraph3 != null){
            accelerationGraph1.getViewport().setMinX(accelerometerXseries.getLowestValueX());
            accelerationGraph1.getViewport().setMaxX(accelerometerXseries.getHighestValueX());
            accelerationGraph1.getViewport().setMinY(accelerometerXseries.getLowestValueY());
            accelerationGraph1.getViewport().setMaxY(accelerometerXseries.getHighestValueY());

            accelerationGraph2.getViewport().setMinX(accelerometerYseries.getLowestValueX());
            accelerationGraph2.getViewport().setMaxX(accelerometerYseries.getHighestValueX());
            accelerationGraph2.getViewport().setMinY(accelerometerYseries.getLowestValueY());
            accelerationGraph2.getViewport().setMaxY(accelerometerYseries.getHighestValueY());

            accelerationGraph3.getViewport().setMinX(accelerometerZseries.getLowestValueX());
            accelerationGraph3.getViewport().setMaxX(accelerometerZseries.getHighestValueX());
            accelerationGraph3.getViewport().setMinY(accelerometerZseries.getLowestValueY());
            accelerationGraph3.getViewport().setMaxY(accelerometerZseries.getHighestValueY());
        }
    }

    public void updateTempHumidityPressureGraph(TempHumidPressure tempHumidPressure){
        temperatureSeries.appendData(new DataPoint(tempHumidPressure.getDate(), tempHumidPressure.getTemp()), false, 300);
        humiditySeries.appendData(new DataPoint(tempHumidPressure.getDate(), tempHumidPressure.getHumid()), false, 300);
        pressureSeries.appendData(new DataPoint(tempHumidPressure.getDate(), tempHumidPressure.getPres()), false, 300);

        if(tempGraph != null && humidityGraph != null){
            tempGraph.getViewport().setMinX(temperatureSeries.getLowestValueX());
            tempGraph.getViewport().setMaxX(temperatureSeries.getHighestValueX());
            tempGraph.getViewport().setMinY(temperatureSeries.getLowestValueY());
            tempGraph.getViewport().setMaxY(temperatureSeries.getHighestValueY());

            humidityGraph.getViewport().setMinX(humiditySeries.getLowestValueX());
            humidityGraph.getViewport().setMaxX(humiditySeries.getHighestValueX());
            humidityGraph.getViewport().setMinY(humiditySeries.getLowestValueY());
            humidityGraph.getViewport().setMaxY(humiditySeries.getHighestValueY());

            pressureGraph.getViewport().setMinX(pressureSeries.getLowestValueX());
            pressureGraph.getViewport().setMaxX(pressureSeries.getHighestValueX());
            pressureGraph.getViewport().setMinY(pressureSeries.getLowestValueY());
            pressureGraph.getViewport().setMaxY(pressureSeries.getHighestValueY());
        }
    }
}
