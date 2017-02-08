package com.tenegi.busstops.tflService;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.tenegi.busstops.BusTimeResult;
import com.tenegi.busstops.BusTimeResultList;

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
    String errMessage = "";

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
        if(mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    myTask = new TheTask();
                    myTask.execute();
                }
            }, 60000);
        }
    }
    @Override
    public void onDestroy(){
        Log.d(TAG, "service destroy called");
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }


    class TheTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "get data called");
            Log.d(TAG, "url is " + URL_TO_CALL);
            errMessage = "";
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
                errMessage = "http call failed " + e.getMessage();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            Intent timesResults = new Intent();
            timesResults.setAction(GET_TIMES_SERVICE_RESULTS);
            Bundle mBundle = new Bundle();
            if(errMessage == "") {
                Log.d(TAG, "post execute  results = " + result);
                //ArrayList<String> timings = new ArrayList<String>();
                BusTimeResultList resultList = new BusTimeResultList();
                try {
                    JSONArray jsa = new JSONArray(result);
                    for (int i = 0; i < jsa.length(); i++) {
                        JSONObject jo = (JSONObject) jsa.get(i);
                        String routeName = jo.getString("lineName");
                        String arrivalTime = jo.getString("expectedArrival");
                        int timeToStop = jo.getInt("timeToStation");
                        String destination = jo.getString("destinationName");
                        BusTimeResult b = new BusTimeResult(routeName, arrivalTime, timeToStop, destination);
                        resultList.add(b);
                        Log.d(TAG, "have got " + arrivalTime + " result ");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    errMessage = "Failed parsing json " + e.getMessage();
                }


                int numResults = resultList.size();
                Log.d(TAG, "have got " + numResults + " results ");
                mBundle.putInt("numResults", numResults);
                if(errMessage == "") {
                    mBundle.putString("message", "OK");
                }else{
                    mBundle.putString("message", errMessage);
                }
                if (numResults > 0) {

                    mBundle.putParcelable("timings", resultList);
                    timesResults.putExtras(mBundle);
                }
            } else {
                mBundle.putInt("numResults", 0);
                mBundle.putString("message", errMessage);
            }
            sendBroadcast(timesResults);
            scheduleNext();
        }
    }
}
