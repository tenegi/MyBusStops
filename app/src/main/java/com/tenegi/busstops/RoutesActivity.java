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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tenegi.busstops.tflService.tflService;

import static com.tenegi.busstops.data.BusStopContract.BASE_CONTENT_URI;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_ROUTE;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_STOP_NAME;
import static com.tenegi.busstops.data.BusStopContract.PATH_BUSROUTES;
import static com.tenegi.busstops.data.BusStopContract.PATH_FAVOURITES;

public class RoutesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>

         {
    private static final String TAG = "Routes Activity";
    private TextView statusTextView;
    private RouteListAdapter mAdapter;
    RecyclerView routesRecyclerView;
    CursorLoader cursorLoader;
    String queryText = "";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                String filePath = bundle.getString(tflService.FILEPATH);
                int resultCode = bundle.getInt(tflService.RESULT);
                if(resultCode == RESULT_OK)  {
                    Toast.makeText(RoutesActivity.this, "Download complete. Download URI: " + filePath,Toast.LENGTH_LONG).show();
                    statusTextView.setText("Download done");
                } else {
                    Toast.makeText(RoutesActivity.this, "Download failed", Toast.LENGTH_LONG).show();
                    statusTextView.setText("Download failed");
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        statusTextView = (TextView) findViewById(R.id.status);
        routesRecyclerView = (RecyclerView) findViewById(R.id.routes_list_view);
        routesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getSupportLoaderManager().initLoader(1,null,this);
    }
    public void restartLoader(){

        getSupportLoaderManager().restartLoader(1,null,this);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1){
        Uri CONTENT_URI;
        if(queryText == "") {
            CONTENT_URI =
                    BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUSROUTES).build();
        } else {
            CONTENT_URI =
                    BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUSROUTES).appendPath(queryText).build();
        }
        Log.d(TAG, "Content uri = " + CONTENT_URI + " base uri = " + BASE_CONTENT_URI);
        cursorLoader = new CursorLoader(this,CONTENT_URI,null,null,null,null );
        return cursorLoader;
    }
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        int rows = cursor.getCount();
        Log.d(TAG, rows + " Routes found");
        if (rows == 0) {
            statusTextView.setText("No Routes found");
        } else {
            cursor.moveToFirst();

            statusTextView.setText(rows + " routes found");
            cursor.moveToFirst();
            mAdapter = new RouteListAdapter(this, cursor);

            // Link the adapter to the RecyclerView
            routesRecyclerView.setAdapter(mAdapter);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> arg0){

    }
    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(receiver, new IntentFilter(tflService.NOTIFICATION));
    }
    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }
    public void onClick(View view){
        Intent intent = new Intent(this, tflService.class);
        intent.putExtra(tflService.FILENAME, "x.csv");
        intent.putExtra(tflService.REMOTEURL, "http://maplyndon.azurewebsites.net/x.csv");
        startService(intent);
        statusTextView.setText("Service Started");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.route_acivity_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryText = query;
                restartLoader();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchView.getQuery().length() == 0) {
                    queryText = "";
                    restartLoader();
                }
                return false;
            }
        });
        return true;
    }

}
