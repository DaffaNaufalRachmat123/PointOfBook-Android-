package com.example.asus.taskapp.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.example.asus.taskapp.Config;
import com.example.asus.taskapp.Model.Books;
import com.example.asus.taskapp.R;

import java.util.List;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.MyViewAdapter> {
    public List<Books> booksList;
    public Context context;
    public BooksListener listener;
    public BooksAdapter(List<Books> booksList , Context context , BooksListener listener){
        this.booksList = booksList;
        this.context = context;
        this.listener = listener;
    }
    public class MyViewAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageBook;
        public CardView cardBooks;
        public TextView book_name , count_book , price_book , status_teks;
        public MyViewAdapter(View itemView){
            super(itemView);
            imageBook = itemView.findViewById(R.id.image_book);
            book_name = itemView.findViewById(R.id.book_name);
            count_book = itemView.findViewById(R.id.count_book);
            price_book = itemView.findViewById(R.id.price_book);
            status_teks = itemView.findViewById(R.id.status_teks);
            cardBooks = itemView.findViewById(R.id.card_books);
            cardBooks.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onBookClicked(booksList.get(getAdapterPosition()));
        }
    }

    @NonNull
    @Override
    public MyViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_books , parent , false);
        return new MyViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewAdapter holder, int position) {
        Books books = booksList.get(position);
        holder.book_name.setText("Nama : " + books.getBookName());
        holder.count_book.setText("Jumlah : " + String.valueOf(books.getCountBook()));
        holder.price_book.setText("Harga : " + String.valueOf(books.getPriceBook()));
        holder.status_teks.setText("Status : " + books.getStatus());
        Glide.with(context)
                .load(new Config().getServerAddress() + "/books_list/" + books.getImagePath())
                .priority(Priority.HIGH)
                .fitCenter()
                .placeholder(R.mipmap.ic_books)
                .error(R.mipmap.ic_books)
                .into(holder.imageBook);
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }
    public interface BooksListener {
        void onBookClicked(Books books);
    }
}
