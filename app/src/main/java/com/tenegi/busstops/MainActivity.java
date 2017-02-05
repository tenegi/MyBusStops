package com.tenegi.busstops;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tenegi.busstops.data.BusStopContract;
import com.tenegi.busstops.tflService.tflService;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.tenegi.busstops.data.BusStopContract.BASE_CONTENT_URI;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_ROUTE;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_STOP_NAME;
import static com.tenegi.busstops.data.BusStopContract.PATH_FAVOURITES;
import static com.tenegi.busstops.data.BusStopContract.PATH_SETTINGS;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "Main Activity";
    private static final int FAVOURITES_LOADER = 1;
    private static final int SETTINGS_LOADER = 2;
    private String ROUTES_URL = "";
    private String STOPPOINT_URL = "";
    private String STOPPOINT_PATH = "";
    private String STOPPOINT_APPID = "";
    private String STOPPOINT_APPKEY = "";
    private Date DATE_LAST_UPDATED;
    private boolean NEED_ROUTES_REFRESH = false;
    private FavouriteListAdapter mAdapter;
    private TextView statusTextView;
    private TextView settingsStatusTextView;
    RecyclerView favouriteRecyclerView;
    CursorLoader cursorLoader;
    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportLoaderManager().initLoader(SETTINGS_LOADER,null,this);
        setContentView(R.layout.activity_main);

        statusTextView = (TextView) findViewById(R.id.status);
        settingsStatusTextView = (TextView) findViewById(R.id.settingsStatus);
        //RecyclerView favouriteRecyclerView;
        favouriteRecyclerView = (RecyclerView) findViewById(R.id.favourites_list_view);
        favouriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getSupportLoaderManager().initLoader(FAVOURITES_LOADER,null,this);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //do nothing, we only care about swiping
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long id = (long) viewHolder.itemView.getTag();
                removeFavourite(id);
                restartLoader(FAVOURITES_LOADER);
            }
        }).attachToRecyclerView(favouriteRecyclerView);

        }
    public void onClick(View view){

        getSupportLoaderManager().initLoader(FAVOURITES_LOADER,null,this);
    }
    public void restartLoader(int loader){

        getSupportLoaderManager().initLoader(loader,null,this);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1){
        Uri CONTENT_URI;
        switch(arg0){
            case SETTINGS_LOADER:
                CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SETTINGS).build();
                break;
            case FAVOURITES_LOADER:
                CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).build();
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
            case FAVOURITES_LOADER:
                int rows = cursor.getCount();
                if (rows == 0) {
                    statusTextView.setText("No Favourites found");
                } else {
                    cursor.moveToFirst();
                    StringBuilder sb = new StringBuilder();
                    while (!cursor.isAfterLast()) {
                        String route = cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE));
                        String stopname = cursor.getString(cursor.getColumnIndex(COLUMN_STOP_NAME));
                        Log.d(TAG, "Route found " + route + " stopname = " + stopname);
                        sb.append("\n" + route + ", " + stopname);
                        cursor.moveToNext();
                    }
                    statusTextView.setText(rows + " Favourites found" + sb);
                    cursor.moveToFirst();
                    mAdapter = new FavouriteListAdapter(this, cursor);
                    favouriteRecyclerView.setAdapter(mAdapter);
                }
                break;
            case SETTINGS_LOADER:
                int settingsRows = cursor.getCount();
                Log.d(TAG, settingsRows + " Settings found");
                cursor.moveToFirst();
                ROUTES_URL = cursor.getString(cursor.getColumnIndex(BusStopContract.SettingsEntry.COLUMN_ROUTES_URL));
                STOPPOINT_URL = cursor.getString(cursor.getColumnIndex(BusStopContract.SettingsEntry.COLUMN_STOPPOINT_URL));
                STOPPOINT_PATH = cursor.getString(cursor.getColumnIndex(BusStopContract.SettingsEntry.COLUMN_STOPPOINT_PATH));
                STOPPOINT_APPID = cursor.getString(cursor.getColumnIndex(BusStopContract.SettingsEntry.COLUMN_STOPPOINT_APPID));
                STOPPOINT_APPKEY = cursor.getString(cursor.getColumnIndex(BusStopContract.SettingsEntry.COLUMN_STOPPOINT_APPKEY));
                String s= cursor.getString(cursor.getColumnIndex(BusStopContract.SettingsEntry.COLUMN_DATE_UPDATED));
                cursor.close();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date d=new Date();
                try {
                    d=  dateFormat.parse(s);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                DATE_LAST_UPDATED = d;
                Calendar today = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                cal.add(Calendar.DAY_OF_YEAR, 7);
                NEED_ROUTES_REFRESH = cal.before(today);
                settingsStatusTextView.setText(NEED_ROUTES_REFRESH ? "need to refresh" : "routes up to date");
                break;

        }

    }
    @Override
    public void onLoaderReset(Loader<Cursor> arg0){

    }
    @Override
    public void onDestroy(){

        super.onDestroy();
    }

    private void removeFavourite(long id){
        ContentValues busStopValues = new ContentValues();
        busStopValues.put(BusStopContract.BusStopEntry.COLUMN_FAVOURITE, 0);
        ContentResolver contentResolver = this.getContentResolver();
        Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).appendPath(String.valueOf(id)).build();
        Log.d(TAG, "Content uri for update favourite = " + CONTENT_URI);
        int count = contentResolver.update(CONTENT_URI, busStopValues,null,null);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_routes) {
            Intent startSettingsActivity = new Intent(this, RoutesActivity.class);

            startActivity(startSettingsActivity);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
