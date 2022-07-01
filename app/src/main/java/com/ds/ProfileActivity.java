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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawerLayout;
    private View header;
    private Menu menu;

    String name_extra = "name_extra";
    String ip_extra = "ip_extra";
    String username, ip;
    Date dateAccountCreated;
    MultimediaFile profilePic;
    String bio;
    HashMap<String, ArrayList<Value>> userVideoFilesMap;

    Button editUsername_btn;
    Button editBio_btn;

    TextView usernameDisplay;
    TextView bioDisplay;
    TextView ipDisplay;
    TextView dateDisplayed;

    EditText editUsernameET;
    EditText editBioET;

    /*
    todo
    PROFILE PIC
    DATE ACCOUNT CREATED
    bio
    ip port
    BUTTONS
    EDIT PROFILE P THA EXEI META
    EDIT BIO
    EDIT USERNAME
     */

    TextView txtuser
            ,txtip;

    Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        client = ((MyApp)getApplication()).getClient();
        //username = client.getProfile().getUsername();

        ip = this.getIntent().getStringExtra(ip_extra);

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view_profile);

        header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);

        setNav();

        editUsername_btn = findViewById(R.id.edit_username_button);
        editBio_btn = findViewById(R.id.edit_bio_button);
        editUsername_btn.setEnabled(false);
        editBio_btn.setEnabled(false);



        TextView ipDisplay = (TextView)findViewById(R.id.getIP);
        ipDisplay.setText(ip);

        editUsernameET = findViewById(R.id.editUsernameText);
        editBioET = findViewById(R.id.editBioText);

        editUsernameET.addTextChangedListener(editUsername);
        editBioET.addTextChangedListener(editBIO);



    }
    private TextWatcher editUsername = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
           String editU = editUsernameET.getText().toString().trim();
           editUsername_btn.setEnabled(!editU.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private TextWatcher editBIO = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String editB = editBioET.getText().toString().trim();
            editUsername_btn.setEnabled(!editB.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    @Override
    protected void onStart() {
        client = ((MyApp)getApplication()).getClient();

        username = client.getProfile().getUsername();
        profilePic = client.getProfile().getProfilePic();
        bio = client.getProfile().getBio();
        dateAccountCreated = client.getProfile().getDateAccountCreated();
        userVideoFilesMap = client.getProfile().getUserVideoFilesMap();

        TextView bioDisplay = (TextView)findViewById(R.id.getBio);
        bioDisplay.setText(client.getProfile().getBio());

        TextView dateDisplay = (TextView)findViewById(R.id.getDateAcc);
        dateDisplay.setText(dateAccountCreated.toString());

        TextView usernameDisplay = (TextView)findViewById(R.id.getUsername);
        usernameDisplay.setText(username);

        super.onStart();

        editUsername_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newUsername = editUsernameET.getText().toString();
                if(newUsername.equals(username)){
                    Toast.makeText(ProfileActivity.this,
                            "You already have that username.",
                            Toast.LENGTH_SHORT).show();
                }else if (newUsername.isEmpty()) {
                    Toast.makeText(ProfileActivity.this,
                            "You must have a username.",
                            Toast.LENGTH_SHORT).show();
                }else{
                    new EditUsernameTask().execute(newUsername);
                }
            }
        });

        editBio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newBio = editBioET.getText().toString();
                if(newBio.equals(bio)){
                    Toast.makeText(ProfileActivity.this,
                            "You already have that bio.",
                            Toast.LENGTH_SHORT).show();
                }else if(newBio.isEmpty()) {
                    Toast.makeText(ProfileActivity.this,
                            "You must have a bio.",
                            Toast.LENGTH_SHORT).show();
                }else{
                    new EditBioTask().execute(newBio);
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    class EditBioTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            client.getProfile().setBio(strings[0]);
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(ProfileActivity.this,
                    "Successfully changed bio to " + s,
                    Toast.LENGTH_SHORT).show();
        }
    }

    class EditUsernameTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            client.getProfile().setUsername(strings[0]);
            return strings[0];
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(ProfileActivity.this,
                    "Successfully changed username to " + s,
                    Toast.LENGTH_SHORT).show();
        }
    }




    //set up nav bar
    public void setNav(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view_profile);
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
        switch (item.getItemId()){
            case R.id.nav_chat:
                startChat();
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

    public void startChat(){
        Intent intent = new Intent(ProfileActivity.this,MainMenu.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }
    public void startStories(){
        Intent intent = new Intent(ProfileActivity.this,Stories.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startBlockedUsers(){
        Intent intent = new Intent(ProfileActivity.this,BlockedUsers.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startFriends(){
        Intent intent = new Intent(ProfileActivity.this,Friends.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }
    public void startNotifications(){
        Intent intent = new Intent(ProfileActivity.this,Notifications.class);
        intent.putExtra(name_extra,username);
        intent.putExtra(ip_extra,ip);
        startActivity(intent);
    }

    public void startLogOut() {
        new MainMenu.CloseClientTask().execute(client);
        Intent intent = new Intent(ProfileActivity.this, LogIn.class);
        Toast.makeText(ProfileActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }


}