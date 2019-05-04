package com.example.asus.taskapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.asus.taskapp.AccountUtils.LoginActivity;
import com.example.asus.taskapp.AccountUtils.ProfileActivity;
import com.example.asus.taskapp.Fragments.AddFragment;
import com.example.asus.taskapp.Fragments.HomeFragment;
import com.example.asus.taskapp.Fragments.SoldFragment;
import com.example.asus.taskapp.Model.Users;
import com.example.asus.taskapp.Utils.FetchData;
import com.example.asus.taskapp.Utils.UserAvailable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    public BottomNavigationView navigationView;
    public ImageView profileImage;
    public SharedPreferences preferences;
    public String image = null;
    public File defaultFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"task_images");
    public Toolbar toolbar;
    public FetchData fetchData;
    public int id = 0;
    public String token = null;
    public static MainActivity ma;
    public SharedPreferences.Editor editor;
    public Fragment activeFragmnet = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(new UserAvailable(MainActivity.this).check_user_exist() == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Akun Anda Telah Dihapus / Ada Kesalahan Aplikasi \n Anda Akan Logout Otomatis");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    preferences = getSharedPreferences("user_data",0);
                    editor = preferences.edit();
                    editor.clear();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        ma = this;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferences = getSharedPreferences("user_data",0);
        id = preferences.getInt("id",0);
        String name = preferences.getString("name",null);
        if(name != null){
            toolbar.setTitle(name);
        } else {
            toolbar.setTitle("POB");
        }
        image = preferences.getString("image_path",null);
        token = preferences.getString("token",null);
        Log.d("id",String.valueOf(id));
        profileImage = findViewById(R.id.profile_activity);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        navigationView = findViewById(R.id.navigationView);
        navigationView.setOnNavigationItemSelectedListener(this);
        if(!defaultFile.exists()){
            defaultFile.mkdir();
        }
        File file = new File(defaultFile + "/blank_image.png");
        if(!file.exists()){
            try {
                @SuppressLint("ResourceType") InputStream inStream = getResources().openRawResource(R.drawable.ic_user_default);
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
        load_new_data();
        open_fragment(R.id.container_fragment , new HomeFragment());
    }
    public void load_new_data(){
        new UserAvailable(MainActivity.this).check_user_exist();
        fetchData = new FetchData(MainActivity.this,new Config().getServerAddress() + "/users/" + String.valueOf(id), FetchData.FetchType.USERS_FIND_BY_ID,token,false);
        fetchData.setUsersListener(new FetchData.OnUsersListener() {
            @Override
            public void onUsersData(List<Users> usersList) {
                Glide.with(MainActivity.this)
                        .load(new Config().getServerAddress() + "/" + usersList.get(0).getImagePath())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .error(R.mipmap.ic_persons)
                        .into(profileImage);
            }
        });
        fetchData.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout){
            preferences = getSharedPreferences("user_data",0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("id");
            editor.remove("token");
            editor.remove("image_path");
            editor.commit();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.ic_home:
                open_fragment(R.id.container_fragment , new HomeFragment());
                toolbar.setTitle("Home");
                break;
            case R.id.ic_add:
                open_fragment(R.id.container_fragment , new AddFragment());
                toolbar.setTitle("Add");
                break;
            case R.id.ic_minutes:
                open_fragment(R.id.container_fragment , new SoldFragment());
                toolbar.setTitle("Shop");
                break;
        }
        return true;
    }
    public void open_fragment(int viewContainer , Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(viewContainer , fragment)
                .commit();
    }
}
