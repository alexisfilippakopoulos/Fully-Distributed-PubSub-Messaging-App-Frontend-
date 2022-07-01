package com.ds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;

public class Friends extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private View header;
    private Menu menu;
    String name_extra = "name_extra";
    String ip_extra = "ip_extra";
    String username, ip;
    TextView txtuser, txtip;
    private static final String FRIEND_REQUEST_RESPONSE = "friend_request_response";


    Button addFriend_btn;
    EditText addFriend_editTxt;
    ListView friendsLV;
    SwipeRefreshLayout refreshLayout;
    ArrayAdapter<String> adapter;
    ArrayList<String> friends;

    Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        client = ((MyApp) getApplication()).getClient();
        //username = client.getProfile().getUsername();
        ip = this.getIntent().getStringExtra(ip_extra);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_profile);

        header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);

        setNav();

        addFriend_btn = findViewById(R.id.add_friend_btn);
        addFriend_btn.setEnabled(false);
        addFriend_editTxt = findViewById(R.id.add_friend_editTxt);
        addFriend_editTxt.addTextChangedListener(addFriendTextWatcher);

        friendsLV = findViewById(R.id.friends_lv);
        refreshLayout = findViewById(R.id.friends_swiperefresh);
    }
    private TextWatcher addFriendTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String friendToAdd =  addFriend_editTxt.getText().toString().trim();
            addFriend_btn.setEnabled(!friendToAdd.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        client = ((MyApp) getApplication()).getClient();
        friends = client.getProfile().getFriendsList();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, friends);
        friendsLV.setAdapter(adapter);

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        adapter.notifyDataSetChanged();
                        refreshLayout.setRefreshing(false);
                    }
                }
        );

        addFriend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userToAdd = addFriend_editTxt.getText().toString();
                // run async task with client object
                if (userToAdd.equalsIgnoreCase("") || userToAdd.trim().length() == 0) {
                    Toast.makeText(Friends.this,
                                "Invalid username",
                                    Toast.LENGTH_SHORT).show();
                } else if (client.getProfile().getFriendRequestsSent().contains(userToAdd)) {
                    Toast.makeText(Friends.this,
                            "Request has already been sent to " + userToAdd,
                            Toast.LENGTH_SHORT).show();
                }
                //check if he/she is already in friends list
                else if (client.getProfile().getFriendsList().contains(userToAdd)) {
                    Toast.makeText(Friends.this,
                            userToAdd + " is already in friends list",
                            Toast.LENGTH_SHORT).show();
                }
                else if (userToAdd.equalsIgnoreCase(client.getUsername())) {
                    Toast.makeText(Friends.this,
                            "Cannot send friend request to yourself!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    new AddFriendTask().execute(userToAdd);
                    Toast.makeText(Friends.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                }
                addFriend_editTxt.setText("");
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
    class AddFriendTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            Value v = new Value("FRIEND_REQUEST");
            v.setCommand(true);
            client.push(v);
            client.push(new Value(strings[0]));
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            client.getProfile().addFriendRequest(s);
        }
    }




    //nav bar setup
    public void setNav() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_profile);
        header = navigationView.getHeaderView(0);
        menu = navigationView.getMenu();

        txtuser = ((TextView) header.findViewById(R.id.navbar_username));
        txtip = ((TextView) header.findViewById(R.id.navbar_userIP));
        txtuser.setText(username);
        txtip.setText(ip);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_chat:
                startChat();
                break;
            case R.id.nav_stories:
                startStories();
                break;
            case R.id.nav_blocked_users:
                startBlockedUsers();
                break;
            case R.id.nav_profile:
                startProfile();
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

    public void startChat() {
        Intent intent = new Intent(Friends.this, MainMenu.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startStories() {
        Intent intent = new Intent(Friends.this, Stories.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startBlockedUsers() {
        Intent intent = new Intent(Friends.this, BlockedUsers.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }
    public void startProfile() {
        Intent intent = new Intent(Friends.this, ProfileActivity.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startNotifications() {
        Intent intent = new Intent(Friends.this, Notifications.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startLogOut() {
        new MainMenu.CloseClientTask().execute(client);
        Intent intent = new Intent(Friends.this, LogIn.class);
        Toast.makeText(Friends.this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }
}
