package com.example.teddy.swipewindowspractice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Teddy on 2017/10/17.
 */

public class MyDBHelper extends SQLiteOpenHelper {
    private final static int _DBVersion = 1;
    private final static String _DBName = "FallSystem.db";
    private final static String _TableName = "FallTable";

    public MyDBHelper(Context context) {
        super(context, _DBName, null, _DBVersion);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL = "CREATE TABLE IF NOT EXISTS " + _TableName +"("+
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "_DATE VARCHAR(20), "+
                "_TIME VARCHAR(20), "+
                "_STATE INTEGER"+
                ");";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + _TableName);
        // 呼叫onCreate建立新版的表格
        onCreate(db);
    }

    public void DeleteTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE"+_TableName);
    }
}
