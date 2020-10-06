/*
  The Settings Activity allows the user to view their existing information and update the other fields
  (including date of birth, phone number and gender). However, the user cannot update their profile image
  from the settings page and may only be accessed through the setup activity.
  Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private EditText userName, userFullname, userLocation, userDob, userGender;
    private EditText userStatus, userPhoneNumber;
    private CircleImageView userProfileImage;

    private DatabaseReference settingsUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Assign Firebase references
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Get user ID to display on settings page
        String currentUserId = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserId);

        // Assign toolbar and add back to home property
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.settingsToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Casting fields
        userName = findViewById(R.id.edtSettingsUsername);
        userFullname = findViewById(R.id.edtSettingsFullName);
        userLocation = findViewById(R.id.edtSettingsLocation);
        userDob = findViewById(R.id.edtSettingsDOB);
        userPhoneNumber = findViewById(R.id.edtSettingsNumber);
        userGender = findViewById(R.id.edtSettingsGender);

        userStatus = findViewById(R.id.edtSettingsStatus);
        userStatus.setEnabled(false);

        Button updateAccountSettings = findViewById(R.id.butUpdateSettings);
        userProfileImage = findViewById(R.id.settingsProfileImage);

        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();
                    String fullName = dataSnapshot.child("fullname").getValue().toString();
                    String location = dataSnapshot.child("location").getValue().toString();
                    String DOB = dataSnapshot.child("dob").getValue().toString();
                    String phoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();

                    String status = dataSnapshot.child("accountType").getValue().toString();
                    String statusMessage = status;

                    if(dataSnapshot.child("SOS").exists())
                    {
                        statusMessage = ("SOS: Enabled " + "Account: " + status);
                    } else {
                        statusMessage = ("Account: " + status);
                    }

                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    userStatus.setText(statusMessage);
                    userName.setText(username);
                    userFullname.setText(fullName);
                    userLocation.setText(location);

                    // Check if date of birth and/or gender fields are default
                    if(!DOB.equals("none"))
                    {
                        userDob.setText(DOB);
                    }
                    if(!phoneNumber.equals("none"))
                    {
                        userPhoneNumber.setText(phoneNumber);
                    }
                    if(!gender.equals("Please select")){
                        userGender.setText(gender);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateSettingsInformation();
            }
        });
    }

    private void validateSettingsInformation() {
        String username = userName.getText().toString();
        String fullname = userFullname.getText().toString();
        String location = userLocation.getText().toString();
        String dob = userDob.getText().toString();
        String number = userPhoneNumber.getText().toString();
        String gender = userGender.getText().toString();

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(SettingsActivity.this, "Please enter information", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(fullname))
        {} else if(TextUtils.isEmpty(location))
        {} else if(TextUtils.isEmpty(dob))
        {} else if(TextUtils.isEmpty(gender))
        {} else {
            updateAccountInformation(username, fullname, location, dob, number, gender);
        }
    }

    private void updateAccountInformation(String username, String fullname, String location,
                                          String dob, String number, String gender) {
        HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("location", location);
            userMap.put("dob", dob);
            userMap.put("phoneNumber", number);
            userMap.put("gender", gender);
        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    sendToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Settings Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Error Updating", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendToMainActivity(){
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
