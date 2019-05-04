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
import com.example.asus.taskapp.Model.SoldBooks;
import com.example.asus.taskapp.R;

import java.util.List;

public class SoldBooksAdapter extends RecyclerView.Adapter<SoldBooksAdapter.MyViewAdapter> {
    public List<SoldBooks> soldBooksList;
    public Context context;
    public SoldBooksListener listener;
    public SoldBooksAdapter(Context context , List<SoldBooks> soldBooksList , SoldBooksListener listener){
        this.context = context;
        this.soldBooksList = soldBooksList;
        this.listener = listener;
    }
    public class MyViewAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {
        public CardView soldCard;
        public ImageView imageSold;
        public TextView soldBooksTitle , soldBooksCount , soldBooksPrice , soldBooksStatus , soldBooksBuyer;
        public MyViewAdapter(View itemView){
            super(itemView);
            soldCard = itemView.findViewById(R.id.sold_card);
            imageSold = itemView.findViewById(R.id.imageSold);
            soldBooksTitle = itemView.findViewById(R.id.sold_books_title);
            soldBooksCount = itemView.findViewById(R.id.sold_books_count);
            soldBooksPrice = itemView.findViewById(R.id.sold_books_price);
            soldBooksStatus = itemView.findViewById(R.id.sold_books_status);
            soldBooksBuyer = itemView.findViewById(R.id.sold_books_buyer);
            soldCard.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onSoldClick(soldBooksList.get(getAdapterPosition()));
        }
    }

    @NonNull
    @Override
    public SoldBooksAdapter.MyViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sold , parent , false);
        return new MyViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoldBooksAdapter.MyViewAdapter holder, int position) {
        SoldBooks books = soldBooksList.get(position);
        holder.soldBooksTitle.setText("Nama : " + books.getBookName());
        holder.soldBooksCount.setText("Jumlah : " + String.valueOf(books.getCountBook()));
        holder.soldBooksPrice.setText("Total : " + String.valueOf(books.getTotalPrice()));
        holder.soldBooksStatus.setText("Status : " + books.getStatus());
        holder.soldBooksBuyer.setText("Pembeli : " + books.getBuyer());
        Glide.with(context)
                .load(new Config().getServerAddress() + "/books_sold/" + books.getImagePath())
                .priority(Priority.HIGH)
                .fitCenter()
                .placeholder(R.mipmap.ic_books)
                .error(R.mipmap.ic_books)
                .into(holder.imageSold);
    }

    @Override
    public int getItemCount() {
        return soldBooksList.size();
    }
    public interface SoldBooksListener {
        void onSoldClick(SoldBooks soldBooks);
    }
}
