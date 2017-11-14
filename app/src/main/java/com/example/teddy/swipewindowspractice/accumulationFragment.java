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
public class accumulationFragment extends Fragment {
    private BarChart barChart;
    private MyDBHelper myDBHelper = null;
    private TextView txtTime1, txtTime2, txtTime3, txtTime4, txtTime5;

    public static final int FREQUENCY_HALF_HOUR=0, FREQUENCY_ONE_HOUR=1,
                        FREQUENCY_TWO_HOUR=2, FREQUENCY_FOUR_HOUR=3,
                        FREQUENCY_EIGHT_HOUR=4, FREQUENCY_ONE_DAY =5;
    public static int FrequencyState =0;

    public accumulationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accumulation, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        barChart = (BarChart)getView().findViewById(R.id.Barchart);

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


        //取得現在時間


        /***
         * 從DataBase取出資料
         * 放進FallArray、ComaArray、DropArray中
         */
        SQLiteDatabase db =myDBHelper.getReadableDatabase();
        /*SQLiteDatabase db =myDBHelper.getWritableDatabase();
        String SQL = "CREATE TABLE IF NOT EXISTS " + "FallTable" +"("+
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_DATE VARCHAR(20), "+
                "_TIME VARCHAR(20), "+
                "_STATE INTEGER"+
                ");";
        db.execSQL(SQL);*/

        //將cursor指向每個資料表的第一個
        Cursor cursorTime = db.rawQuery("SELECT _TIME FROM FallTable", null);
        cursorTime.moveToFirst();
        Cursor cursorDate = db.rawQuery("SELECT _DATE FROM FallTable", null);
        cursorDate.moveToFirst();
        Cursor cursorState = db.rawQuery("SELECT _STATE FROM FallTable", null);
        cursorState.moveToFirst();


        ArrayList<String> dbTimeList =new ArrayList<String>();
        ArrayList<Integer> dbStateList = new ArrayList<Integer>();
        int DataCount = 0;

        //取得資料庫的每個值存入dbTimeList、dbStateList
        if (cursorTime.getCount()>0 && cursorDate.getCount()>0 && cursorState.getCount()>0){
            do {
                //日期跟時間都放dbTimeList，轉成long方便進行時間計算
                dbTimeList.add(cursorDate.getString(0)+" "+cursorTime.getString(0));
                Log.d("讀出資料庫時間", dbTimeList.get(DataCount));
                Log.d("資料庫狀態", cursorState.getString(0));
                dbStateList.add(cursorState.getInt(0));
                DataCount++;
            } while (cursorTime.moveToNext() && cursorDate.moveToNext() && cursorState.moveToNext());
        }
        //Log.d("stateData",dbStateList.toString());

        //取得現在時間
        Calendar mCal = Calendar.getInstance();
        String dataFormat = "yyyy-MM-dd kk:mm:ss";
        SimpleDateFormat df = new SimpleDateFormat(dataFormat);
        long nowTime= 0;
        try {
            nowTime = df.parse(df.format(mCal.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //每個狀態存一個，以放入BarChart之中
        float[] FallArray = new float[5];
        float[] ComaArray = new float[5];
        float[] DropArray = new float[5];

        //將每一筆資料都放入應放入的Array之中
        for (int i = 0; i <dbStateList.size();i++) {
            //Log.d("What State",Integer.toString(dbStateList.get(i)));
            switch (FrequencyState) {
                case FREQUENCY_HALF_HOUR:
                    switch (dbStateList.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, dbTimeList, 0.5f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,dbTimeList,0.5f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,dbTimeList,0.5f,DropArray);
                            break;
                    }
                case FREQUENCY_ONE_HOUR:
                    switch (dbStateList.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, dbTimeList, 1.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,dbTimeList,1.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,dbTimeList,1.0f,DropArray);
                            break;
                    }
                    break;
                case FREQUENCY_TWO_HOUR:
                    switch (dbStateList.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, dbTimeList, 2.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,dbTimeList,2.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,dbTimeList,2.0f,DropArray);
                            break;
                    }
                    break;
                case FREQUENCY_FOUR_HOUR:
                    switch (dbStateList.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, dbTimeList, 4.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,dbTimeList,4.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,dbTimeList,4.0f,DropArray);
                            break;
                    }
                    break;
                case FREQUENCY_EIGHT_HOUR:
                    switch (dbStateList.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, dbTimeList, 8.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,dbTimeList,8.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,dbTimeList,8.0f,DropArray);
                            break;
                    }
                    break;
                case FREQUENCY_ONE_DAY:
                    switch (dbStateList.get(i)) {
                        case STATE_FALL:
                            FallArray = getBarArray(i, nowTime, df, dbTimeList, 24.0f,FallArray);
                            break;
                        case STATE_COMA:
                            ComaArray = getBarArray(i,nowTime,df,dbTimeList,24.0f,ComaArray);
                            break;
                        case STATE_DROP:
                            DropArray= getBarArray(i,nowTime,df,dbTimeList,24.0f,DropArray);
                            break;
                    }
                    break;
            }
        }


        /***
         * 做X軸的日期+時間
         */
        String[] xTimeArray = new String[5];

        //根據設定的取樣頻率，來決定要放的時間
        switch(FrequencyState) {
            case FREQUENCY_HALF_HOUR:
                xTimeArray = getTimeArray(mCal,xTimeArray,1);
                break;
            case FREQUENCY_ONE_HOUR:
                xTimeArray = getTimeArray(mCal,xTimeArray,2);
                break;
            case FREQUENCY_TWO_HOUR:
                xTimeArray = getTimeArray(mCal,xTimeArray,4);
                break;
            case FREQUENCY_FOUR_HOUR:
                xTimeArray = getTimeArray(mCal,xTimeArray,8);
                break;
            case FREQUENCY_EIGHT_HOUR:
                xTimeArray = getTimeArray(mCal,xTimeArray,16);
                break;
            case FREQUENCY_ONE_DAY:
                xTimeArray = getTimeArray(mCal,xTimeArray,48);
                break;
        }

        XAxis xAxis = barChart.getXAxis();
        //設定X軸的值為xTimeArray中的值
        xAxis.setValueFormatter(new xAxisValueFormatter(xTimeArray));

        YAxis yAxisR = barChart.getAxis(YAxis.AxisDependency.RIGHT);
        //將Y軸的右邊取消顯示
        yAxisR.setEnabled(false);
        YAxis yAxisL = barChart.getAxis(YAxis.AxisDependency.LEFT);
        //設定Y軸的最大最小值
        yAxisL.setAxisMinimum(0);
        yAxisL.setAxisMaximum(30);

        Legend legend = barChart.getLegend();
        legend.setXEntrySpace(30f);

        List<BarEntry> entries = new ArrayList<>();

        //將先前的Array放入BarEntry之中
        entries.add(new BarEntry(0, new float[] { FallArray[0], ComaArray[0], DropArray[0] }));
        entries.add(new BarEntry(1, new float[] { FallArray[1], ComaArray[1], DropArray[1] }));
        entries.add(new BarEntry(2, new float[] { FallArray[2], ComaArray[2], DropArray[2] }));
        entries.add(new BarEntry(3, new float[] { FallArray[3], ComaArray[3], DropArray[3] }));
        entries.add(new BarEntry(4, new float[] { FallArray[4], ComaArray[4], DropArray[4] }));


        BarDataSet barDataSet = new BarDataSet(entries, "");

        //設定bar的顯示和顏色
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

    private String[] getTimeArray(Calendar mCal, String[] TimeArray,int multi){
        SimpleDateFormat dfTime = new SimpleDateFormat("kk:mm:ss");
        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
        mCal.add(Calendar.MINUTE, -120 * multi);
        TimeArray[0] = dfTime.format(mCal.getTime());
        txtTime1.setText(dfDate.format(mCal.getTime()));

        //將時間放入X軸Array
        for (int i = 1; i < 5; i++) {
            mCal.add(Calendar.MINUTE, 30* multi );
            TimeArray[i] = dfTime.format(mCal.getTime());

            //寫上x軸的日期 (API只能畫一個) 用textView畫剩下的
            switch (i) {
                case 1:
                    txtTime2.setText(dfDate.format(mCal.getTime()));
                    break;
                case 2:
                    txtTime3.setText(dfDate.format(mCal.getTime()));
                    break;
                case 3:
                    txtTime4.setText(dfDate.format(mCal.getTime()));
                    break;
                case 4:
                    txtTime5.setText(dfDate.format(mCal.getTime()));
                    break;
            }
        }
        return TimeArray;
    }

    private float[] getBarArray(int count, long nowTime,SimpleDateFormat df,ArrayList<String> dbTimeList ,float multi,float[] barArray){

            //判斷資料應該放在哪個Bar之中
                    try {
                        //資料庫資料轉long
                        long dbTime = df.parse(dbTimeList.get(count)).getTime();
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

        int stackSize = 3;

        // have as many colors as stack-values per entry
        int[] colors = new int[stackSize];

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
