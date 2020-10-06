/*
  The User Post Activity allows a user to add a new post to the social feed on the Main Activity.
  The user should be able to click on the image placeholder, select an image from their gallery,
  add a post description and click on the upload post button.
  Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import androidx.appcompat.widget.Toolbar;

import static com.spud6y.a908306.nightlife_buddy.SetupActivity.GALLERY_PICK;

public class UserPostActivity extends AppCompatActivity {

    private ImageButton selectPostImage;
    private EditText postDescription;

    private ProgressDialog loadingPrompt;

    private Uri imageUri;
    private String description;

    private StorageReference postImageRef;
    private DatabaseReference userRef, postRef;

    private String currentDateSave, currentTimeSave, randomPostName;
    private String downloadURL, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_post);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postImageRef = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        selectPostImage = findViewById(R.id.imgButSelectImage);
        postDescription = findViewById(R.id.edtPostDescription);
        Button updatePost = findViewById(R.id.butUpdatePost);
        loadingPrompt = new ProgressDialog(this);

        // Assign toolbar and add back to home property
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.postTitle);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageGallery();
            }
        });

        updatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePost();
            }
        });
    }

    private void validatePost() {
        String description = postDescription.getText().toString();

        if(imageUri == null)
        {
            Toast.makeText(UserPostActivity.this, "Select Post Image", Toast.LENGTH_SHORT);
        } else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(UserPostActivity.this, "Enter Post Description", Toast.LENGTH_SHORT).show();
        } else
            {
                loadingPrompt.setTitle(R.string.loadingTitle);
                loadingPrompt.setMessage("Please wait while we upload your post");
                loadingPrompt.show();
                loadingPrompt.setCanceledOnTouchOutside(true);

                storeImageToDatabase();
            }
    }

    private void storeImageToDatabase() {
        Calendar postDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        currentDateSave = currentDate.format(postDate.getTime());

        Calendar postTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        currentTimeSave = currentTime.format(postTime.getTime());

        randomPostName = currentDateSave + currentTimeSave;

        StorageReference filePath = postImageRef.child("Post Images").child(imageUri.getLastPathSegment()
                + randomPostName + ".jpg"); // Assign random name to avoid naming conflicts

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                downloadURL = uri.toString();
                                savePostToDatabase();
                            }
                        });
                    }
                }
            }
        });
    }

    // Code to save post information to database
    private void savePostToDatabase() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    description = postDescription.getText().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", currentUserId);
                    postMap.put("date", currentDateSave);
                    postMap.put("time", currentTimeSave);
                    postMap.put("description", description);
                    postMap.put("postimage", downloadURL);
                    postMap.put("profileimage", profileImage);
                    postMap.put("fullname", fullname);
                    postRef.child(currentUserId + randomPostName).updateChildren(postMap) // Assign random
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        sendToMainActivity();
                                        Toast.makeText(UserPostActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();

                                        loadingPrompt.dismiss();
                                    } else {
                                        Toast.makeText(UserPostActivity.this, "Update Error", Toast.LENGTH_SHORT).show();
                                        loadingPrompt.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openImageGallery() {
        Intent imageIntent = new Intent();
        imageIntent.setAction(Intent.ACTION_GET_CONTENT);
        imageIntent.setType("image/*");
        startActivityForResult(imageIntent, GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }
    }

    // Check if user has selected back button from toolbar
    // IF selected send back to main activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            sendToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendToMainActivity() {
        Intent mainIntent = new Intent(UserPostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
