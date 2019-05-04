package com.example.asus.taskapp.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.FormDataPost;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.UserAvailable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SoldBooks extends AppCompatActivity {
    public TextInputEditText books_count_sold , price_teks , buyer_teks;
    public int jumlah = 0;
    public ImageView image_sold_books;
    public int jumlah_buku = 0;
    public String book_name = null;
    public TextView count_books , harga_per_buku;
    public Button btnSold;
    public SharedPreferences preferences;
    public int id = 0;
    public int book_id = 0;
    public String name = null;
    public String image_path;
    public int harga_buku = 0;
    public File filePaths = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public File filePath = new File(filePaths , "task_images");
    public String token = null;
    public boolean isGlideDrawable = false;
    public SharedPreferences.Editor editor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sold);
        if(new UserAvailable(SoldBooks.this).check_user_exist() == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(SoldBooks.this);
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
        id = preferences.getInt("id",0);
        token = preferences.getString("token",null);
        name = preferences.getString("name",null);
        image_path = getIntent().getStringExtra("image_path");
        book_id = getIntent().getIntExtra("book_id",0);
        harga_buku = getIntent().getIntExtra("harga_buku",0);
        jumlah_buku = getIntent().getIntExtra("jumlah_buku",0);
        book_name = getIntent().getStringExtra("book_name");
        count_books.setText("Jumlah Buku : " + jumlah_buku);
        harga_per_buku.setText("Harga : " + harga_buku + " / buku");
        Glide.with(SoldBooks.this)
                .load(new Config().getServerAddress() + "/books_list/" + image_path)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .error(R.mipmap.ic_books)
                .into(image_sold_books);
        books_count_sold.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(books_count_sold.getText().toString().equalsIgnoreCase("")){
                    price_teks.setText("0");
                } else {
                    price_teks.setText(String.valueOf(Integer.parseInt(books_count_sold.getText().toString()) * harga_buku));
                }
            }
        });
        btnSold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Integer.parseInt(books_count_sold.getText().toString()) > jumlah_buku){
                    Toast.makeText(SoldBooks.this,"Jumlah Melampaui Batas",Toast.LENGTH_SHORT).show();
                    books_count_sold.requestFocus();
                    return;
                }
                if(books_count_sold.getText().toString().equalsIgnoreCase("")){
                    books_count_sold.requestFocus();
                    Toast.makeText(SoldBooks.this , "Masukan Jumlah Yang Akan Dibeli",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(buyer_teks.getText().toString().equalsIgnoreCase("")){
                    Toast.makeText(SoldBooks.this , "Masukan Nama Pembeli",Toast.LENGTH_SHORT).show();
                    buyer_teks.requestFocus();
                    return;
                }
                if(image_sold_books.getDrawable() instanceof BitmapDrawable){
                    Bitmap bitmap = ((BitmapDrawable)image_sold_books.getDrawable()).getBitmap();
                    File files = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"task_images/" + image_path);
                    try {
                        FileOutputStream outStream = new FileOutputStream(files);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG , 0 ,bos);
                        byte[] result = bos.toByteArray();
                        outStream.write(result);
                        outStream.flush();
                        outStream.close();
                        JSONObject object = new JSONObject();
                        try {
                            object.put("user_id",id);
                            object.put("book_id",book_id);
                            object.put("book_name",book_name);
                            object.put("count_book",Integer.parseInt(books_count_sold.getText().toString()));
                            object.put("total_price",Integer.parseInt(price_teks.getText().toString()));
                            object.put("status","Sold Out");
                            object.put("users_sold",name);
                            object.put("buyer",buyer_teks.getText().toString());
                            object.put("sold_at",new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                            object.put("image_path",String.valueOf(id) + new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"task_images/" + image_path).getName());
                            object.put("original_image_path",new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"task_images/" + image_path).getName());
                            if(jumlah_buku - Integer.parseInt(books_count_sold.getText().toString()) == 0){
                                object.put("result_count",jumlah_buku - Integer.parseInt(books_count_sold.getText().toString()));
                                object.put("result_status","Sold Out All");
                            } else {
                                object.put("result_count",jumlah_buku - Integer.parseInt(books_count_sold.getText().toString()));
                                object.put("result_status","Available");
                            }
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                        BooksSold post = new BooksSold(new Config().getServerAddress() + "/sold_books",filePath.toString() + "/" + image_path,
                                "upload_sold_books","json_data",object.toString(),"POST",
                                "x-token",token,"x-reason","with image");
                        post.execute();
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                } else if(image_sold_books.getDrawable() instanceof GlideBitmapDrawable) {
                    Bitmap bitmap = ((GlideBitmapDrawable)image_sold_books.getDrawable()).getBitmap();
                    File files = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"task_images/" + image_path);
                    try {
                        FileOutputStream outStream = new FileOutputStream(files);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG , 0 ,bos);
                        byte[] result = bos.toByteArray();
                        outStream.write(result);
                        outStream.flush();
                        outStream.close();
                        JSONObject object = new JSONObject();
                        try {
                            object.put("user_id",id);
                            object.put("book_id",book_id);
                            object.put("book_name",book_name);
                            object.put("count_book",Integer.parseInt(books_count_sold.getText().toString()));
                            object.put("total_price",Integer.parseInt(price_teks.getText().toString()));
                            object.put("status","Sold Out");
                            object.put("users_sold",name);
                            object.put("buyer",buyer_teks.getText().toString());
                            object.put("sold_at",new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
                            object.put("image_path",String.valueOf(id) + new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"task_images/" + image_path).getName());
                            object.put("original_image_path",new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"task_images/" + image_path).getName());
                            if(jumlah_buku - Integer.parseInt(books_count_sold.getText().toString()) == 0){
                                object.put("result_count",jumlah_buku - Integer.parseInt(books_count_sold.getText().toString()));
                                object.put("result_status","Sold Out All");
                            } else {
                                object.put("result_count",jumlah_buku - Integer.parseInt(books_count_sold.getText().toString()));
                                object.put("result_status","Available");
                            }
                        } catch(JSONException e){
                            e.printStackTrace();
                        }
                        BooksSold post = new BooksSold(new Config().getServerAddress() + "/sold_books",filePath.toString() + "/" + image_path,
                                "upload_sold_books","json_data",object.toString(),"POST",
                                "x-token",token,"x-reason","with image");
                        post.execute();
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public class BooksSold extends AsyncTask<String , Void , Integer> {
        public String uri;
        public String fileName;
        public String fieldName;
        public String fieldString;
        public String data;
        public String requestType;
        public String headerToken;
        public String token;
        public String headerReason;
        public String reasonValue;
        public ProgressDialog progressDialog;
        public BooksSold(String uri , String fileName , String fieldName , String fieldString , String data , String requestType , String headerToken , String token , String headerReason , String reasonValue){
            this.uri = uri;
            this.fileName = fileName;
            this.fieldName = fieldName;
            this.fieldString = fieldString;
            this.data = data;
            this.requestType = requestType;
            this.headerToken = headerToken;
            this.token = token;
            this.headerReason = headerReason;
            this.reasonValue = reasonValue;
        }
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(SoldBooks.this);
            progressDialog.setMessage("Selling books....");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            return new FormDataPost().UploadMultipart(uri , fileName , fieldName , fieldString , data , requestType,
                    headerToken , token , headerReason , reasonValue);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            progressDialog.dismiss();
            startActivity(new Intent(SoldBooks.this, MainActivity.class));
            MainActivity.ma.navigationView.setSelectedItemId(R.id.appbar_edit_books);
            finish();
            super.onPostExecute(integer);
        }
    }
    private void init(){
        books_count_sold = findViewById(R.id.books_count_sold);
        image_sold_books = findViewById(R.id.image_books_sold);
        price_teks = findViewById(R.id.price_teks);
        buyer_teks = findViewById(R.id.buyer_teks);
        btnSold = findViewById(R.id.btnSold);
        count_books = findViewById(R.id.count_books);
        harga_per_buku = findViewById(R.id.harga_per_buku);
    }
}
