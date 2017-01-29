package com.tenegi.busstops.tflService;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.tenegi.busstops.data.BusStopContract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static android.content.ContentValues.TAG;

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
    private final Context mContext;
    public tflService() {
        super("tflService");
        mContext = this.getBaseContext();
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
        updateDatabase(output.getAbsolutePath());
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
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_RUN, str[1]);
                    busStopValues.put(BusStopContract.BusStopEntry.COLUMN_SEQUENCE, str[2]);
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

}
