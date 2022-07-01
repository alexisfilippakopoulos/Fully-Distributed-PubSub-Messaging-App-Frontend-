package com.ds;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Base64;

public class ChatRoomActivity extends AppCompatActivity {
    String topicName;
    ImageButton sendMsgBtn, galleryBtn,secretChatBtn,openCameraBtn,editTopicImageBtn;
    MultimediaFile topicIconMMF, MMFToSend;
    CircularImageView activityChatIcon;
    EditText sendMsgTxt,editTopicNameTxt,editUserNicknameNew;
    Client client;
    Button editTopicBtn,editTopicNicknameBtn,editUserNicknameBtn;
    MessageAdapter messageAdapter;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Intent intent = getIntent();
        topicName = intent.getStringExtra("topicName");

        Toolbar toolbar = findViewById(R.id.chat_room_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(topicName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endChatSession();
            }
        });

        recyclerView = findViewById(R.id.chat_room_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        sendMsgBtn = findViewById(R.id.chat_room_msgBtn);
        sendMsgTxt = findViewById(R.id.chat_room_msgTxt);
        galleryBtn = findViewById(R.id.chat_room_mediaBtn);
        secretChatBtn = findViewById(R.id.chat_room_secretChatBtn);
        editTopicBtn = findViewById(R.id.edit_topic);
        openCameraBtn = findViewById(R.id.open_camera_btn);

        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 101);
            }
        });

        client = ((MyApp)getApplication()).getClient();
        new InitChatTask().execute(topicName);
        activityChatIcon = findViewById(R.id.chat_room_title_image);
        topicIconMMF = client.getProfile().getSubbedTopicsImages().get(client.getSubbedTopics().indexOf(topicName));
        Icon icon = Icon.createWithData(topicIconMMF.getMultimediaFileChunk(), 0, (int)topicIconMMF.getLength());
        activityChatIcon.setImageIcon(icon);

        messageAdapter = new MessageAdapter(getApplicationContext(), client.getChatMessages(), client.getUsername(), client.getSavedMedia(), client.getNicknames().get(topicName));
        recyclerView.setAdapter(messageAdapter);

    }



    @Override
    protected void onStart() {
        super.onStart();
        messageAdapter = new MessageAdapter(getApplicationContext(), client.getChatMessages(), client.getUsername(), client.getSavedMedia(), client.getNicknames().get(topicName));
        messageAdapter.notifyDataSetChanged();
        topicName = client.getSubbedTopics().get(client.getSubbedTopics().indexOf(topicName));
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = sendMsgTxt.getText().toString();

                if (!message.equals("") && !message.trim().equals("")){
                    new SendMessageTask().execute(new Value(message));
                }
                if (MMFToSend != null) {
                    new SendMessageTask().execute(new Value(MMFToSend));
                    MMFToSend = null;
                }
            }
        });

        editTopicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(ChatRoomActivity.this);
                dialog.setTitle("Edit topic properties");
                dialog.setContentView(R.layout.edit_topic_box_layout);

                editTopicNicknameBtn = dialog.findViewById(R.id.editTopicName_btn);
                editTopicNameTxt = dialog.findViewById(R.id.editTopicName_edtTxt);
                editTopicImageBtn = dialog.findViewById(R.id.editTopicImageBtn);
                dialog.show();
                editTopicNicknameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newTopicName = editTopicNameTxt.getText().toString();
                        editTopicNameTxt.setText("");
                        new EditTopicNameTask().execute(newTopicName);

                    }
                });

                editUserNicknameBtn = dialog.findViewById(R.id.editUserNickname_btn);
                editUserNicknameNew = dialog.findViewById(R.id.editUserNicknameEnd_edtTxt);
                editUserNicknameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newNickname = editUserNicknameNew.getText().toString();
                        client.getNicknames().put(topicName,newNickname);
                        editUserNicknameNew.setText("");
                        new EditNicknameTask().execute(newNickname);
                    }
                });
                editTopicImageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/* video/*");
                        startActivityForResult(intent, 5);
                    }
                });
            }
        });

        secretChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!client.getSecretToggle()){
                    Toast.makeText(ChatRoomActivity.this,
                            "You have entered secret chat mode.",
                            Toast.LENGTH_SHORT).show();
                    Toast.makeText(ChatRoomActivity.this,
                            "All the messages from now on will be deleted when you exit the chat",
                            Toast.LENGTH_SHORT).show();
                    Toast.makeText(ChatRoomActivity.this,
                            "To revert it back to normal just press the button again. ",
                            Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ChatRoomActivity.this,
                            "You have exited secret chat mode.",
                            Toast.LENGTH_SHORT).show();
                }
                new SecretChatTask().execute();

            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/* video/*");
                startActivityForResult(intent, 3);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
            MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),bitmap,String.valueOf(System.currentTimeMillis()),null);
            Toast.makeText(this,"Image saved successfully",Toast.LENGTH_SHORT).show();
        } else if (requestCode==3 && resultCode == RESULT_OK && data != null) {
            Uri selectedFile = data.getData();
            String fileExtension = getFileName(selectedFile);
            fileExtension = fileExtension.substring(fileExtension.lastIndexOf(".")+1);
            System.out.println(fileExtension);
            System.out.println(selectedFile);
            System.out.println(selectedFile.getPath());
            try {
                if (!fileExtension.equalsIgnoreCase("mp4")){
                    new GenerateImageFileTask().execute(selectedFile);
                } else {
                    new GenerateVideoFileTask().execute(selectedFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(requestCode==5 && resultCode == RESULT_OK && data != null){
            Uri selectedFile = data.getData();
            String fileExtension = getFileName(selectedFile);
            fileExtension = fileExtension.substring(fileExtension.lastIndexOf(".")+1);

            try{
                if (!fileExtension.equalsIgnoreCase("mp4")) {
                    new GenerateTopicImageFileTask().execute(selectedFile);
                }
            }catch (Exception e){
                e.printStackTrace();
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
        super.onBackPressed();
        endChatSession();
    }

    //Async Tasks
    private class GenerateTopicImageFileTask extends AsyncTask<Uri, Void, MultimediaFile> {

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
            new EditTopicImageTask().execute(multimediaFile);
        }
    }
    private class EditTopicImageTask extends AsyncTask<MultimediaFile, Void, Void> {
        @Override
        protected Void doInBackground(MultimediaFile... multimediaFiles) {
            MultimediaFile topic = multimediaFiles[0];
            Value v = new Value("upload_topic_image");
            v.setCommand(true);
            client.push(v);
            client.push(new Value(topic));
            client.getProfile().getSubbedTopicsImages().set(client.getSubbedTopics().indexOf(topicName),topic);
            return null;
        }


    }
    private class EditNicknameTask extends AsyncTask <String,Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            Value v = new Value("set_nickname" + " " + strings[0]);
            v.setCommand(true);
            client.push(v);
            return null;
        }
    }

    private class GenerateImageFileTask extends AsyncTask<Uri, Void, MultimediaFile> {
        private final ProgressDialog progressDialog = new ProgressDialog(ChatRoomActivity.this);
        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Processing Image...");
            this.progressDialog.show();
        }

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
            System.out.println(multimediaFile);
            MMFToSend = multimediaFile;
            progressDialog.dismiss();
        }
    }

    private class GenerateVideoFileTask extends AsyncTask<Uri, Void, MultimediaFile> {
        private final ProgressDialog progressDialog = new ProgressDialog(ChatRoomActivity.this);
        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Processing Video...");
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
                ((MyApp)getApplication()).setVideoUri(selectedFile);
                return videoToSend;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MultimediaFile multimediaFile) {
            System.out.println(multimediaFile);
            MMFToSend = multimediaFile;
            progressDialog.dismiss();
        }
    }

    private class EditTopicNameTask extends AsyncTask <String,Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            System.out.println("New Topic Name Input" + strings[0]);
            Value v = new Value("set_topic_name" + " " + strings[0]);
            v.setCommand(true);
            client.push(v);
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            client.getSubbedTopics().set(client.getSubbedTopics().indexOf(topicName), s);
        }
    }

    private class SecretChatTask extends AsyncTask <Void,Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            client.setSecretToggle(!client.getSecretToggle());
            Value v = new Value("secret_chat");
            v.setCommand(true);
            client.push(v);
            return null;
        }
    }

    private class InitChatTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            client.push(new Value("MESSAGE"));
            client.push(new Value(strings[0]));  //push topicName
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            messageAdapter.notifyDataSetChanged();
        }
    }

    private class QuitTopicTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Value v = new Value("quit_topic");
            v.setCommand(true);
            client.push(v);
            return null;
        }
    }

    private class SendMessageTask extends AsyncTask<Value, Void, Void> {
        @Override
        protected Void doInBackground(Value... values) {
            Value msg = values[0];
            client.push(msg);
            client.addMsgToChat(msg);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            sendMsgTxt.setText("");
            messageAdapter.notifyDataSetChanged();
        }
    }

    //methods
    private void endChatSession() {
        System.out.println(client.getChatMessages());
        client.setChatMessages(new ArrayList<>());
        new QuitTopicTask().execute();
        finish();
    }


}