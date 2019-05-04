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
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.FilePath;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.Model.Books;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class EditBooks extends AppCompatActivity {
    public ImageView imageEditBooks;
    public TextInputEditText edit_books_name , edit_count , edit_books_price , edit_books_status;
    public Button btnUpdateBooks;
    public SharedPreferences preferences;
    public String token;
    public int id = 0;
    public FetchData fetchData;
    public String sourcePath = null;
    public FloatingActionButton fab_edit_books;
    public SharedPreferences.Editor editor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_books);
        if(new UserAvailable(EditBooks.this).check_user_exist() == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(EditBooks.this);
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
        Toolbar toolbar = findViewById(R.id.toolbar_edit_books);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditBooks.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        preferences = getSharedPreferences("user_data",0);
        imageEditBooks = findViewById(R.id.imageEditBooks);
        edit_books_name = findViewById(R.id.edit_books_name);
        edit_count = findViewById(R.id.edit_count);
        edit_books_price = findViewById(R.id.edit_price_book);
        edit_books_status = findViewById(R.id.edit_books_status);
        btnUpdateBooks = findViewById(R.id.btnUpdateBooks);
        fab_edit_books = findViewById(R.id.fab_edit_books);
        fab_edit_books.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent,"Choose an image"),10);
            }
        });
        if(getIntent().getBooleanExtra("is_add_data",false) == false){
            if(getIntent().getIntExtra("id",0) != 0){
                id = getIntent().getIntExtra("id",0);
                preferences = getSharedPreferences("user_data",0);
                token = preferences.getString("token",null);
                load_books_data(id , token);
            }
        } else {
            btnUpdateBooks.setText("Add");
        }
        btnUpdateBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnUpdateBooks.getText().toString().equalsIgnoreCase("update")){
                    JSONObject object = new JSONObject();
                    try {
                        object.put("id",id);
                        object.put("book_name",edit_books_name.getText().toString());
                        object.put("count_book",Integer.parseInt(edit_count.getText().toString()));
                        object.put("price_book",Integer.parseInt(edit_books_price.getText().toString()));
                        object.put("status",edit_books_status.getText().toString());
                        if(sourcePath != null){
                            object.put("image_path",String.valueOf(id) + new File(sourcePath).getName());
                            object.put("original_image_path",new File(sourcePath).getName());
                        }
                    } catch(JSONException e){
                        e.printStackTrace();
                    }
                    if(sourcePath != null){
                        Post post = new Post(new Config().getServerAddress() + "/books" , sourcePath , "upload_books","json_data",object.toString(),"PUT");
                        post.execute();
                    } else {
                        JsonPost post = new JsonPost(new Config().getServerAddress() + "/books",object.toString());
                        post.execute();
                    }
                } else {
                    if(sourcePath == null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditBooks.this);
                        builder.setMessage("Gambar tak boleh kosong");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        JSONObject object = new JSONObject();
                        try {
                            int id = preferences.getInt("id",0);
                            object.put("user_id",id);
                            object.put("book_name",edit_books_name.getText().toString());
                            object.put("count_book",Integer.parseInt(edit_count.getText().toString()));
                            object.put("price_book",Integer.parseInt(edit_books_price.getText().toString()));
                            object.put("status",edit_books_status.getText().toString());
                            object.put("image_path",String.valueOf(id) + new File(sourcePath).getName());
                            object.put("original_image_path",new File(sourcePath).getName());
                            object.put("created_at",formatDate(new Date().toString()));
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                        Post post = new Post(new Config().getServerAddress() + "/books",sourcePath,"upload_books","json_data",object.toString(),"POST");
                        post.execute();
                    }
                }
            }
        });
    }
    public String formatDate(String dateString){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'H'H:mm:ss.SSS");
            Date d = dateFormat.parse(dateString);
            dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return dateFormat.format(d);
        } catch(ParseException e){
            e.printStackTrace();
        }
        return "";
    }

    public String rand_string(int length){
        char[] chars = {'0','1','2','3','4','5','6','7','8','9'};
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        while(0 < length--){
            sb.append(chars[random.nextInt(chars.length)]);
        }
        return sb.toString();
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
            progressDialog = new ProgressDialog(EditBooks.this);
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
            MainActivity.ma.load_new_data();
            startActivity(new Intent(EditBooks.this,MainActivity.class));
            finish();
            super.onPostExecute(integer);
        }
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
            dialog = new ProgressDialog(EditBooks.this);
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
            startActivity(new Intent(EditBooks.this,MainActivity.class));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 10){
            if(resultCode == RESULT_OK && data != null){
                sourcePath = FilePath.getPath(EditBooks.this,data.getData());
                Glide.with(EditBooks.this)
                        .load(sourcePath)
                        .into(imageEditBooks);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void load_books_data(int id , String token){
        fetchData = new FetchData(EditBooks.this,new Config().getServerAddress() + "/books/" + String.valueOf(id),FetchData.FetchType.BOOKS_FIND_BY_ID,token,false);
        fetchData.setBooksListener(new FetchData.OnBooksListener() {
            @Override
            public void onBooksData(List<Books> booksList) {
                Books books = booksList.get(0);
                edit_books_name.setText(books.getBookName());
                edit_count.setText(String.valueOf(books.getCountBook()));
                edit_books_price.setText(String.valueOf(books.getPriceBook()));
                edit_books_status.setText(books.getStatus());
                Glide.with(EditBooks.this)
                        .load(new Config().getServerAddress() + "/books_list/" + books.getImagePath())
                        .priority(Priority.HIGH)
                        .fitCenter()
                        .error(R.mipmap.ic_books)
                        .into(imageEditBooks);
            }
        });
        fetchData.execute();
    }
}
