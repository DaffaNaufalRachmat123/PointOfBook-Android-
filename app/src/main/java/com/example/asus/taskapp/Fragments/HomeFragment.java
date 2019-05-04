package com.example.asus.taskapp.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.taskapp.AccountUtils.LoginActivity;
import com.example.asus.taskapp.AccountUtils.RegisterActivity;
import com.example.asus.taskapp.Activities.AddUsers;
import com.example.asus.taskapp.Activities.EditUser;
import com.example.asus.taskapp.Adapters.UsersAdapter;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.MainActivity;
import com.example.asus.taskapp.Model.Users;
import com.example.asus.taskapp.R;
import com.example.asus.taskapp.Utils.FetchData;
import com.example.asus.taskapp.Utils.RemoveRequest;
import com.example.asus.taskapp.Utils.UserAvailable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class HomeFragment extends Fragment implements UsersAdapter.UsersClickListener {
    public SharedPreferences preferences;
    public String token = "";
    public String id = null;
    public TextView teksView;
    public TextView loadingTeks;
    public FetchData fetchData;
    public RecyclerView recyclerView;
    public UsersAdapter usersAdapter;
    public UsersAdapter.UsersClickListener listener;
    public List<Users> userList = new ArrayList<>();
    public SwipeRefreshLayout refreshLayout;
    public FloatingActionButton fabs;
    public SharedPreferences.Editor editor;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment , container , false);
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
        if(preferences.getString("token",null) != null && preferences.getInt("id",0) != 0){
            token = preferences.getString("token",null);
            id = String.valueOf(preferences.getInt("id",0));
            Log.d("id_data",String.valueOf(id));
        } else {
            Log.d("token","nul");
        }
        listener = this;
        fabs = view.findViewById(R.id.fab_add_user);
        fabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(),AddUsers.class);
                startActivity(intent);
            }
        });
        refreshLayout = view.findViewById(R.id.swipe_refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                userList.clear();
                load_data();
                refreshLayout.setRefreshing(false);
            }
        });
        loadingTeks = view.findViewById(R.id.loading_teks);
        teksView = view.findViewById(R.id.no_data_teks);
        recyclerView = view.findViewById(R.id.recyclerViewUsers);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        load_data();
        return view;
    }

    public void load_data(){
        fetchData = new FetchData(getContext() , new Config().getServerAddress() + "/users", FetchData.FetchType.USERS,token,true);
        fetchData.setTextView(loadingTeks);
        fetchData.setUsersListener(new FetchData.OnUsersListener() {
            @Override
            public void onUsersData(List<Users> usersList) {
                if(usersList != null){
                    userList.clear();
                    userList = usersList;
                    usersAdapter = new UsersAdapter(userList , getContext() , listener);
                    recyclerView.setAdapter(usersAdapter);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    teksView.setVisibility(View.VISIBLE);
                }
            }
        });
        fetchData.execute();
    }

    @Override
    public void onUserClicked(final Users users) {
        final CharSequence[] sequences = {"Ubah" , "Hapus"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Pilihan");
        builder.setItems(sequences, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case 0:
                        Intent intent = new Intent(getContext() , EditUser.class);
                        intent.putExtra("id",users.getId());
                        startActivity(intent);
                        break;
                    case 1:
                        int id = preferences.getInt("id",0);
                        final String token = preferences.getString("token",null);
                        if(id == users.getId()){
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Yakin ingin menghapus data ? Setelah \n Data Terhapus Anda Akan Logout Otomatis");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    RemoveRequests request = new RemoveRequests(new Config().getServerAddress() + "/users/" + String.valueOf(users.getId()),token);
                                    request.execute();
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.remove("id");
                                    editor.remove("name");
                                    editor.remove("image_path");
                                    editor.remove("token");
                                    editor.commit();
                                    Intent inten = new Intent(getContext() , LoginActivity.class);
                                    startActivity(inten);
                                    getActivity().finish();
                                }
                            });
                            builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            RemoveRequest request = new RemoveRequest(new Config().getServerAddress() + "/users/" + users.getId() , getContext() , token , true);
                            request.setActivity(getActivity());
                            request.execute();
                            load_data();
                            if(userList.size() == 0){
                                MainActivity.ma.navigationView.setSelectedItemId(R.id.appbar_edit_user);
                            }
                        }
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public class RemoveRequests extends AsyncTask<String , Void , Integer> {
        public ProgressDialog dialog;
        public String uri;
        public String token;
        public RemoveRequests(String uri , String token){
            this.uri = uri;
            this.token = token;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int responseCode = 0;
            try {
                URL url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setDoInput(true);
                connection.setRequestProperty("x-token",token);
                connection.setRequestMethod("DELETE");
                responseCode = connection.getResponseCode();
            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }
    }
}
