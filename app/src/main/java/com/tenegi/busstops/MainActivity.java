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
    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTextView = (TextView) findViewById(R.id.status);
        //RecyclerView favouriteRecyclerView;
        favouriteRecyclerView = (RecyclerView) findViewById(R.id.favourites_list_view);
        favouriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getSupportLoaderManager().initLoader(1,null,this);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //do nothing, we only care about swiping
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // COMPLETED (8) Inside, get the viewHolder's itemView's tag and store in a long variable id
                //get the id of the item being swiped
                long id = (long) viewHolder.itemView.getTag();
                // COMPLETED (9) call removeGuest and pass through that id
                //remove from DB
                removeFavourite(id);
                // COMPLETED (10) call swapCursor on mAdapter passing in getAllGuests() as the argument
                //update the list
                restartLoader();
            }

            //COMPLETED (11) attach the ItemTouchHelper to the waitlistRecyclerView
        }).attachToRecyclerView(favouriteRecyclerView);

        }
    public void onClick(View view){

        getSupportLoaderManager().initLoader(1,null,this);
    }
    public void restartLoader(){
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
