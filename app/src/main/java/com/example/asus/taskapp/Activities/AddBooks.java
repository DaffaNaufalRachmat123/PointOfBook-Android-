package com.example.asus.taskapp.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.example.asus.taskapp.AccountUtils.LoginActivity;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.FilePath;
import com.example.asus.taskapp.FormDataPost;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.UserAvailable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class AddBooks extends AppCompatActivity {
    public TextInputEditText books_name , books_count , books_price , books_status;
    public ImageView imageBooks;
    public Button btnSelect , btnAddBooks;
    public String sourcePath = null;
    public SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    public String token = null;
    public int id = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_books);
        if(new UserAvailable(AddBooks.this).check_user_exist() == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddBooks.this);
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
        init();
        preferences = getSharedPreferences("user_data",0);
        token = preferences.getString("token",null);
        id = preferences.getInt("id",0);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent , "Choose an image"),10);
            }
        });
        btnAddBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sourcePath == null){
                    Toast.makeText(AddBooks.this,"Wajib Pake Gambar", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject object = new JSONObject();
                try {
                    object.put("user_id",id);
                    object.put("book_name",books_name.getText().toString());
                    object.put("count_book",Integer.parseInt(books_count.getText().toString()));
                    object.put("price_book",Integer.parseInt(books_price.getText().toString()));
                    object.put("status",books_status.getText().toString());
                    object.put("image_path",String.valueOf(Integer.parseInt(rand_string(3))) + new File(sourcePath).getName());
                    object.put("original_image_path",new File(sourcePath).getName());
                    object.put("created_at",String.valueOf(dateFormat.format(new Date())));
                } catch(JSONException e){
                    e.printStackTrace();
                }
                BooksAdd addBooks = new BooksAdd(new Config().getServerAddress() + "/books",sourcePath,"upload_books","json_data",object.toString(),"x-token",token, "x-reason","with image");
                addBooks.execute();
            }
        });
    }

    public class BooksAdd extends AsyncTask<String , Void , Integer> {
        public ProgressDialog dialog;
        public String uri;
        public String fileName;
        public String fieldName;
        public String fieldString;
        public String data;
        public String headerToken;
        public String token;
        public String headerReason;
        public String reasonValue;
        public BooksAdd(String uri , String fileName , String fieldName , String fieldString , String data ,
                        String headerToken , String token , String headerReason , String reasonValue){
            this.uri = uri;
            this.fileName = fileName;
            this.fieldName = fieldName;
            this.fieldString = fieldString;
            this.data = data;
            this.headerToken = headerToken;
            this.token = token;
            this.headerReason = headerReason;
            this.reasonValue = reasonValue;
        }
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(AddBooks.this);
            dialog.setMessage("Adding books....");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            return new FormDataPost().UploadMultipart(uri , fileName , fieldName , fieldString , data , "POST","x-token",token,
                    "x-reason","with image");
        }

        @Override
        protected void onPostExecute(Integer integer) {
            dialog.dismiss();
            Intent intent = new Intent(AddBooks.this, MainActivity.class);
            startActivity(intent);
            finish();
            super.onPostExecute(integer);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 10){
            if(resultCode == RESULT_OK && data != null){
                sourcePath = FilePath.getPath(AddBooks.this,data.getData());
                Glide.with(this)
                        .load(sourcePath)
                        .priority(Priority.HIGH)
                        .fitCenter()
                        .error(R.mipmap.ic_books)
                        .into(imageBooks);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void init(){
        books_name = findViewById(R.id.books_name);
        books_count = findViewById(R.id.books_count);
        books_price = findViewById(R.id.books_price);
        books_status = findViewById(R.id.books_status);
        imageBooks = findViewById(R.id.image_books);
        btnSelect = findViewById(R.id.btnSelect);
        btnAddBooks = findViewById(R.id.btnAddBooks);
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
}
