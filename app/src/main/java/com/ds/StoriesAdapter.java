package com.ds;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.io.File;
import java.util.ArrayList;


public class StoriesAdapter extends PagerAdapter {

    private Context context;
    ArrayList<Uri> files;

    StoriesAdapter(Context context, ArrayList<Uri> files) {
        this.context = context;
        this.files = files;
    }

    public void setFilesList(ArrayList<Uri> files) {
        this.files = files;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        String uri = files.get(position).toString();
        String extension = uri.substring(uri.lastIndexOf("."));
        System.out.println("EDWS");
        System.out.println(uri);
        if (!extension.equalsIgnoreCase(".mp4")){
            System.out.println("FTIAXNW EIKONA STO STORY");
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageURI(files.get(position));
            container.addView(imageView, 0);
            return imageView;
        } else {
            System.out.println("FTIAXNW VIDEO STO STORY");
            VideoView videoView = new VideoView(context);
            videoView.setVideoURI(files.get(position));
            MediaController mediaController = new MediaController(context);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
            container.addView(videoView, 0);
            return videoView;
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if (object instanceof ImageView) {
            container.removeView((ImageView)object);
        } else {
            container.removeView((VideoView)object);
        }
    }
}
