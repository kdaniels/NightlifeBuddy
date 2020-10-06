/*
  The setup activity can be accessed through the registration page and through the main activity,
  though only if the user account has not been assigned an entry in the User database.
  The activity gets the user information from the input fields “username” “full name” “location” and casts
  these to variables before storing online. Additionally, the user can also select a personalised profile image by clicking on the image placeholder.
  Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText userName, fullName, locationName;
    private Button saveUserInformation;
    private CircleImageView profileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference userProfileImageRef;

    private String currentUserID;
    final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users")
            .child(currentUserID);
        userProfileImageRef = FirebaseStorage.getInstance().getReference()
                .child("Profile Images");

        userName = findViewById(R.id.txt_setup_username);
        fullName = findViewById(R.id.txt_setup_full_name);
        locationName = findViewById(R.id.txt_setup_location);

        saveUserInformation = findViewById(R.id.butSaveProfile);
        profileImage = findViewById(R.id.profile_image);
        loadingBar = new ProgressDialog(this);

        saveUserInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageIntent = new Intent();
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                imageIntent.setType("image/*");
                startActivityForResult(imageIntent, GALLERY_PICK);
            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        // Get profile image and display back to user
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                    } else
                        {
                            Toast.makeText(SetupActivity.this, "Please Select Profile Image", Toast.LENGTH_SHORT).show();
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * On activity result used for user profile picture and cropping facility
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK && data!=null)
        {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if(taskSnapshot.getMetadata() != null)
                        {
                            if(taskSnapshot.getMetadata().getReference() != null)
                            {
                                Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadURL = uri.toString();
                                        userRef.child("profileimage").setValue(downloadURL)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Intent setupIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                    startActivity(setupIntent);
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }
                });
            } else {
                Toast.makeText(SetupActivity.this, "Error Occurred Please Try Again", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void saveAccountSetupInformation() {
        String username = userName.getText().toString();
        String fullname = fullName.getText().toString();
        String location = locationName.getText().toString();

        if(TextUtils.isEmpty(username)) // Validation for text inputs
        {
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Please enter full name", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(location))
        {
            Toast.makeText(this, "Please enter your location", Toast.LENGTH_SHORT).show();
        } else
            {
                loadingBar.setTitle(R.string.loadingTitle);
                loadingBar.setMessage("Please wait while we are creating your account");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                HashMap userMap = new HashMap();
                userMap.put("username", username);
                userMap.put("fullname", fullname);
                userMap.put("location", location);
                userMap.put("accountType", "Standard User");
                userMap.put("gender", "Please select");
                userMap.put("dob", "none");
                userMap.put("phoneNumber", "none");
                userMap.put("lastLocation", "none");
                userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()) {
                            sendToMainActivity();
                            loadingBar.dismiss();
                            Toast.makeText(SetupActivity.this, "Account Success", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = task.getException().getMessage();
                            loadingBar.dismiss();
                            Toast.makeText(SetupActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
    }

    /**
     * Send user back the main activity after completing setup
     */
    private void sendToMainActivity() {
        Intent setupIntent = new Intent(SetupActivity.this, MainActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
