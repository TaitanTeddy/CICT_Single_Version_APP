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
import android.os.IBinder;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.ContentValues.TAG;
import static com.example.teddy.swipewindowspractice.Fragment1.TimeArray;
import static com.example.teddy.swipewindowspractice.Fragment1.Treshold;
import static com.example.teddy.swipewindowspractice.Fragment1.lineData;
import static com.example.teddy.swipewindowspractice.MainActivity.STATE_COMA;
import static com.example.teddy.swipewindowspractice.MainActivity.STATE_FALL;

public class AcclerService extends Service implements SensorEventListener {
    //Binder given to client
    private final IBinder mBinder = new LocalBinder();

    long LastUpdateTime;
    private static final int Update_Interval_Time = 100;
    private float Xval,Yval,Zval = 0.0f;
    private Boolean isValueInitiate = false;
    private int comaCount = 0;
    public static double svmVal;

    private MyDBHelper myDBHelper = null;

    SensorManager sensorManager;
    Sensor sensor;

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

            //前減後(使初始值接近0)
            float x = event.values[0]-Xval;
            Xval=event.values[0];
            float y = event.values[1]-Yval;
            Yval=event.values[1];
            float z = event.values[2]-Zval;
            Zval = event.values[2];

            String AccValue = (String.format("%1.2f,%1.2f,%1.2f,",x,y,z));

            //第一個值不準確，跳過
            if(!isValueInitiate){ isValueInitiate =true;return;}
            //取得演算法結果
            svmVal = fallAlgo(x,y,z);
            lineData.addEntry(new Entry(lineData.getEntryCount(),(float)svmVal),0);
            TimeArray.add(getCurrentTime());
            if (svmVal>Treshold) {
                addToDB(STATE_FALL);
            }
            else if(svmVal<0.2){
                comaCount++;
                Log.d("ComaCount ",Integer.toString(comaCount));
                if (comaCount > 600){
                    addToDB(STATE_COMA);
                    Log.d("Coma", Integer.toString(comaCount));
                    comaCount = 0;
                }
            }
            else comaCount=0;

            //Log.d("Service",AccValue);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public ArrayList<String> getTimeArray(){
        ArrayList<String> TimeArray = Fragment1.TimeArray;
        //取得現在時間
        TimeArray.add(getCurrentTime());
        return TimeArray;
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

    private double fallAlgo (float x,float y, float z){
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

    public String getText(){
        return "I got text.";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDB();
    }
}
