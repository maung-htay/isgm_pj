package com.isgm.camreport.model;

import androidx.annotation.NonNull;

public class Route {
    private int id;
    private String text;

    public Route() { }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return this.text;
    }

}
