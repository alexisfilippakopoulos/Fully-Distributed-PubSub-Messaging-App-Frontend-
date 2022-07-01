package com.ds;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

public class Stories extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private View header;
    String name_extra = "name_extra";
    String ip_extra = "ip_extra";
    String username, ip;
    TextView txtuser
            ,txtip;

    Client client;
    ViewPager viewPager;
    StoriesAdapter storiesAdapter;
    FloatingActionButton uploadStoryBtn;
    SwipeRefreshLayout refreshLayout;
    ImageButton openCameraBtn;
    private int requestCode;
    private int resultCode;
    private Intent data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stories);

        client = ((MyApp) getApplication()).getClient();
        username = client.getProfile().getUsername();
        ip = this.getIntent().getStringExtra(ip_extra);
        //get header of navigation view
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view_123);


        header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        setNav();

        viewPager = findViewById(R.id.stories_viewPager);

        uploadStoryBtn = findViewById(R.id.upload_story_btn);
        refreshLayout = findViewById(R.id.stories_swiperefresh);
        openCameraBtn = findViewById(R.id.open_camera_btn);

        if(ContextCompat.checkSelfPermission(Stories.this,
                Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Stories.this,
                    new String[]{Manifest.permission.CAMERA},101);
        }


        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 101);
            }
        });

        client = ((MyApp) getApplication()).getClient();
        new GetStoriesTask().execute();

    }


    @Override
    protected void onStart() {
        super.onStart();

        uploadStoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/* video/*");
                startActivityForResult(intent, 3);
            }
        });

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new GetStoriesTask().execute();
                        storiesAdapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
            byte[] byte_arr = bytes.toByteArray();
            MultimediaFile imageToSend = new MultimediaFile(byte_arr, "temp.bmp", client.getUsername());
            new UploadStoryTask().execute(imageToSend);
            //MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),bitmap,String.valueOf(System.currentTimeMillis()),null);
            //Toast.makeText(this,"Image saved successfully",Toast.LENGTH_SHORT).show();

        }else if(requestCode == 3){
            if (resultCode == RESULT_OK && data != null) {
                Uri selectedFile = data.getData();
                String fileExtension = getFileName(selectedFile);
                fileExtension = fileExtension.substring(fileExtension.lastIndexOf(".")+1);
                try {
                    if (!fileExtension.equalsIgnoreCase("mp4")) {
                        new GenerateImageFileTask().execute(selectedFile);
//                        InputStream inputStream = getContentResolver().openInputStream(selectedFile);
//                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                        byte[] byte_arr = stream.toByteArray();
//                        MultimediaFile story = new MultimediaFile(byte_arr, getFileName(selectedFile), client.getUsername());
//                        new UploadStoryTask().execute(story);
                    } else {
                        new GenerateVideoFileTask().execute(selectedFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Async Tasks
    private class UploadStoryTask extends AsyncTask<MultimediaFile, Void, Void> {
        @Override
        protected Void doInBackground(MultimediaFile... multimediaFiles) {
            MultimediaFile story = multimediaFiles[0];
            story.setExpiryDate(new Date(new Date().getTime()+ client.getStory_deletion_delay()));
            //client.saveFile(story, client.getStoryPath());
            client.getProfile().addStory(story);
            client.push(new Value("UPLOAD_STORY"));
            client.push(new Value(story));
            return null;
        }
        @Override
        protected void onPostExecute(Void unused){
            Intent intent = new Intent(Stories.this,SplashStoryUploading.class);
            startActivity(intent);
        }

    }


    private class GetStoriesTask extends AsyncTask<Void, Void, Void> {
        private final ProgressDialog progressDialog = new ProgressDialog(Stories.this);
        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Fetching...");
            this.progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            client.push(new Value("VIEW_STORIES"));
            if (client.get_stories_count() != -1) {
                while (client.get_stories_count() != 0) {
                    if (client.get_stories_count() == 0) {
                        break;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            File[] files = new File(client.getOthersStoriesPath()).listFiles();
            System.out.println(files.length);
            ArrayList<Uri> files_uri = new ArrayList<>();
            for (File f : files) {
                files_uri.add(Uri.fromFile(f));
            }
            System.out.println(files);
            storiesAdapter = new StoriesAdapter(Stories.this, files_uri);
            viewPager.setAdapter(storiesAdapter);
            storiesAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        }
    }

    private class GenerateImageFileTask extends AsyncTask<Uri, Void, MultimediaFile> {

        @Override
        protected MultimediaFile doInBackground(Uri... uris) {
            try {
                Uri selectedFile = uris[0];
                InputStream inputStream = getContentResolver().openInputStream(selectedFile);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byte_arr = stream.toByteArray();

                MultimediaFile imageToSend = new MultimediaFile(byte_arr, getFileName(selectedFile), client.getUsername());
                return imageToSend;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MultimediaFile multimediaFile) {
            new UploadStoryTask().execute(multimediaFile);
        }
    }

    private class GenerateVideoFileTask extends AsyncTask<Uri, Void, MultimediaFile> {
        private final ProgressDialog progressDialog = new ProgressDialog(Stories.this);
        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Processing...");
            this.progressDialog.show();
        }

        @Override
        protected MultimediaFile doInBackground(Uri... uris) {
            try {
                Uri selectedFile = uris[0];
                InputStream inputStream = getContentResolver().openInputStream(selectedFile);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                AssetFileDescriptor fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(selectedFile, "r");
                long fileSize = fileDescriptor.getLength();
                byte[] video_bytes = new byte[(int) fileSize];

                System.out.println(fileSize);

                while ((nRead = inputStream.read(video_bytes, 0, video_bytes.length)) != -1) {
                    buffer.write(video_bytes, 0, nRead);
                }
                MultimediaFile videoToSend = new MultimediaFile(video_bytes, getFileName(selectedFile), client.getUsername());
                return videoToSend;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MultimediaFile multimediaFile) {
            new UploadStoryTask().execute(multimediaFile);
            System.out.println(multimediaFile);
            progressDialog.dismiss();
        }
    }


    //nav bar setup
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_profile:
                startProfile();
                break;
            case R.id.nav_chat:
                starMainMenu();
                break;
            case R.id.nav_blocked_users:
                starBlockedUsers();
                break;
            case R.id.nav_friends:
                startFriends();
                break;
            case R.id.nav_notifications:
                startNotifications();
                break;
            case R.id.log_out:
                startLogOut();
                break;
            default:
                break;

        }
        return true;
    }

    public void setNav(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        //set user,ip strings
        txtuser = ((TextView) header.findViewById(R.id.navbar_username));
        txtip = ((TextView) header.findViewById(R.id.navbar_userIP));
        txtuser.setText(username);
        txtip.setText(ip);

        toggle.syncState();
    }
    public void startProfile(){
        Intent intent = new Intent(Stories.this , ProfileActivity.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);

    }

    public void starMainMenu(){
        Intent intent = new Intent(Stories.this , MainMenu.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void starBlockedUsers(){
        Intent intent = new Intent(Stories.this , BlockedUsers.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startFriends(){
        Intent intent = new Intent(Stories.this , Friends.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);

    }

    public void startNotifications(){
        Intent intent = new Intent(Stories.this , Notifications.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startLogOut() {
        new MainMenu.CloseClientTask().execute(client);
        Intent intent = new Intent(Stories.this, LogIn.class);
        Toast.makeText(Stories.this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}