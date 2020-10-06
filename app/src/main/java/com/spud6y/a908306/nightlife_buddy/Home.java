/*
  The home activity checks if a user has previously signed on.
  If they are already authenticated and reopen the app, they should be directed to the main activity rather than through the login process.
  Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Check if there is an active user - sign in and redirect to main
public class Home extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if(firebaseUser != null) { // Check if user has signed in
            startActivity(new Intent(Home.this, MainActivity.class));
        }
    }
}
