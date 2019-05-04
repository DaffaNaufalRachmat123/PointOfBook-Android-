package com.example.asus.taskapp.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.taskapp.Activities.ViewSold;
import com.example.asus.taskapp.Adapters.SoldBooksAdapter;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.Model.SoldBooks;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.FetchData;
import com.example.asus.taskapp.Utils.RemoveRequest;
import com.example.asus.taskapp.Utils.UserAvailable;

import java.util.ArrayList;
import java.util.List;

public class SoldFragment extends Fragment implements SoldBooksAdapter.SoldBooksListener {
    public RecyclerView recyclerViewSold;
    public TextView teksLoading , teksNoData;
    public FetchData fetchData;
    public SwipeRefreshLayout swipeRefreshSold;
    public List<SoldBooks> soldBooksLists = new ArrayList<>();
    public SharedPreferences preferences;
    public String token = null;
    public SoldBooksAdapter.SoldBooksListener listener = this;
    public SoldBooksAdapter soldAdapter;
    public SharedPreferences.Editor editor;
    public int id = 0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sold_fragment, container , false);
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
        preferences = getActivity().getApplicationContext().getSharedPreferences("user_data",0);
        token = preferences.getString("token",null);
        id = preferences.getInt("id",0);
        recyclerViewSold = view.findViewById(R.id.recyclerViewSold);
        teksLoading = view.findViewById(R.id.loading_teks);
        swipeRefreshSold = view.findViewById(R.id.sold_refresh);
        swipeRefreshSold.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                soldBooksLists.clear();
                load_data(id , token);
                swipeRefreshSold.setRefreshing(false);
            }
        });
        teksNoData = view.findViewById(R.id.teks_no_data);
        recyclerViewSold.setHasFixedSize(true);
        recyclerViewSold.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        load_data(id , token);
        return view;
    }
    public void load_data(int id , String token){
        fetchData = new FetchData(getActivity().getApplicationContext(),new Config().getServerAddress() + "/sold_books/users/" + String.valueOf(id), FetchData.FetchType.SOLD_BOOKS_FIND_BY_USER_ID,token,true);
        fetchData.setTextView(teksLoading);
        fetchData.setSoldBooksListener(new FetchData.OnSoldBooksListener() {
            @Override
            public void onSoldBooksData(List<SoldBooks> soldBooksList) {
                if(soldBooksList != null){
                    soldBooksLists = soldBooksList;
                    soldAdapter = new SoldBooksAdapter(getActivity().getApplicationContext() , soldBooksLists , listener);
                    recyclerViewSold.setAdapter(soldAdapter);
                } else{
                    recyclerViewSold.setVisibility(View.GONE);
                    teksNoData.setVisibility(View.VISIBLE);
                }
            }
        });
        fetchData.execute();
    }

    @Override
    public void onSoldClick(final SoldBooks soldBooks) {
        final CharSequence[] sequences = {"Lihat" , "Hapus"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Actions");
        builder.setItems(sequences, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case 0:
                        Intent intent = new Intent(getContext() , ViewSold.class);
                        intent.putExtra("id",soldBooks.getId());
                        startActivity(intent);
                        break;
                    case 1:
                        RemoveRequest request = new RemoveRequest(new Config().getServerAddress() + "/sold_books/" + soldBooks.getId() , getContext() , token, true);
                        request.setActivity(getActivity());
                        request.execute();
                        soldBooksLists.clear();
                        load_data(id , token);
                        if(soldBooksLists.size() == 0){
                            MainActivity.ma.navigationView.setSelectedItemId(R.id.appbar_sold);
                        }
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
