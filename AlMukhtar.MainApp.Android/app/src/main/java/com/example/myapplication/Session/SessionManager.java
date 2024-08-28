package com.example.myapplication.Session;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class SessionManager {

    public SharedPreferences sharedPreferences;

    private Context context;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    int PRIVATE_MODE = 0;



    public SessionManager(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("SessionData",context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    public void saveisPlayingState(Boolean state){

        editor.putBoolean("isPlaying",state);
        editor.apply();
        editor.commit();
    }

    public Boolean getPlayingState(){

        return sharedPreferences.getBoolean("isPlaying",false);
    }

    public void saveisPlayingOrPaused(Boolean state){

        editor.putBoolean("isPlayingPaused",state);
        editor.apply();
        editor.commit();
    }

    public Boolean getPlayingorPaused(){

        return sharedPreferences.getBoolean("isPlayingPaused",false);
    }





    public void PlayingFileMeta(String filename,String duration,String image){

        editor.putString("meta_filename",filename);
        editor.putString("meta_duration",duration);
        editor.putString("meta_image",image);

        editor.apply();
        editor.commit();


    }


    public String getfilename(){
        return sharedPreferences.getString("meta_filename","empty");
    }

    public ArrayList<String> getPlayingFileMeta(){

        String meta_filename = sharedPreferences.getString("meta_filename","empty");
        String meta_duration = sharedPreferences.getString("meta_duration","empty");
        String meta_image = sharedPreferences.getString("meta_image","empty");

        ArrayList<String> metaList = new ArrayList<>();

        metaList.add(meta_filename);
        metaList.add(meta_duration);
        metaList.add(meta_image);

        return  metaList;



    }



    public void SaveCurrentStateofMediaPlayer(){

    }








}
