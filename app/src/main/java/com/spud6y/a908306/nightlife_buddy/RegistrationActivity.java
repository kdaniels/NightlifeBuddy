/*
  If the user has entered correct credentials, the system checks whether an account exists with the same username,
  and if not creates an entry in the authentication system.
  The user is then logged in and sent to the main activity.
  If the user has no entry in the database, they should be redirected to the setup page to select a profile image,
  username, full name and location.
  Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    private ProgressBar progressBar; // Assign variables for user interaction
    private EditText email;
    private EditText password, confirmPassword;

    private FirebaseAuth mAuth; // Assign Firebase variable for user authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        progressBar = findViewById(R.id.progressBar);
        email = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtPassword);
        confirmPassword = findViewById(R.id.edtPasswordRepeat);
        Button register = findViewById(R.id.butSignUp);

        Toolbar toolbar = findViewById(R.id.toolbar); // Assign toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbarRegistration);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // If user clicks on registration, take in credentials and assign new user
                progressBar.setVisibility(View.VISIBLE);
                if(confirmPassword()) // Check if passwords match - returns either true or false
                {
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(),
                        password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(RegistrationActivity.this, "Registered Successfully",
                                    Toast.LENGTH_LONG).show();
                                sendUserToSetupActivity();
                                email.setText(""); // Erase data in email and password edit text boxes
                                password.setText("");
                            } else {
                                Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), // Return error from Firebase
                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE); // If password do not match, remove progress bar and display error
                }
            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(RegistrationActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private boolean confirmPassword() {
        String userPassword = password.getText().toString();
        String userPasswordSecond = confirmPassword.getText().toString();
        if(!userPassword.equals(userPasswordSecond)) {
            Toast.makeText(this, "Passwords not the same", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

}
