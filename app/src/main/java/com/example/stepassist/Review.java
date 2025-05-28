package com.example.stepassist;

public class Review {
    private final String username;
    private String text;
    private float rating;

    public Review(String username, String text, float rating) {
        this.username = username;
        this.text = text;
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public float getRating() {
        return rating;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
