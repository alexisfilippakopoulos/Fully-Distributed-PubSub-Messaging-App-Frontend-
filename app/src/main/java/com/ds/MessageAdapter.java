package com.ds;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static  final int MSG_TYPE_LEFT = 0;
    public static  final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private ArrayList<Value> mChat;
    private String username, savedMediaPath;
    private String nickname;



    public MessageAdapter(Context mContext, ArrayList<Value> mChat, String username, String savedMediaPath, String nickname){
        this.mContext = mContext;
        this.mChat = mChat;
        System.out.println("sssssSSDSDADASD");
        System.out.println(mChat);
        this.username = username;
        this.savedMediaPath = savedMediaPath;
        this.nickname = nickname;

    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Value chat = mChat.get(position);
        //text message
        if (chat.getMessage() != null){
            holder.show_message.setText(chat.getMessage());
        }
        //media
        if (chat.getMultiMediaFile() != null) {
            holder.show_message.setVisibility(View.GONE);
            MultimediaFile media = chat.getMultiMediaFile();
            //image
            if (!media.getFileExtension().equals("mp4")) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(media.getMultimediaFileChunk(), 0, (int) media.getLength());
                holder.mmf_image.getLayoutParams().width = bitmap.getWidth();
                holder.mmf_image.getLayoutParams().height = bitmap.getHeight();
                holder.mmf_image.setImageBitmap(bitmap);
                holder.mmf_image.setVisibility(View.VISIBLE);
            }
            //video
            else {
                holder.playVideoBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, PlayVideoActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("username", chat.getSender());
                        intent.putExtra("videoPath", savedMediaPath + media.getFileName());
                        mContext.startActivity(intent);
                    }
                });
                holder.playVideoBtn.setVisibility(View.VISIBLE);
            }
        }
        holder.txt_seen.setText(chat.getSender());
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public ImageView profile_image, mmf_image;
        public TextView txt_seen;
        public Button playVideoBtn;
        //public VideoView videoView;

        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            mmf_image = itemView.findViewById(R.id.chat_img);
            playVideoBtn = itemView.findViewById(R.id.play_video_btn);

            //videoView = itemView.findViewById(R.id.chat_video);
        }
    }

    @Override
    public int getItemViewType(int position) {
        //mChat.get(position).getSender().equals(nickname) ||
        System.out.println(mChat);
        if (mChat.get(position).getSender().equals(username)){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}