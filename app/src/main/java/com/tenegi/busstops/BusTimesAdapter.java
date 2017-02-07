package com.tenegi.busstops;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lyndon on 06/02/2017.
 */

public class BusTimesAdapter extends BaseAdapter{
    private Activity activity;
    private static ArrayList<String> times;
    private static LayoutInflater inflater = null;

    public BusTimesAdapter(Activity a, ArrayList<String> results){
        activity = a;
        times = results;
        inflater =(LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if(convertView == null){
            v = inflater.inflate(R.layout.bus_time_item, null);
        }
        TextView timeSlot = (TextView) v.findViewById(R.id.bus_arrival_time);
        timeSlot.setText(times.get(position).toString());
        return v;
    }
    public int getCount(){
        return times.size();
    }
    public Object getItem(int position){
        return position;
    }
    public long getItemId(int position){
        return position;
    }
}
