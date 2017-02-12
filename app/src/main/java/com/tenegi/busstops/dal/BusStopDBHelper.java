package com.tenegi.busstops.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.tenegi.busstops.dal.BusStopContract.*;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lyndon on 29/01/2017.
 */

public class BusStopDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "busstops.db";
    private static final int DATABASE_VERSION = 5;

    public BusStopDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createBusStopsTable(sqLiteDatabase);
        createLoaderTable(sqLiteDatabase);
        createSettingsTable(sqLiteDatabase);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // For now simply drop the table and create a new one. This means if you change the
        // DATABASE_VERSION the table will be dropped.
        // In a production app, this method might be modified to ALTER the table
        // instead of dropping it, so that existing data is not deleted.
        // COMPLETED (9) Inside, execute a drop table query, and then call onCreate to re-create it
        switch(newVersion) {
            case 3:
                createSettingsTable(sqLiteDatabase);
                break;
            case 4:
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SettingsEntry.TABLE_NAME);
                createSettingsTable(sqLiteDatabase);
            case 5:
                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SettingsEntry.TABLE_NAME);
                createSettingsTable(sqLiteDatabase);
        }
    }
    private void createBusStopsTable(SQLiteDatabase db){
        final String SQL_CREATE_BUSSTOP_TABLE = "CREATE TABLE " + BusStopEntry.TABLE_NAME + " (" +
                BusStopEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                BusStopEntry.COLUMN_ROUTE + " TEXT NOT NULL, " +
                BusStopEntry.COLUMN_RUN + " INTEGER NOT NULL, " +
                BusStopEntry.COLUMN_SEQUENCE + " INTEGER NOT NULL, " +
                BusStopEntry.COLUMN_STOP_CODE_LBSL + " TEXT NOT NULL, " +
                BusStopEntry.COLUMN_BUS_STOP_CODE + " TEXT NOT NULL, " +
                BusStopEntry.COLUMN_NAPTAN_ATCO + " TEXT NOT NULL, " +
                BusStopEntry.COLUMN_STOP_NAME + " TEXT NOT NULL, " +
                BusStopEntry.COLUMN_LOCATION_EASTING + " INTEGER NOT NULL, " +
                BusStopEntry.COLUMN_LOCATION_NORTHING + " INTEGER NOT NULL, " +
                BusStopEntry.COLUMN_HEADING + " INTEGER NOT NULL, " +
                BusStopEntry.COLUMN_FAVOURITE + " INTEGER DEFAULT 0 NOT NULL " +
                "); ";

        db.execSQL(SQL_CREATE_BUSSTOP_TABLE);
    }
    private void createLoaderTable(SQLiteDatabase db){
        final String SQL_CREATE_LOADER_TABLE = "CREATE TABLE " + LoadEntry.TABLE_NAME + " (" +
                LoadEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                LoadEntry.COLUMN_ROUTE + " TEXT NOT NULL, " +
                LoadEntry.COLUMN_RUN + " INTEGER NOT NULL, " +
                LoadEntry.COLUMN_SEQUENCE + " INTEGER NOT NULL, " +
                LoadEntry.COLUMN_STOP_CODE_LBSL + " TEXT NOT NULL, " +
                LoadEntry.COLUMN_BUS_STOP_CODE + " TEXT NOT NULL, " +
                LoadEntry.COLUMN_NAPTAN_ATCO + " TEXT NOT NULL, " +
                LoadEntry.COLUMN_STOP_NAME + " TEXT NOT NULL, " +
                LoadEntry.COLUMN_LOCATION_EASTING + " INTEGER NOT NULL, " +
                LoadEntry.COLUMN_LOCATION_NORTHING + " INTEGER NOT NULL, " +
                LoadEntry.COLUMN_HEADING + " INTEGER NOT NULL, " +
                LoadEntry.COLUMN_FAVOURITE + " INTEGER DEFAULT 0 NOT NULL " +
                "); ";

        db.execSQL(SQL_CREATE_LOADER_TABLE);
    }
    private void createSettingsTable(SQLiteDatabase db){
        final String SQL_CREATE_SETTINGS_TABLE = "CREATE TABLE " + SettingsEntry.TABLE_NAME + " (" +
                SettingsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SettingsEntry.COLUMN_ROUTES_URL + " TEXT NOT NULL, " +
                SettingsEntry.COLUMN_STOPPOINT_URL + " TEXT NOT NULL, " +
                SettingsEntry.COLUMN_STOPPOINT_PATH + " TEXT NOT NULL, " +
                SettingsEntry.COLUMN_STOPPOINT_APPID + " TEXT NOT NULL, " +
                SettingsEntry.COLUMN_STOPPOINT_APPKEY + " TEXT NOT NULL, " +
                SettingsEntry.COLUMN_DATE_UPDATED + " DATETIME " +
                "); ";

        db.execSQL(SQL_CREATE_SETTINGS_TABLE);
        seedDatabase((db));
    }
    private void seedDatabase(SQLiteDatabase sqLiteDatabase){
        ContentValues value = new ContentValues();
        value.put(SettingsEntry.COLUMN_ROUTES_URL,
                "https://www.tfl.gov.uk/tfl/businessandpartners/syndication/feed.aspx?email=lyndon@tenegi.com&feedId=11");
        value.put(SettingsEntry.COLUMN_STOPPOINT_URL,
                "https://api.tfl.gov.uk/StopPoint/");
        value.put(SettingsEntry.COLUMN_STOPPOINT_PATH,
                "arrivals");
        value.put(SettingsEntry.COLUMN_STOPPOINT_APPID,
                "9dd3c5b5");
        value.put(SettingsEntry.COLUMN_STOPPOINT_APPKEY,
                "f832b8ea6dc6611ed8162128c0836656");
        value.put(SettingsEntry.COLUMN_DATE_UPDATED,getDateTime());
        long id = sqLiteDatabase.insert(SettingsEntry.TABLE_NAME, null, value);
    }
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


}
