package com.selfietime.selfietime.Model;

public class Wish {

    private String date;
    private String from;
    private String wish_id;
    private String wish_image;
    private String to;
    private String name;
    private String greeting;

    public Wish() {
    }

    public Wish(String date, String from, String wish_id, String wish_image, String to, String name, String greeting) {
        this.date = date;
        this.from = from;
        this.wish_id = wish_id;
        this.wish_image = wish_image;
        this.to = to;
        this.name = name;
        this.greeting = greeting;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getWish_id() {
        return wish_id;
    }

    public void setWish_id(String wish_id) {
        this.wish_id = wish_id;
    }

    public String getWish_image() {
        return wish_image;
    }

    public void setWish_image(String wish_image) {
        this.wish_image = wish_image;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
}
