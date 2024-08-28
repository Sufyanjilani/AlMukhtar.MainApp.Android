package com.example.myapplication.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;

import com.example.myapplication.Helper.ServiceHelper;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.example.myapplication.databinding.ActivityMediaPlayerBinding;
import com.example.myapplication.service.BackgroundService;

import java.util.concurrent.TimeUnit;

public class MediaPlayerActivity extends AppCompatActivity {

    public static Intent serviceIntent = null;
    public ActivityMediaPlayerBinding mediaPlayerBinding;
    public ServiceHelper serviceHelper;

    SessionManager sessionManager;
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("SendDuration")) {
                String duration = intent.getStringExtra("mediaduration");
                mediaPlayerBinding.txttotalduration.setText(duration);
            }

            if (intent.getAction().equals("SeekBarUpdate")) {
                int progress = intent.getIntExtra("progress", 0);

                String remaining = intent.getStringExtra("remaining");

                mediaPlayerBinding.txtrunningduration.setText(remaining);

                mediaPlayerBinding.seekbar.setProgress(progress);

                Log.d("Progress", String.valueOf(progress));
            }

            //Handle Media Notification Click changes to Activity UI
            if (intent.getAction().equals("Media_Notification_service")) {

                String command = intent.getStringExtra("command");
                Log.d("TAG",command);



                if (command.equals("Media_notification_service_pause")) {

                    mediaPlayerBinding.btnPlayPauseIcon.setImageResource(R.drawable.baseline_play_arrow_white);

                }

                if (command.equals("Media_notification_service_play")) {

                    mediaPlayerBinding.btnPlayPauseIcon.setImageResource(R.drawable.baseline_pause_24_white);

                }
            }

        }
    };


    BackgroundService service;
    String play_pause_FLag = "pause";

    String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        mediaPlayerBinding = ActivityMediaPlayerBinding.inflate(getLayoutInflater());
        setContentView(mediaPlayerBinding.getRoot());
        service = new BackgroundService();

        serviceIntent = new Intent(MediaPlayerActivity.this, BackgroundService.class);

        sessionManager = new SessionManager(this);

        serviceHelper = new ServiceHelper(this);



        String mediaActivityState = getIntent().getExtras()==null?"":getIntent().getExtras().getString("MediaState");

        if (mediaActivityState.equals("Resume")){

            ResumeMediaPlayerActivity();
        }
        else{

            LoadMediaPlayerActivity();

        }



        Transition enterTransition = TransitionInflater.from(this).inflateTransition(R.transition.slow_enter_transition);
        getWindow().setSharedElementEnterTransition(enterTransition);

        Transition exitTransition = TransitionInflater.from(this).inflateTransition(R.transition.slow_exit_transition);
        getWindow().setSharedElementExitTransition(exitTransition);



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getWindow().setSharedElementReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.slow_exit_transition));

    }

    public void LoadMediaPlayerActivity(){



        String path = "android/resource://" + getPackageName() + "/" + R.raw.file_example;
        filename = "" + R.raw.file_example;
        String subtext = "Playing";
        serviceIntent.putExtra("media_source", path);
        serviceIntent.putExtra("filename", filename);
        serviceIntent.putExtra("subtext", subtext);

        mediaPlayerBinding.btnPlayPauseIcon.setImageResource(R.drawable.baseline_pause_24_white);

        mediaPlayerBinding.txttitle.setText(path);





        //check if audio is playing
        mediaPlayerBinding.btnPlaypauseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (serviceHelper.backgroundService.mediaPlayer.isPlaying()) {

                    mediaPlayerBinding.btnPlayPauseIcon.setImageResource(R.drawable.baseline_play_arrow_white);
                    UpdateNotificationFromActivity("MEDIA_NOTIFICATION_PLAY_PAUSE");
                } else {

                    mediaPlayerBinding.btnPlayPauseIcon.setImageResource(R.drawable.baseline_pause_24_white);
                    UpdateNotificationFromActivity("MEDIA_NOTIFICATION_PLAY_PAUSE");
                }
            }
        });


        sessionManager.saveisPlayingOrPaused(true);
        startService(serviceIntent);
        SeekbarHandler();


    }


    public void ResumeMediaPlayerActivity(){

        SeekbarHandler();
        if (serviceHelper.backgroundService.mediaPlayer.isPlaying()) {

            mediaPlayerBinding.btnPlayPauseIcon.setImageResource(R.drawable.baseline_pause_24_white);

        } else {

            mediaPlayerBinding.btnPlayPauseIcon.setImageResource(R.drawable.baseline_play_arrow_white);

        }

        int totalDuration = service.mediaPlayer.getDuration();
        int progress = service.mediaPlayer.getCurrentPosition() * 100 / totalDuration + 1;
        mediaPlayerBinding.seekbar.setProgress(progress);
        String Filename = sessionManager.getfilename();
        mediaPlayerBinding.txttitle.setText(Filename);
        String totalDurationString = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(totalDuration), TimeUnit.MILLISECONDS.toSeconds(totalDuration));
        mediaPlayerBinding.txttotalduration.setText(totalDurationString);

        int currentPosition = service.mediaPlayer.getCurrentPosition();
        int totalduration = service.mediaPlayer.getDuration();

        int remainingDuration = totalduration - currentPosition;

        String formatcurrentDuration = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(remainingDuration), TimeUnit.MILLISECONDS.toSeconds(remainingDuration));




        mediaPlayerBinding.txtrunningduration.setText(formatcurrentDuration);

        //onPlay Pause

        mediaPlayerBinding.btnPlaypauseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (serviceHelper.backgroundService.mediaPlayer.isPlaying()) {

                    mediaPlayerBinding.btnPlayPauseIcon.setImageResource(R.drawable.baseline_play_arrow_white);
                    UpdateNotificationFromActivity("MEDIA_NOTIFICATION_PLAY_PAUSE");
                    sessionManager.saveisPlayingOrPaused(false);
                } else {

                    mediaPlayerBinding.btnPlayPauseIcon.setImageResource(R.drawable.baseline_pause_24_white);
                    UpdateNotificationFromActivity("MEDIA_NOTIFICATION_PLAY_PAUSE");
                    sessionManager.saveisPlayingOrPaused(true);
                }
            }
        });

    }

    public void stopservice() {

        // unbindService(serviceConnection);
        stopService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        //stopservice();

    }

    private void startSeekBarUpdateFromActivity() {
        // Start updating the SeekBar from the service
        Intent intent = new Intent("START_PAUSE_SEEK_BAR_UPDATE");
        intent.putExtra("command", "play");
        sendBroadcast(intent);
    }

    // Call this when pausing media playback
    private void pauseSeekBarUpdateFromActivity() {
        // Pause updating the SeekBar from the service
        Intent intent = new Intent("START_PAUSE_SEEK_BAR_UPDATE");
        intent.putExtra("command", "pause");
        sendBroadcast(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("SendDuration");
        // filter.addAction("RemainingDuration");
        filter.addAction("SeekBarUpdate");
        filter.addAction("Media_Notification_service");
        registerReceiver(broadcastReceiver, filter);

    }

    public void SeekbarHandler(){
        mediaPlayerBinding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (b){


                    Log.d("seek",String.valueOf(i));
                    sendUpdateBroadcast(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    public void UpdateNotificationFromActivity(String action){

        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public void sendUpdateBroadcast(int progress){

        Intent intent = new Intent("MediaPlayerSeekTo");
        intent.putExtra("Seekprogress",progress);
        sendBroadcast(intent);
    }



}