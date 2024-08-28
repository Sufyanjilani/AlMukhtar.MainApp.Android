package com.example.myapplication.Models;

public class ListModel {

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    String title;
    String name;
    String image;

    public ListModel(String title, String name, String image) {
        this.title = title;
        this.name = name;
        this.image = image;
    }



}
