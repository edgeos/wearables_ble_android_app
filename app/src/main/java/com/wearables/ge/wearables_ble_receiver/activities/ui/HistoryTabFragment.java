package com.wearables.ge.wearables_ble_receiver.activities.ui;

import android.graphics.Color;
import android.graphics.Typeface;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.wearables.ge.wearables_ble_receiver.R;
import com.wearables.ge.wearables_ble_receiver.utils.AccelerometerData;
import com.wearables.ge.wearables_ble_receiver.utils.TempHumidPressure;
import com.wearables.ge.wearables_ble_receiver.utils.VoltageAlarmStateChar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HistoryTabFragment extends Fragment {
    private static final String TAG = "History Tab Fragment";

    public static final String TAB_NAME = "History";

    private ScaleAnimation expandAnimation = new ScaleAnimation(1, 1, 0, 1);
    private ScaleAnimation collapseAnimation = new ScaleAnimation(1, 1, 1, 0);

    LineChart voltageGraph1;
    LineChart voltageGraph2;
    LineChart voltageGraph3;
    LineChart accelerationGraph1;
    LineChart accelerationGraph2;
    LineChart accelerationGraph3;
    LineChart tempGraph;
    LineChart humidityGraph;
    LineChart pressureGraph;

    private int lastX = 0;

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreate called");
        rootView = inflater.inflate(R.layout.fragment_tab_history, container, false);
        initializeVoltageGraphs();
        initializeAccelerationGraphs();
        initializeTempHumidPressureGraphs();

        setRetainInstance(true);

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

    public int increment;
    List<VoltageAlarmStateChar> averageList = new ArrayList<>();
    List<Integer> ch1Avgs = new ArrayList<>();
    List<Integer> ch2Avgs = new ArrayList<>();
    List<Integer> ch3Avgs = new ArrayList<>();
    public void updateVoltageGraph(VoltageAlarmStateChar voltageAlarmState) {
        increment = voltageAlarmState.getFft_bin_size();
        if(averageList.size() < 5){
            averageList.add(voltageAlarmState);
        } else if (averageList.size() == 5) {
            ch1Avgs = new ArrayList<>();
            ch2Avgs = new ArrayList<>();
            ch3Avgs = new ArrayList<>();
            for(int i = 0; i < averageList.get(0).getCh1_fft_results().size(); i++){
                int ch1Avg = (averageList.get(0).getCh1_fft_results().get(i)
                        + averageList.get(1).getCh1_fft_results().get(i)
                        + averageList.get(2).getCh1_fft_results().get(i)
                        + averageList.get(3).getCh1_fft_results().get(i)
                        + averageList.get(4).getCh1_fft_results().get(i)) / 5;
                ch1Avgs.add(ch1Avg);

                int ch2Avg = (averageList.get(0).getCh2_fft_results().get(i)
                        + averageList.get(1).getCh2_fft_results().get(i)
                        + averageList.get(2).getCh2_fft_results().get(i)
                        + averageList.get(3).getCh2_fft_results().get(i)
                        + averageList.get(4).getCh2_fft_results().get(i)) / 5;
                ch2Avgs.add(ch2Avg);

                int ch3Avg = (averageList.get(0).getCh3_fft_results().get(i)
                        + averageList.get(1).getCh3_fft_results().get(i)
                        + averageList.get(2).getCh3_fft_results().get(i)
                        + averageList.get(3).getCh3_fft_results().get(i)
                        + averageList.get(4).getCh3_fft_results().get(i)) / 5;
                ch3Avgs.add(ch3Avg);
            }

            averageList.add(new VoltageAlarmStateChar(ch1Avgs, ch2Avgs, ch3Avgs));
        } else {
            List<Integer> ch1AvgsNew = new ArrayList<>();
            List<Integer> ch2AvgsNew = new ArrayList<>();
            List<Integer> ch3AvgsNew = new ArrayList<>();
            for(int i = 0; i < ch1Avgs.size(); i++){
                int ch1Avg = ch1Avgs.get(i) + (voltageAlarmState.getCh1_fft_results().get(i) / 5) - (averageList.get(0).getCh1_fft_results().get(i) / 5);
                int ch2Avg = ch1Avgs.get(i) + (voltageAlarmState.getCh2_fft_results().get(i) / 5) - (averageList.get(0).getCh2_fft_results().get(i) / 5);
                int ch3Avg = ch1Avgs.get(i) + (voltageAlarmState.getCh3_fft_results().get(i) / 5) - (averageList.get(0).getCh3_fft_results().get(i) / 5);
                ch1AvgsNew.add(ch1Avg);
                ch2AvgsNew.add(ch2Avg);
                ch3AvgsNew.add(ch3Avg);
            }
            averageList.remove(0);
            ch1Avgs = ch1AvgsNew;
            ch2Avgs = ch2AvgsNew;
            ch3Avgs = ch3AvgsNew;
            averageList.add(new VoltageAlarmStateChar(ch1Avgs, ch2Avgs, ch3Avgs));

            if(voltageGraph1 != null){
                ArrayList<Entry> ch1Entries = new ArrayList<>();
                for(int result : voltageAlarmState.getCh1_fft_results()){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch1Entries.add(new Entry(xValue, result));
                }
                lastX = 0;
                LineDataSet ch1Set = new LineDataSet(ch1Entries, "Channel 1");
                ch1Set.setColor(ColorTemplate.getHoloBlue());
                ch1Set.setDrawValues(false);
                ch1Set.setAxisDependency(YAxis.AxisDependency.LEFT);
                ch1Set.setCircleRadius(1);
                ch1Set.setCircleHoleColor(ch1Set.getColor());
                ch1Set.setCircleColor(ch1Set.getColor());

                List<ILineDataSet> ch1DataSet = new ArrayList<>();
                ch1DataSet.add(ch1Set);

                //add average line
                ArrayList<Entry> ch1AvgEntries = new ArrayList<>();
                for(int result : ch1Avgs){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch1AvgEntries.add(new Entry(xValue, result));
                }
                lastX = 0;
                LineDataSet ch1AvgSet = new LineDataSet(ch1AvgEntries, "Channel 1 Average");
                ch1AvgSet.setColor(Color.RED);
                ch1AvgSet.setDrawValues(false);
                ch1AvgSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                ch1AvgSet.setCircleRadius(1);
                ch1AvgSet.setCircleHoleColor(ch1AvgSet.getColor());
                ch1AvgSet.setCircleColor(ch1AvgSet.getColor());

                ch1DataSet.add(ch1AvgSet);

                LineData ch1Data = new LineData(ch1DataSet);

                voltageGraph1.setData(ch1Data);
                voltageGraph1.invalidate();
            }

            if(voltageGraph2 != null){
                ArrayList<Entry> ch2Entries = new ArrayList<>();
                for(int result : voltageAlarmState.getCh2_fft_results()){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch2Entries.add(new Entry(xValue, result));
                }
                lastX = 0;
                LineDataSet ch2Set = new LineDataSet(ch2Entries, "Channel 1");
                ch2Set.setColor(ColorTemplate.getHoloBlue());
                ch2Set.setDrawValues(false);
                ch2Set.setAxisDependency(YAxis.AxisDependency.LEFT);
                ch2Set.setCircleRadius(1);
                ch2Set.setCircleHoleColor(ch2Set.getColor());
                ch2Set.setCircleColor(ch2Set.getColor());

                List<ILineDataSet> ch2DataSet = new ArrayList<>();
                ch2DataSet.add(ch2Set);

                //add average line
                ArrayList<Entry> ch2AvgEntries = new ArrayList<>();
                for(int result : ch2Avgs){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch2AvgEntries.add(new Entry(xValue, result));
                }
                lastX = 0;
                LineDataSet ch2AvgSet = new LineDataSet(ch2AvgEntries, "Channel 1 Average");
                ch2AvgSet.setColor(Color.RED);
                ch2AvgSet.setDrawValues(false);
                ch2AvgSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                ch2AvgSet.setCircleRadius(1);
                ch2AvgSet.setCircleHoleColor(ch2AvgSet.getColor());
                ch2AvgSet.setCircleColor(ch2AvgSet.getColor());

                ch2DataSet.add(ch2AvgSet);

                LineData ch2Data = new LineData(ch2DataSet);

                voltageGraph2.setData(ch2Data);
                voltageGraph2.invalidate();
            }

            if(voltageGraph3 != null){
                ArrayList<Entry> ch3Entries = new ArrayList<>();
                for(int result : voltageAlarmState.getCh3_fft_results()){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch3Entries.add(new Entry(xValue, result));
                }
                lastX = 0;
                LineDataSet ch3Set = new LineDataSet(ch3Entries, "Channel 1");
                ch3Set.setColor(ColorTemplate.getHoloBlue());
                ch3Set.setDrawValues(false);
                ch3Set.setAxisDependency(YAxis.AxisDependency.LEFT);
                ch3Set.setCircleRadius(1);
                ch3Set.setCircleHoleColor(ch3Set.getColor());
                ch3Set.setCircleColor(ch3Set.getColor());

                List<ILineDataSet> ch3DataSet = new ArrayList<>();
                ch3DataSet.add(ch3Set);

                //add average line
                ArrayList<Entry> ch3AvgEntries = new ArrayList<>();
                for(int result : ch3Avgs){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch3AvgEntries.add(new Entry(xValue, result));
                }
                lastX = 0;
                LineDataSet ch3AvgSet = new LineDataSet(ch3AvgEntries, "Channel 1 Average");
                ch3AvgSet.setColor(Color.RED);
                ch3AvgSet.setDrawValues(false);
                ch3AvgSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                ch3AvgSet.setCircleRadius(1);
                ch3AvgSet.setCircleHoleColor(ch3AvgSet.getColor());
                ch3AvgSet.setCircleColor(ch3AvgSet.getColor());

                ch3DataSet.add(ch3AvgSet);

                LineData ch3Data = new LineData(ch3DataSet);

                voltageGraph3.setData(ch3Data);
                voltageGraph3.invalidate();
            }
        }
    }

    int i = 0;
    public void updateAccelerometerGraph(AccelerometerData accelerometerData){
        i++;
        if(accelerationGraph1 != null ){
            LineData data1 = accelerationGraph1.getData();
            if (data1 != null) {

                ILineDataSet set = data1.getDataSetByIndex(0);
                // set.addEntry(...); // can be called as well

                if (set == null) {
                    set = createSet("X");
                    data1.addDataSet(set);
                }


                data1.addEntry(new Entry(i, (float) accelerometerData.getxValue()), 0);
                data1.notifyDataChanged();

                // let the chart know it's data has changed
                accelerationGraph1.notifyDataSetChanged();

                // limit the number of visible entries
                accelerationGraph1.setVisibleXRangeMaximum(40);
                // chart.setVisibleYRange(30, AxisDependency.LEFT);

                // move to the latest entry
                accelerationGraph1.moveViewToX(data1.getEntryCount());
            }
        }
        if(accelerationGraph2 != null ){
            LineData data2 = accelerationGraph2.getData();
            if (data2 != null) {

                ILineDataSet set = data2.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("Y");
                    data2.addDataSet(set);
                }

                data2.addEntry(new Entry(i, (float) accelerometerData.getyValue()), 0);
                data2.notifyDataChanged();

                accelerationGraph2.notifyDataSetChanged();

                accelerationGraph2.setVisibleXRangeMaximum(40);
                accelerationGraph2.moveViewToX(data2.getEntryCount());
            }
        }
        if(accelerationGraph3 != null ){
            LineData data3 = accelerationGraph3.getData();
            if (data3 != null) {

                ILineDataSet set = data3.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("Z");
                    data3.addDataSet(set);
                }

                data3.addEntry(new Entry(i2, (float) accelerometerData.getzValue()), 0);
                data3.notifyDataChanged();

                accelerationGraph3.notifyDataSetChanged();

                accelerationGraph3.setVisibleXRangeMaximum(40);
                accelerationGraph3.moveViewToX(data3.getEntryCount());
            }
        }
    }

    int i2 = 0;
    public void updateTempHumidityPressureGraph(TempHumidPressure tempHumidPressure){
        i2++;
        if(tempGraph != null ){
            LineData data1 = tempGraph.getData();
            if (data1 != null) {

                ILineDataSet set = data1.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("humidity");
                    data1.addDataSet(set);
                }

                data1.addEntry(new Entry(i2, (float) tempHumidPressure.getTemp()), 0);
                data1.notifyDataChanged();

                tempGraph.notifyDataSetChanged();

                tempGraph.setVisibleXRangeMaximum(40);
                tempGraph.moveViewToX(data1.getEntryCount());
            }
        }
        if(humidityGraph != null ){
            LineData data2 = humidityGraph.getData();
            if (data2 != null) {

                ILineDataSet set = data2.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("temperature");
                    data2.addDataSet(set);
                }

                data2.addEntry(new Entry(i2, (float) tempHumidPressure.getHumid()), 0);
                data2.notifyDataChanged();

                humidityGraph.notifyDataSetChanged();

                humidityGraph.setVisibleXRangeMaximum(40);
                humidityGraph.moveViewToX(data2.getEntryCount());
            }
        }
        if(pressureGraph != null ){
            LineData data3 = pressureGraph.getData();
            if (data3 != null) {

                ILineDataSet set = data3.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("pressure");
                    data3.addDataSet(set);
                }

                data3.addEntry(new Entry(i2, (float) tempHumidPressure.getPres()), 0);
                data3.notifyDataChanged();

                pressureGraph.notifyDataSetChanged();

                pressureGraph.setVisibleXRangeMaximum(40);
                pressureGraph.moveViewToX(data3.getEntryCount());
            }
        }
    }

    public void initializeVoltageGraphs(){
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

        voltageGraph1 = rootView.findViewById(R.id.voltage_sensor_graph_1);
        voltageGraph1.setDragEnabled(true);
        voltageGraph1.setScaleEnabled(true);
        voltageGraph1.setDrawGridBackground(false);
        voltageGraph1.getDescription().setEnabled(false);

        voltageGraph1.setPinchZoom(true);

        LineData data1 = new LineData();
        data1.setValueTextColor(Color.RED);

        voltageGraph1.setData(data1);

        XAxis x1 = voltageGraph1.getXAxis();
        x1.setTypeface(Typeface.SANS_SERIF);
        x1.setTextColor(Color.BLACK);
        x1.setDrawGridLines(true);
        x1.setEnabled(true);
        x1.setDrawGridLines(true);

        YAxis leftAxis = voltageGraph1.getAxisLeft();
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = voltageGraph1.getAxisRight();
        rightAxis.setEnabled(false);

        // second acceleration graph
        voltageGraph2 = rootView.findViewById(R.id.voltage_sensor_graph_2);
        voltageGraph2.setDragEnabled(true);
        voltageGraph2.setScaleEnabled(true);
        voltageGraph2.setDrawGridBackground(false);
        voltageGraph2.getDescription().setEnabled(false);

        voltageGraph2.setPinchZoom(true);

        LineData data2 = new LineData();
        data2.setValueTextColor(Color.RED);

        voltageGraph2.setData(data2);

        XAxis x2 = voltageGraph2.getXAxis();
        x2.setTypeface(Typeface.SANS_SERIF);
        x2.setTextColor(Color.BLACK);
        x2.setDrawGridLines(true);
        x2.setEnabled(true);
        x2.setDrawGridLines(true);

        YAxis leftAxis2 = voltageGraph2.getAxisLeft();
        leftAxis2.setTypeface(Typeface.SANS_SERIF);
        leftAxis2.setTextColor(Color.BLACK);
        leftAxis2.setDrawGridLines(true);

        YAxis rightAxis2 = voltageGraph2.getAxisRight();
        rightAxis2.setEnabled(false);

        // third acceleration graph
        voltageGraph3 = rootView.findViewById(R.id.voltage_sensor_graph_3);
        voltageGraph3.setDragEnabled(true);
        voltageGraph3.setScaleEnabled(true);
        voltageGraph3.setDrawGridBackground(false);
        voltageGraph3.getDescription().setEnabled(false);

        voltageGraph3.setPinchZoom(true);

        LineData data3 = new LineData();
        data3.setValueTextColor(Color.RED);

        voltageGraph3.setData(data3);

        XAxis x3 = voltageGraph3.getXAxis();
        x3.setTypeface(Typeface.SANS_SERIF);
        x3.setTextColor(Color.BLACK);
        x3.setDrawGridLines(true);
        x3.setEnabled(true);
        x3.setDrawGridLines(true);

        YAxis leftAxis3 = voltageGraph3.getAxisLeft();
        leftAxis3.setTypeface(Typeface.SANS_SERIF);
        leftAxis3.setTextColor(Color.BLACK);
        leftAxis3.setDrawGridLines(true);

        YAxis rightAxis3 = voltageGraph3.getAxisRight();
        rightAxis3.setEnabled(false);
    }

    public void initializeAccelerationGraphs(){
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

        //Acceleration Graphs
        accelerationGraph1 = rootView.findViewById(R.id.acceleration_sensor_graph_1);
        accelerationGraph1.setDragEnabled(true);
        accelerationGraph1.setScaleEnabled(true);
        accelerationGraph1.setDrawGridBackground(false);
        accelerationGraph1.getDescription().setEnabled(false);

        accelerationGraph1.setPinchZoom(true);

        LineData data1 = new LineData();
        data1.setValueTextColor(Color.RED);

        accelerationGraph1.setData(data1);

        XAxis x1 = accelerationGraph1.getXAxis();
        x1.setTypeface(Typeface.SANS_SERIF);
        x1.setTextColor(Color.BLACK);
        x1.setDrawGridLines(true);
        x1.setEnabled(true);
        x1.setDrawGridLines(true);
        x1.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis = accelerationGraph1.getAxisLeft();
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = accelerationGraph1.getAxisRight();
        rightAxis.setEnabled(false);

        // second acceleration graph
        accelerationGraph2 = rootView.findViewById(R.id.acceleration_sensor_graph_2);
        accelerationGraph2.setDragEnabled(true);
        accelerationGraph2.setScaleEnabled(true);
        accelerationGraph2.setDrawGridBackground(false);
        accelerationGraph2.getDescription().setEnabled(false);

        accelerationGraph2.setPinchZoom(true);

        LineData data2 = new LineData();
        data2.setValueTextColor(Color.RED);

        accelerationGraph2.setData(data2);

        XAxis x2 = accelerationGraph2.getXAxis();
        x2.setTypeface(Typeface.SANS_SERIF);
        x2.setTextColor(Color.BLACK);
        x2.setDrawGridLines(true);
        x2.setEnabled(true);
        x2.setDrawGridLines(true);
        x2.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis2 = accelerationGraph2.getAxisLeft();
        leftAxis2.setTypeface(Typeface.SANS_SERIF);
        leftAxis2.setTextColor(Color.BLACK);
        leftAxis2.setDrawGridLines(true);

        YAxis rightAxis2 = accelerationGraph2.getAxisRight();
        rightAxis2.setEnabled(false);

        // third acceleration graph
        accelerationGraph3 = rootView.findViewById(R.id.acceleration_sensor_graph_3);
        accelerationGraph3.setDragEnabled(true);
        accelerationGraph3.setScaleEnabled(true);
        accelerationGraph3.setDrawGridBackground(false);
        accelerationGraph3.getDescription().setEnabled(false);

        accelerationGraph3.setPinchZoom(true);

        LineData data3 = new LineData();
        data3.setValueTextColor(Color.RED);

        accelerationGraph3.setData(data3);

        XAxis x3 = accelerationGraph3.getXAxis();
        x3.setTypeface(Typeface.SANS_SERIF);
        x3.setTextColor(Color.BLACK);
        x3.setDrawGridLines(true);
        x3.setEnabled(true);
        x3.setDrawGridLines(true);
        x3.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis3 = accelerationGraph3.getAxisLeft();
        leftAxis3.setTypeface(Typeface.SANS_SERIF);
        leftAxis3.setTextColor(Color.BLACK);
        leftAxis3.setDrawGridLines(true);

        YAxis rightAxis3 = accelerationGraph3.getAxisRight();
        rightAxis3.setEnabled(false);
    }

    public void initializeTempHumidPressureGraphs(){
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

        //temp graph
        tempGraph = rootView.findViewById(R.id.temperature_graph);
        tempGraph.setDragEnabled(true);
        tempGraph.setScaleEnabled(true);
        tempGraph.setDrawGridBackground(false);
        tempGraph.getDescription().setEnabled(false);

        tempGraph.setPinchZoom(true);

        LineData tempData = new LineData();
        tempData.setValueTextColor(Color.RED);

        tempGraph.setData(tempData);

        XAxis tempX = tempGraph.getXAxis();
        tempX.setTypeface(Typeface.SANS_SERIF);
        tempX.setTextColor(Color.BLACK);
        tempX.setDrawGridLines(true);
        tempX.setEnabled(true);
        tempX.setDrawGridLines(true);
        tempX.setValueFormatter(new DateValueFormatter());

        YAxis leftAxisTemp = tempGraph.getAxisLeft();
        leftAxisTemp.setTypeface(Typeface.SANS_SERIF);
        leftAxisTemp.setTextColor(Color.BLACK);
        leftAxisTemp.setDrawGridLines(true);

        YAxis rightAxisTemp = tempGraph.getAxisRight();
        rightAxisTemp.setEnabled(false);

        //humidity graph
        humidityGraph = rootView.findViewById(R.id.humidity_graph);
        humidityGraph.setDragEnabled(true);
        humidityGraph.setScaleEnabled(true);
        humidityGraph.setDrawGridBackground(false);
        humidityGraph.getDescription().setEnabled(false);

        humidityGraph.setPinchZoom(true);

        LineData humudData = new LineData();
        humudData.setValueTextColor(Color.RED);

        humidityGraph.setData(humudData);

        XAxis humidX = humidityGraph.getXAxis();
        humidX.setTypeface(Typeface.SANS_SERIF);
        humidX.setTextColor(Color.BLACK);
        humidX.setDrawGridLines(true);
        humidX.setEnabled(true);
        humidX.setDrawGridLines(true);
        humidX.setValueFormatter(new DateValueFormatter());

        YAxis leftAxisHumid = humidityGraph.getAxisLeft();
        leftAxisHumid.setTypeface(Typeface.SANS_SERIF);
        leftAxisHumid.setTextColor(Color.BLACK);
        leftAxisHumid.setDrawGridLines(true);

        YAxis rightAxisHumid = humidityGraph.getAxisRight();
        rightAxisHumid.setEnabled(false);

        //pressure graph
        pressureGraph = rootView.findViewById(R.id.pressure_graph);
        pressureGraph.setDragEnabled(true);
        pressureGraph.setScaleEnabled(true);
        pressureGraph.setDrawGridBackground(false);
        pressureGraph.getDescription().setEnabled(false);

        pressureGraph.setPinchZoom(true);

        LineData presData = new LineData();
        presData.setValueTextColor(Color.RED);

        pressureGraph.setData(presData);

        XAxis presX = pressureGraph.getXAxis();
        presX.setTypeface(Typeface.SANS_SERIF);
        presX.setTextColor(Color.BLACK);
        presX.setDrawGridLines(true);
        presX.setEnabled(true);
        presX.setDrawGridLines(true);
        presX.setValueFormatter(new DateValueFormatter());

        YAxis leftAxisPres = pressureGraph.getAxisLeft();
        leftAxisPres.setTypeface(Typeface.SANS_SERIF);
        leftAxisPres.setTextColor(Color.BLACK);
        leftAxisPres.setDrawGridLines(true);

        YAxis rightAxisPres = pressureGraph.getAxisRight();
        rightAxisPres.setEnabled(false);
    }

    public class DateValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis){
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date d = new Date();
            return (dateFormat.format(d));
        }
    }

    private LineDataSet createSet(String label) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setCircleRadius(3);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setCircleHoleColor(set.getColor());
        set.setDrawValues(false);
        return set;
    }
}
