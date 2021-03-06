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

import com.tenegi.busstops.dal.BusStopContract;

import static com.tenegi.busstops.dal.BusStopContract.BusStopEntry.COLUMN_ROUTE;
import static com.tenegi.busstops.dal.BusStopContract.BusStopEntry.COLUMN_STOP_NAME;

public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.FavouriteViewHolder>{

    private Context mContext;
    private Cursor mCursor;
    private static ClickListener clickListener;

    public interface ClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }
    public void setOnItemClickListener(ClickListener clickListener) {
        FavouriteListAdapter.clickListener = clickListener;
    }

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


        holder.routeNumberView.setText(route);
        holder.bustopNameView.setText(stopname);
        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {

        return mCursor.getCount();
    }
    class FavouriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        TextView routeNumberView;
        TextView bustopNameView;

        public FavouriteViewHolder(View itemView) {

            super(itemView);
            routeNumberView = (TextView) itemView.findViewById(R.id.route_number);
            bustopNameView = (TextView) itemView.findViewById(R.id.stop_name);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
    }
}
