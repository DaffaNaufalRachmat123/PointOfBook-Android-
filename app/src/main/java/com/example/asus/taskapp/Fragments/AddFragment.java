package com.example.asus.taskapp.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.example.asus.taskapp.Activities.AddBooks;
import com.example.asus.taskapp.Activities.AddUsers;
import com.example.asus.taskapp.Activities.EditBooks;
import com.example.asus.taskapp.Activities.SoldBooks;
import com.example.asus.taskapp.Adapters.BooksAdapter;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.Model.Books;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.FetchData;
import com.example.asus.taskapp.Utils.RemoveRequest;
import com.example.asus.taskapp.Utils.UserAvailable;

import java.util.ArrayList;
import java.util.List;

public class AddFragment extends Fragment implements BooksAdapter.BooksListener {
    public BooksAdapter booksAdapter;
    public RecyclerView recyclerViewBooks;
    public BooksAdapter.BooksListener listener;
    public TextView teksLoading , teksNoData;
    public FetchData fetchData;
    public SharedPreferences preferences;
    public SwipeRefreshLayout layoutRefresh;
    public List<Books> bookList = new ArrayList<>();
    public FloatingActionButton fabs;
    public int id = 0;
    public SharedPreferences.Editor editor;
    public String token = null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_fragment , container , false);
        if(new UserAvailable(getContext()).check_user_exist() == true){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Akun Anda Telah Dihapus / Ada Kesalahan Aplikasi \n Anda Akan Logout Otomatis");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    preferences = getActivity().getApplicationContext().getSharedPreferences("user_data",0);
                    editor = preferences.edit();
                    editor.clear();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        teksLoading = view.findViewById(R.id.teks_loading);
        teksNoData = view.findViewById(R.id.teks_no_data);
        listener = this;
        fabs = view.findViewById(R.id.fab_add_books);
        fabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inten = new Intent(getActivity().getApplicationContext() , AddBooks.class);
                startActivity(inten);
            }
        });
        preferences = getActivity().getApplicationContext().getSharedPreferences("user_data",0);
        token = preferences.getString("token",null);
        id = preferences.getInt("id",0);
        layoutRefresh = view.findViewById(R.id.swipe_refresh_layout);
        layoutRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load_books_data(id , token);
                layoutRefresh.setRefreshing(false);
            }
        });
        recyclerViewBooks = view.findViewById(R.id.recyclerViewBooks);
        recyclerViewBooks.setHasFixedSize(true);
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        if(preferences.getString("token",null) != null){
            String token = preferences.getString("token",null);
            int id = preferences.getInt("id",0);
            load_books_data(id , token);
        }
        return view;
    }

    public void load_books_data(int id , String token){
        fetchData = new FetchData(getContext(),new Config().getServerAddress() + "/books/users/" + String.valueOf(id) , FetchData.FetchType.BOOKS_FIND_BY_USER_ID,token,true);
        fetchData.setTextView(teksLoading);
        fetchData.setBooksListener(new FetchData.OnBooksListener() {
            @Override
            public void onBooksData(List<Books> booksList) {
                if(booksList != null){
                    bookList.clear();
                    bookList = booksList;
                    booksAdapter = new BooksAdapter(bookList , getContext(),listener);
                    recyclerViewBooks.setAdapter(booksAdapter);
                } else{
                    recyclerViewBooks.setVisibility(View.GONE);
                    teksNoData.setVisibility(View.VISIBLE);
                }
            }
        });
        fetchData.execute();
    }

    @Override
    public void onBookClicked(final Books books) {
        final CharSequence[] multipleChoices = {"Jual" , "Edit" , "Hapus"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(multipleChoices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case 0:
                        if(books.getCountBook() != 0){
                            Intent intent = new Intent(getActivity().getApplicationContext() , SoldBooks.class);
                            intent.putExtra("image_path",books.getImagePath());
                            intent.putExtra("book_id",books.getId());
                            intent.putExtra("harga_buku",books.getPriceBook());
                            intent.putExtra("jumlah_buku",books.getCountBook());
                            intent.putExtra("book_name",books.getBookName());
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Buku ini sudah habis , apakah anda ingin menghapusnya ?");
                            builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    RemoveRequest request = new RemoveRequest(new Config().getServerAddress() + "/books/" + String.valueOf(books.getId()) , getContext() , token , true);
                                    request.setActivity(getActivity());
                                    request.execute();
                                    load_books_data(id , token);
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        break;
                    case 1:
                        Intent intents = new Intent(getActivity().getApplicationContext() , EditBooks.class);
                        intents.putExtra("id",books.getId());
                        intents.putExtra("is_add_data",false);
                        startActivity(intents);
                        break;
                    case 2:
                        RemoveRequest request = new RemoveRequest(new Config().getServerAddress() + "/books/" + String.valueOf(books.getId()) , getContext() , token , true);
                        request.setActivity(getActivity());
                        request.execute();
                        load_books_data(id , token);
                        if(bookList.size() == 0){
                            MainActivity.ma.navigationView.setSelectedItemId(R.id.appbar_edit_books);
                        }
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
