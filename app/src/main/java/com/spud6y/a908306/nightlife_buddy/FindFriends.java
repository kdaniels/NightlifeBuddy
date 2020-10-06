/**
 * The FindFriends activity defines the getters and setters for the friends system information
 * Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

public class FindFriends {

    public String fullname, username, profileimage;
    public String SOS;

    public FindFriends() {
        // Empty Constructor
    }

    public FindFriends(String profileimage, String fullname, String username) {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.username = username;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
