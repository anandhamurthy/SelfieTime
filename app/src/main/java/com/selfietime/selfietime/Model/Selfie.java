package com.selfietime.selfietime.Model;

public class Selfie {
    private String selfie_id;
    private String selfie_image;
    private String selfie_description;
    private String user_id;

    public Selfie() {
    }

    public Selfie(String selfie_id, String selfie_image, String selfie_description, String user_id) {
        this.selfie_id = selfie_id;
        this.selfie_image = selfie_image;
        this.selfie_description = selfie_description;
        this.user_id = user_id;
    }

    public String getSelfie_id() {
        return selfie_id;
    }

    public void setSelfie_id(String selfie_id) {
        this.selfie_id = selfie_id;
    }

    public String getSelfie_image() {
        return selfie_image;
    }

    public void setSelfie_image(String selfie_image) {
        this.selfie_image = selfie_image;
    }

    public String getSelfie_description() {
        return selfie_description;
    }

    public void setSelfie_description(String selfie_description) {
        this.selfie_description = selfie_description;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
