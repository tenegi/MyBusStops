package com.tenegi.busstops.tflService;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lyndon on 06/02/2017.
 */

public class tflGetBusTimesService extends Service {
    private final static String TAG = "tflGetBusTimesService";

    public static final String GET_TIMES_SERVICE = "com.tenegi.busstops.tflService";
    public static final String GET_TIMES_SERVICE_RESULTS = "com.tenegi.busstops.tflService.tflGetBusTimesService";
    private String URL_TO_CALL;
    TheTask myTask = null;
    Handler mHandler = new Handler();


    @Override
    public void onCreate(){
        super.onCreate();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        URL_TO_CALL = intent.getExtras().getString("url");
        Log.d(TAG,"Starting timer...");
        mHandler = new android.os.Handler();
        myTask = new TheTask();
        myTask.execute();

        return super.onStartCommand(intent, flags, startId);
    }
    private void scheduleNext() {
        mHandler.postDelayed(new Runnable() {
            public void run() {
                myTask = new TheTask();
                myTask.execute();
            }
        }, 60000);
    }
    @Override
    public void onDestroy(){

        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }


    private ArrayList<String> getData(){
        Log.d(TAG, "get data called");
        Log.d(TAG, "url is " + URL_TO_CALL);
        StringBuilder result = new StringBuilder();
        ArrayList<String> timings = new ArrayList<String>();
        try {
            URL url = new URL(URL_TO_CALL);
            String line;
            URLConnection urlc = url.openConnection();
            BufferedReader bfr=new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            while((line=bfr.readLine())!=null) {
                result.append(line);
            }
            }catch(Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "get data results = " + result);
        try {
            JSONArray jsa = new JSONArray(result);
            for (int i = 0; i < jsa.length(); i++) {
                JSONObject jo = (JSONObject) jsa.get(i);
                timings.add(jo.getString("expectedArrival"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return timings;
    }
    class TheTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "get data called");
            Log.d(TAG, "url is " + URL_TO_CALL);
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(URL_TO_CALL);
                String line;
                URLConnection urlc = url.openConnection();
                BufferedReader bfr = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
                while ((line = bfr.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "post execute  results = " + result);
            ArrayList<String> timings = new ArrayList<String>();
            try {
                JSONArray jsa = new JSONArray(result);
                for (int i = 0; i < jsa.length(); i++) {
                    JSONObject jo = (JSONObject) jsa.get(i);
                    String timeInfo = jo.getString("expectedArrival");
                    Log.d(TAG, "have got " + timeInfo + " result ");
                    timings.add(timeInfo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (timings.size() > 0) {
                Log.d(TAG, "have got " + timings.size() + " results ");

                Intent timesResults = new Intent();
                timesResults.setAction(GET_TIMES_SERVICE_RESULTS);
                Bundle mBundle = new Bundle();
                mBundle.putStringArrayList("timings", timings);
                //timesResults.putStringArrayListExtra("timings", (ArrayList<String>)timings);
                timesResults.putExtras(mBundle);
                sendBroadcast(timesResults);
            }
            scheduleNext();
        }
    }
}
