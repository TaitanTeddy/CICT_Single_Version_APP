package com.example.teddy.swipewindowspractice;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.teddy.swipewindowspractice.MainActivity.STATE_COMA;
import static com.example.teddy.swipewindowspractice.MainActivity.STATE_DROP;
import static com.example.teddy.swipewindowspractice.MainActivity.STATE_FALL;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment2 extends Fragment {
    private BarChart barChart;
    private MyDBHelper myDBHelper = null;
    private TextView txtTime1, txtTime2, txtTime3, txtTime4, txtTime5;

    public static final int FREQUENCY_HALF_HOUR=0, FREQUENCY_ONE_HOUR=1, FREQUENCY_TWO_HOUR=2, FREQUENCY_FOUR_HOUR=3, FREQUENCY_EIGHT_HOUR=4,
                    FREQENCY_ONE_DAY =5;
    public static int FrequencyState =0;

    public Fragment2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment2, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        barChart = (BarChart)getView().findViewById(R.id.Barchart);

        //預設頻率為每半小時4
        setBasicUI();
        openDB();
        setBasicChart();
    }
    private void openDB() {
        myDBHelper = new MyDBHelper(this.getContext());
    }

    private void closeDB(){
        myDBHelper.close();
    }

    public void setBasicUI(){
        txtTime1 = (TextView)getView().findViewById(R.id.txtTime1);
        txtTime2 = (TextView)getView().findViewById(R.id.txtTime2);
        txtTime3 = (TextView)getView().findViewById(R.id.txtTime3);
        txtTime4 = (TextView)getView().findViewById(R.id.txtTime4);
        txtTime5 = (TextView)getView().findViewById(R.id.txtTime5);
    }
    public void setBasicChart(){


        XAxis xAxis = barChart.getXAxis();
        //取得現在時間
        Calendar mCal = Calendar.getInstance();
        String dataFormat = "yyyy-MM-dd kk:mm:ss";
        SimpleDateFormat df = new SimpleDateFormat(dataFormat);
        SimpleDateFormat dfXaxis = new SimpleDateFormat("kk:mm:ss");
        SimpleDateFormat dfXText = new SimpleDateFormat("yyyy-MM-dd");
        //String now = df.format(mCal.getTime());
        //Log.d(TAG,"現在時間是 " + now);

        String[] TimeArray = new String[5];

        switch(FrequencyState) {
            case FREQUENCY_HALF_HOUR:
                TimeArray = getTimeArray(mCal,TimeArray,dfXaxis,dfXText,1);
                break;
            case FREQUENCY_ONE_HOUR:
                TimeArray = getTimeArray(mCal,TimeArray,dfXaxis,dfXText,2);
                break;
            case FREQUENCY_TWO_HOUR:
                TimeArray = getTimeArray(mCal,TimeArray,dfXaxis,dfXText,4);
                break;
            case FREQUENCY_FOUR_HOUR:
                TimeArray = getTimeArray(mCal,TimeArray,dfXaxis,dfXText,8);
                break;
            case FREQUENCY_EIGHT_HOUR:
                TimeArray = getTimeArray(mCal,TimeArray,dfXaxis,dfXText,16);
                break;
            case FREQENCY_ONE_DAY:
                TimeArray = getTimeArray(mCal,TimeArray,dfXaxis,dfXText,48);
                break;
        }

        SQLiteDatabase db =myDBHelper.getReadableDatabase();
        /*SQLiteDatabase db =myDBHelper.getWritableDatabase();
        String SQL = "CREATE TABLE IF NOT EXISTS " + "FallTable" +"("+
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_DATE VARCHAR(20), "+
                "_TIME VARCHAR(20), "+
                "_STATE INTEGER"+
                ");";
        db.execSQL(SQL);*/
        Cursor cursorTime = db.rawQuery("SELECT _TIME FROM FallTable", null);
        cursorTime.moveToFirst();
        Cursor cursorDate = db.rawQuery("SELECT _DATE FROM FallTable", null);
        cursorDate.moveToFirst();
        Cursor cursorState = db.rawQuery("SELECT _STATE FROM FallTable", null);
        cursorState.moveToFirst();

        ArrayList<String> intPreData =new ArrayList<String>();
        ArrayList<Integer> stateData = new ArrayList<Integer>();
        int DataCount = 0;

        //取得資料庫的每個值存入strPreData[]
        if (cursorTime.getCount()>0 && cursorDate.getCount()>0 && cursorState.getCount()>0){
            do {
                //包括日期轉成long方便進行時間計算
                intPreData.add(cursorDate.getString(0)+" "+cursorTime.getString(0));
                Log.d("讀出資料庫時間", intPreData.get(DataCount));
                Log.d("資料庫狀態", cursorState.getString(0));
                stateData.add(cursorState.getInt(0));
                DataCount++;
            } while (cursorTime.moveToNext() && cursorDate.moveToNext() && cursorState.moveToNext());
        }
        Log.d("stateData",stateData.toString());
        //累計危險發生幾次的陣列
        //float[] BarArray = new float[5];

        //取的現在時間
        long nowTime = 0;
        try {
            nowTime = df.parse(df.format(mCal.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        float[] FallArray = new float[5];
        float[] ComaArray = new float[5];
        float[] DropArray = new float[5];

        for (int i = 0; i <stateData.size();i++) {
            Log.d("What State",Integer.toString(stateData.get(i)));
            switch (FrequencyState) {
                case FREQUENCY_HALF_HOUR:
                    switch (stateData.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, intPreData, 0.5f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,intPreData,0.5f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,intPreData,0.5f,DropArray);
                            break;
                    }
                case FREQUENCY_ONE_HOUR:
                    switch (stateData.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, intPreData, 1.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,intPreData,1.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,intPreData,1.0f,DropArray);
                            break;
                    }
                    break;
                case FREQUENCY_TWO_HOUR:
                    switch (stateData.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, intPreData, 2.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,intPreData,2.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,intPreData,2.0f,DropArray);
                            break;
                    }
                    break;
                case FREQUENCY_FOUR_HOUR:
                    switch (stateData.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, intPreData, 4.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,intPreData,4.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,intPreData,4.0f,DropArray);
                            break;
                    }
                    break;
                case FREQUENCY_EIGHT_HOUR:
                    switch (stateData.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, intPreData, 8.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,intPreData,8.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,intPreData,8.0f,DropArray);
                            break;
                    }
                    break;
                case FREQENCY_ONE_DAY:
                    switch (stateData.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, intPreData, 24.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,intPreData,24.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,intPreData,24.0f,DropArray);
                            break;
                    }
                    break;
            }
        }

        xAxis.setValueFormatter(new xAxisValueFormatter(TimeArray));

        YAxis yAxisR = barChart.getAxis(YAxis.AxisDependency.RIGHT);
        YAxis yAxisL = barChart.getAxis(YAxis.AxisDependency.LEFT);
        yAxisR.setEnabled(false);
        yAxisL.setAxisMinimum(0);
        yAxisL.setAxisMaximum(20);

        Legend legend = barChart.getLegend();
        legend.setXEntrySpace(30f);

        //加入第一筆資料
        List<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0, new float[] { FallArray[0], ComaArray[0], DropArray[0] }));
        entries.add(new BarEntry(1, new float[] { FallArray[1], ComaArray[1], DropArray[1] }));
        entries.add(new BarEntry(2, new float[] { FallArray[2], ComaArray[2], DropArray[2] }));
        entries.add(new BarEntry(3, new float[] { FallArray[3], ComaArray[3], DropArray[3] }));
        entries.add(new BarEntry(4, new float[] { FallArray[4], ComaArray[4], DropArray[4] }));


        BarDataSet barDataSet = new BarDataSet(entries, "");

        barDataSet.setColors(getColors());
        barDataSet.setStackLabels(new String[]{"跌倒","昏迷","墜落"});

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.8f); // set custom bar width

        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setData(barData);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.invalidate(); // refresh
    }

    /*private ArrayList<String> PreTimeArray(){
        return
    }*/

    private String[] getTimeArray(Calendar mCal, String[] TimeArray, SimpleDateFormat dfXaxis,SimpleDateFormat dfXText,int multi){
        mCal.add(Calendar.MINUTE, -120 * multi);
        TimeArray[0] = dfXaxis.format(mCal.getTime());
        txtTime1.setText(dfXText.format(mCal.getTime()));

        //將時間放入X軸Array
        for (int i = 1; i < 5; i++) {
            mCal.add(Calendar.MINUTE, 30* multi );
            TimeArray[i] = dfXaxis.format(mCal.getTime());

            //寫上x軸的日期 (API只能畫一個)
            switch (i) {
                case 1:
                    txtTime2.setText(dfXText.format(mCal.getTime()));
                    break;
                case 2:
                    txtTime3.setText(dfXText.format(mCal.getTime()));
                    break;
                case 3:
                    txtTime4.setText(dfXText.format(mCal.getTime()));
                    break;
                case 4:
                    txtTime5.setText(dfXText.format(mCal.getTime()));
                    break;
            }
        }
        return TimeArray;
    }

    private float[] getBarArray(int count, long nowTime,SimpleDateFormat df,ArrayList<String> intPreData ,float multi,float[] barArray){

                //判斷資料應該放在哪個Bar之中
                    try {
                        //資料庫資料轉long
                        long dbTime = df.parse(intPreData.get(count)).getTime();
                        //相減算區間
                        long tmp = nowTime - dbTime;
                        //計算時間 秒數,要放入的Array,頻率(半小時)
                        //換算成毫秒來進行時間加減
                        long Hour = 60 * 60 * 1000;

                        if (0 <= tmp && tmp < Hour*multi) {
                            barArray[4]++;
                        } else if (Hour * multi <= tmp && tmp < Hour * multi*2) {
                            barArray[3]++;
                        } else if (Hour * multi*2 <= tmp && tmp < Hour * multi*3) {
                            barArray[2]++;
                        } else if (Hour * multi*3 <= tmp && tmp < Hour * multi*4) {
                            barArray[1]++;
                        } else if (Hour * multi*4 <= tmp && tmp < Hour * multi*5) {
                            barArray[0]++;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
        return barArray;
    }


    private int[] getColors() {

        int stacksize = 3;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i];
        }

        return colors;
    }

    public class xAxisValueFormatter implements IAxisValueFormatter {

        private String[] mTimeValues;

        public xAxisValueFormatter(String[] timeValues){
            this.mTimeValues = timeValues;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mTimeValues[(int)value];
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDB();
    }
}
