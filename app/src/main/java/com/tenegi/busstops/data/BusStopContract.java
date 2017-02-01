package com.tenegi.busstops.data;


import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by lyndon on 29/01/2017.
 */

public class BusStopContract {

    public static final String AUTHORITY = "com.tenegi.busstops";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_BUSSTOPS = "busstops";
    public static final String PATH_BUSROUTES = "busstops/routes";
    public static final String PATH_FAVOURITES = "favourites";

    public static final class BusStopEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BUSSTOPS).build();

        public static final String TABLE_NAME = "busstops";
        public static final String COLUMN_ROUTE = "Route";
        public static final String COLUMN_RUN = "Run";
        public static final String COLUMN_SEQUENCE = "Sequence";
        public static final String COLUMN_STOP_CODE_LBSL = "Stop_Code_LBSL";
        public static final String COLUMN_BUS_STOP_CODE = "Bus_Stop_Code";
        public static final String COLUMN_NAPTAN_ATCO = "Naptan_Atco";
        public static final String COLUMN_STOP_NAME = "Stop_Name";
        public static final String COLUMN_LOCATION_EASTING = "Location_Easting";
        public static final String COLUMN_LOCATION_NORTHING = "Location_Northing";
        public static final String COLUMN_HEADING = "Heading";
        public static final String COLUMN_FAVOURITE = "Favourite";
    }
}
