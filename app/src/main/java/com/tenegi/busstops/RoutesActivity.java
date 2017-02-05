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
import static com.tenegi.busstops.data.BusStopContract.PATH_BUSROUTES;
import static java.security.AccessController.getContext;

public class RoutesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "Routes Activity";
    private static final int ROUTES_LOADER = 1;
    //private static final int SETTINGS_LOADER = 2;

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
        getSupportLoaderManager().initLoader(ROUTES_LOADER,null,this);
        //getSupportLoaderManager().initLoader(SETTINGS_LOADER,null,this);
    }
    public void restartLoader(int loader){

        getSupportLoaderManager().restartLoader(loader,null,this);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1){
        Uri CONTENT_URI;
        switch(arg0) {
            case ROUTES_LOADER:
                if (queryText == "") {
                    CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUSROUTES).build();
                } else {
                    CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUSROUTES).appendPath(queryText).build();
                }
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
            case ROUTES_LOADER:
                int rows = cursor.getCount();
                Log.d(TAG, rows + " Routes found");
                if (rows == 0) {
                    statusTextView.setText("No Routes found");
                } else {
                    cursor.moveToFirst();
                    //statusTextView.setText(rows + " routes found");
                    cursor.moveToFirst();
                    mAdapter = new RouteListAdapter(this, cursor);
                    mAdapter.setOnItemClickListener(new RouteListAdapter.ClickListener() {
                        @Override
                        public void onItemClick(int position, View v) {
                            String t = (String) v.getTag();
                            Log.d(TAG, "onItemClick position: " + position + ", tag = " + t);
                            //Toast.makeText(RoutesActivity.this, "Item Clicked " + t, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), StopsActivity.class);
                            i.putExtra("route",t);
                            startActivity(i);
                        }

                        @Override
                        public void onItemLongClick(int position, View v) {
                            String t = (String) v.getTag();
                            Log.d(TAG, "onItemLongClick position: " + position + ", tag = " + t);
                            //Toast.makeText(RoutesActivity.this, "Item Long Clicked " + t, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), StopsActivity.class);
                            i.putExtra("route",t);
                            startActivity(i);
                        }
                    });
                    routesRecyclerView.setAdapter(mAdapter);
                }
                break;

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
        inflater.inflate(R.menu.route_activity_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryText = query;
                restartLoader(ROUTES_LOADER);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchView.getQuery().length() == 0) {
                    queryText = "";
                    restartLoader(ROUTES_LOADER);
                }
                return false;
            }
        });
        return true;
    }

}
