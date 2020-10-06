/*
  The ProfileActivity simply displays the user’s information in an easy-to-read format.
  Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userFullname, userLocation, userDob, userGender;
    private TextView userPhoneNumber;

    private CircleImageView userProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserID);

        userName = findViewById(R.id.profileUsername);
        userFullname = findViewById(R.id.profileFullName);
        userLocation = findViewById(R.id.profileLocation);
        userDob = findViewById(R.id.profileDob);
        userPhoneNumber = findViewById(R.id.profilePhoneNumber);
        userGender = findViewById(R.id.profileGender);

        TextView userStatus = findViewById(R.id.profileStatus);
        userProfilePicture = findViewById(R.id.profileProfileImage);

        userRef.addValueEventListener(new ValueEventListener() {
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

                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(userProfilePicture);
                    userName.setText("@" + username);
                    userFullname.setText(fullName);
                    userLocation.setText("Location: " + location);
                    userDob.setText("DOB: " + DOB);
                    userPhoneNumber.setText("Phone Number: " + phoneNumber);
                    userGender.setText("Gender: " + gender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
