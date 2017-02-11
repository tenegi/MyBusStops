package com.tenegi.busstops;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tenegi.busstops.tflService.tflService;

import java.util.Date;

import static com.tenegi.busstops.data.BusStopContract.BASE_CONTENT_URI;
import static com.tenegi.busstops.data.BusStopContract.PATH_BUSROUTES;
import static java.security.AccessController.getContext;

public class RoutesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "Routes Activity";
    private static final int ROUTES_LOADER = 1;
    //private static final int SETTINGS_LOADER = 2;

    private TextView statusTextView;
    private Button updateButton;
    private RouteListAdapter mAdapter;
    private SearchView mSearchView;
    RecyclerView routesRecyclerView;
    CursorLoader cursorLoader;
    String queryText = "";
    String routeUrl="";
    Boolean needToRefresh = false;
    String lastUpdated;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                String filePath = bundle.getString(tflService.FILEPATH);
                int resultCode = bundle.getInt(tflService.RESULT);
                if(resultCode == RESULT_OK)  {
                    Toast.makeText(RoutesActivity.this, R.string.routes_status_dl_complete_with_uri + filePath,Toast.LENGTH_LONG).show();
                    statusTextView.setText(R.string.routes_status_dl_complete);
                    restartLoader(ROUTES_LOADER);
                } else {
                    Toast.makeText(RoutesActivity.this, R.string.routes_status_dl_failed, Toast.LENGTH_LONG).show();
                    statusTextView.setText(R.string.routes_status_dl_failed);
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        ActionBar actionBar = this.getSupportActionBar();

        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Resources resources = this.getResources();

        statusTextView = (TextView) findViewById(R.id.status);
        updateButton = (Button) findViewById(R.id.btnUpdate);
        routesRecyclerView = (RecyclerView) findViewById(R.id.routes_list_view);
        routesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getSupportLoaderManager().initLoader(ROUTES_LOADER,null,this);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            routeUrl = b.getString("route_url");
            needToRefresh = b.getBoolean("need_to_update");
            lastUpdated = b.getString("last_update");
            if (needToRefresh) {
                statusTextView.setText(String.format(resources.getString(R.string.routes_update_needed),lastUpdated));
                updateButton.setVisibility(View.GONE);
            } else {
                statusTextView.setText(String.format(resources.getString(R.string.routes_status_updated),lastUpdated));
                updateButton.setVisibility(View.INVISIBLE);
            }
        } else {
            updateButton.setVisibility(View.GONE);
            statusTextView.setText("");
        }
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
                    statusTextView.setText(R.string.routes_no_routes);
                } else {
                    cursor.moveToFirst();
                    //statusTextView.setText(rows + " routes found");
                    cursor.moveToFirst();
                    mAdapter = new RouteListAdapter(this, cursor);
                    mAdapter.setOnItemClickListener(new RouteListAdapter.ClickListener() {
                        @Override
                        public void onItemClick(int position, View v) {
                            String t = v.getTag().toString();
                            Log.d(TAG, "onItemClick position: " + position + ", tag = " + t);
                            //Toast.makeText(RoutesActivity.this, "Item Clicked " + t, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), StopsActivity.class);
                            i.putExtra("route",t);
                            startActivity(i);
                        }

                        @Override
                        public void onItemLongClick(int position, View v) {
                            String t = v.getTag().toString();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSearchView.setQuery("", false);
        mSearchView.setIconified(true);
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
        updateRoutes();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.route_activity_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.route_action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryText = query;
                restartLoader(ROUTES_LOADER);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mSearchView.getQuery().length() == 0) {
                    queryText = "";
                    restartLoader(ROUTES_LOADER);
                }
                return false;
            }
        });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the VisualizerActivity
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);

            //NavUtils.navigateUpTo(NavUtils.getParentActivityName(NavUtils.PARENT_ACTIVITY),null);
        }
        if (id == R.id.action_update) {
            updateRoutes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateRoutes(){
        Intent intent = new Intent(this, tflService.class);
        //intent.putExtra(tflService.FILENAME, "x.csv");
        intent.putExtra(tflService.REMOTEURL, routeUrl);
        startService(intent);
        statusTextView.setText(R.string.routes_service_started);
    }

}
