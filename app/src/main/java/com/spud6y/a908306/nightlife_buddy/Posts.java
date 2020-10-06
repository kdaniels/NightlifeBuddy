/*
  The Posts activity defines the getters and setters for the user post information
  Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

public class Posts {

    private String uid, date, time, fullname, description;
    private String postimage;
    private String profileimage;

    public Posts() {
        // Default constructor if method below invokes error
    }

    public Posts(String uid, String date, String time, String fullname, String description, String postimage, String profileimage) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.fullname = fullname;
        this.description = description;
        this.postimage = postimage;
        this.profileimage = profileimage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

}
