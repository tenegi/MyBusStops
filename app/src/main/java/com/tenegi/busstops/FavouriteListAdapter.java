package com.tenegi.busstops;

/**
 * Created by lyndon on 02/02/2017.
 */
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

public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.FavouriteViewHolder>{

    private Context mContext;
    private Cursor mCursor;

    public FavouriteListAdapter(Context context, Cursor cursor){
        this.mContext = context;
        this.mCursor = cursor;
    }
    @Override
    public FavouriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.favourite_item, parent, false);
        return new FavouriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavouriteViewHolder holder, int position){
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null
        long id = mCursor.getLong(mCursor.getColumnIndex(BusStopContract.BusStopEntry._ID));
        String route = mCursor.getString(mCursor.getColumnIndex(COLUMN_ROUTE));
        String stopname = mCursor.getString(mCursor.getColumnIndex(COLUMN_STOP_NAME));
        int run = mCursor.getInt(mCursor.getColumnIndex(COLUMN_RUN));

        holder.routeNumberView.setText(route);
        holder.bustopNameView.setText(stopname);
        holder.routeDirectionView.setText(String.valueOf(run));
        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
    class FavouriteViewHolder extends RecyclerView.ViewHolder{

        TextView routeNumberView;
        TextView routeDirectionView;
        TextView bustopNameView;

        public FavouriteViewHolder(View itemView) {

            super(itemView);
            routeNumberView = (TextView) itemView.findViewById(R.id.route_number);
            routeDirectionView = (TextView) itemView.findViewById(R.id.route_direction);
            bustopNameView = (TextView) itemView.findViewById(R.id.stop_name);
        }
    }
}
