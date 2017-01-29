package com.tenegi.busstops.com.tenegi.busstops.data;

/**
 * Created by lyndon on 29/01/2017.
 */
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.tenegi.busstops.TestUtils.TestUtilities;
import com.tenegi.busstops.data.BusStopContentProvider;
import com.tenegi.busstops.data.BusStopContract;
import com.tenegi.busstops.data.BusStopDBHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class BusStopContractProviderTests {
    private final Context mContext = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp() {
        /* Use TaskDbHelper to get access to a writable database */
        BusStopDBHelper dbHelper = new BusStopDBHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(BusStopContract.BusStopEntry.TABLE_NAME, null, null);
    }

    @Test
    public void testProviderRegistry() {

        /*
         * A ComponentName is an identifier for a specific application component, such as an
         * Activity, ContentProvider, BroadcastReceiver, or a Service.
         *
         * Two pieces of information are required to identify a component: the package (a String)
         * it exists in, and the class (a String) name inside of that package.
         *
         * We will use the ComponentName for our ContentProvider class to ask the system
         * information about the ContentProvider, specifically, the authority under which it is
         * registered.
         */
        String packageName = mContext.getPackageName();
        String taskProviderClassName = BusStopContentProvider.class.getName();
        ComponentName componentName = new ComponentName(packageName, taskProviderClassName);

        try {

            /*
             * Get a reference to the package manager. The package manager allows us to access
             * information about packages installed on a particular device. In this case, we're
             * going to use it to get some information about our ContentProvider under test.
             */
            PackageManager pm = mContext.getPackageManager();

            /* The ProviderInfo will contain the authority, which is what we want to test */
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = packageName;

            /* Make sure that the registered authority matches the authority from the Contract */
            String incorrectAuthority =
                    "Error: BusStopContentProvider registered with authority: " + actualAuthority +
                            " instead of expected authority: " + expectedAuthority;
            assertEquals(incorrectAuthority,
                    actualAuthority,
                    expectedAuthority);

        } catch (PackageManager.NameNotFoundException e) {
            String providerNotRegisteredAtAll =
                    "Error: BusStopContentProvider not registered at " + mContext.getPackageName();
            /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            fail(providerNotRegisteredAtAll);
        }
    }
    private static final Uri TEST_BUSSTOP = BusStopContract.BusStopEntry.CONTENT_URI;
    // Content URI for a single task with id = 1
    private static final Uri TEST_BUSSTOP_WITH_ID = TEST_BUSSTOP.buildUpon().appendPath("1").build();
    private static final Uri TEST_BUSROUTES = TEST_BUSSTOP.buildUpon().appendPath("routes").build();


    /**
     * This function tests that the UriMatcher returns the correct integer value for
     * each of the Uri types that the ContentProvider can handle. Uncomment this when you are
     * ready to test your UriMatcher.
     */
    @Test
    public void testUriMatcher() {

        /* Create a URI matcher that the TaskContentProvider uses */
        UriMatcher testMatcher = BusStopContentProvider.buildUriMatcher();

        /* Test that the code returned from our matcher matches the expected TASKS int */
        String tasksUriDoesNotMatch = "Error: The BUSSTOPS URI was matched incorrectly.";
        int actualTasksMatchCode = testMatcher.match(TEST_BUSSTOP);
        int expectedTasksMatchCode = BusStopContentProvider.BUSSTOPS;
        assertEquals(tasksUriDoesNotMatch,
                actualTasksMatchCode,
                expectedTasksMatchCode);

        /* Test that the code returned from our matcher matches the expected TASK_WITH_ID */
        String taskWithIdDoesNotMatch =
                "Error: The BUSSTOP_WITH_ID URI was matched incorrectly.";
        int actualTaskWithIdCode = testMatcher.match(TEST_BUSSTOP_WITH_ID);
        int expectedTaskWithIdCode = BusStopContentProvider.BUSSTOPS_WITH_ID;
        assertEquals(taskWithIdDoesNotMatch,
                actualTaskWithIdCode,
                expectedTaskWithIdCode);

        /* Test that the code returned from our matcher matches the expected TASK_WITH_ID */
        String routeDoesNotMatch =
                "Error: The Routes URI was matched incorrectly.";
        int actualRoute = testMatcher.match(TEST_BUSROUTES);
        int expectedRoute = BusStopContentProvider.BUSROUTES;
        assertEquals(taskWithIdDoesNotMatch,
                actualTaskWithIdCode,
                expectedTaskWithIdCode);
    }

    //================================================================================
    // Test Insert
    //================================================================================


    /**
     * Tests inserting a single row of data via a ContentResolver
     */
    @Test
    public void testInsert() {

        /* Create values to insert */
        ContentValues testTaskValues = new ContentValues();
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_ROUTE, "1");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_RUN, 1);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_SEQUENCE, 1);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_STOP_CODE_LBSL, "14456");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_BUS_STOP_CODE, "53369");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_NAPTAN_ATCO, "490000235Z");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_STOP_NAME, "NEW OXFORD STREET");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_LOCATION_EASTING, 529998);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_LOCATION_NORTHING, 181428);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_HEADING, 74);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_FAVOURITE, 1);

        /* TestContentObserver allows us to test if notifyChange was called appropriately */
        TestUtilities.TestContentObserver taskObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        /* Register a content observer to be notified of changes to data at a given URI (tasks) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                BusStopContract.BusStopEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                taskObserver);


        Uri uri = contentResolver.insert(BusStopContract.BusStopEntry.CONTENT_URI, testTaskValues);


        Uri expectedUri = ContentUris.withAppendedId(BusStopContract.BusStopEntry.CONTENT_URI, 1);

        String insertProviderFailed = "Unable to insert item through Provider";
        assertEquals(insertProviderFailed, uri, expectedUri);

        /*
         * If this fails, it's likely you didn't call notifyChange in your insert method from
         * your ContentProvider.
         */
        taskObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(taskObserver);
    }
    //================================================================================
    // Test Query (for tasks directory)
    //================================================================================


    /**
     * Inserts data, then tests if a query for the tasks directory returns that data as a Cursor
     */
    @Test
    public void testQuery() {

        /* Get access to a writable database */
        BusStopDBHelper dbHelper = new BusStopDBHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        /* Create values to insert */
        ContentValues testTaskValues = new ContentValues();
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_ROUTE, "1");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_RUN, 1);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_SEQUENCE, 1);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_STOP_CODE_LBSL, "14456");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_BUS_STOP_CODE, "53369");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_NAPTAN_ATCO, "490000235Z");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_STOP_NAME, "NEW OXFORD STREET");
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_LOCATION_EASTING, 529998);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_LOCATION_NORTHING, 181428);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_HEADING, 74);
        testTaskValues.put(BusStopContract.BusStopEntry.COLUMN_FAVOURITE, 1);

        /* Insert ContentValues into database and get a row ID back */
        long taskRowId = database.insert(
                /* Table to insert values into */
                BusStopContract.BusStopEntry.TABLE_NAME,
                null,
                /* Values to insert into table */
                testTaskValues);

        String insertFailed = "Unable to insert directly into the database";
        assertTrue(insertFailed, taskRowId != -1);

        /* We are done with the database, close it now. */
        database.close();

        /* Perform the ContentProvider query */
        Cursor BusStopCursor = mContext.getContentResolver().query(
                BusStopContract.BusStopEntry.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort order to return in Cursor */
                null);


        String queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, BusStopCursor != null);

        /* We are done with the cursor, close it now. */
        BusStopCursor.close();
    }


}
