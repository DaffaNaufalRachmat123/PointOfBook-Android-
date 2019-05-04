package com.example.asus.taskapp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.Model.SoldBooks;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.FetchData;
import com.example.asus.taskapp.Utils.UserAvailable;

import java.util.List;

public class ViewSold extends AppCompatActivity {
    public ImageView soldImage , btnMore;
    public TextView sold_books_name, sold_books_count , sold_books_price , sold_books_status , sold_books_users_sold , sold_books_buyer , sold_books_sold_at;
    public FetchData fetchData;
    public SharedPreferences preferences;
    public RelativeLayout moreData;
    public String token = null;
    public boolean isOpen = false;
    public SharedPreferences.Editor editor;
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ViewSold.this, MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sold);
        if(new UserAvailable(ViewSold.this).check_user_exist() == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewSold.this);
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
        Toolbar toolbar = findViewById(R.id.toolbar_sold);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewSold.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        init();
        preferences = getSharedPreferences("user_data",0);
        token = preferences.getString("token",null);
        int id = getIntent().getIntExtra("id",0);
        fetchData = new FetchData(this,new Config().getServerAddress() + "/sold_books/" + String.valueOf(id), FetchData.FetchType.SOLD_BOOKS_FIND_BY_ID,token,false);
        fetchData.setSoldBooksListener(new FetchData.OnSoldBooksListener() {
            @Override
            public void onSoldBooksData(List<SoldBooks> soldBooksList) {
                SoldBooks books = soldBooksList.get(0);
                sold_books_name.setText(books.getBookName());
                sold_books_count.setText(String.valueOf(books.getCountBook()));
                sold_books_price.setText(String.valueOf(books.getTotalPrice()));
                sold_books_status.setText("Status : " + books.getStatus());
                sold_books_users_sold.setText("Dijual Oleh : " + books.getUsersSold());
                sold_books_buyer.setText("Pembeli : " + books.getBuyer());
                sold_books_sold_at.setText("Terjual Pada : " + books.getSoldAt());
                Glide.with(ViewSold.this)
                        .load(new Config().getServerAddress() + "/books_sold/" + books.getImagePath())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .error(R.mipmap.ic_books)
                        .into(soldImage);
            }
        });
        fetchData.execute();
        moreData.setVisibility(View.GONE);
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOpen){
                    moreData.setVisibility(View.GONE);
                    btnMore.setImageResource(android.R.drawable.ic_input_add);
                    isOpen = false;
                } else {
                    moreData.setVisibility(View.VISIBLE);
                    btnMore.setImageResource(R.drawable.ic_minus);
                    isOpen = true;
                }
            }
        });
    }
    private void init(){
        moreData = findViewById(R.id.more_data);
        soldImage = findViewById(R.id.sold_image);
        btnMore = findViewById(R.id.btn_more);
        sold_books_name = findViewById(R.id.sold_books_name);
        sold_books_count = findViewById(R.id.sold_books_count);
        sold_books_price = findViewById(R.id.sold_books_price);
        sold_books_status = findViewById(R.id.sold_books_status);
        sold_books_users_sold = findViewById(R.id.sold_books_users_sold);
        sold_books_buyer = findViewById(R.id.sold_books_buyer);
        sold_books_sold_at = findViewById(R.id.sold_books_sold_at);
    }
}
