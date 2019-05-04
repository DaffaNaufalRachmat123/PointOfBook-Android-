package com.example.asus.taskapp.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.taskapp.Model.Books;
import com.example.asus.taskapp.Model.SoldBooks;
import com.example.asus.taskapp.Model.Users;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchData extends AsyncTask<String , Void , String> {
    public enum FetchType {
        USERS,
        USERS_FIND_BY_ID,
        BOOKS,
        BOOKS_FIND_BY_USER_ID,
        BOOKS_FIND_BY_ID,
        SOLD_BOOKS,
        SOLD_BOOKS_FIND_BY_USER_ID,
        SOLD_BOOKS_FIND_BY_ID
    }
    public Context context;
    public String uri;
    public FetchType fetchType;
    public String token;
    public TextView teksView = null;
    public ProgressDialog progressDialog;
    public OnUsersListener usersListener;
    public OnBooksListener booksListener;
    public OnSoldBooksListener soldBooksListener;
    public boolean isFragment = false;
    public FetchData(Context context , String uri , FetchType fetchType , String token , boolean isFragment){
        this.context = context;
        this.uri = uri;
        this.fetchType = fetchType;
        this.token = token;
        this.isFragment = isFragment;
    }
    public void setTextView(TextView teksView){
        this.teksView = teksView;
    }
    public void setUsersListener(OnUsersListener usersListener){
        this.usersListener = usersListener;
    }
    public void setBooksListener(OnBooksListener booksListener){
        this.booksListener = booksListener;
    }
    public void setSoldBooksListener(OnSoldBooksListener soldBooksListener){
        this.soldBooksListener = soldBooksListener;
    }
    public String getDataFromURL(String uri , String token){
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoInput(true);
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-token",token);
            InputStreamReader inStreamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(inStreamReader);
            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line = reader.readLine()) != null){
                sb.append(line);
            }
            return sb.toString();
        } catch(MalformedURLException e){
            e.printStackTrace();
        } catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        if(!isFragment){
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Status");
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Waiting....");
            progressDialog.show();
        } else {
            if(teksView != null){
                teksView.setVisibility(View.VISIBLE);
                teksView.setText("Fetching data.....");
            }
        }
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        return getDataFromURL(uri , token);
    }

    @Override
    protected void onPostExecute(String s) {
        if(fetchType == FetchType.USERS){
            try {
                JSONArray arrays = new JSONArray(s);
                if(arrays.length() > 0){
                    List<Users> usersList = new ArrayList<>();
                    for(int i = 0; i < arrays.length(); i++){
                        JSONObject object = arrays.getJSONObject(i);
                        JSONObject objects = object.getJSONObject("users_detail");
                        usersList.add(new Users(
                                object.getInt("id"),object.getString("email"),object.getString("name"),
                                object.getString("password"),object.getString("sekolah"),
                                objects.getString("kelamin"),objects.getString("image_path"),
                                objects.getString("original_image_path")
                        ));
                    }
                    usersListener.onUsersData(usersList);
                } else {
                    usersListener.onUsersData(null);
                }
            } catch(JSONException e){
                e.printStackTrace();
            }
        } else if(fetchType == FetchType.USERS_FIND_BY_ID){
            try {
                if(s != null){
                    JSONObject object = new JSONObject(s);
                    JSONObject objects = object.getJSONObject("users_detail");
                    List<Users> usersList = new ArrayList<>();
                    usersList.add(new Users(
                            object.getInt("id"),object.getString("email"),object.getString("name"),
                            object.getString("password"),object.getString("sekolah"),objects.getString("kelamin"),
                            objects.getString("image_path"),objects.getString("original_image_path")
                    ));
                    usersListener.onUsersData(usersList);
                } else if(s == null){
                    usersListener.onUsersData(null);
                }
            } catch(JSONException e){
                e.printStackTrace();
                usersListener.onUsersData(null);
            }
        } else if(fetchType == FetchType.BOOKS){
            try {
                if(s != null){
                    JSONArray array = new JSONArray(s);
                    if(array.length() != 0){
                        List<Books> booksList = new ArrayList<>();
                        for(int i = 0; i < array.length(); i++){
                            JSONObject object = array.getJSONObject(i);
                            Books books = new Books();
                            books.setId(object.getInt("id"));
                            books.setUserId(object.getInt("user_id"));
                            books.setBookName(object.getString("book_name"));
                            books.setCountBook(object.getInt("count_book"));
                            books.setPriceBook(object.getInt("price_book"));
                            books.setStatus(object.getString("status"));
                            books.setImagePath(object.getString("image_path"));
                            books.setOriginalImagePath(object.getString("original_image_path"));
                            books.setCreatedAt(object.getString("created_at"));
                            booksList.add(books);
                        }
                        booksListener.onBooksData(booksList);
                    } else {
                        booksListener.onBooksData(null);
                    }
                } else {
                    booksListener.onBooksData(null);
                }
            } catch(JSONException e){
                e.printStackTrace();
            }
        } else if(fetchType == FetchType.BOOKS_FIND_BY_USER_ID){
            try {
                JSONArray arrays = new JSONArray(s);
                List<Books> booksList = new ArrayList<>();
                if(arrays.length() != 0){
                    for(int i = 0; i < arrays.length(); i++){
                        JSONObject object = arrays.getJSONObject(i);
                        Books books = new Books();
                        books.setId(object.getInt("id"));
                        books.setUserId(object.getInt("user_id"));
                        books.setBookName(object.getString("book_name"));
                        books.setCountBook(object.getInt("count_book"));
                        books.setPriceBook(object.getInt("price_book"));
                        books.setStatus(object.getString("status"));
                        books.setImagePath(object.getString("image_path"));
                        books.setOriginalImagePath(object.getString("original_image_path"));
                        books.setCreatedAt(object.getString("created_at"));
                        booksList.add(books);
                    }
                    booksListener.onBooksData(booksList);
                } else {
                    booksListener.onBooksData(null);
                }
            } catch(JSONException e){
                e.printStackTrace();
            }
        } else if(fetchType == FetchType.BOOKS_FIND_BY_ID){
            try {
                JSONObject object = new JSONObject(s);
                if(object != null){
                    List<Books> booksList = new ArrayList<>();
                    Books books = new Books();
                    books.setId(object.getInt("id"));
                    books.setUserId(object.getInt("user_id"));
                    books.setBookName(object.getString("book_name"));
                    books.setCountBook(object.getInt("count_book"));
                    books.setPriceBook(object.getInt("price_book"));
                    books.setStatus(object.getString("status"));
                    books.setImagePath(object.getString("image_path"));
                    books.setOriginalImagePath(object.getString("original_image_path"));
                    booksList.add(books);
                    booksListener.onBooksData(booksList);
                } else {
                    booksListener.onBooksData(null);
                }
            } catch(JSONException e){
                e.printStackTrace();
            }
        } else if(fetchType == FetchType.SOLD_BOOKS){
            try {
                JSONArray arrays = new JSONArray(s);
                if(arrays.length() != 0){
                    List<SoldBooks> soldBooksList = new ArrayList<>();
                    for(int i = 0; i < arrays.length(); i++){
                        JSONObject object = arrays.getJSONObject(i);
                        SoldBooks soldBooks = new SoldBooks();
                        soldBooks.setId(object.getInt("id"));
                        soldBooks.setUserId(object.getInt("user_id"));
                        soldBooks.setBookName(object.getString("book_name"));
                        soldBooks.setCountBook(object.getInt("count_book"));
                        soldBooks.setTotalPrice(object.getInt("total_price"));
                        soldBooks.setStatus(object.getString("status"));
                        soldBooks.setUsersSold(object.getString("users_sold"));
                        soldBooks.setBuyer(object.getString("buyer"));
                        soldBooks.setSoldAt(object.getString("sold_at"));
                        soldBooks.setImagePath(object.getString("image_path"));
                        soldBooks.setOriginalImagePath(object.getString("original_image_path"));
                        soldBooksList.add(soldBooks);
                        soldBooksListener.onSoldBooksData(soldBooksList);
                    }
                } else {
                    soldBooksListener.onSoldBooksData(null);
                }
            } catch(JSONException e){
                e.printStackTrace();
            }
        } else if(fetchType == FetchType.SOLD_BOOKS_FIND_BY_USER_ID){
            try {
                if(s != null){
                    List<SoldBooks> soldList = new ArrayList<>();
                    JSONArray arrays = new JSONArray(s);
                    for(int i = 0; i < arrays.length(); i++){
                        JSONObject object = arrays.getJSONObject(i);
                        SoldBooks soldBooks = new SoldBooks();
                        soldBooks.setId(object.getInt("id"));
                        soldBooks.setUserId(object.getInt("user_id"));
                        soldBooks.setBookId(object.getInt("book_id"));
                        soldBooks.setBookName(object.getString("book_name"));
                        soldBooks.setCountBook(object.getInt("count_book"));
                        soldBooks.setTotalPrice(object.getInt("total_price"));
                        soldBooks.setStatus(object.getString("status"));
                        soldBooks.setUsersSold(object.getString("users_sold"));
                        soldBooks.setBuyer(object.getString("buyer"));
                        soldBooks.setSoldAt(object.getString("sold_at"));
                        soldBooks.setImagePath(object.getString("image_path"));
                        soldBooks.setOriginalImagePath(object.getString("original_image_path"));
                        soldList.add(soldBooks);
                        soldBooksListener.onSoldBooksData(soldList);
                    }
                } else {
                    soldBooksListener.onSoldBooksData(null);
                }
            } catch(JSONException e){
                e.printStackTrace();
            }
        } else if(fetchType == FetchType.SOLD_BOOKS_FIND_BY_ID){
            try {
                JSONObject object = new JSONObject(s);
                if(object.length() != 0){
                    List<SoldBooks> soldBooks = new ArrayList<>();
                    SoldBooks books = new SoldBooks();
                    books.setId(object.getInt("id"));
                    books.setUserId(object.getInt("user_id"));
                    books.setBookName(object.getString("book_name"));
                    books.setCountBook(object.getInt("count_book"));
                    books.setTotalPrice(object.getInt("total_price"));
                    books.setStatus(object.getString("status"));
                    books.setUsersSold(object.getString("users_sold"));
                    books.setBuyer(object.getString("buyer"));
                    books.setSoldAt(object.getString("sold_at"));
                    books.setImagePath(object.getString("image_path"));
                    books.setOriginalImagePath(object.getString("original_image_path"));
                    soldBooks.add(books);
                    soldBooksListener.onSoldBooksData(soldBooks);
                } else {
                    soldBooksListener.onSoldBooksData(null);
                }
            } catch(JSONException e){
                e.printStackTrace();
            }
        }
        if(!isFragment){
            progressDialog.dismiss();
        } else {
            if(teksView != null){
                teksView.setVisibility(View.GONE);
            }
        }
        super.onPostExecute(s);
    }

    public interface OnUsersListener {
        void onUsersData(List<Users> usersList);
    }
    public interface OnBooksListener {
        void onBooksData(List<Books> booksList);
    }
    public interface OnSoldBooksListener {
        void onSoldBooksData(List<SoldBooks> soldBooksList);
    }
}
