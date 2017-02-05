package com.tenegi.busstops;

import android.content.ContentResolver;
import android.content.ContentValues;
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
import static com.tenegi.busstops.data.BusStopContract.PATH_FAVOURITES;

public class StopsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "Stops Activity";
    private static final int STOPS_LOADER = 1;

    private StopListAdapter mAdapter;
    RecyclerView stopsRecyclerView;
    CursorLoader cursorLoader;
    TextView mRouteNumberView;
    String route = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stops);
        route = getIntent().getExtras().getString("route");
        Bundle routeBundle = new Bundle();
        routeBundle.putString("route", route);
        stopsRecyclerView = (RecyclerView) findViewById(R.id.stops_list_view);
        stopsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRouteNumberView = (TextView) findViewById(R.id.routeNumber);
        mRouteNumberView.setText(route);
        getSupportLoaderManager().initLoader(STOPS_LOADER,routeBundle,this);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle params){
        Uri CONTENT_URI;
        switch(arg0) {
            case STOPS_LOADER:
                CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUSSTOPS).appendPath(params.getString("route")).build();

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
            case STOPS_LOADER:
                int rows = cursor.getCount();
                Log.d(TAG, rows + " Routes found");
                if (rows == 0) {
                    mRouteNumberView.setText(route + " - No stops found");
                } else {
                    cursor.moveToFirst();
                    cursor.moveToFirst();
                    mAdapter = new StopListAdapter(this, cursor);
                    mAdapter.setOnItemClickListener(new StopListAdapter.ClickListener() {
                        @Override
                        public void onItemLongClick(int position, View v) {
                            String t = v.getTag().toString();
                            Log.d(TAG, "onItemLongClick position: " + position + ", tag = " + t);
                            Toast.makeText(StopsActivity.this, "Added to favourites " + t, Toast.LENGTH_LONG).show();
                            addFavourite(Long.parseLong(t));
                        }
                    });
                    stopsRecyclerView.setAdapter(mAdapter);
                }
                break;
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> arg0){

    }
    private void addFavourite(long id){
        ContentValues busStopValues = new ContentValues();
        busStopValues.put(BusStopContract.BusStopEntry.COLUMN_FAVOURITE, 1);
        ContentResolver contentResolver = this.getContentResolver();
        Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).appendPath(String.valueOf(id)).build();
        Log.d(TAG, "Content uri for update favourite = " + CONTENT_URI);
        int count = contentResolver.update(CONTENT_URI, busStopValues,null,null);
    }
}
