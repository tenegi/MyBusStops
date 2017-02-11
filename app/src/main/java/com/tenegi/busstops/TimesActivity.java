package com.tenegi.busstops;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tenegi.busstops.data.BusStopContract;
import com.tenegi.busstops.tflService.tflGetBusTimesService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.tenegi.busstops.data.BusStopContract.BASE_CONTENT_URI;
import static com.tenegi.busstops.data.BusStopContract.PATH_BUSSTOPS;

public class TimesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "Times Activity";
    private static final int SETTINGS_LOADER = 1;

    private String STOPPOINT_URL = "";
    private String STOPPOINT_PATH = "";
    private String STOPPOINT_APPID = "";
    private String STOPPOINT_APPKEY = "";
    private String STOPPOINT_NAPTAN_ATCO = "";
    private String selectedRoute ="";
    long stopId;
    CursorLoader cursorLoader;

    //RecyclerView timesRecyclerView;
    ListView timesRecyclerView;
    TextView mHdrView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_times);
        //timesRecyclerView = (RecyclerView) findViewById(R.id.times_list_view);
        //timesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        timesRecyclerView = (ListView) findViewById(R.id.times_list_view);
        mHdrView = (TextView) findViewById(R.id.times_hdr);

        STOPPOINT_URL = getIntent().getExtras().getString("STOPPOINT_URL");
        STOPPOINT_PATH = getIntent().getExtras().getString("STOPPOINT_PATH");
        STOPPOINT_APPID = getIntent().getExtras().getString("STOPPOINT_APPID");
        STOPPOINT_APPKEY = getIntent().getExtras().getString("STOPPOINT_APPKEY");
        selectedRoute = getIntent().getExtras().getString("SELECTED_ROUTE");
        stopId = getIntent().getExtras().getLong("id");

        getSupportLoaderManager().initLoader(SETTINGS_LOADER,null,this);

    }
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast receiver has received");
            Bundle b = intent.getExtras();
            if(b != null) {
                int numResults = b.getInt("numResults");
                String message = b.getString("message");
                if(numResults == 0){
                    if(message != "OK") {
                        mHdrView.setText(message);
                    } else {
                        mHdrView.setText("No bus times received from service");
                    }
                } else {
                    BusTimeResultList resultList = b.getParcelable("timings");
                    if(resultList != null) {
                        Log.d(TAG, "Broadcast receiver results = " + resultList.size());
                        for (BusTimeResult br: resultList){

                            if(br.getRoute().equals(selectedRoute)) {
                                br.setSelectedRoute(1);
                                Log.d(TAG, "Set selected route item:" + br.getSelectedRoute() + " selected:" + selectedRoute);
                            }
                        }
                        Collections.sort(resultList, new Comparator<BusTimeResult>() {
                            public int compare(BusTimeResult o1, BusTimeResult o2) {
                                return Integer.compare(o1.getTimeToStop(),o2.getTimeToStop());
                            }
                        });
                        BusTimesAdapter adapter = new BusTimesAdapter(TimesActivity.this, resultList);
                        timesRecyclerView.setAdapter(adapter);
                    }else {
                        Log.d(TAG, "Broadcast receiver got bundle but not array list");
                        String s = bundle2string(b);
                        Log.d(TAG, "Broadcast receiver got bundle = " + s);
                    }

                }


            } else {
                Log.d(TAG, "Broadcast receiver did not receive any data");
            }
        }
    };
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1){
        Uri CONTENT_URI;
        switch(arg0){
            case SETTINGS_LOADER:
                CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                        .appendPath(PATH_BUSSTOPS)
                        .appendPath(String.valueOf(stopId)).build();
                break;

            default:
                throw new UnsupportedOperationException("Unknown loader id " + arg0);
        }
        Log.d(TAG, "Content uri = " + CONTENT_URI + " base uri = " + BASE_CONTENT_URI);
        cursorLoader = new CursorLoader(this,CONTENT_URI,null,null,null,null );
        return cursorLoader;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        switch(arg0.getId()){
            case SETTINGS_LOADER:
                int rows = cursor.getCount();
                Log.d(TAG, rows + " stops found");
                if (rows == 0) {
                    mHdrView.setText(String.valueOf(stopId) + " - No stop found");
                } else {

                    cursor.moveToFirst();
                    STOPPOINT_NAPTAN_ATCO = cursor.getString(cursor.getColumnIndex(BusStopContract.BusStopEntry.COLUMN_NAPTAN_ATCO));
                    String stopName =  cursor.getString(cursor.getColumnIndex(BusStopContract.BusStopEntry.COLUMN_STOP_NAME));
                    mHdrView.setText(stopName);
                    Intent timesServiceIntent = new Intent(this, tflGetBusTimesService.class);
                    String urlString = STOPPOINT_URL + STOPPOINT_NAPTAN_ATCO + "/" + STOPPOINT_PATH + "?app_id=" + STOPPOINT_APPID + "&app_key=" + STOPPOINT_APPKEY;
                    timesServiceIntent.putExtra("url", urlString);
                    startService(timesServiceIntent);
                }
                break;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the Main page
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            //finish();
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> arg0){

    }
    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(br, new IntentFilter(tflGetBusTimesService.GET_TIMES_SERVICE_RESULTS));
    }
    @Override
    public void onStop(){
        try        {
           unregisterReceiver(br);
        }catch(Exception e){

        }
        super.onStop();
    }
    @Override
    public void onDestroy(){
        Log.d(TAG, "times activity destroyed");
        stopService(new Intent(this, tflGetBusTimesService.class));
        super.onDestroy();
    }
    public static String bundle2string(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "Bundle{";
        for (String key : bundle.keySet()) {
            string += " " + key + " => " + bundle.get(key) + ";";
        }
        string += " }Bundle";
        return string;
    }
}
