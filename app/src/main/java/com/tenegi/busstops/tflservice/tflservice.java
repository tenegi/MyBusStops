package com.tenegi.busstops.tflService;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.tenegi.busstops.data.BusStopContentProvider;
import com.tenegi.busstops.data.BusStopContract;
import com.tenegi.busstops.data.BusStopDBHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.tenegi.busstops.data.BusStopContract.BASE_CONTENT_URI;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_ROUTE;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_RUN;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_SEQUENCE;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_STOP_NAME;
import static com.tenegi.busstops.data.BusStopContract.PATH_FAVOURITES;
import static com.tenegi.busstops.data.BusStopContract.PATH_LOADER_TABLE;

/**
 * Created by lyndon on 29/01/2017.
 */

public class tflService extends IntentService {
    public int result = Activity.RESULT_CANCELED;
    public static final String REMOTEURL = "urlpath";
    public static final String FILENAME = "filename";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.tenegi.busstops.tflservice";

    public tflService() {
        super("tflService");
    }
    @Override
    protected void onHandleIntent(Intent intent){
        String urlPath = intent.getStringExtra(REMOTEURL);
        String fileName = intent.getStringExtra(FILENAME);
        //File output = new File(Environment..getExternalStorageDirectory(), fileName);
        File output = new File(this.getFilesDir(), fileName);
        if(output.exists()){
            output.delete();
        }

        InputStream stream = null;
        FileOutputStream fos = null;
        try {
           URL url = new URL(urlPath);
            stream = url.openConnection().getInputStream();
            InputStreamReader reader = new InputStreamReader(stream);
            fos = new FileOutputStream(output.getPath());
            int next = -1;
            while((next = reader.read()) != -1){
                fos.write(next);
            }
            result = Activity.RESULT_OK;
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(stream != null){
                try{
                    stream.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            if(fos != null){
                try {
                    fos.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG, "File downloaded");
        updateDatabaseBatch(output.getAbsolutePath());
        publishResults(output.getAbsolutePath(), result);
    }
    private void publishResults(String outputPath, int result){
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILEPATH, outputPath);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }
    private void updateDatabase(String outputPath){
        try {
            FileReader file = new FileReader(outputPath);
            BufferedReader buffer = new BufferedReader(file);
            String line = "";
            int linesRead =0;
            ContentResolver contentResolver = this.getContentResolver();
            while((line = buffer.readLine()) != null){
                linesRead++;
                Log.d(TAG, "Line Read " +linesRead);
                Log.d(TAG, "Line = " + line);
                ContentValues busStopValues = new ContentValues();
                String[] str = line.split(",");
                Log.d(TAG, "Fields in array = " + str.length);
                if(str.length == 11) {
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_ROUTE, str[0]);
                    busStopValues.put(COLUMN_RUN, str[1]);
                    busStopValues.put(COLUMN_SEQUENCE, str[2]);
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_STOP_CODE_LBSL, str[3]);
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_BUS_STOP_CODE, str[4]);
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_NAPTAN_ATCO, str[5]);
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_STOP_NAME, str[6]);
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_LOCATION_EASTING, str[7]);
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_LOCATION_NORTHING, str[8]);
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_HEADING, str[9]);
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_FAVOURITE, 0);
                    Uri uri = contentResolver.insert(BusStopContract.BusStopEntry.CONTENT_URI, busStopValues);
                } else {
                     Log.d(TAG, "skipping malformed line " + line);
                }
            }

        } catch(IOException e){
        e.printStackTrace();
        }
    }
    private void updateDatabaseBatch(String outputPath){
        try {
            ContentResolver contentResolver = this.getContentResolver();
            ContentValues[] favourites = getFavourites(contentResolver);
            Log.d(TAG, "favourites found = " + favourites.length);
            int rowsDeleted = clearLoaderTable(contentResolver);
            Log.d(TAG, "rows deleted from loader table = " + rowsDeleted);
            FileReader file = new FileReader(outputPath);
            BufferedReader buffer = new BufferedReader(file);
            String line = "";
            int linesRead =0;
            final int batchSize =1000;
            int thisBatch = 0;
            int insertCount = 0;
            //ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            ContentValues[] linesToInsert = new ContentValues[batchSize];
            line = buffer.readLine(); // skip line with header titles
            while((line = buffer.readLine()) != null){

                linesRead++;
                //thisBatch ++;
                ContentValues busStopValues = new ContentValues();
                String[] str = line.split(",");
                if(str.length == 11) {
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_ROUTE, str[0]);
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_RUN, str[1]);
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_SEQUENCE, str[2]);
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_STOP_CODE_LBSL, str[3]);
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_BUS_STOP_CODE, str[4]);
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_NAPTAN_ATCO, str[5]);
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_STOP_NAME, str[6]);
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_LOCATION_EASTING, str[7]);
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_LOCATION_NORTHING, str[8]);
                    busStopValues.put(BusStopContract.LoadEntry.COLUMN_HEADING, str[9]);
                    if(isFavourite(str[0], Integer.parseInt(str[1]), Integer.parseInt(str[2]), favourites)) {
                        busStopValues.put(BusStopContract.LoadEntry.COLUMN_FAVOURITE, 1);
                    } else {
                        busStopValues.put(BusStopContract.LoadEntry.COLUMN_FAVOURITE, 0);
                    }
                    //Uri uri = contentResolver.insert(BusStopContract.BusStopEntry.CONTENT_URI, busStopValues);
                    //thisBatch ++;
                    linesToInsert[thisBatch++] = busStopValues;
                } else {
                    Log.d(TAG, "skipping malformed line " + line);
                }
                //linesToInsert[thisBatch++] = busStopValues;
                if(thisBatch == batchSize){
                    Log.d(TAG, "Inserting batch of " + linesToInsert.length + "lines Read = " + linesRead);
                    insertCount = contentResolver.bulkInsert(BusStopContract.LoadEntry.CONTENT_URI, linesToInsert);
                    thisBatch = 0;
                    linesToInsert = new ContentValues[batchSize];
                    Log.d(TAG, "batch inserted " + insertCount);
                }
            }
            if(linesRead > 0){

                BusStopDBHelper mBusStopDbHelper = new BusStopDBHelper(this);
                SQLiteDatabase db = mBusStopDbHelper.getWritableDatabase();
                db.beginTransaction();
                try{
                    db.execSQL("ALTER TABLE " + BusStopContract.BusStopEntry.TABLE_NAME +
                            " RENAME TO " + BusStopContract.BusStopEntry.TABLE_NAME +"_OLD;");
                    db.execSQL("ALTER TABLE " + BusStopContract.LoadEntry.TABLE_NAME +
                            " RENAME TO " + BusStopContract.BusStopEntry.TABLE_NAME +";");
                    db.execSQL("ALTER TABLE " + BusStopContract.BusStopEntry.TABLE_NAME + "_OLD" +
                            " RENAME TO " + BusStopContract.LoadEntry.TABLE_NAME +";");
                    db.setTransactionSuccessful();
                } finally{
                    db.endTransaction();
                }

            }

        } catch(IOException e){
            e.printStackTrace();
        }
    }
    private ContentValues[] getFavourites(ContentResolver contentResolver){

        Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).build();
        Cursor cursor = contentResolver.query(CONTENT_URI,null,null,null,null);
        int numFavourites = cursor.getCount();
        int numLoaded = 0;
        ContentValues[] values = new ContentValues[numFavourites];
        if (numFavourites != 0) {

            cursor.moveToFirst();


            while (!cursor.isAfterLast()) {
                String route = cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE));
                int run = cursor.getInt(cursor.getColumnIndex(COLUMN_RUN));
                int seq = cursor.getInt(cursor.getColumnIndex(COLUMN_SEQUENCE));

                ContentValues favourite = new ContentValues();
                favourite.put(BusStopContract.BusStopEntry.COLUMN_ROUTE, route);
                favourite.put(BusStopContract.BusStopEntry.COLUMN_RUN, run);
                favourite.put(BusStopContract.BusStopEntry.COLUMN_SEQUENCE, seq);
                Log.d(TAG, "Favourite found " + route + " run = " + run + " sequence = " + seq);
                values[numLoaded++] = favourite;
                cursor.moveToNext();
            }
        }
        return values;
    }
    private int clearLoaderTable(ContentResolver contentResolver){
        Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOADER_TABLE).build();
        return contentResolver.delete(CONTENT_URI,null,null);
    }
    private boolean isFavourite(String route, int run, int sequence, ContentValues[] faves ){
        for(ContentValues value : faves ){
            String favRoute = value.get(BusStopContract.BusStopEntry.COLUMN_ROUTE).toString();
            int favRun = (int) value.get(BusStopContract.BusStopEntry.COLUMN_RUN);
            int favSeq = (int) value.get(BusStopContract.BusStopEntry.COLUMN_SEQUENCE);
            if(favRoute.equals(route) && favRun == run && favSeq == sequence){
                return true;
            }
        }
        return false;
    }
}
