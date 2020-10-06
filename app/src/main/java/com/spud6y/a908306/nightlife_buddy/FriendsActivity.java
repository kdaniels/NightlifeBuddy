/*
  The Friends Activity allows users to search for other users on the system using their full name.
  Currently, the system displays all the users with that name.
  For example, searching for “Kyle Daniels” returns multiple accounts (as can be seen to the left).
  Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private EditText searchInput;
    private RecyclerView searchResultList;

    private DatabaseReference allUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        allUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Assign toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.friendsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchResultList = findViewById(R.id.friendsSearchResult);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        searchInput = findViewById(R.id.edtFriendsInput);
        ImageButton searchButton = findViewById(R.id.friendsSearchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = searchInput.getText().toString();

                searchPeopleandFriends(input);
            }
        });
    }

    /**
     * Function to search for people and friends from the database reference
     * @param input text bot input to use for searching through users
     */
    private void searchPeopleandFriends(final String input) {
        Query searchQuery = allUserRef.orderByChild("fullname") // Custom query to cycle through user's full names
                .startAt(searchInput + "").endAt(searchInput + "\uf8ff");

        // Set the options for displaying users
        FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(allUserRef, FindFriends.class)
                //.setQuery(allUserRef.child(input), FindFriends.class)
                //.setQuery(searchQuery, FindFriends.class)
                .build();

        // Search for users in Firebase Database
        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder findFriendsViewHolder, int i, @NonNull FindFriends findFriends) {
                String fullname = findFriends.getFullname();
                if(input.equals(fullname)){
                    findFriendsViewHolder.username.setText(findFriends.getUsername());
                    findFriendsViewHolder.fullName.setText(findFriends.getFullname());
                    Picasso.get().load(findFriends.getProfileimage()).into(findFriendsViewHolder.profileimage);

                    findFriendsViewHolder.sosLayout.setVisibility(View.VISIBLE);
                } else {
                    findFriendsViewHolder.friendsLayout.setVisibility(View.INVISIBLE);
                }
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_user_display_layout,parent,false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        searchResultList.setAdapter(firebaseRecyclerAdapter); // Set adapter for the user list
        firebaseRecyclerAdapter.startListening(); // Start the recycler adapter
    }

    /**
     * Class extending recycler view to allow for custom declarations
     */
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView username, fullName;
        CircleImageView profileimage;
        LinearLayout sosLayout;
        RelativeLayout friendsLayout;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            username = itemView.findViewById(R.id.allUsersUsername);
            fullName = itemView.findViewById(R.id.allUsersFullName);
            profileimage = itemView.findViewById(R.id.allUsersProfilePicture);
            sosLayout = itemView.findViewById(R.id.friendsSosLayout);
            friendsLayout = itemView.findViewById(R.id.friendsIndLayout);
        }
    }
}
