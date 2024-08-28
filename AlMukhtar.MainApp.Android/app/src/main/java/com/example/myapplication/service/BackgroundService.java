package com.example.myapplication.service;

import static android.content.Intent.getIntent;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.SeekBar;


import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.example.myapplication.UI.MediaPlayerActivity;

import java.io.Console;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class BackgroundService extends Service {

    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static MediaPlayer mediaPlayer;
    private final IBinder binder = new MyBinder();
    public String duration;
    MediaPlayerActivity mediaPlayerActivity;
    Thread seekbarThread;
    Thread remainingDurationThread;
    String filename;
    String subtext;
    NotificationManager mNotificationManager;
    SessionManager sessionManager;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private MediaSessionCompat mediaSession;
    private boolean isPlaying = false;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("START_PAUSE_SEEK_BAR_UPDATE")) {
                String command = intent.getStringExtra("command");


                if (command.equals("play")) {

                    play();
                } else {

                    pause();

                }
            }
        }
    };
    private BroadcastReceiver mediareceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals("MEDIA_NOTIFICATION_PLAY")) {
                // Pause the music
                pauseFromNotification();

            }

            if (action.equals("MEDIA_NOTIFICATION_PAUSE")) {

                //Play the Music
                playFromNotification();
            }


            if (action.equals("MEDIA_NOTIFICATION_PLAY_PAUSE")) {
                Log.d("MediaPlayer", String.valueOf(mediaPlayer.isPlaying()));

                if (mediaPlayer.isPlaying()) {


                    pauseFromNotification();
                    UpdateNotification("Paused", R.drawable.baseline_play_arrow_white, filename, subtext);

                } else {


                    playFromNotification();
                    UpdateNotification("Playing", R.drawable.baseline_pause_24_white, filename, subtext);
                }
            }

            if (action.equals("MediaPlayerSeekTo")) {

                int mediaSeekTo = intent.getIntExtra("Seekprogress", 0);
                Log.d("seekService", String.valueOf(mediaSeekTo));
                int _progress = (mediaSeekTo * mediaPlayer.getDuration()) / 100;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    mediaPlayer.seekTo(_progress, MediaPlayer.SEEK_CLOSEST);
                } else {
                    mediaPlayer.seekTo(_progress);
                }

            }



            if (action.equals("MEDIA_REPLAY_Notification")) {

                play();
                mediaPlayer.seekTo(0);
                UpdateNotification("Playing", R.drawable.baseline_pause_24_white, filename, subtext);
                playFromNotification();

            }
        }
    };

    public void pauseFromNotification() {


        pause();
        Log.d("media", "from notification");
        Intent intent = new Intent("Media_Notification_service");
        intent.putExtra("command", "Media_notification_service_pause");
        sendBroadcast(intent);


    }

    public void playFromNotification() {

        play();
        Log.d("media", "play notification");
        Intent intent = new Intent("Media_Notification_service");
        intent.putExtra("command", "Media_notification_service_play");
        sendBroadcast(intent);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String mediaSource = intent.getStringExtra("media_source");
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.file_example);
        filename = intent.getStringExtra("filename");
        subtext = intent.getStringExtra("subtext");


        // Set the MediaPlayer object to play the audio file
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                //Play the music
                play();
                sessionManager.saveisPlayingState(true);


            }
        });


        //handle notification controls

        handleControls();


        // Start the MediaPlayer object


        Log.d("service", "Service started");


        SetTotalDuration();

        //   startForeground(1,createMediaNotification().build());
        //   createMediaNotification2();
        // startForeground(1, createMediaNotification(filename, subtext).build());
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, createMediaNotification(filename, subtext).build());


        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayerActivity = new MediaPlayerActivity();
        Log.d("service", "Service created");
        IntentFilter filter = new IntentFilter("START_PAUSE_SEEK_BAR_UPDATE");
        filter.addAction("MEDIA_NOTIFICATION_PLAY");
        filter.addAction("MEDIA_NOTIFICATION_PAUSE");
        filter.addAction("MEDIA_NOTIFICATION_PLAY_PAUSE");
        filter.addAction("MediaPlayerSeekTo");
        filter.addAction("MEDIA_REPLAY_Notification");
        registerReceiver(broadcastReceiver, filter);
        registerReceiver(mediareceiver, filter);
        sessionManager = new SessionManager(this);

    }

    public NotificationCompat.Builder createMediaNotification(String filename, String subtext) {


        sessionManager.PlayingFileMeta(filename, String.valueOf(mediaPlayer.getDuration()), "image");
        // Create a MediaSessionCompat object.
        mediaSession = new MediaSessionCompat(this, "My Media Session");
        mediaSession.setCallback(new MediaSessionCallback());
        mediaSession.setActive(true);

        // Create a MediaStyle object and set the MediaSessionCompat object on it.
        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken());

        // Create a NotificationCompat.Builder object and set the MediaStyle object on it.
        notificationBuilder = new NotificationCompat.Builder(this, "myapp01").setStyle(mediaStyle);

        // Set the properties of the notification.
        notificationBuilder.setContentTitle(filename);
        notificationBuilder.setContentText(subtext);
        notificationBuilder.setSmallIcon(android.R.drawable.ic_notification_clear_all).setSilent(true);

        // Add actions to the notification.

        Intent pauseIntent = new Intent("MEDIA_NOTIFICATION_PLAY");
        Intent playIntent = new Intent("MEDIA_NOTIFICATION_PAUSE");
        Intent playpause = new Intent("MEDIA_NOTIFICATION_PLAY_PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, 2, playpause, PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder.setOngoing(true);
        notificationBuilder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "MEDIA_NOTIFICATION_PLAY_PAUSE", playPausePendingIntent));


        // Build the notification and show it.
        notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//
//            PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
//                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1.0f)
//                    .build();
//            mediaSession.setPlaybackState(playbackState);
//
//// Set the duration of the media.
//            MediaMetadataCompat mediaMetadata = new MediaMetadataCompat.Builder()
//                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration())
//                    .build();
//            mediaSession.setMetadata(mediaMetadata);
//
//
//        }

        return notificationBuilder;

    }


    public void UpdateNotification(String message, int drawable, String filename, String subtext) {
        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken());

        // Create a NotificationCompat.Builder object and set the MediaStyle object on it.
        notificationBuilder = new NotificationCompat.Builder(this, "myapp01").setStyle(mediaStyle);

        // Set the properties of the notification.
        notificationBuilder.setContentTitle(filename);
        notificationBuilder.setContentText(message);
        notificationBuilder.setSmallIcon(android.R.drawable.ic_notification_clear_all).setSilent(true);
        notificationBuilder.setOngoing(true);
        // Add actions to the notification.

        Intent pauseIntent = new Intent("MEDIA_NOTIFICATION_PLAY");
        Intent playIntent = new Intent("MEDIA_NOTIFICATION_PAUSE");
        Intent playpause = new Intent("MEDIA_NOTIFICATION_PLAY_PAUSE");
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 1, playIntent, PendingIntent.FLAG_IMMUTABLE);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, 2, playpause, PendingIntent.FLAG_IMMUTABLE);


        notificationBuilder.addAction(new NotificationCompat.Action(drawable, "MEDIA_NOTIFICATION_PLAY_PAUSE", playPausePendingIntent));
//

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//
//            PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
//                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1.0f)
//                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                    .build();
//            mediaSession.setPlaybackState(playbackState);
//
//// Set the duration of the media.
//            MediaMetadataCompat mediaMetadata = new MediaMetadataCompat.Builder()
//                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration())
//                    .build();
//            mediaSession.setMetadata(mediaMetadata);
//
//
//// Set other content and actions as needed
//
//            // Build the notification and show it.
//
//
//        }


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notificationBuilder.build());
    }


    public void UpdateReplayNotification(String message, int drawable, String filename, String subtext) {
        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken());

        // Create a NotificationCompat.Builder object and set the MediaStyle object on it.
        notificationBuilder = new NotificationCompat.Builder(this, "myapp01").setStyle(mediaStyle);

        // Set the properties of the notification.
        notificationBuilder.setContentTitle(filename);
        notificationBuilder.setContentText(message);
        notificationBuilder.setSmallIcon(android.R.drawable.ic_notification_clear_all).setSilent(true);
        notificationBuilder.setOngoing(true);
        // Add actions to the notification.

        Intent playpause = new Intent("MEDIA_REPLAY_Notification");
        PendingIntent replayPendingintent = PendingIntent.getBroadcast(this, 3, playpause, PendingIntent.FLAG_IMMUTABLE);


        notificationBuilder.addAction(new NotificationCompat.Action(drawable, "replay", replayPendingintent));


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notificationBuilder.build());
    }


    public void handleControls() {


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //release media
        mediaPlayer.release();

        // Release the MediaSessionCompat object.
        mediaSession.release();

        //stop seekbar Update Thread
        try {
            stopSeekBarUpdate();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(mediareceiver);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();

        sessionManager.saveisPlayingState(false);

        Log.d("service", "Service destroyed");


    }

    public void play() {
        mediaPlayer.start();
        startSeekBarUpdate();
        //startRemainingTimeUpdate();
        isPlaying = true;
    }

    public void pause() {
        mediaPlayer.pause();
        isPlaying = false;
    }


    private void stopSeekBarUpdate() throws InterruptedException {
        isPlaying = false;
        if (seekbarThread.isAlive()) {

            try {
                // Wait for the thread to finish
                seekbarThread.join();
            } catch (InterruptedException e) {
                // Handle the InterruptedException
                Thread.currentThread().interrupt();
            }
        }
    }

    private void stopRemainingDurationUpdate() throws InterruptedException {
        // Interrupt the thread to stop it
        if (remainingDurationThread != null && remainingDurationThread.isAlive()) {
            remainingDurationThread.interrupt();
        }
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void SetTotalDuration() {
        int totalDuration = mediaPlayer.getDuration();

        String totalDurationString = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(totalDuration), TimeUnit.MILLISECONDS.toSeconds(totalDuration));

        duration = totalDurationString;

        Intent intent = new Intent("SendDuration");
        intent.putExtra("mediaduration", totalDurationString);
        sendBroadcast(intent);

    }


    private void startSeekBarUpdate() {

        seekbarThread = new Thread(this::updateSeekBar);
        seekbarThread.start();


    }

    private void startRemainingTimeUpdate() {

        remainingDurationThread = new Thread(this::updateCurrentDuration);
        remainingDurationThread.start();

    }

    private void updateSeekBar() {

        while (isPlaying) {

                int progress = mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration() + 1;

                int currentPosition = mediaPlayer.getCurrentPosition();
                int totalduration = mediaPlayer.getDuration();

                int remainingDuration = totalduration - currentPosition;
                String formatcurrentDuration = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(remainingDuration), TimeUnit.MILLISECONDS.toSeconds(remainingDuration));

                Intent intent = new Intent("SeekBarUpdate");
                intent.putExtra("progress", progress);
                intent.putExtra("remaining", formatcurrentDuration);


                if (progress >= 99) {
                    intent.putExtra("state", true);
                    UpdateReplayNotification("Replay", R.drawable.baseline_replay_24_white, filename, subtext);

                } else {

                    intent.putExtra("state", false);
                }


                sendBroadcast(intent);
                Log.d("position", String.valueOf(progress));
                Log.d("remaining", formatcurrentDuration);
                try {
                    Thread.sleep(1000);  // Update every second (adjust as needed)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

        }
    }


    private void updateCurrentDuration() {

        while (isPlaying) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int totalduration = mediaPlayer.getDuration();

                int remainingDuration = totalduration - currentPosition;
                String formatcurrentDuration = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(remainingDuration), TimeUnit.MILLISECONDS.toSeconds(remainingDuration));
                Intent intent = new Intent("RemainingDuration");
                intent.putExtra("remaining", formatcurrentDuration);

                sendBroadcast(intent);
//                try {
//                    Thread.sleep(1000);  // Update every second (adjust as needed)
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }


            }
        }
    }

    public void mediaCompleted(Boolean state) {

        Intent intent = new Intent("isMediaCompleted");
        intent.putExtra("state", state);
        sendBroadcast(intent);
    }

    public void checkIsMediaCompleted() {
        int currentPlaybackPosition = mediaPlayer.getCurrentPosition();


        int mediaDuration = mediaPlayer.getDuration();


        if (currentPlaybackPosition >= mediaDuration) {
            mediaCompleted(true);

        } else {

            mediaCompleted(false);
        }
    }

    public class MyBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            // Handle play action
            Log.d("TAG", "onplay");
        }

        @Override
        public void onPause() {
            // Handle pause action
            Log.d("TAG", "onpaused");
        }

        @Override
        public void onSkipToNext() {
            // Handle skip to next action
        }

        @Override
        public void onSkipToPrevious() {
            // Handle skip to previous action
        }
    }





}
