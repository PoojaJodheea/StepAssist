package com.example.stepassist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReviewManager {
    private static final String TAG = "ReviewManager";
    private static final String PREF_NAME = "StepAssistReviews";

    public interface ReviewEditListener {
        void onEditRequested(String reviewText, float ratingValue, JSONObject originalReview);
    }

    public static void showReviews(Context context, LinearLayout container, String locationId, ReviewEditListener listener) {
        container.removeAllViews();

        List<JSONObject> stored = getSavedReviews(context, locationId);
        List<JSONObject> reviews = new ArrayList<>(stored);
        reviews.addAll(getFakeReviews());

        for (JSONObject review : reviews) {
            View reviewView = LayoutInflater.from(context).inflate(R.layout.review_item, container, false);
            TextView user = reviewView.findViewById(R.id.review_user);
            RatingBar rating = reviewView.findViewById(R.id.review_rating);
            TextView comment = reviewView.findViewById(R.id.review_text);
            Button edit = reviewView.findViewById(R.id.edit_button);
            Button delete = reviewView.findViewById(R.id.delete_button);

            try {
                String reviewer = review.optString("user", "Anonymous");
                float ratingValue = (float) review.optDouble("rating", 0);
                String reviewText = review.optString("review", "");

                user.setText(reviewer);
                rating.setRating(ratingValue);
                comment.setText(reviewText);

                if (reviewer.equals("Lara Croft")) {
                    edit.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);

                    // Set white text color for buttons
                    edit.setTextColor(Color.WHITE);
                    delete.setTextColor(Color.WHITE);

                    // Optionally, set button background color if needed
                    edit.setBackgroundColor(Color.DKGRAY);
                    delete.setBackgroundColor(Color.DKGRAY);

                    edit.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onEditRequested(reviewText, ratingValue, review);
                        }
                    });

                    delete.setOnClickListener(v -> {
                        stored.remove(review);
                        saveReviews(context, locationId, stored);
                        showReviews(context, container, locationId, listener);
                        Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Hide buttons for other users
                    edit.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error rendering review", e);
            }

            container.addView(reviewView);

            // Add separator line
            View separator = new View(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1); // 1 pixel height line
            params.setMargins(0, 8, 0, 8); // spacing around separator
            separator.setLayoutParams(params);
            separator.setBackgroundColor(Color.LTGRAY);

            container.addView(separator);
        }
    }


    public static void submitReview(Context context, String locationId, float rating, String reviewText) {
        submitReview(context, locationId, "Lara Croft", rating, reviewText, null);
    }

    public static void submitReview(Context context, String locationId, String username, float rating, String reviewText) {
        submitReview(context, locationId, username, rating, reviewText, null);
    }

    // New method supporting update if existingReview != null
    public static void submitReview(Context context, String locationId, String username, float rating, String reviewText, JSONObject existingReview) {
        List<JSONObject> reviews = getSavedReviews(context, locationId);

        try {
            if (existingReview != null) {
                // Update existing review fields
                existingReview.put("rating", rating);
                existingReview.put("review", reviewText);
            } else {
                // Add new review
                JSONObject review = new JSONObject();
                review.put("user", username);
                review.put("rating", rating);
                review.put("review", reviewText);
                reviews.add(review);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error creating/updating review JSON", e);
        }

        saveReviews(context, locationId, reviews);
    }

    public static void saveReviews(Context context, String locationId, List<JSONObject> reviews) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        JSONArray array = new JSONArray();
        for (JSONObject obj : reviews) {
            array.put(obj);
        }

        editor.putString(locationId, array.toString());
        editor.apply();
    }

    public static List<JSONObject> getSavedReviews(Context context, String locationId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(locationId, "[]");

        List<JSONObject> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getJSONObject(i));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading saved reviews", e);
        }

        return list;
    }

    private static List<JSONObject> getFakeReviews() {
        List<JSONObject> fakes = new ArrayList<>();
        String[] names = {"Alex", "Maria", "JohnDoe42", "QueenBee", "Mohit", "Julia98", "Zorah", "Claudia8"};
        String[] texts = {"Very accessible!", "Could be better.", "Helpful facilities.", "Well maintained.",
                "Loved the location.", "Not great.", "Facilities need an upgrade!", "Great location!"};
        Random rand = new Random();

        for (int i = 0; i < 2; i++) {
            JSONObject fake = new JSONObject();
            try {
                fake.put("user", names[rand.nextInt(names.length)]);
                fake.put("rating", 2 + rand.nextInt(4));
                fake.put("review", texts[rand.nextInt(texts.length)]);
            } catch (JSONException e) {
                Log.e(TAG, "Error generating fake reviews", e);
            }
            fakes.add(fake);
        }

        return fakes;
    }
}
