package com.example.asus.taskapp.AccountUtils;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.FilePath;
import com.example.asus.taskapp.FormDataPost;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.Model.Users;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.FetchData;
import com.example.asus.taskapp.Utils.JSON_POST;
import com.example.asus.taskapp.Utils.UserAvailable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements FetchData.OnUsersListener {
    public Button btnSunting , btnOK;
    public Spinner kelamin_teks;
    public ImageView imageView;
    public String sourcePath = null;
    public ProgressDialog dialogs = null;
    public SharedPreferences preferences;
    public FetchData fetchData;
    public int id = 0;
    public SharedPreferences.Editor editor;
    public String token = null;
    public TextInputEditText email_teks , name_teks , password_teks , sekolah_teks , image_path_teks;
    public FloatingActionButton profile_fab;
    public String original_image = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        if(new UserAvailable(ProfileActivity.this).check_user_exist() == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setMessage("Akun Anda Telah Dihapus / Ada Kesalahan Aplikasi \n Anda Akan Logout Otomatis");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    preferences = getSharedPreferences("user_data",0);
                    editor = preferences.edit();
                    editor.clear();
                    Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        preferences = getSharedPreferences("user_data",0);
        if(preferences.getString("token",null) != null){
            token = preferences.getString("token",null);
        }
        profile_fab = findViewById(R.id.profile_fab);
        profile_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent , "Choose image"),10);
            }
        });
        btnSunting = findViewById(R.id.btnEdit);
        btnOK = findViewById(R.id.btnOk);
        imageView = findViewById(R.id.profile_image);
        preferences = getSharedPreferences("user_data",0);
        if(preferences.getString("token",null) != null && preferences.getInt("id",0) != 0){
            String token = preferences.getString("token",null);
            id = preferences.getInt("id",0);
            fetchData = new FetchData(ProfileActivity.this,new Config().getServerAddress() + "/users/" + String.valueOf(id), FetchData.FetchType.USERS_FIND_BY_ID,token,false);
            fetchData.setUsersListener(this);
            fetchData.execute();
        }
        btnSunting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email_teks.setEnabled(true);
                name_teks.setEnabled(true);
                password_teks.setEnabled(true);
                sekolah_teks.setEnabled(true);
                kelamin_teks.setEnabled(true);
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final JSONObject object = new JSONObject();
                try {
                    object.put("id",id);
                    object.put("email",email_teks.getText().toString());
                    object.put("name",name_teks.getText().toString());
                    object.put("password",password_teks.getText().toString());
                    object.put("sekolah",sekolah_teks.getText().toString());
                    object.put("kelamin",kelamin_teks.getSelectedItem().toString());
                    if(sourcePath != null){
                        object.put("image_path",String.valueOf(id) + new File(sourcePath).getName());
                        object.put("original_image_path",new File(sourcePath).getName());
                    }
                } catch(JSONException e){
                    e.printStackTrace();
                }
                if(sourcePath != null){
                    Post post = new Post(new Config().getServerAddress() + "/users" , sourcePath , "upload_image" , "json_data",object.toString(),"PUT");
                    post.execute();
                } else {
                    JsonPost posts = new JsonPost(new Config().getServerAddress() + "/users",object.toString());
                    posts.execute();
                }
            }
        });
        ArrayAdapter<String> items = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,new String[]{"Laki-Laki","Perempuan"});
        items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kelamin_teks = findViewById(R.id.kelamin_teks);
        kelamin_teks.setAdapter(items);
        email_teks = findViewById(R.id.email_teks);
        name_teks = findViewById(R.id.name_teks);
        password_teks = findViewById(R.id.password_teks);
        sekolah_teks = findViewById(R.id.sekolah_teks);
        image_path_teks = findViewById(R.id.image_path_teks);
        email_teks.setEnabled(false);
        name_teks.setEnabled(false);
        password_teks.setEnabled(false);
        sekolah_teks.setEnabled(false);
        image_path_teks.setEnabled(false);
        kelamin_teks.setEnabled(false);
        initCollapsingToolbar();
    }
    public class Post extends AsyncTask<String , Void , Integer> {
        public String uri;
        public String fileName;
        public String fileField;
        public String fieldString;
        public String data;
        public String requestType;
        public ProgressDialog dialog;
        public Post(String uri , String fileName , String fileField , String fieldString , String data , String requestType){
            this.uri = uri;
            this.fileName = fileName;
            this.fileField = fileField;
            this.fieldString = fieldString;
            this.data = data;
            this.requestType = requestType;
        }
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ProfileActivity.this);
            dialog.setMessage("Updating...");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            return UploadMultipart(uri , fileName , fileField , fieldString , data , requestType);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            dialog.dismiss();
            startActivity(new Intent(ProfileActivity.this,MainActivity.class));
            finish();
            super.onPostExecute(integer);
        }
    }
    public class JsonPost extends AsyncTask<String , Void , Integer> {
        public ProgressDialog progressDialog;
        public String uri;
        public String data;
        public JsonPost(String uri , String data){
            this.uri = uri;
            this.data = data;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ProfileActivity.this);
            progressDialog.setMessage("Updating....");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            try {
                URL url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Accept","application/json");
                connection.setRequestProperty("Content-Type","application/json");
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("x-token",token);
                connection.setRequestProperty("x-image","no image");
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data);
                writer.flush();
                int code = connection.getResponseCode();
                return code;
            } catch(Exception e){
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            progressDialog.dismiss();
            startActivity(new Intent(ProfileActivity.this,MainActivity.class));
            finish();
            super.onPostExecute(integer);
        }
    }
    public int UploadMultipart(String uri , String fileName , String fieldName , String fieldString,  String data , String requestType){
        HttpURLConnection connection;
        DataOutputStream outputStream;
        String lineEnd = "\r\n";
        String boundary = "*****";
        String two = "--";
        int maxBufferSize = 2 * 2048 * 2048;
        int bytesRead , bytesAvailable , bufferSize;
        byte[] buffer;
        File selectedFile = new File(fileName);
        int responseCode = 0;
        if(!selectedFile.exists()){
            Log.e("Error","Not A File");
            return 0;
        } else {
            try {
                FileInputStream fis = new FileInputStream(selectedFile);
                URL url = new    URL(uri);
                connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod(requestType);
                connection.setRequestProperty("x-token",token);
                connection.setRequestProperty("Connection","Keep-Alive");
                connection.setRequestProperty("ENCTYPE","multipart/form-data");
                connection.setRequestProperty("Content-Type","multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty(fieldName , fileName);
                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(two + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldString + "\"" + lineEnd + lineEnd);
                outputStream.writeBytes(data + lineEnd + two + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                bytesAvailable = fis.available();
                bufferSize = Math.min(bytesAvailable , maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fis.read(buffer , 0 , bufferSize);
                while(bytesRead > 0){
                    outputStream.write(buffer , 0 , bufferSize);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable , maxBufferSize);
                    bytesRead = fis.read(buffer , 0 ,bufferSize);
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(two + boundary + two + lineEnd);
                fis.close();
                outputStream.flush();
                outputStream.close();
                responseCode = connection.getResponseCode();
            } catch(FileNotFoundException e){
                e.printStackTrace();
            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return responseCode;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent,"choose image file"),10);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 10){
            if(resultCode == RESULT_OK && data != null){
                sourcePath = FilePath.getPath(ProfileActivity.this,data.getData());
                image_path_teks.setText(sourcePath);
                Glide.with(this)
                        .load(sourcePath)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .error(R.mipmap.ic_persons)
                        .into(imageView);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initCollapsingToolbar(){
        final CollapsingToolbarLayout collapsing = findViewById(R.id.collapsing);
        AppBarLayout appBar = findViewById(R.id.appbar);
        appBar.setExpanded(true);
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                boolean isShow = false;
                int scrollRange = -1;
                if(scrollRange == -1){
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if(verticalOffset + scrollRange == 0){
                    collapsing.setTitle("Profile");
                    isShow = true;
                } else if(isShow){
                    collapsing.setTitle("");
                    isShow = false;
                }
            }
        });
    }

    @Override
    public void onUsersData(List<Users> usersList) {
        Users users = usersList.get(0);
        email_teks.setText(users.getEmail());
        name_teks.setText(users.getName());
        password_teks.setText(users.getPassword());
        sekolah_teks.setText(users.getSekolah());
        original_image = users.getOriginalImagePath();
        if(users.getKelamin().equalsIgnoreCase("laki laki")){
            kelamin_teks.setSelection(0);
        } else {
            kelamin_teks.setSelection(1);
        }
        image_path_teks.setText(users.getImagePath());
        Glide.with(this)
                .load(new Config().getServerAddress() + "/" + users.getImagePath())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .error(R.mipmap.ic_persons)
                .into(imageView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
