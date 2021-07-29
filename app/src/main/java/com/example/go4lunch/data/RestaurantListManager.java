package com.example.go4lunch.data;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.go4lunch.model.Restaurant;
import com.example.go4lunch.services.MyJobService;
import com.example.go4lunch.util.Constants;

import java.util.ArrayList;

@SuppressLint("StaticFieldLeak")
public class RestaurantListManager{

    public interface OnRestaurantListReceive{
        void onResponse(ArrayList<Restaurant> restaurantList);
    }

    private final Context mContext;
    private BroadcastReceiver broadcastReceiver;
    private static RestaurantListManager INSTANCE;
    private ArrayList<Restaurant> restaurantArrayList = new ArrayList<>();
    private final JobScheduler jobScheduler;

    public RestaurantListManager(Context mContext) {
        this.mContext = mContext;
        jobScheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        startGettingListInBackground();
    }

    public static synchronized RestaurantListManager getInstance(Context context){
        if (INSTANCE == null)
            INSTANCE = new RestaurantListManager(context);

        return INSTANCE;
    }

    public void receiveRestaurantList(OnRestaurantListReceive callback){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    restaurantArrayList = bundle.getParcelableArrayList(Constants.LIST);

                    if (callback != null)
                        callback.onResponse(restaurantArrayList);

                }
            }
        };
    }

    private void startGettingListInBackground(){
//        jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(Constants.JOB_ID, new ComponentName(mContext.getApplicationContext(), MyJobService.class))
//                .setMinimumLatency(0)
//                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED) //The action will stop if there is no more internet
//                .setPersisted(true) //Set if the action will continue to be executed even when the device is rebooting
//                .setPeriodic(15 * 60 * 1000)  //Set the interval of time that the action will be executed, here it's every 15 minutes
                .build();



        jobScheduler.schedule(jobInfo);
    }

    public void registerBroadcastReceiverFromManager(String action){
        mContext.registerReceiver(broadcastReceiver,new IntentFilter(action));
    }

    public void unregisterBroadcastReceiverFromManager(){
        mContext.unregisterReceiver(broadcastReceiver);
    }
}
