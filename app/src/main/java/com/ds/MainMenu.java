package com.ds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.ds.databinding.ActivityMainMenuBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/*TODO:
*  story loads after two refreshes + fix aspect ratio
*  large images/video loading + image quality on bitmap
*  secret chat crashed the other user?
*  try with more than one brokers
*  story camera button
*  chat camera button
*  image size in
*/


public class MainMenu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PopupMenu.OnMenuItemClickListener {
    public final String TAG = "MainMenu";
    private DrawerLayout drawerLayout;
    private View header;
    String name_extra = "name_extra";
    String ip_extra = "ip_extra";
    String username, ip;
    TextView txtuser, txtip;
    String topicName_to_unsub;

    int logout_counter;
    ArrayList<String> topicNames = new ArrayList<>();
    ArrayList<MultimediaFile> topicImages = new ArrayList<>();

    public static Client client;
    ActivityMainMenuBinding binding;
    ChatGridAdapter gridAdapter;
    FloatingActionButton addChatBtn;
    Button createChatBtn;
    EditText chatToCreateEditTxt;
    ArrayList<String> availableTopics;
    ArrayList<MultimediaFile> availableTopicsImages;
    ArrayAdapter<String> availableTopicsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //client = ((MyApp) getApplication()).getClient();
        //username = client.getProfile().getUsername();

        ip = this.getIntent().getStringExtra(ip_extra);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_menu);

        header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        setNav();

        addChatBtn = findViewById(R.id.add_chat_btn);

    }


    @Override
    protected void onStart() {
        super.onStart();
        logout_counter = 0;

        //get client
        client = ((MyApp) getApplication()).getClient();
        topicNames = client.getSubbedTopics();
        topicImages = client.getProfile().getSubbedTopicsImages();

        //set up to grid view
        gridAdapter = new ChatGridAdapter(MainMenu.this, topicNames, topicImages);
        binding.chatsGridView.setAdapter(gridAdapter);
        username = client.getProfile().getUsername();


        //GIA TO MEGALO ARXIKO KOYMPI
        addChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                Dialog dialog = new Dialog(MainMenu.this);
                dialog.setTitle("Create or Subscribe to chat");
                dialog.setContentView(R.layout.add_chat_dialog_box_layout);

                //get topics from broker and show them, and if clicked -> subscribe
                new GetAvailableTopics().execute(dialog);


                //views for topic creation
                createChatBtn = dialog.findViewById(R.id.create_chat_Btn);
                chatToCreateEditTxt = dialog.findViewById(R.id.create_chat_editTxt);

                //GIA NA KANEIS CREATE TOPIC
                createChatBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String chatName = chatToCreateEditTxt.getText().toString();
                        if (client.getSubbedTopics().contains(chatName)) {
                            Snackbar.make(findViewById(R.id.main_menu_LL), "Already subscribed", Snackbar.LENGTH_SHORT).show();
                            chatToCreateEditTxt.setText("");
                        } else {
                            new CreateTopicTask().execute(chatName);
                            chatToCreateEditTxt.setText("");
                        }
                    }
                });
            }
        });


        //ENTER CHAT BUTTON
        binding.chatsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainMenu.this, ChatRoomActivity.class);
                intent.putExtra("topicName", topicNames.get(position));
                startActivity(intent);
            }
        });

        binding.chatsGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                topicName_to_unsub = client.getSubbedTopics().get(position);
                showChatManagementMenu(view);
                return false;
            }
        });

        gridAdapter.notifyDataSetChanged();
    } //onStart

    @Override
    public void onBackPressed() {
        logout_counter++;
        if (logout_counter == 1) {
            Toast.makeText(MainMenu.this, "Press back again to logout", Toast.LENGTH_SHORT).show();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    logout_counter = 0;
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 1000 * 2);  // 2 sec
        }
        if (logout_counter == 2) {
            startLogOut();
        }
    }//onBackPressed

    private void showChatManagementMenu(View v) {
        PopupMenu popup = new PopupMenu(MainMenu.this, v);
        popup.setOnMenuItemClickListener(MainMenu.this);
        popup.inflate(R.menu.popup_manage_chat);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.unsubscribe_chat_menuBtn:
                new UnsubscribeTask().execute(topicName_to_unsub);
                break;
            default:
                return false;
        }
        return true;
    }

    //Async Tasks
    private class GetAvailableTopics extends AsyncTask<Dialog, Void, Dialog> {
        private final ProgressDialog progressDialog = new ProgressDialog(MainMenu.this);

        @Override
        protected void onPreExecute() {
            this.progressDialog.setMessage("Fetching...");
            this.progressDialog.show();
        }

        @Override
        protected Dialog doInBackground(Dialog... dialogs) {
            client.push(new com.ds.Value("GET_TOPICS"));
            while (client.getRetrieved_topics() == null || client.getRetrieved_topic_images() == null) {
                if (client.getRetrieved_topics() != null && client.getRetrieved_topic_images() != null) {  //wait for topics names and images retrieval
                    break;
                }
            }
            return dialogs[0];
        }

        @Override
        protected void onPostExecute(Dialog dialog) {
            availableTopics = client.getRetrieved_topics();
            availableTopicsImages = client.getRetrieved_topic_images();

            ListView retrievedTopicsList = dialog.findViewById(R.id.retrieved_topics_LV);
            availableTopicsAdapter = new ArrayAdapter<>(dialog.getContext(), android.R.layout.simple_list_item_1, availableTopics);
            retrievedTopicsList.setAdapter(availableTopicsAdapter);
            availableTopicsAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
            dialog.show();

            //GIA NA KANW SUBSCRIBE
            retrievedTopicsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String topicName = availableTopics.get(position);
                    if (!client.getSubbedTopics().contains(topicName)) {
                        new SubscribeToTopicTask().execute(position);
                    } else {
                        Snackbar.make(findViewById(R.id.main_menu_LL), "Already subscribed", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });// Set button for sub

        }
    } // GetAvailableTopics

    private class CreateTopicTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String topicName = strings[0];
            client.register(topicName, client.getDefaultTopicImage());
            return topicName;
        }

        @Override
        protected void onPostExecute(String s) {
            gridAdapter.notifyDataSetChanged();
        }
    }

    private class SubscribeToTopicTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... integers) {
            String topicName = availableTopics.get(integers[0]);
            client.sub(topicName, availableTopicsImages.get(integers[0]));
            return topicName;
        }

        @Override
        protected void onPostExecute(String topic) {
            gridAdapter.notifyDataSetChanged();

        }
    }

    private class UnsubscribeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String topic_to_unsub = strings[0];
            client.unsubscribe(topic_to_unsub);
            return topic_to_unsub;
        }

        @Override
        protected void onPostExecute(String s) {
            gridAdapter.notifyDataSetChanged();
            Toast.makeText(MainMenu.this, "Exit topic " + s, Toast.LENGTH_SHORT).show();
        }
    }


    //nav bar setup
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                startProfile();
                break;
            case R.id.nav_stories:
                startStories();
                break;
            case R.id.nav_blocked_users:
                startBlockedUsers();
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

    public void setNav() {
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

    public void startProfile() {
        Intent intent = new Intent(MainMenu.this, ProfileActivity.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);

    }

    public void startStories() {
        Intent intent = new Intent(MainMenu.this, Stories.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startBlockedUsers() {
        Intent intent = new Intent(MainMenu.this, BlockedUsers.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startFriends() {
        Intent intent = new Intent(MainMenu.this, Friends.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startNotifications() {
        Intent intent = new Intent(MainMenu.this, Notifications.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startLogOut() {
        new CloseClientTask().execute(client);
        Intent intent = new Intent(MainMenu.this, LogIn.class);
        Toast.makeText(MainMenu.this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

    static class CloseClientTask extends AsyncTask<Client, Void, Void> {
        @Override
        protected Void doInBackground(Client... clients) {
            clients[0].closeEverything();
            return null;
        }
    }

}