package com.ds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class PlayVideoActivity extends AppCompatActivity {

    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();

        videoView = findViewById(R.id.play_video_view);
        if (!(((MyApp)getApplication()).getClient().getUsername().equals(intent.getStringExtra("username")))){
            videoView.setVideoURI(Uri.fromFile(new File(intent.getStringExtra("videoPath"))));
        }else {
            videoView.setVideoURI(((MyApp)getApplication()).getVideoUri());
        }
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
    }
}