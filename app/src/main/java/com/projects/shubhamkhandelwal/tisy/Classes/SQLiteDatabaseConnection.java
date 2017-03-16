package com.projects.shubhamkhandelwal.tisy.Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shubham Khandelwal on 3/10/2017.
 */
public class SQLiteDatabaseConnection {
    DatabaseConnection databaseConnection;
    Context context;
    public SQLiteDatabaseConnection(Context context) {
        databaseConnection = new DatabaseConnection(context);
        this.context = context;
    }
    public boolean checkForEvent(String eventID){
        List<String> eventIDs = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = databaseConnection.getWritableDatabase();
        String [] cols = {DatabaseConnection.EVENT_ID_COL};
        Cursor cursor = sqLiteDatabase.query(DatabaseConnection.TABLE_NAME,cols, null, null, null, null, null);
        while(cursor.moveToNext()){
            eventIDs.add(cursor.getString(cursor.getColumnIndex(DatabaseConnection.EVENT_ID_COL)));

        }
        if(eventIDs.contains(eventID)){
            return true;
        }else{
            return false;
        }
    }
    public void emptyTable(){
        SQLiteDatabase sqLiteDatabase = databaseConnection.getWritableDatabase();
        sqLiteDatabase.delete(DatabaseConnection.TABLE_NAME, null, null);
    }
    public long insertRow(String eventID, int chatsReadCount){
        SQLiteDatabase sqLiteDatabase = databaseConnection.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConnection.EVENT_ID_COL, eventID);
        contentValues.put(DatabaseConnection.CHAT_READS_COUNT_COL, chatsReadCount);
        long id = sqLiteDatabase.insert(DatabaseConnection.TABLE_NAME, null, contentValues);
        return id;
    }
    public int getCount(String eventID){
        int count = 0;
        SQLiteDatabase sqLiteDatabase = databaseConnection.getWritableDatabase();
        String [] cols = {DatabaseConnection.CHAT_READS_COUNT_COL};
        Cursor cursor = sqLiteDatabase.query(DatabaseConnection.TABLE_NAME,cols, DatabaseConnection.EVENT_ID_COL + "= '"+ eventID + "'", null, null, null, null);
        while(cursor.moveToNext()){
            count = cursor.getInt(cursor.getColumnIndex(DatabaseConnection.CHAT_READS_COUNT_COL));

        }
        return count;
    }

    public int updateCount(String eventID, int chatsReadCount){
        SQLiteDatabase sqLiteDatabase = databaseConnection.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConnection.CHAT_READS_COUNT_COL, chatsReadCount);
        String [] whereArgs = {eventID};
        int count = sqLiteDatabase.update(DatabaseConnection.TABLE_NAME, contentValues, DatabaseConnection.EVENT_ID_COL + "=?", whereArgs);
        return count;
    }


    static public class DatabaseConnection extends SQLiteOpenHelper {
        private final static String DB_NAME = "tisy_database";
        private final static int DB_VERSION = 1;
        private final static String TABLE_NAME = "chat_counts";
        private final static String EVENT_ID_COL = "event_id";
        private final static String CHAT_READS_COUNT_COL = "chat_reads_count";
        private final static String CREATE_TABLE = "create table " + TABLE_NAME + "(" + EVENT_ID_COL + " varchar(255), " + CHAT_READS_COUNT_COL + " INTEGER);";


        public DatabaseConnection(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            try {
                sqLiteDatabase.execSQL(CREATE_TABLE);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }
    }
}