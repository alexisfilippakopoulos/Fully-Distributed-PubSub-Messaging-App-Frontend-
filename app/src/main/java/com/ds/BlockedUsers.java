package com.ds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class BlockedUsers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, PopupMenu.OnMenuItemClickListener{
    private DrawerLayout drawerLayout;
    private View header;
    private Menu menu;
    String name_extra = "name_extra";
    String ip_extra = "ip_extra";
    String username, ip;
    TextView txtuser, txtip;

    Button blockUser_btn;
    EditText blockUser_editTxt;
    ListView blockedUsersLV;
    ArrayAdapter<String> adapter;
    ArrayList<String> blockedUsers;
    int pos;

    Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        client = ((MyApp) getApplication()).getClient();
        username = client.getProfile().getUsername();
        ip = this.getIntent().getStringExtra(ip_extra);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_profile);

        header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);

        setNav();

        blockUser_btn = findViewById(R.id.block_user_btn);
        blockUser_btn.setEnabled(false);
        blockUser_editTxt = findViewById(R.id.block_user_editTxt);
        blockUser_editTxt.addTextChangedListener(blockUserTextWatcher);

        blockedUsersLV = findViewById(R.id.blocked_users_lv);
    }

    private TextWatcher blockUserTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String userToBlock = blockUser_editTxt.getText().toString().trim();
            blockUser_btn.setEnabled(!userToBlock.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onStart() {
        client = ((MyApp)getApplication()).getClient();

        blockedUsers = client.getProfile().getBlockList();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, blockedUsers);
        blockedUsersLV.setAdapter(adapter);

        super.onStart();

        //BLOCK BUTTON ACTION
        blockUser_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userToBlock = blockUser_editTxt.getText().toString();
                // run async task with client object
                if (userToBlock.equalsIgnoreCase("") || userToBlock.trim().length() == 0) {
                    Toast.makeText(BlockedUsers.this,
                            "Invalid username",
                            Toast.LENGTH_SHORT).show();
                } else if(blockedUsers.contains(userToBlock)) {
                    Toast.makeText(BlockedUsers.this,
                                    "The user " + userToBlock + "has already been blocked",
                                    Toast.LENGTH_SHORT).show();
                } else if (client.getUsername().equalsIgnoreCase(userToBlock)) {
                    Toast.makeText(BlockedUsers.this,
                                    "Cannot block yourself",
                                    Toast.LENGTH_SHORT).show();
                } else {
                    new BlockUserTask().execute(userToBlock);
                }
            }
        });
        //CLICK LIST VIEW ITEM ACTION
        blockedUsersLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PopupMenu popup = new PopupMenu(BlockedUsers.this, view);
                popup.setOnMenuItemClickListener(BlockedUsers.this);
                popup.inflate(R.menu.popup_unblock_user_menu);
                popup.show();
                pos=i;
            }
        });

    }
    //MENU UNBLOCK BUTTON ACTION
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.unblock_user_menuBtn:
                //if user is blocked -> unblock else -> Toast
                String name = blockedUsers.get(pos);
                if (client.getProfile().getBlockList().contains(name)) {
                    new UnlockUserTask().execute(name);
                } else {
                    Toast.makeText(this,
                            name + " is not block",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return false;
        }
        return true;
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
    class BlockUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            Value v = new Value("BLOCK_USER");
            v.setCommand(true);
            client.push(v);
            client.push(new Value(strings[0]));

            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            client.getProfile().blockUser(s);
            adapter.notifyDataSetChanged();
            blockUser_editTxt.setText("");
            Toast.makeText(BlockedUsers.this,
                    "Blocked user " + s,
                    Toast.LENGTH_SHORT).show();
        }
    }

    class UnlockUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            Value v = new Value("UNBLOCK_USER");
            v.setCommand(true);
            client.push(v);
            client.push(new Value(strings[0]));

            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            client.getProfile().removeBlockedUser(s);
            adapter.notifyDataSetChanged();
            blockUser_editTxt.setText("");
            Toast.makeText(BlockedUsers.this,
                    "Unlocked user " + s,
                    Toast.LENGTH_SHORT).show();
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
            case R.id.nav_friends:
                startFriends();
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
        Intent intent = new Intent(BlockedUsers.this, MainMenu.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startStories() {
        Intent intent = new Intent(BlockedUsers.this, Stories.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startFriends() {
        Intent intent = new Intent(BlockedUsers.this, Friends.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startProfile() {
        Intent intent = new Intent(BlockedUsers.this, ProfileActivity.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startNotifications() {
        Intent intent = new Intent(BlockedUsers.this, Notifications.class);
        intent.putExtra(name_extra, username);
        intent.putExtra(ip_extra, ip);
        startActivity(intent);
    }

    public void startLogOut() {
        new MainMenu.CloseClientTask().execute(client);
        Intent intent = new Intent(BlockedUsers.this, LogIn.class);
        Toast.makeText(BlockedUsers.this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

}