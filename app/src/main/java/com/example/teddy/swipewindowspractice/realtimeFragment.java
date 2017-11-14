package com.example.teddy.swipewindowspractice;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class realtimeFragment extends Fragment implements AcclerService.Callbacks{

    private LineChart chart;
    private LineDataSet lineDataSet;
    public static LineData lineData;
    private List<ILineDataSet> dataSets;
    public static ArrayList<String> TimeArray = new ArrayList<String>();
    private XAxis xAxis;
    private YAxis yAxisL;
    private LimitLine limitLine;
    private MyDBHelper myDBHelper = null;

    public static int Threshold = 15;
    public final static int ThresholdSen = 15, ThresholdNor = 20,ThresholdNotSen = 25 ;

    AcclerService acclerService;
    public static Boolean isBound =false;


    public realtimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_realtime, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        chart = (LineChart) getView().findViewById(R.id.chart);
        //畫出基礎圖表
        setBasicChart();
        //
        openDB();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), AcclerService.class);
        getActivity().bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (isBound) {
            getActivity().unbindService(serviceConnection);
            isBound = false;
        }
        closeDB();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AcclerService.LocalBinder binder = (AcclerService.LocalBinder) service;
            acclerService = binder.getService();
            isBound = true;
            acclerService.registerClient(realtimeFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };


    private void openDB() {
        myDBHelper = new MyDBHelper(this.getContext());
    }

    private void closeDB(){
        myDBHelper.close();
    }



    public void setBasicChart(){
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

        //加入第一筆資料
        List<Entry> entries = new ArrayList<Entry>();

        //輸入一個值避免error
        entries.add(new Entry(0, 0));
        //第一筆資料的標題
        lineDataSet = new LineDataSet(entries, "加速規數值");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighLightColor(Color.BLACK);
        lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS[1]);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawValues(false);

        //將兩個資料放入List之中
        dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(lineDataSet);

        //將list放到lineData之中
        lineData = new LineData(dataSets);
        lineData.setValueTextColor(Color.RED);

        xAxis = chart.getXAxis();
        xAxis.setLabelRotationAngle(90f);
        xAxis.setTextSize(10f);
        xAxis.setDrawLabels(true);
        xAxis.setTextColor(Color.BLUE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisR = chart.getAxis(YAxis.AxisDependency.RIGHT);
        yAxisL = chart.getAxis(YAxis.AxisDependency.LEFT);
        yAxisR.setEnabled(false);
        yAxisL.setAxisMinimum(0.0f);
        yAxisL.setAxisMaximum(30.0f);
        yAxisL.enableAxisLineDashedLine(1f,1f,1f);

        limitLine = new LimitLine((float)Threshold,"門檻值");
        limitLine.setLineColor(Color.RED);
        limitLine.setLineWidth(2f);
        yAxisL.addLimitLine(limitLine);

        //初始化TimeArray
        TimeArray.add("");

        //格式化x軸(使x軸用String呈現)
        xAxis.setValueFormatter(new  xAxisValueFormatter(TimeArray));

        //將lineData的data放入chart中
        chart.setData(lineData);

        //refresh
        chart.invalidate();
        chart.moveViewToX(lineData.getEntryCount());

    }

    //接收AccelerService的資料，並畫出來
    @Override
    public void updateChart() {

        //依據選擇的門檻值改變圖表上的門檻值
        switch (Threshold){
            case ThresholdSen:
                designThreshold(ThresholdSen);
                break;
            case ThresholdNor:
                designThreshold(ThresholdNor);
                break;
            case ThresholdNotSen:
                designThreshold(ThresholdNotSen);
                break;
        }

        //提醒lineData有更新了
        lineData.notifyDataChanged();
        chart.notifyDataSetChanged();
        //設定圖表中x軸最大的值
        chart.setVisibleXRangeMaximum(600);
        chart.moveViewToX(lineData.getEntryCount());
    }

    private void designThreshold(int threshold){
        yAxisL.removeAllLimitLines();
        limitLine = new LimitLine((float)threshold,"門檻值");
        limitLine.setLineColor(Color.RED);
        limitLine.setLineWidth(2f);
        yAxisL.addLimitLine(limitLine);
    }


    //將X軸變成String
    public class xAxisValueFormatter implements IAxisValueFormatter {

        private ArrayList<String> mTimeValues;

        public xAxisValueFormatter(ArrayList<String> timeValues){
            this.mTimeValues = timeValues;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mTimeValues.get((int)value);
        }
    }
}
