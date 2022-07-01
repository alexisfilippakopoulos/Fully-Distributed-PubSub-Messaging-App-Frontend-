package com.ds;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class LogIn extends AppCompatActivity {

    Button next_button;
    String name, ip, bio;
    String name_extra = "name_extra";
    String ip_extra = "ip_extra";

    EditText name_view;
    EditText ip_view;
    EditText bio_view;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        setTitle("Log In");
        next_button = (Button) findViewById(R.id.log_in_button);

        name_view = (EditText) findViewById(R.id.log_in_name);
        ip_view = (EditText) findViewById(R.id.log_in_ip);
        bio_view = (EditText) findViewById(R.id.log_in_bio);

        name_view.addTextChangedListener(loginTextWatcher);
        ip_view.addTextChangedListener(loginTextWatcher);
        bio_view.addTextChangedListener(loginTextWatcher);
        next_button.setEnabled(false);
        next_button.setOnClickListener(v -> this.startMenuPage());
    }
     private TextWatcher loginTextWatcher = new TextWatcher() {

         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
         }

         @Override
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             String usernameInput = name_view.getText().toString().trim();
             String bioInput = bio_view.getText().toString().trim();
             String ipInput = ip_view.getText().toString().trim();
             next_button.setEnabled(!usernameInput.isEmpty() && !bioInput.isEmpty() && !ipInput.isEmpty());
         }

         @Override
         public void afterTextChanged(Editable editable) {

         }
     };



    public void startMenuPage(){
        Toast.makeText(this,
                        "Logged In",
                        Toast.LENGTH_SHORT)
                .show();
        Intent intent = new Intent(this,MainMenu.class);
        name = name_view.getText().toString();
        ip = ip_view.getText().toString();
        bio = bio_view.getText().toString();
        intent.putExtra(name_extra,name);
        intent.putExtra(ip_extra,ip);
        new InitClientObject().execute();
        startActivity(intent);
    }

    class InitClientObject extends AsyncTask<Void, Void, Client> {

        private final ProgressDialog progressDialog = new ProgressDialog(LogIn.this);
        @Override
        protected void onPreExecute() {

            this.progressDialog.setMessage("Processing...");
            this.progressDialog.show();
        }

        @Override
        protected Client doInBackground(Void... strings) {
            try {
                String[] info = ip.split(":");
                Socket socket = new Socket(info[0], Integer.parseInt(info[1]));
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                oos.writeObject(name);
                Client c;
                c = new Client(socket, name, ois, oos);
                c.setCurrentBroker(ip);
                c.init();
                c.listenForMessage();
                c.setContext(getApplicationContext());
                c.getProfile().setBio(bio);
                return c;
            } catch (Exception e) {
                Log.d("TAG", "doInBackground: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Client client) {
            ((MyApp)getApplication()).setClient(client);
            progressDialog.dismiss();
        }
    }
    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}