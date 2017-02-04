package com.tenegi.busstops.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.tenegi.busstops.data.BusStopContract.*;

/**
 * Created by lyndon on 29/01/2017.
 */

public class BusStopDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "busstops.db";
    private static final int DATABASE_VERSION = 2;

    public BusStopDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

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

        sqLiteDatabase.execSQL(SQL_CREATE_BUSSTOP_TABLE);

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

        sqLiteDatabase.execSQL(SQL_CREATE_LOADER_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // For now simply drop the table and create a new one. This means if you change the
        // DATABASE_VERSION the table will be dropped.
        // In a production app, this method might be modified to ALTER the table
        // instead of dropping it, so that existing data is not deleted.
        // COMPLETED (9) Inside, execute a drop table query, and then call onCreate to re-create it
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BusStopEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LoadEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
