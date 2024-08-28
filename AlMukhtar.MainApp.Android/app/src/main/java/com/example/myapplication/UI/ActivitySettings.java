package com.example.myapplication.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;

import com.example.myapplication.databinding.ActivitySettingsBinding;
import com.example.myapplication.service.BackgroundService;

public class ActivitySettings extends AppCompatActivity {

    ActivitySettingsBinding settingsBinding;
    BackgroundService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(settingsBinding.getRoot());



    }



}