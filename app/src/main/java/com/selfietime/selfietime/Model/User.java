package com.selfietime.selfietime.Model;

public class User {
    public String user_name, profile_image, user_id, bio, date_of_birth, email_id, place, terms, gender, device_token;

    public User() {
    }

    public User(String user_name, String profile_image, String user_id, String bio, String date_of_birth, String email_id, String place, String terms, String gender, String device_token) {
        this.user_name = user_name;
        this.profile_image = profile_image;
        this.user_id = user_id;
        this.bio = bio;
        this.date_of_birth = date_of_birth;
        this.email_id = email_id;
        this.place = place;
        this.terms = terms;
        this.gender = gender;
        this.device_token = device_token;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }
}
