package com.example.parkmapofficial.userrating;

import java.io.Serializable;

public class UserRating implements Serializable {
    private String username;
    private String comment;
    private int rating;

    public UserRating(String username, String comment, int rating) {
        this.username = username;
        this.comment = comment;
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }
}

