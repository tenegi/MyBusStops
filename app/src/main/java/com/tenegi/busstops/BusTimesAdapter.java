package com.tenegi.busstops;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String arrivalTime = times.get(position).toString();
        arrivalTime = arrivalTime.replace("T", " ");
        arrivalTime = arrivalTime.replace("Z", "");
        Date d=new Date();
        try {
            d=  dateFormat.parse(arrivalTime);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        String arrTime = timeFormat.format(d);
        timeSlot.setText(arrTime);
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