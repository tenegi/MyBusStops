package com.tenegi.busstops;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.security.ProviderInstaller;
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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        ProviderInstaller.ProviderInstallListener{

    private static final int ERROR_DIALOG_REQUEST_CODE = 1;

    private boolean mRetryProviderInstall;

    private static final int VERTICAL_ITEM_SPACE = 28;
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

    RecyclerView favouriteRecyclerView;
    CursorLoader cursorLoader;
    Context mContext = this;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportLoaderManager().initLoader(SETTINGS_LOADER, null, this);
        setContentView(R.layout.activity_main);
        ProviderInstaller.installIfNeededAsync(this, this);

        statusTextView = (TextView) findViewById(R.id.status);
        favouriteRecyclerView = (RecyclerView) findViewById(R.id.favourites_list_view);
        favouriteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favouriteRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));
        favouriteRecyclerView.addItemDecoration(new com.tenegi.busstops.DividerItemDecoration(mContext));
        getSupportLoaderManager().initLoader(FAVOURITES_LOADER, null, this);
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

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onClick(View view) {

        getSupportLoaderManager().initLoader(FAVOURITES_LOADER, null, this);
    }

    public void restartLoader(int loader) {

        getSupportLoaderManager().initLoader(loader, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Uri CONTENT_URI;
        switch (arg0) {
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
        cursorLoader = new CursorLoader(this, CONTENT_URI, null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        switch (arg0.getId()) {
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
                    statusTextView.setVisibility(View.INVISIBLE);
                    cursor.moveToFirst();
                    mAdapter = new FavouriteListAdapter(this, cursor);
                    mAdapter.setOnItemClickListener(new FavouriteListAdapter.ClickListener() {
                        @Override
                        public void onItemClick(int position, View v) {
                            long tagValue = (long) v.getTag();
                            String t = String.valueOf(tagValue);
                            TextView routeView = (TextView) v.findViewById(R.id.route_number);
                            String route = routeView.getText().toString();
                            Log.d(TAG, "onItemClick position: " + position + ", tag = " + t);
                            //Toast.makeText(MainActivity.this, "Item Clicked " + t, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), TimesActivity.class);
                            i.putExtra("STOPPOINT_URL", STOPPOINT_URL);
                            i.putExtra("STOPPOINT_PATH", STOPPOINT_PATH);
                            i.putExtra("STOPPOINT_APPID", STOPPOINT_APPID);
                            i.putExtra("STOPPOINT_APPKEY", STOPPOINT_APPKEY);
                            i.putExtra("SELECTED_ROUTE", route);
                            i.putExtra("id", tagValue);
                            startActivity(i);
                        }

                        @Override
                        public void onItemLongClick(int position, View v) {
                            long tagValue = (long) v.getTag();
                            String t = String.valueOf(tagValue);
                            Log.d(TAG, "onItemLongClick position: " + position + ", tag = " + t);
                            //Toast.makeText(MainActivity.this, "Item Long Clicked " + t, Toast.LENGTH_LONG).show();
                            Intent i = new Intent(getApplicationContext(), TimesActivity.class);
                            i.putExtra("STOPPOINT_URL", STOPPOINT_URL);
                            i.putExtra("STOPPOINT_PATH", STOPPOINT_PATH);
                            i.putExtra("STOPPOINT_APPID", STOPPOINT_APPID);
                            i.putExtra("STOPPOINT_APPKEY", STOPPOINT_APPKEY);
                            i.putExtra("id", tagValue);
                            startActivity(i);
                        }
                    });
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
                String s = cursor.getString(cursor.getColumnIndex(BusStopContract.SettingsEntry.COLUMN_DATE_UPDATED));
                cursor.close();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date d = new Date();
                try {
                    d = dateFormat.parse(s);
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
                break;

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private void removeFavourite(long id) {
        ContentValues busStopValues = new ContentValues();
        busStopValues.put(BusStopContract.BusStopEntry.COLUMN_FAVOURITE, 0);
        ContentResolver contentResolver = this.getContentResolver();
        Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).appendPath(String.valueOf(id)).build();
        Log.d(TAG, "Content uri for update favourite = " + CONTENT_URI);
        int count = contentResolver.update(CONTENT_URI, busStopValues, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_routes) {
            Intent startRoutesActivity = new Intent(this, RoutesActivity.class);

            startActivity(startRoutesActivity);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
    /**
     * This method is only called if the provider is successfully updated
     * (or is already up-to-date).
     */
    @Override
    public void onProviderInstalled() {
        // Provider is up-to-date, app can make secure network calls.
    }

    /**
     * This method is called if updating fails; the error code indicates
     * whether the error is recoverable.
     */
    @Override
    public  void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Recoverable error. Show a dialog prompting the user to
            // install/update/enable Google Play services.
            GooglePlayServicesUtil.showErrorDialogFragment(
                    errorCode,
                    this,
                    ERROR_DIALOG_REQUEST_CODE,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // The user chose not to take the recovery action
                            onProviderInstallerNotAvailable();
                        }
                    });
        } else {
            // Google Play services is not available.
            onProviderInstallerNotAvailable();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GooglePlayServicesUtil.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
            mRetryProviderInstall = true;
        }
    }
    private void onProviderInstallerNotAvailable() {
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
    }
}
