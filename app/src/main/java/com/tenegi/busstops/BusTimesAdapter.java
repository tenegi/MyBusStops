package com.tenegi.busstops;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lyndon on 06/02/2017.
 */

public class BusTimesAdapter extends BaseAdapter{
    private Activity activity;
    //private static ArrayList<String> times;
    private static BusTimeResultList times;
    private static LayoutInflater inflater = null;


    public BusTimesAdapter(Activity a, BusTimeResultList results){
        activity = a;
        times = results;
        inflater =(LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if(convertView == null){
            v = inflater.inflate(R.layout.bus_time_item, null);
        }
        TextView routeView = (TextView) v.findViewById(R.id.bus_time_route);
        TextView arrivalTimeView = (TextView) v.findViewById(R.id.bus_arrival_time);
        TextView timeToStopView = (TextView) v.findViewById(R.id.bus_time_to_stop);
        TextView destinationView = (TextView) v.findViewById(R.id.bus_time_destination);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        BusTimeResult b = times.get(position);
        String arrivalTime = b.getExpectedArival();
        arrivalTime = arrivalTime.replace("T", " ");
        arrivalTime = arrivalTime.replace("Z", "");
        Date d=new Date();
        try {
            d=  dateFormat.parse(arrivalTime);
        } catch (ParseException e) {

            e.printStackTrace();
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        String arrTime = timeFormat.format(d);
        routeView.setText(b.getRoute());
        arrivalTimeView.setText(arrTime);
        timeToStopView.setText(b.getTimeToStopInMinutes());
        destinationView.setText(b.getDestination());
        if(b.getSelectedRoute() == 1) {
            routeView.setBackgroundResource(R.drawable.blue_circle);
            routeView.setTextColor(Color.WHITE);
        } else{
            routeView.setBackgroundResource(R.drawable.white_circle);
            routeView.setTextColor(Color.BLACK);
        }
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
