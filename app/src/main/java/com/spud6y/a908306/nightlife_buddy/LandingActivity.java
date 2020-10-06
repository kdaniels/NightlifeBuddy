/*
  The landing page is the opening screen to the “Nightlife Companion” application.
  From here you can see the main logo and select to either login or register an account.
  Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Assign button for login
        Button buttonLogin = findViewById(R.id.butLandingLogin);
        // Assign button for registration
        Button buttonRegister = findViewById(R.id.butLandingRegister);

        // Call the register intent for user registration
        buttonRegister.setOnClickListener(new View.OnClickListener() { // If user clicks on button - call intent
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        // Call the login intent for user login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
