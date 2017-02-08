package com.tenegi.busstops;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


/**
 * Created by lyndon on 08/02/2017.
 */

public class BusTimeResultList extends ArrayList<BusTimeResult> implements Parcelable {

    public BusTimeResultList(){

    }
    public BusTimeResultList(Parcel in){
        readFromParcel(in);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){

        public BusTimeResultList createFromParcel(Parcel in){
            return new BusTimeResultList(in);
        }

        public Object[] newArray(int arg0){
            return null;
        }
    };
    private void readFromParcel(Parcel in){

        this.clear();

        int size = in.readInt();
        for(int i = 0; i < size; i++){
            BusTimeResult b = new BusTimeResult();
            b.setRoute(in.readString());
            b.setExpectedArival(in.readString());
            b.setTimeToStop(in.readInt());
            b.setDestiniation(in.readString());
            this.add(b);
        }
    }
    public int describeContents(){
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags){
        int size = this.size();

        dest.writeInt(size);
        for(int i = 0; i< size;i++){
            BusTimeResult b = this.get(i);
            dest.writeString(b.getRoute());
            dest.writeString(b.getExpectedArival());
            dest.writeInt(b.getTimeToStop());
            dest.writeString(b.getDestination());
        }
    }
}
