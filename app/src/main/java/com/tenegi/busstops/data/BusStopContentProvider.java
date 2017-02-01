package com.tenegi.busstops.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_FAVOURITE;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.TABLE_NAME;

/**
 * Created by lyndon on 29/01/2017.
 */

public class BusStopContentProvider extends ContentProvider {

    private BusStopDBHelper mBusStopDbHelper;
    public static final int BUSSTOPS = 100;
    public static final int BUSSTOPS_WITH_ID = 101;
    public static final int BUSROUTES = 200;
    public static final int BUSROUTE_WITH_ID = 201;
    public static final int FAVOURITES = 300;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */
        uriMatcher.addURI(BusStopContract.AUTHORITY, BusStopContract.PATH_BUSSTOPS, BUSSTOPS);
        uriMatcher.addURI(BusStopContract.AUTHORITY, BusStopContract.PATH_BUSSTOPS + "/#", BUSSTOPS_WITH_ID);
        uriMatcher.addURI(BusStopContract.AUTHORITY, BusStopContract.PATH_BUSROUTES, BUSROUTES);
        uriMatcher.addURI(BusStopContract.AUTHORITY, BusStopContract.PATH_BUSROUTES + "/#", BUSROUTE_WITH_ID);
        uriMatcher.addURI(BusStopContract.AUTHORITY, BusStopContract.PATH_FAVOURITES, FAVOURITES);

        return uriMatcher;
    }
    @Override
    public boolean onCreate() {
        Context context = getContext();
        mBusStopDbHelper = new BusStopDBHelper(context);
        return true;
    }
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        final SQLiteDatabase db = mBusStopDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        switch (match) {
            case BUSSTOPS:
               long id = db.insert(TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(BusStopContract.BusStopEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // COMPLETED (4) Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)throws OperationApplicationException{
        final SQLiteDatabase db = mBusStopDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }

    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){
        final SQLiteDatabase db = mBusStopDbHelper.getWritableDatabase();
        int insertCount = 0;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BUSSTOPS:
                try{
                    db.beginTransaction();
                    for(ContentValues value : values){
                        long id = db.insert(TABLE_NAME, null, value);
                        if(id > 0){
                            insertCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } catch(Exception e){
                    e.printStackTrace();
                } finally{
                    db.endTransaction();
                }
                break;
            // COMPLETED (4) Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertCount;
    }



    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mBusStopDbHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        // COMPLETED (3) Query for the tasks directory and write a default case
        switch (match) {
            // Query for the tasks directory
            case BUSSTOPS:
                retCursor =  db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVOURITES:
                builder.setTables(TABLE_NAME);
                builder.appendWhere(COLUMN_FAVOURITE +"= 1");
                retCursor = builder.query(db,projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                //retCursor =  db.query(TABLE_NAME,
                      //  projection,
                      //  selection,
                      //  selectionArgs,
                      //  null,
                       // null,
                      //  sortOrder);
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // COMPLETED (4) Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return retCursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public String getType(@NonNull Uri uri) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

}
