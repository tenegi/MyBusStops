package com.tenegi.busstops.data;


import android.net.Uri;
import android.provider.BaseColumns;

import java.sql.Date;

/**
 * Created by lyndon on 29/01/2017.
 */

public class BusStopContract {

    public static final String AUTHORITY = "com.tenegi.busstops";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_BUSSTOPS = "busstops";
    public static final String PATH_BUSROUTES = "routes";
    public static final String PATH_FAVOURITES = "favourites";
    public static final String PATH_LOADER_TABLE = "load";
    public static final String PATH_SETTINGS = "settings";

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

    public static final class LoadEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOADER_TABLE).build();

        public static final String TABLE_NAME = "load";
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

    public static final class SettingsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SETTINGS).build();

        public static final String TABLE_NAME = "settings";
        public static final String COLUMN_ROUTES_URL = "Routes_url";
        public static final String COLUMN_STOPPOINT_URL = "StopPoint_Url";
        public static final String COLUMN_STOPPOINT_PATH = "StopPoint_Path";
        public static final String COLUMN_STOPPOINT_APPID = "Times_AppId";
        public static final String COLUMN_STOPPOINT_APPKEY = "Times_AppKey";
        public static final String COLUMN_DATE_UPDATED = "Last_Updated";
    }
}