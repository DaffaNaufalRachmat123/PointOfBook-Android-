package com.example.asus.taskapp.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.example.asus.taskapp.AccountUtils.ProfileActivity;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.FilePath;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.Model.Books;
import com.example.asus.taskapp.Model.Users;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.FetchData;
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

public class EditUser extends AppCompatActivity {
    public ImageView imageEditUsers;
    public TextInputEditText edit_email , edit_name , edit_password , edit_sekolah;
    public Spinner edit_kelamin;
    public SharedPreferences preferences;
    public FetchData fetchData;
    public String token = null;
    public FloatingActionButton fab_edit;
    public String sourcePath = null;
    public Button btnUpdate;
    public SharedPreferences.Editor editor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        if(new UserAvailable(EditUser.this).check_user_exist() == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(EditUser.this);
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
        Toolbar toolbar = findViewById(R.id.toolbar_edit_user);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditUser.this,MainActivity.class));
                finish();
            }
        });
        ArrayAdapter<String> adapters = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,new String[]{"Laki Laki","Perempuan"});
        adapters.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imageEditUsers = findViewById(R.id.image_edit_user);
        fab_edit = findViewById(R.id.fab_edit_user);
        btnUpdate = findViewById(R.id.btnSunting);
        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent,"Choose an image"),10);
            }
        });
        edit_email = findViewById(R.id.edit_users_email);
        edit_name = findViewById(R.id.edit_users_name);
        edit_password = findViewById(R.id.edit_users_password);
        edit_sekolah = findViewById(R.id.edit_users_sekolah);
        edit_kelamin = findViewById(R.id.edit_users_kelamin);
        edit_kelamin.setAdapter(adapters);
        final int id = getIntent().getIntExtra("id",0);
        preferences = getSharedPreferences("user_data",0);
        if(id != 0 && preferences.getString("token",null) != null){
            token = preferences.getString("token",null);
            load(id , token);
        } else {
            Toast.makeText(EditUser.this,"No Id or Token detected",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(EditUser.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject object = new JSONObject();
                try {
                    object.put("id",id);
                    object.put("email",edit_email.getText().toString());
                    object.put("name",edit_name.getText().toString());
                    object.put("password",edit_password.getText().toString());
                    object.put("sekolah",edit_sekolah.getText().toString());
                    object.put("kelamin",edit_kelamin.getSelectedItem().toString());
                    if(sourcePath != null){
                        object.put("image_path",String.valueOf(id) + new File(sourcePath).getName());
                        object.put("original_image_path",new File(sourcePath).getName());
                    }
                } catch(JSONException e){
                    e.printStackTrace();
                }
                if(sourcePath != null){
                    Post post = new Post(new Config().getServerAddress() + "/users",sourcePath , "upload_image","json_data",object.toString(),"PUT");
                    post.execute();
                } else {
                    JsonPost post = new JsonPost(new Config().getServerAddress() + "/users",object.toString());
                    post.execute();
                }
            }
        });
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
            progressDialog = new ProgressDialog(EditUser.this);
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
            startActivity(new Intent(EditUser.this,MainActivity.class));
            finish();
            super.onPostExecute(integer);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 10){
            if(resultCode == RESULT_OK && data != null){
                sourcePath = FilePath.getPath(EditUser.this,data.getData());
                Glide.with(this)
                        .load(sourcePath)
                        .into(imageEditUsers);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            dialog = new ProgressDialog(EditUser.this);
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
            startActivity(new Intent(EditUser.this,MainActivity.class));
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
                URL url = new URL(uri);
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
    public void load(int id , String token){
        fetchData = new FetchData(EditUser.this,new Config().getServerAddress() + "/users/" + String.valueOf(id),FetchData.FetchType.USERS_FIND_BY_ID,token,false);
        fetchData.setUsersListener(new FetchData.OnUsersListener() {
            @Override
            public void onUsersData(List<Users> usersList) {
                Users users = usersList.get(0);
                edit_email.setText(users.getEmail());
                edit_name.setText(users.getName());
                edit_password.setText(users.getPassword());
                edit_sekolah.setText(users.getSekolah());
                if(users.getKelamin().equalsIgnoreCase("laki laki")){
                    edit_kelamin.setSelection(0);
                } else {
                    edit_kelamin.setSelection(1);
                }
                Glide.with(EditUser.this)
                        .load(new Config().getServerAddress() + "/" + users.getImagePath())
                        .priority(Priority.HIGH)
                        .fitCenter()
                        .placeholder(R.mipmap.ic_persons)
                        .error(R.mipmap.ic_persons)
                        .into(imageEditUsers);
            }
        });
        fetchData.execute();
    }
}
