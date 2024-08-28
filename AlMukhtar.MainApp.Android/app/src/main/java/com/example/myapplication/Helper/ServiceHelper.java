package com.example.myapplication.Helper;

import android.content.Context;
import android.content.Intent;

import com.example.myapplication.service.BackgroundService;

public class ServiceHelper {

    public  BackgroundService backgroundService;
    Context context;

    public ServiceHelper(Context context){

        backgroundService = new BackgroundService();
        this.context = context;
    }

    public void startBackgroundService(Intent intent){


        context.startService(intent);
    }

    public void stopService(Intent intent){

        context.stopService(intent);
    }

    public BackgroundService service(){

        return backgroundService;
    }
}
