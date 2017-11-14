package com.example.teddy.swipewindowspractice;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.ContentValues.TAG;
import static com.example.teddy.swipewindowspractice.MainActivity.STATE_COMA;
import static com.example.teddy.swipewindowspractice.MainActivity.STATE_FALL;
import static com.example.teddy.swipewindowspractice.realtimeFragment.Threshold;
import static com.example.teddy.swipewindowspractice.realtimeFragment.TimeArray;
import static com.example.teddy.swipewindowspractice.realtimeFragment.lineData;

public class AcclerService extends Service implements SensorEventListener {
    //Binder given to client
    private final IBinder mBinder = new LocalBinder();
    //設定間隔時間
    long LastUpdateTime;
    private static final int Update_Interval_Time = 100;
    //給x,y,z初值
    private float Xval,Yval,Zval = 0.0f;
    //排除第一個加速規的值
    private Boolean isValueInitiate = false;
    //判斷是否昏迷的count
    private int comaCount = 0;
    //跌倒演算法結果
    public static double svmVal;

    private MyDBHelper myDBHelper = null;

    SensorManager sensorManager;
    Sensor sensor;

    Callbacks activity;

    Handler handler = new Handler();
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            activity.updateChart();
            handler.postDelayed(this,0);
        }
    };

    public class LocalBinder extends Binder{
        AcclerService getService(){
            return AcclerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        openDB();
        return mBinder;
    }

    private void openDB() {
        myDBHelper = new MyDBHelper(this);
    }

    private void closeDB(){
        myDBHelper.close();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long CurrentUpdateTime = System.currentTimeMillis();
            long TimeInterval = CurrentUpdateTime - LastUpdateTime;
            if(TimeInterval < Update_Interval_Time) return;
            LastUpdateTime = CurrentUpdateTime;

            //這次的值減掉上次的值，才是加速度
            float x = event.values[0]-Xval;
            Xval=event.values[0];
            float y = event.values[1]-Yval;
            Yval=event.values[1];
            float z = event.values[2]-Zval;
            Zval = event.values[2];

            //String AccValue = (String.format("%1.2f,%1.2f,%1.2f,",x,y,z));

            //第一個值不準確，跳過
            if(!isValueInitiate){ isValueInitiate =true;return;}

            //取得演算法結果
            svmVal = svmAlgo(x,y,z);
            //將結果放入Real-timeFragment的lineData的最後一個，以供畫圖
            lineData.addEntry(new Entry(lineData.getEntryCount(),(float)svmVal),0);
            //放入Real-timeFragment圖表X軸的時間陣列之中，用以顯示時間
            TimeArray.add(getCurrentTime());

            //演算法結果大於設定的門檻值，將危險存入DataBase
            if (svmVal>Threshold) {
                addToDB(STATE_FALL);
            }
            //結果小於昏迷門檻值，降昏迷資料存入DataBase
            else if(svmVal<0.2){
                comaCount++;
                //Log.d("ComaCount ",Integer.toString(comaCount));
                if (comaCount > 600){
                    addToDB(STATE_COMA);
                    Log.d("Coma", Integer.toString(comaCount));
                    comaCount = 0;
                }
            }
            else comaCount=0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getCurrentTime(){
        Calendar mCal = Calendar.getInstance();
        String dataFormat = "kk:mm:ss";
        SimpleDateFormat df = new SimpleDateFormat(dataFormat);
        String time = df.format(mCal.getTime());

        return time;
    }

    public String getCurrentDate(){
        Calendar mCal = Calendar.getInstance();
        String dataFormat = "yyyy-MM-dd";
        SimpleDateFormat df = new SimpleDateFormat(dataFormat);
        String date = df.format(mCal.getTime());

        return date;
    }

    private double svmAlgo (float x,float y, float z){
        float svmSqaure = (x*x)+(y*y)+(z*z);
        double svmVal = Math.pow(svmSqaure,0.5);
        return svmVal;
    }

    public void addToDB(int state) {
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_DATE",getCurrentDate());
        values.put("_TIME",getCurrentTime());
        values.put("_STATE",state);
        db.insert("FallTable",null,values);
        Log.d(TAG,"存入"+getCurrentTime()+" "+state);

        Cursor cursor = db.rawQuery("SELECT _TIME FROM FallTable",null);
        cursor.moveToFirst();
        do{
            Log.d(TAG,cursor.getString(0));
        }while (cursor.moveToNext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    public void registerClient(realtimeFragment mRealtimeFragment) {
        this.activity = (Callbacks) mRealtimeFragment;

        handler.postDelayed(serviceRunnable,0);
        Log.d("Register","Client");
    }


    //callbacks interface for communication with service clients
    public interface Callbacks{
        void updateChart();
    }
}
