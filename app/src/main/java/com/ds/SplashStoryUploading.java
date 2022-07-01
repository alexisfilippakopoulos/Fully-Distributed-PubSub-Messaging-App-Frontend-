package com.ds;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SplashStoryUploading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_upload);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashStoryUploading.this, Stories.class);
                Toast.makeText(SplashStoryUploading.this, "Story uploaded successfully", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
            }
        },2500);
    }
}