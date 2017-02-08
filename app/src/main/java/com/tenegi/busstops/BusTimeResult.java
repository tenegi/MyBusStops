package com.tenegi.busstops;

/**
 * Created by lyndon on 08/02/2017.
 */

public class BusTimeResult{
    private String mRoute;
    private String mExpectedArrival;
    private int mTimeToStop;
    private String mDestination;
    public BusTimeResult(){
    }
    public BusTimeResult(String route, String expectedArrival, int timeToStop, String destination) {
        this.mRoute = route;
        this.mExpectedArrival = expectedArrival;
        this.mTimeToStop = timeToStop;
        this.mDestination = destination;
    }
    public void setRoute(String route){
        this.mRoute = route;
    }
    public String getRoute(){
        return mRoute;
    }
    public void setExpectedArival(String expectedArrival){
        this.mExpectedArrival = expectedArrival;
    }
    public String getExpectedArival(){
        return mExpectedArrival;
    }
    public void setTimeToStop(int timeToStop){
        mTimeToStop = timeToStop;
    }
    public int getTimeToStop(){
        return mTimeToStop;
    }
    public String getTimeToStopInMinutes(){
        int t = (mTimeToStop + 59) / 60;
        return String.valueOf(t) + " minutes";
    }
    public void setDestiniation(String destination){
        this.mDestination = destination;
    }
    public String getDestination(){
        return mDestination;
    }


}
