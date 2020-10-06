/**
 * The main activity of the application handles several operations internally and externally.
 * Side navigation from the top left, settings option from the top right, ‘social media’ feed and map fob at the bottom right.
 * Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView postList;

    private TextView navProfileUsername;
    private TextView navProfileEmail;
    private ImageView navProfileImage;

    private FirebaseAuth mAuth; // Assign Firebase authentication for user credentials
    private FirebaseUser firebaseUser;
    private DatabaseReference userRef, postRef;

    private String currentUserID;
    private boolean sosChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.alternate_app_name); // Set toolbar title

        FloatingActionButton fab = findViewById(R.id.fabSOS);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Closing Maps", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                sendToMapActivity();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Inflating navigation view to access elements
        View header = navigationView.getHeaderView(0);
        //View navView = navigationView.inflateHeaderView(R.layout.nav_header_main);

        // Navigation bar casting
        navProfileUsername = header.findViewById(R.id.navProfileUsername);
        navProfileEmail = header.findViewById(R.id.navProfileEmail);
        navProfileImage = header.findViewById(R.id.navProfilePicture);

        TextView userEmail = findViewById(R.id.txtSafetyMessage);
        ImageButton userMaps = findViewById(R.id.imgButMaps);

        // User post casting
        // New posts should appear at the top while older posts underneath
        postList = (RecyclerView) findViewById(R.id.all_user_posts); // Recycler list for user posts
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        ImageButton addNewPostBut = (ImageButton) findViewById(R.id.imgNewPost);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        firebaseUser = mAuth.getCurrentUser(); // Get signed in user - has to be parsed from login

        // Navigation setting the profile username and password
        currentUserID = mAuth.getCurrentUser().getUid();

        // Get current user database entry and update navigation details
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("username")) // Check if username exists in database avoiding errors
                    {
                        String username = dataSnapshot.child("username").getValue().toString();
                        navProfileUsername.setText(username);
                    }
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        navProfileEmail.setText(firebaseUser.getEmail());
                        //navProfileEmail.setText(fullname);
                    }
                    else
                        {
                            Toast.makeText(MainActivity.this, "Profile Information Error", Toast.LENGTH_SHORT).show();
                        }

                    String profileimage = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(navProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Send the user to the maps page
        userMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToMapActivity();
            }
        });

        addNewPostBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToPostActivity();
            }
        });

        displayUserPosts();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendToLoginActivity();
        } else {
            checkUserDatabaseExistence();
        }
    }

    private void displayUserPosts() {
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(postRef, Posts.class)
                .build();
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int i, @NonNull Posts posts) {
                        holder.username.setText(posts.getFullname());
                        holder.date.setText(" " + posts.getDate());
                        holder.time.setText(" " + posts.getTime());
                        holder.description.setText(posts.getDescription());
                        Picasso.get().load(posts.getProfileimage()).into(holder.userPostImage);
                        Picasso.get().load(posts.getPostimage()).into(holder.postImage);
                    }

                    @NonNull
                    @Override
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.posts_display_layout,parent,false);
                        PostsViewHolder viewHolder = new PostsViewHolder(view);
                        return viewHolder;
                    }
                };
            postList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView username, date, time, description;
        CircleImageView userPostImage;
        ImageView postImage;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            username = itemView.findViewById(R.id.txtPostName);
            date = itemView.findViewById(R.id.txtPostDate);
            time = itemView.findViewById(R.id.txtPostTime);
            description = itemView.findViewById(R.id.postDescription);
            userPostImage = itemView.findViewById(R.id.postProfilePicture);
            postImage = itemView.findViewById(R.id.postImage);
        }
    }

    // Check if the user has an account in the Firebase database
    // IF NOT send to the setup activity to reinstate account
    private void checkUserDatabaseExistence() {
        final String currentUserId = mAuth.getCurrentUser().getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(currentUserId))
                {
                    sendToSetupActivity(); // IF NOT in the database
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendToMapActivity() {
        Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(mapIntent);
    }

    private void sendToPostActivity() {
        Intent userPostIntent = new Intent(MainActivity.this, UserPostActivity.class);
        startActivity(userPostIntent);
    }

    private void sendToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    private void sendToFriendsActivity() {
        Intent friendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void createSOSRequest() {
        // Write new SOS to account database
        final String SOS = "SOS";
        sosChecked = false;
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!sosChecked) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.child("SOS").exists()) {
                            userRef.child(currentUserID).child("SOS").removeValue();
                            Toast.makeText(MainActivity.this, "SOS Removed", Toast.LENGTH_SHORT).show();
                            sosChecked = true;
                        } else {
                            HashMap sosMap = new HashMap();
                            sosMap.put("SOS", SOS);
                            userRef.child(currentUserID).updateChildren(sosMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    Toast.makeText(MainActivity.this, "SOS Updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                            sosChecked = true;
                        }
                    } else {
                        sosChecked = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            sendToSettingsActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_new_post) {
            sendToPostActivity();
        } else if (id == R.id.nav_profile) {
            sendToProfileActivity();
        } else if (id == R.id.nav_friends) {
            sendToFriendsActivity();
        } else if (id == R.id.nav_manage) {
            sendToSettingsActivity();
        } else if (id == R.id.nav_share) {
            createSOSRequest();
        } else if (id == R.id.nav_logout) { // Sign user out from side navigation
            FirebaseAuth.getInstance().signOut(); // Sign out user profile from application
            Intent logOut = new Intent(MainActivity.this, LandingActivity.class);
            logOut.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear tasks to prevent user from reversing action
            logOut.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logOut);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
