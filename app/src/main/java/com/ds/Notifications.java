package com.ds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class Notifications extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private View header;
    String name_extra = "name_extra";
    String ip_extra = "ip_extra";
    String username, ip;
    TextView txtuser
            ,txtip;

    Client client;
    ListView notificationsLV;
    ArrayAdapter<String> adapter;
    ArrayList<String> notifications;
    LinearLayout linearLayout;
    SwipeRefreshLayout refreshLayout;
    private static final String FRIEND_REQUEST_RESPONSE = "friend_request_response";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        client = ((MyApp) getApplication()).getClient();
        username = client.getProfile().getUsername();
        ip = this.getIntent().getStringExtra(ip_extra);
        //get header of navigation view

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view_menu);

        header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        setNav();
        linearLayout = findViewById(R.id.notifications_linearLayout);
        notificationsLV = findViewById(R.id.notifications_lv);
        refreshLayout = findViewById(R.id.notifications_swiperefresh);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("Notification","Notification",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        client = ((MyApp)getApplication()).getClient();

        notifications = client.getNotifications();
        if(notifications.size()>0){
            NotificationCompat.Builder builder = new NotificationCompat.Builder(Notifications.this,"Notification")
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle("Friend Request Recieved !")
                    .setContentText("Wow someone wants to be your friend")
                    .setAutoCancel(true);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(Notifications.this);
            managerCompat.notify(1, builder.build());
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notifications);
        notificationsLV.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        adapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }
                }
        );

        notificationsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = notifications.get(position).substring(0, notifications.get(position).indexOf(" "));
                if (notifications.get(position).contains("friend request")){
                    AlertDialog alertDialog = new AlertDialog.Builder(Notifications.this)
                            //set icon
                            .setIcon(R.drawable.ic_taxi)
                            //set title
                            .setTitle("Manage friend request")
                            //set message
                            .setMessage("Accept friend request from " + name + "?")
                            //set positive button
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //set what would happen when positive button is clicked
                                    Snackbar.make(linearLayout, name + " is now your friend!", Snackbar.LENGTH_SHORT).show();
                                    new HandleFriendRequestTask().execute(name, "yes", String.valueOf(position));
                                }
                            })
                            //set negative button
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //set what should happen when negative button is clicked
                                    Snackbar.make(linearLayout, "Rejected " + name + "'s friend request", Snackbar.LENGTH_SHORT).show();
                                    new HandleFriendRequestTask().execute(name, "no", String.valueOf(position));
                                }
                            })
                            .show();
                }
            }
        });
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
    private class HandleFriendRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String name = strings[0];
            String answer = strings[1];
            if (answer.equalsIgnoreCase("yes")) {
                client.getProfile().addFriend(name);
            }
            client.push(new com.ds.Value(FRIEND_REQUEST_RESPONSE.toUpperCase()));
            client.push(new Value(FRIEND_REQUEST_RESPONSE + " " + name + " " + answer));
            return strings[2];
        }

        @Override
        protected void onPostExecute(String s) {
            client.getProfile().removeNotification(Integer.parseInt(s));
            adapter.notifyDataSetChanged();
        }
    }


    //nav bar setup
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
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
            case R.id.nav_chat:
                startChat();
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
        Intent intent = new Intent(Notifications.this , ProfileActivity.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);

    }

    public void startStories(){
        Intent intent = new Intent(Notifications.this , Stories.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startBlockedUsers(){
        Intent intent = new Intent(Notifications.this , BlockedUsers.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startFriends(){
        Intent intent = new Intent(Notifications.this , Friends.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startChat(){
        Intent intent = new Intent(Notifications.this , MainMenu.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }
    public void startLogOut() {
        new MainMenu.CloseClientTask().execute(client);
        Intent intent = new Intent(Notifications.this, LogIn.class);
        Toast.makeText(Notifications.this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}