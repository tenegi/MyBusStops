package com.tenegi.busstops;

import android.content.BroadcastReceiver;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tenegi.busstops.tflService.tflService;

import java.io.File;

import static com.tenegi.busstops.data.BusStopContract.BASE_CONTENT_URI;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_ROUTE;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_STOP_NAME;
import static com.tenegi.busstops.data.BusStopContract.PATH_FAVOURITES;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "Main Activity";
    private FavouriteListAdapter mAdapter;
    private TextView statusTextView;
    RecyclerView favouriteRecyclerView;
    CursorLoader cursorLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTextView = (TextView) findViewById(R.id.status);
        //RecyclerView favouriteRecyclerView;
        favouriteRecyclerView = (RecyclerView) findViewById(R.id.favourites_list_view);
        favouriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getSupportLoaderManager().initLoader(1,null,this);
    }
    public void onClick(View view){
        getSupportLoaderManager().initLoader(1,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1){
        Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).build();
        Log.d(TAG, "Content uri = " + CONTENT_URI + " base uri = " + BASE_CONTENT_URI);
        cursorLoader = new CursorLoader(this,CONTENT_URI,null,null,null,null );
        return cursorLoader;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        int rows = cursor.getCount();
        if (rows == 0) {
            statusTextView.setText("No Favourites found");
        } else {
            cursor.moveToFirst();
            StringBuilder sb = new StringBuilder();
            while(!cursor.isAfterLast()){
                String route = cursor.getString(cursor.getColumnIndex(COLUMN_ROUTE));
                String stopname = cursor.getString(cursor.getColumnIndex(COLUMN_STOP_NAME));
                Log.d(TAG, "Route found " + route + " stopname = " + stopname);
                sb.append("\n" + route + ", " + stopname);
                cursor.moveToNext();
            }
            statusTextView.setText(rows + " Favourites found" + sb);
            cursor.moveToFirst();
            mAdapter = new FavouriteListAdapter(this, cursor);

            // Link the adapter to the RecyclerView
            favouriteRecyclerView.setAdapter(mAdapter);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> arg0){

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
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
