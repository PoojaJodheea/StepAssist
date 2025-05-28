package com.example.stepassist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<Review> reviewList;
    private final Context context;
    private final OnReviewActionListener listener;

    public interface OnReviewActionListener {
        void onEdit(Review review, int position);
        void onDelete(int position);
    }

    public ReviewAdapter(Context context, List<Review> reviews, OnReviewActionListener listener) {
        this.context = context;
        this.reviewList = reviews;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.reviewUser.setText(review.getUsername());
        holder.reviewText.setText(review.getText());
        holder.reviewRating.setRating(review.getRating());

        // Only show Edit/Delete if user is "Lara Croft"
        if ("Lara Croft".equals(review.getUsername())) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> listener.onEdit(review, position));
            holder.deleteButton.setOnClickListener(v -> listener.onDelete(position));
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void updateReview(int position, Review updatedReview) {
        reviewList.set(position, updatedReview);
        notifyItemChanged(position);
    }

    public void removeReview(int position) {
        reviewList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView reviewUser, reviewText;
        RatingBar reviewRating;
        Button editButton, deleteButton;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewUser = itemView.findViewById(R.id.review_user);
            reviewText = itemView.findViewById(R.id.review_text);
            reviewRating = itemView.findViewById(R.id.review_rating);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
