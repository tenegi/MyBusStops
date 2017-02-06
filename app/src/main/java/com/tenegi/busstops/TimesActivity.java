package com.tenegi.busstops;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tenegi.busstops.data.BusStopContract;

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
    long stopId;
    CursorLoader cursorLoader;

    RecyclerView timesRecyclerView;
    TextView mHdrView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_times);
        timesRecyclerView = (RecyclerView) findViewById(R.id.times_list_view);
        timesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mHdrView = (TextView) findViewById(R.id.times_hdr);
        STOPPOINT_URL = getIntent().getExtras().getString("STOPPOINT_URL");
        STOPPOINT_PATH = getIntent().getExtras().getString("STOPPOINT_PATH");
        STOPPOINT_APPID = getIntent().getExtras().getString("STOPPOINT_APPID");
        STOPPOINT_APPKEY = getIntent().getExtras().getString("STOPPOINT_APPKEY");
        stopId = getIntent().getExtras().getLong("id");
        getSupportLoaderManager().initLoader(SETTINGS_LOADER,null,this);

    }
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
                }
                break;
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> arg0){

    }
}
