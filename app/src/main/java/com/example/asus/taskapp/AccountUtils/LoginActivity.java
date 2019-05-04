package com.example.asus.taskapp.AccountUtils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.UserAvailable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    public TextInputEditText nameTeks , passwordTeks;
    public Button btnLogin;
    public TextView daftar_teks;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    public boolean isMarshmallow = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    public File defaultFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"task_images");
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);
        nameTeks = findViewById(R.id.nameTxt);
        passwordTeks = findViewById(R.id.passwordTxt);
        btnLogin = findViewById(R.id.btnLogin);
        daftar_teks = findViewById(R.id.daftar_teks);
        if(isMarshmallow){
            if(ActivityCompat.checkSelfPermission(LoginActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(LoginActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(LoginActivity.this,new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },100);
            }
        }
        preferences = getSharedPreferences("user_data",0);
        int id = preferences.getInt("id",0);
        String token = preferences.getString("token",null);
        String image_path = preferences.getString("image_path",null);
        String name = preferences.getString("name",null);
        if(id != 0 && token != null && image_path != null && name != null){
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        if(!defaultFile.exists()){
            defaultFile.mkdir();
        }
        File file = new File(defaultFile + "/blank_image.png");
        if(!file.exists()){
            try {
                file.createNewFile();
                @SuppressLint("ResourceType") InputStream inStream = getResources().openRawResource(R.mipmap.ic_persons);
                OutputStream stream = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[2048];
                while((len = inStream.read(buffer)) > 0){
                    stream.write(buffer , 0 ,len);
                }
                stream.flush();
                stream.close();
                inStream.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        File files = new File(defaultFile + "/books_image.png");
        if(!files.exists()){
            try {
                files.createNewFile();
                @SuppressLint("ResourceType") InputStream inStreams = getResources().openRawResource(R.mipmap.ic_books);
                OutputStream outStream = new FileOutputStream(files);
                int len;
                byte[] buffer = new byte[1024];
                while((len = inStreams.read(buffer)) > 0){
                    outStream.write(buffer , 0 , len);
                }
                outStream.flush();
                outStream.close();
                inStreams.close();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nameTeks.getText().toString() == null){
                    nameTeks.requestFocus();
                    nameTeks.setHighlightColor(getResources().getColor(android.R.color.holo_red_dark));
                }
                if(passwordTeks.getText().toString() == null){
                    passwordTeks.requestFocus();
                    passwordTeks.setHighlightColor(getResources().getColor(android.R.color.holo_red_dark));
                }
                if(nameTeks.getText() != null && passwordTeks.getText() != null){
                    JSONObject object = new JSONObject();
                    try {
                        object.put("name",nameTeks.getText());
                        object.put("password",passwordTeks.getText());
                    } catch(JSONException e){
                        e.printStackTrace();
                    }
                    LoginAsync login;
                    login = new LoginAsync(object);
                    login.execute();
                }
            }
        });
        daftar_teks.setMovementMethod(LinkMovementMethod.getInstance());
        daftar_teks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                intent.putExtra("is_register",true);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("status","permission granted");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public class LoginAsync extends AsyncTask<String , Void , String> {
        public JSONObject object = null;
        public SharedPreferences preferences;
        public SharedPreferences.Editor editor;
        public ProgressDialog dialog = null;
        public LoginAsync(JSONObject object){
            this.object = object;
        }

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setTitle("Status");
            dialog.setMessage("Wait a second....");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            int responseCode = 0;
            try {
                URL url = new URL(new Config().getServerAddress() + "/login");
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept","application/json");
                connection.setRequestProperty("Content-Type","application/json");
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(object.toString());
                writer.flush();
                responseCode = connection.getResponseCode();
                if(responseCode == 200){
                    StringBuilder sb = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = "";
                    while((line = reader.readLine()) != null){
                        sb.append(line);
                    }
                    JSONObject objects = new JSONObject(sb.toString());
                    return objects.toString();
                }
            } catch(JSONException e){
                e.printStackTrace();
            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return "no data";
        }

        @Override
        protected void onPostExecute(String integer) {
            dialog.dismiss();
            if(integer != "no data"){
                try{
                    JSONObject object = new JSONObject(integer);
                    preferences = getSharedPreferences("user_data",0);
                    editor = preferences.edit();
                    editor.putInt("id",object.getInt("id"));
                    editor.putString("name",object.getString("name"));
                    editor.putString("token",object.getString("token"));
                    editor.putString("image_path",object.getString("image_path"));
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch(JSONException e){
                    e.printStackTrace();
                }

            } else if(integer == "no data") {
                int red_color = getResources().getColor(android.R.color.holo_red_dark);
                nameTeks.setHighlightColor(red_color);
                passwordTeks.setHighlightColor(red_color);
                nameTeks.requestFocus();
            }
            super.onPostExecute(integer);
        }
    }
}
