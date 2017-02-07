package com.tenegi.busstops;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tenegi.busstops.data.BusStopContract.*;

/**
 * Created by lyndon on 05/02/2017.
 */

public class StopListAdapter extends RecyclerView.Adapter<StopListAdapter.StopViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private static ClickListener clickListener;

    public interface ClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

    public StopListAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }
    public void setOnItemClickListener(ClickListener clickListener) {
        StopListAdapter.clickListener = clickListener;
    }
    @Override
    public StopListAdapter.StopViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.stop_item, parent, false);
        return new StopListAdapter.StopViewHolder(view);
    }
    @Override
    public void onBindViewHolder(StopListAdapter.StopViewHolder holder, int position){
        if (!mCursor.moveToPosition(position))
            return; // bail if returned null
        long id = mCursor.getLong(mCursor.getColumnIndex(BusStopEntry._ID));
        int stopNumber = mCursor.getInt(mCursor.getColumnIndex(BusStopEntry.COLUMN_SEQUENCE));
        String stopname = mCursor.getString(mCursor.getColumnIndex(BusStopEntry.COLUMN_STOP_NAME));

        holder.stopNumberView.setText(String.valueOf(stopNumber));
        holder.bustopNameView.setText(stopname);
        holder.itemView.setTag(id);
    }
    @Override
    public int getItemCount() {

        return mCursor.getCount();
    }

    class StopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView stopNumberView;
        TextView bustopNameView;

        public StopViewHolder(View itemView){
            super(itemView);
            stopNumberView = (TextView) itemView.findViewById(R.id.stop_number);
            bustopNameView = (TextView) itemView.findViewById(R.id.stop_name);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
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
