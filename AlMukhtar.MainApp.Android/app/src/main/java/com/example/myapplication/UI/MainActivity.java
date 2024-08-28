package com.example.myapplication.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.Adapters.Adapter;
import com.example.myapplication.Helper.ServiceHelper;
import com.example.myapplication.Helper.ServiceUtils;
import com.example.myapplication.Models.ListModel;
import com.example.myapplication.R;
import com.example.myapplication.Session.SessionManager;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.service.BackgroundService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    ActivityMainBinding activityMainBinding;
    Boolean isreplayOptionVisible = true;
    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Handle Media Notification Click changes to Activity UI
            if (intent.getAction().equals("Media_Notification_service")) {

                String command = intent.getStringExtra("command");
//                String filename = intent.getStringExtra("meta_name");
//                String imagpath = intent.getStringExtra("meta_image");
                Log.d("TAG", command);


                if (command.equals("Media_notification_service_pause")) {

                    activityMainBinding.btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);

                }

                if (command.equals("Media_notification_service_play")) {

                    activityMainBinding.btnPlayPause.setImageResource(R.drawable.baseline_pause_24);



                }


            }

            if (intent.getAction().equals("SeekBarUpdate")) {

                Boolean state = intent.getBooleanExtra("state",false);
                Log.d("TAG",String.valueOf(state));

                if (state){

                    activityMainBinding.btnPlayPause.setImageResource(R.drawable.baseline_replay_24);
                    isreplayOptionVisible =true;
                }
            }


        }
    };
    Adapter adapter;
    MediaPlayerActivity mediaPlayerActivity;
    int MY_PERMISSION_REQUEST_CODE = 12;
    SessionManager sessionManager;

    public ServiceHelper serviceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());


        //Load UI
        LoadUI();

        //Load RecyclerView
        LoadRecyclerView();

        //Create Notification Channel if app version is equal or greater to 8.0
        CreateNotificationChannel();

        mediaPlayerActivity = new MediaPlayerActivity();

        checkPermission();

        sessionManager = new SessionManager(this);



        serviceHelper = new ServiceHelper(this);


        activityMainBinding.btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (serviceHelper.backgroundService.mediaPlayer.isPlaying()) {

                    activityMainBinding.btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);
                    UpdateNotificationFromActivity("MEDIA_NOTIFICATION_PLAY_PAUSE");
                    sessionManager.saveisPlayingOrPaused(true);
                } else {

                    activityMainBinding.btnPlayPause.setImageResource(R.drawable.baseline_pause_24);
                    UpdateNotificationFromActivity("MEDIA_NOTIFICATION_PLAY_PAUSE");
                    sessionManager.saveisPlayingOrPaused(false);
                }


                if (isreplayOptionVisible) {

                    Intent replayintent = new Intent("MEDIA_REPLAY");
                    sendBroadcast(replayintent);
                    isreplayOptionVisible = false;
                }
            }
        });


    }

    public void UpdateNotificationFromActivity(String action){

        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    public void LoadUI() {

        activityMainBinding.chipgroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, int checkedId) {

                Chip checkedchip = (Chip) group.findViewById(checkedId);


                if (checkedchip != null && checkedchip.getText().toString().equals("Search")) {
                    // Show the search view
                    AnimateViewChanges(activityMainBinding.searchviewcard);
                    activityMainBinding.txtheading.setText(getString(R.string.search));
                    Log.d("TAG", "worked if");
                } else {
                    // Hide the search view
                    AnimateViewChangesGone(activityMainBinding.searchviewcard);
                    activityMainBinding.txtheading.setText(getString(R.string.all));
                    Log.d("TAG", "worked else");
                }


            }
        });

    }

    public void LoadRecyclerView() {

        ArrayList<ListModel> arrayList = new ArrayList<>();

        arrayList.add(new ListModel("Audio01", "Artist", "url"));
        arrayList.add(new ListModel("Audio04", "Artist", "url"));
        arrayList.add(new ListModel("Audio03", "Artist", "url"));
        arrayList.add(new ListModel("Audio04", "Artist", "url"));
        adapter = new Adapter(this, arrayList);
        activityMainBinding.recyclerlist.setAdapter(adapter);


    }

    public void AnimateViewChanges(View myView) {

        Animator animator = ObjectAnimator.ofFloat(myView, "alpha", 0.0f, 1.0f);

        // Set the duration and properties of the animation.
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());

        // Start the animation.
        animator.start();

        // In the onAnimationEnd() callback, set the visibility of the view to VISIBLE.
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                myView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void AnimateViewChangesGone(View myView) {

        Animator animator = ObjectAnimator.ofFloat(myView, "alpha", 1.0f, 0.0f);

        // Set the duration and properties of the animation.
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());

        // Start the animation.
        animator.start();

        // In the onAnimationEnd() callback, set the visibility of the view to GONE.
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                myView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(MediaPlayerActivity.serviceIntent);
        sessionManager.saveisPlayingState(false);


    }

    public void CreateNotificationChannel() {
        // Create a notification channel.
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("myapp01", "BayaanApp", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("This is a notification channel BayaanApp.");

            // Register the notification channel with the system.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
            // You can proceed with the operation that requires this permission
        } else {
            // Permission is not granted
            // You should request the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        MY_PERMISSION_REQUEST_CODE);
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("Media_Notification_service");
        filter.addAction("SeekBarUpdate");
        registerReceiver(broadcastReceiver, filter);
        checkifAudioisPlaying();
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }




    public void checkifAudioisPlaying() {

        if (sessionManager.getPlayingState()) {

            activityMainBinding.miniPlayer.setVisibility(View.VISIBLE);
            ArrayList<String> metaList = sessionManager.getPlayingFileMeta();

//            SwipeDownTouchListener swipeDownTouchListener = new SwipeDownTouchListener(this);
//            activityMainBinding.miniPlayer.setOnTouchListener(swipeDownTouchListener);

            String fileName = metaList.get(0);
            String duration = metaList.get(1);
            String image = metaList.get(2);


            activityMainBinding.playingfileName.setText(fileName);

            if (sessionManager.getPlayingorPaused()){

                activityMainBinding.btnPlayPause.setImageResource(R.drawable.baseline_pause_24);
            }
            else{

                activityMainBinding.btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);
            }


        } else {

            activityMainBinding.miniPlayer.setVisibility(View.GONE);
        }

        activityMainBinding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                sessionManager.saveisPlayingState(false);
                activityMainBinding.miniPlayer.setVisibility(View.GONE);
                if(ServiceUtils.isServiceRunning(MainActivity.this, BackgroundService.class)){

                    stopService(MediaPlayerActivity.serviceIntent);
                }


            }
        });


        activityMainBinding.miniPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                resumeMediaPlayerActivity();
            }
        });
    }




    public void resumeMediaPlayerActivity(){

        Intent intent = new Intent(MainActivity.this,MediaPlayerActivity.class);
        intent.putExtra("MediaState","Resume");
        ImageView imageView = findViewById(R.id.playingFileimage);
        TextView textView = findViewById(R.id.playingfileName);
        Pair<View, String> pair1 = Pair.create(imageView, "image");
        Pair<View, String> pair2 = Pair.create(textView, "playingfileName");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,pair1,pair2);
        startActivity(intent, options.toBundle());


    }





}