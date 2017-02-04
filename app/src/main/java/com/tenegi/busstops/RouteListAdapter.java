package com.tenegi.busstops;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tenegi.busstops.data.BusStopContract;

import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_ROUTE;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_RUN;
import static com.tenegi.busstops.data.BusStopContract.BusStopEntry.COLUMN_STOP_NAME;

/**
 * Created by lyndon on 04/02/2017.
 */

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.RouteViewHolder> {

    private Context mContext;
    private Cursor mCursor;



    public RouteListAdapter(Context context, Cursor cursor){
        this.mContext = context;
        this.mCursor = cursor;


    }

    @Override
    public RouteListAdapter.RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.route_item, parent, false);
        return new RouteListAdapter.RouteViewHolder(view);
    }
    @Override
    public void onBindViewHolder(RouteListAdapter.RouteViewHolder holder, int position){
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null
        //long id = mCursor.getLong(mCursor.getColumnIndex(BusStopContract.BusStopEntry._ID));
        String route = mCursor.getString(mCursor.getColumnIndex(COLUMN_ROUTE));
        String stopname = mCursor.getString(mCursor.getColumnIndex(COLUMN_STOP_NAME));
        int run = mCursor.getInt(mCursor.getColumnIndex(COLUMN_RUN));

        holder.routeNumberView.setText(route);
        holder.routeDescriptionView.setText(String.valueOf(run) + " to " + stopname);
        holder.itemView.setTag(route + ":" + String.valueOf(run));
    }
    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
    class RouteViewHolder extends RecyclerView.ViewHolder{

        TextView routeNumberView;
        TextView routeDescriptionView;
        TextView bustopNameView;

        public RouteViewHolder(View itemView) {

            super(itemView);
            routeNumberView = (TextView) itemView.findViewById(R.id.route_number);
            routeDescriptionView = (TextView) itemView.findViewById(R.id.route_description);
        }
    }
}
