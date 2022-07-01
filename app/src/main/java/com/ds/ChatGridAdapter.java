package com.ds;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

public class ChatGridAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> topicNames;
    ArrayList<MultimediaFile> topicImages;

    LayoutInflater inflater;

    public ChatGridAdapter(Context context, ArrayList<String> imageName, ArrayList<MultimediaFile> images) {
        this.context = context;
        this.topicNames = imageName;
        this.topicImages = images;
    }

    public void addItem(String topicName, Integer topicImage) {

    }

    @Override
    public int getCount() {
        return topicImages.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null){
            convertView = inflater.inflate(R.layout.grid_layout,null);
        }

        ImageView imageView = convertView.findViewById(R.id.chat_grid_image);
        TextView textView = convertView.findViewById(R.id.chat_name);
        byte[] mmf_chunk = topicImages.get(position).getMultimediaFileChunk();
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(mmf_chunk, 0, mmf_chunk.length));
        textView.setText(topicNames.get(position));

        return convertView;
    }
}
