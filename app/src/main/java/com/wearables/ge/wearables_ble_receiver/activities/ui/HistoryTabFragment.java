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

    LineChart Ch1VoltageGraph;
    LineChart Ch2VoltageGraph;
    LineChart Ch3VoltageGraph;
    LineChart accelerationGraphX;
    LineChart accelerationGraphY;
    LineChart accelerationGraphZ;
    LineChart tempGraph;
    LineChart humidityGraph;
    LineChart pressureGraph;

    private int lastX = 0;

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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

        //first gather 5 data points to use to take the average
        if(averageList.size() < 5){
            averageList.add(voltageAlarmState);
        } else if (averageList.size() == 5) {
            //once we have all 5, get the average for every point in the fft_result lists
            ch1Avgs = new ArrayList<>();
            ch2Avgs = new ArrayList<>();
            ch3Avgs = new ArrayList<>();
            //just use the size of the ch1 list, they should all be the same size
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

            //create a new VoltageAlarmStateChar object with just the new averages and add it to the list
            averageList.add(new VoltageAlarmStateChar(ch1Avgs, ch2Avgs, ch3Avgs));
        } else {
            //once we have over 5 items, we use a new formula to compute average
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

            //FIFO the list and keep only 5 items in it
            averageList.remove(0);
            ch1Avgs = ch1AvgsNew;
            ch2Avgs = ch2AvgsNew;
            ch3Avgs = ch3AvgsNew;
            averageList.add(new VoltageAlarmStateChar(ch1Avgs, ch2Avgs, ch3Avgs));

            //now graph normal data with averaged data
            if(Ch1VoltageGraph != null){
                ArrayList<Entry> ch1Entries = new ArrayList<>();
                //add each fft result value to a list of Entry objects incrementing by the step size
                for(int result : voltageAlarmState.getCh1_fft_results()){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch1Entries.add(new Entry(xValue, result));
                }
                //reset increment
                lastX = 0;

                //create a data set for the live values
                LineDataSet ch1Set = new LineDataSet(ch1Entries, "Live");
                //set line color
                ch1Set.setColor(ColorTemplate.getHoloBlue());
                //don't draw numerical value on each point
                ch1Set.setDrawValues(false);
                ch1Set.setAxisDependency(YAxis.AxisDependency.LEFT);
                //size the circles on each individual point
                ch1Set.setCircleRadius(1);
                //set circle color to blend with color of data set
                ch1Set.setCircleHoleColor(ch1Set.getColor());
                ch1Set.setCircleColor(ch1Set.getColor());

                //create data set array to add more data sets later
                List<ILineDataSet> ch1DataSet = new ArrayList<>();
                //add live data set
                ch1DataSet.add(ch1Set);

                //create new Entry object array for the average line
                ArrayList<Entry> ch1AvgEntries = new ArrayList<>();
                //ch1Avgs is now the latest averages
                for(int result : ch1Avgs){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch1AvgEntries.add(new Entry(xValue, result));
                }
                //reset increment  again
                lastX = 0;

                //create new data set for the average line as before
                LineDataSet ch1AvgSet = new LineDataSet(ch1AvgEntries, "Average");
                ch1AvgSet.setColor(Color.RED);
                ch1AvgSet.setDrawValues(false);
                ch1AvgSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                ch1AvgSet.setCircleRadius(1);
                ch1AvgSet.setCircleHoleColor(ch1AvgSet.getColor());
                ch1AvgSet.setCircleColor(ch1AvgSet.getColor());

                //add average data set to the data set array from before
                ch1DataSet.add(ch1AvgSet);

                //create a LineData object out of the data set array
                LineData ch1Data = new LineData(ch1DataSet);

                //set the graph data to the new data object
                Ch1VoltageGraph.setData(ch1Data);
                //refresh the graph
                Ch1VoltageGraph.invalidate();
            }

            if(Ch2VoltageGraph != null){
                ArrayList<Entry> ch2Entries = new ArrayList<>();
                for(int result : voltageAlarmState.getCh2_fft_results()){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch2Entries.add(new Entry(xValue, result));
                }
                lastX = 0;
                LineDataSet ch2Set = new LineDataSet(ch2Entries, "Live");
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
                LineDataSet ch2AvgSet = new LineDataSet(ch2AvgEntries, "Average");
                ch2AvgSet.setColor(Color.RED);
                ch2AvgSet.setDrawValues(false);
                ch2AvgSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                ch2AvgSet.setCircleRadius(1);
                ch2AvgSet.setCircleHoleColor(ch2AvgSet.getColor());
                ch2AvgSet.setCircleColor(ch2AvgSet.getColor());

                ch2DataSet.add(ch2AvgSet);

                LineData ch2Data = new LineData(ch2DataSet);

                Ch2VoltageGraph.setData(ch2Data);
                Ch2VoltageGraph.invalidate();
            }

            if(Ch3VoltageGraph != null){
                ArrayList<Entry> ch3Entries = new ArrayList<>();
                for(int result : voltageAlarmState.getCh3_fft_results()){
                    lastX = lastX + increment;
                    float xValue = lastX - increment;
                    ch3Entries.add(new Entry(xValue, result));
                }
                lastX = 0;
                LineDataSet ch3Set = new LineDataSet(ch3Entries, "Live");
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
                LineDataSet ch3AvgSet = new LineDataSet(ch3AvgEntries, "Average");
                ch3AvgSet.setColor(Color.RED);
                ch3AvgSet.setDrawValues(false);
                ch3AvgSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                ch3AvgSet.setCircleRadius(1);
                ch3AvgSet.setCircleHoleColor(ch3AvgSet.getColor());
                ch3AvgSet.setCircleColor(ch3AvgSet.getColor());

                ch3DataSet.add(ch3AvgSet);

                LineData ch3Data = new LineData(ch3DataSet);

                Ch3VoltageGraph.setData(ch3Data);
                Ch3VoltageGraph.invalidate();
            }
        }
    }

    int i = 0;
    public void updateAccelerometerGraph(AccelerometerData accelerometerData){
        i++;
        //acceleration (and temp/humid/pressure) graphs are not update like the voltage graphs
        //these are continued lists that are added to instead of a whole new data set every time
        if(accelerationGraphX != null ){
            //get the current dataset
            LineData data1 = accelerationGraphX.getData();
            if (data1 != null) {

                //LineData object could be multiple LineDataSets so grab the first one in the list
                //we assume here that there is only one data set and we only care about the first
                ILineDataSet set = data1.getDataSetByIndex(0);
                // set.addEntry(...); // can be called as well

                if (set == null) {
                    //create a new set if there is none
                    set = createSet("X");
                    data1.addDataSet(set);
                }

                //add the new entry
                data1.addEntry(new Entry(i, (float) accelerometerData.getxValue()), 0);
                data1.notifyDataChanged();

                // let the chart know it's data has changed
                accelerationGraphX.notifyDataSetChanged();

                // limit the number of visible entries
                accelerationGraphX.setVisibleXRangeMaximum(40);
                // chart.setVisibleYRange(30, AxisDependency.LEFT);

                // move to the latest entry
                accelerationGraphX.moveViewToX(data1.getEntryCount());
            }
        }
        if(accelerationGraphY != null ){
            LineData data2 = accelerationGraphY.getData();
            if (data2 != null) {

                ILineDataSet set = data2.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("Y");
                    data2.addDataSet(set);
                }

                data2.addEntry(new Entry(i, (float) accelerometerData.getyValue()), 0);
                data2.notifyDataChanged();

                accelerationGraphY.notifyDataSetChanged();

                accelerationGraphY.setVisibleXRangeMaximum(40);
                accelerationGraphY.moveViewToX(data2.getEntryCount());
            }
        }
        if(accelerationGraphZ != null ){
            LineData data3 = accelerationGraphZ.getData();
            if (data3 != null) {

                ILineDataSet set = data3.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet("Z");
                    data3.addDataSet(set);
                }

                data3.addEntry(new Entry(i2, (float) accelerometerData.getzValue()), 0);
                data3.notifyDataChanged();

                accelerationGraphZ.notifyDataSetChanged();

                accelerationGraphZ.setVisibleXRangeMaximum(40);
                accelerationGraphZ.moveViewToX(data3.getEntryCount());
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
        //get the expandable layout container that wraps the voltage graphs
        LinearLayout expandableLayout1 = rootView.findViewById(R.id.collapsibleContainer1);
        //and the switch button attached to it
        Switch switchButton1 = rootView.findViewById(R.id.expand1);
        switchButton1.setChecked(true);
        //set switch button action to expand/collapse the view
        switchButton1.setOnClickListener( v -> {
            if (switchButton1.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_SHORT).show();
                expandView(expandableLayout1, 500);

            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_SHORT).show();
                collapseView(expandableLayout1, 500);
            }
        });

        //get the first voltage graph object
        Ch1VoltageGraph = rootView.findViewById(R.id.voltage_sensor_graph_1);
        //allow the user to drag to view other points
        Ch1VoltageGraph.setDragEnabled(true);
        //allow zooming
        Ch1VoltageGraph.setScaleEnabled(true);
        //disable grid background
        Ch1VoltageGraph.setDrawGridBackground(false);
        //disable description text
        Ch1VoltageGraph.getDescription().setEnabled(false);
        //allow pinch zooming
        Ch1VoltageGraph.setPinchZoom(true);

        //set x axis styling
        XAxis x1 = Ch1VoltageGraph.getXAxis();
        x1.setTypeface(Typeface.SANS_SERIF);
        x1.setTextColor(Color.BLACK);
        x1.setDrawGridLines(true);
        x1.setEnabled(true);
        x1.setDrawGridLines(true);

        YAxis leftAxis = Ch1VoltageGraph.getAxisLeft();
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = Ch1VoltageGraph.getAxisRight();
        rightAxis.setEnabled(false);

        // second voltage graph
        Ch2VoltageGraph = rootView.findViewById(R.id.voltage_sensor_graph_2);
        Ch2VoltageGraph.setDragEnabled(true);
        Ch2VoltageGraph.setScaleEnabled(true);
        Ch2VoltageGraph.setDrawGridBackground(false);
        Ch2VoltageGraph.getDescription().setEnabled(false);

        Ch2VoltageGraph.setPinchZoom(true);

        XAxis x2 = Ch2VoltageGraph.getXAxis();
        x2.setTypeface(Typeface.SANS_SERIF);
        x2.setTextColor(Color.BLACK);
        x2.setDrawGridLines(true);
        x2.setEnabled(true);
        x2.setDrawGridLines(true);

        YAxis leftAxis2 = Ch2VoltageGraph.getAxisLeft();
        leftAxis2.setTypeface(Typeface.SANS_SERIF);
        leftAxis2.setTextColor(Color.BLACK);
        leftAxis2.setDrawGridLines(true);

        YAxis rightAxis2 = Ch2VoltageGraph.getAxisRight();
        rightAxis2.setEnabled(false);

        // third voltage graph
        Ch3VoltageGraph = rootView.findViewById(R.id.voltage_sensor_graph_3);
        Ch3VoltageGraph.setDragEnabled(true);
        Ch3VoltageGraph.setScaleEnabled(true);
        Ch3VoltageGraph.setDrawGridBackground(false);
        Ch3VoltageGraph.getDescription().setEnabled(false);

        Ch3VoltageGraph.setPinchZoom(true);

        XAxis x3 = Ch3VoltageGraph.getXAxis();
        x3.setTypeface(Typeface.SANS_SERIF);
        x3.setTextColor(Color.BLACK);
        x3.setDrawGridLines(true);
        x3.setEnabled(true);
        x3.setDrawGridLines(true);

        YAxis leftAxis3 = Ch3VoltageGraph.getAxisLeft();
        leftAxis3.setTypeface(Typeface.SANS_SERIF);
        leftAxis3.setTextColor(Color.BLACK);
        leftAxis3.setDrawGridLines(true);

        YAxis rightAxis3 = Ch3VoltageGraph.getAxisRight();
        rightAxis3.setEnabled(false);
    }

    public void initializeAccelerationGraphs(){
        LinearLayout expandableLayout2 = rootView.findViewById(R.id.collapsibleContainer2);
        Switch switchButton2 = rootView.findViewById(R.id.expand2);
        switchButton2.setChecked(true);
        switchButton2.setOnClickListener( v -> {
            if (switchButton2.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_SHORT).show();
                expandView(expandableLayout2, 500);
            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_SHORT).show();
                collapseView(expandableLayout2, 500 );
            }
        });

        //Acceleration Graphs
        accelerationGraphX = rootView.findViewById(R.id.acceleration_sensor_graph_1);
        accelerationGraphX.setDragEnabled(true);
        accelerationGraphX.setScaleEnabled(true);
        accelerationGraphX.setDrawGridBackground(false);
        accelerationGraphX.getDescription().setEnabled(false);

        accelerationGraphX.setPinchZoom(true);

        LineData data1 = new LineData();
        data1.setValueTextColor(Color.RED);

        accelerationGraphX.setData(data1);

        XAxis x1 = accelerationGraphX.getXAxis();
        x1.setTypeface(Typeface.SANS_SERIF);
        x1.setTextColor(Color.BLACK);
        x1.setDrawGridLines(true);
        x1.setEnabled(true);
        x1.setDrawGridLines(true);
        //with acceleration and temp/humid/pressure graphs, we use timestamps so here we set the custom DateValueFormatter
        //as the value formatter class for the x axis.
        x1.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis = accelerationGraphX.getAxisLeft();
        leftAxis.setTypeface(Typeface.SANS_SERIF);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = accelerationGraphX.getAxisRight();
        rightAxis.setEnabled(false);

        // second acceleration graph
        accelerationGraphY = rootView.findViewById(R.id.acceleration_sensor_graph_2);
        accelerationGraphY.setDragEnabled(true);
        accelerationGraphY.setScaleEnabled(true);
        accelerationGraphY.setDrawGridBackground(false);
        accelerationGraphY.getDescription().setEnabled(false);

        accelerationGraphY.setPinchZoom(true);

        LineData data2 = new LineData();
        data2.setValueTextColor(Color.RED);

        accelerationGraphY.setData(data2);

        XAxis x2 = accelerationGraphY.getXAxis();
        x2.setTypeface(Typeface.SANS_SERIF);
        x2.setTextColor(Color.BLACK);
        x2.setDrawGridLines(true);
        x2.setEnabled(true);
        x2.setDrawGridLines(true);
        x2.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis2 = accelerationGraphY.getAxisLeft();
        leftAxis2.setTypeface(Typeface.SANS_SERIF);
        leftAxis2.setTextColor(Color.BLACK);
        leftAxis2.setDrawGridLines(true);

        YAxis rightAxis2 = accelerationGraphY.getAxisRight();
        rightAxis2.setEnabled(false);

        // third acceleration graph
        accelerationGraphZ = rootView.findViewById(R.id.acceleration_sensor_graph_3);
        accelerationGraphZ.setDragEnabled(true);
        accelerationGraphZ.setScaleEnabled(true);
        accelerationGraphZ.setDrawGridBackground(false);
        accelerationGraphZ.getDescription().setEnabled(false);

        accelerationGraphZ.setPinchZoom(true);

        LineData data3 = new LineData();
        data3.setValueTextColor(Color.RED);

        accelerationGraphZ.setData(data3);

        XAxis x3 = accelerationGraphZ.getXAxis();
        x3.setTypeface(Typeface.SANS_SERIF);
        x3.setTextColor(Color.BLACK);
        x3.setDrawGridLines(true);
        x3.setEnabled(true);
        x3.setDrawGridLines(true);
        x3.setValueFormatter(new DateValueFormatter());

        YAxis leftAxis3 = accelerationGraphZ.getAxisLeft();
        leftAxis3.setTypeface(Typeface.SANS_SERIF);
        leftAxis3.setTextColor(Color.BLACK);
        leftAxis3.setDrawGridLines(true);

        YAxis rightAxis3 = accelerationGraphZ.getAxisRight();
        rightAxis3.setEnabled(false);
    }

    public void initializeTempHumidPressureGraphs(){
        LinearLayout expandableLayout3 = rootView.findViewById(R.id.collapsibleContainer3);
        Switch switchButton3 = rootView.findViewById(R.id.expand3);
        switchButton3.setChecked(true);
        switchButton3.setOnClickListener( v -> {
            if (switchButton3.isChecked()) {
                Toast.makeText(this.getContext(), "expanding...", Toast.LENGTH_SHORT).show();
                expandView(expandableLayout3, 500);
            } else {
                Toast.makeText(this.getContext(), "collapsing...", Toast.LENGTH_SHORT).show();
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
