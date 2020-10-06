/**
 * The map activity handles the current user’s location, in addition to, the other users on the system.
 * Once opened, the map should direct the user to their current location and allow them to drag the map to other user’s locations in their area.
 * From here, the user should be able to tell if anyone requires help, if public services are nearby and be able to call these users through the interface.
 * Kyle Daniels 908306
 */

package com.spud6y.a908306.nightlife_buddy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code = 99;;

    private DatabaseReference rootRef; // Database references
    private DatabaseReference usersRef;
    private FusedLocationProviderClient mFusedClient;
    private double longitude;  // Variables for map coordinates
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkUserLocationPermission();
        }

        // Assign Firebase references
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Get user ID to display on settings page
        String currentUserId = mAuth.getCurrentUser().getUid();
        // Add database to store users coordinates
        usersRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUserId);

        // Add reference to database root for location mapping
        rootRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            getUserLocation();
            updateUserLocations();
        }

    }

    // Check if the user has sufficient permissions
    // If the user does not, update and ask for permissions
    public boolean checkUserLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        }
        else {
            return true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case Request_User_Location_Code:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(googleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show(); // error message
                }
                return;
        }
    }

    // Build Google API Client
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Location lastLocation = location;

        if(currentUserLocationMarker != null) {
            currentUserLocationMarker.remove();
        }

        // Get user coordinates
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Set marker options
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(getString(R.string.userLocation));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        //currentUserLocationMarker = mMap.addMarker(markerOptions); // Add marker

        // Update camera on map
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomBy(14)); // Zoom by 14 - UPDATABLE

        if(googleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Check to see if the user permissions have been granted for location services
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Updates the pointers on the map and defines the onLongClick listener for the marker prompts
    private void updateUserLocations() {
        rootRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String locationValidation = dataSnapshot.child("lastLocation").getValue().toString();
                if(!locationValidation.equals("none"))
                {
                    // Assign values to variables
                    String[] location = dataSnapshot.child("lastLocation").getValue().toString().split(",");
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();
                    String accountType = dataSnapshot.child("accountType").getValue().toString();
                    String markerTitle = (username + " - " + fullname);
                    boolean sosEnabled = false;

                    // Check if the user has SOS enabled
                    if(dataSnapshot.child("SOS").exists())
                    {
                        sosEnabled = true;
                    }

                    double lat = Double.parseDouble(location[0]); // Set latitude coordinate to that saved
                    double lng = Double.parseDouble(location[1]);

                    LatLng latLng = new LatLng(lat, lng); // Define lat/long from database last location

                    if(!dataSnapshot.getKey().equals(usersRef.getKey()))
                    {
                        if(accountType.equals("Public Service")) // Public service e.g. police
                        {
                            mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(markerTitle)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                .draggable(false));
                        } else if(accountType.equals("Event Organiser")) // Event organiser e.g. Club owner
                        {
                            mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(markerTitle)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                .draggable(false));
                        } else if(accountType.equals("Standard")) // Standard user defaults to red pointer
                        { // Additional if statement for SOS
                            if(sosEnabled)
                            {
                                mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(markerTitle + " SOS")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                    .draggable(false));
                            }
                            else
                            {
                                mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(markerTitle)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .draggable(false));
                            }
                        }
                    }
                }

                // Sets the conditions for long clicks on map markers
                // Users should be able to call other users
                mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                    @Override
                    public void onInfoWindowLongClick(Marker marker) {
                        String[] title = marker.getTitle().split(" "); // Split the title string to obtain username
                        String username = title[0];
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("Users");
                        myRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override // Get the phone number of user if long click on info window
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(final DataSnapshot childSnap : dataSnapshot.getChildren())
                                {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                    builder.setTitle(R.string.callTitle).setMessage(R.string.callPrompt)
                                         .setPositiveButton(R.string.callYes, new DialogInterface.OnClickListener() {
                                             @Override
                                             public void onClick(DialogInterface dialogInterface, int i) {
                                                 String number = childSnap.child("phoneNumber").getValue().toString();
                                                 phoneUser(number);
                                             }
                                         }).setNegativeButton(R.string.callNo, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Get the user's location
    // Check for sufficient permissions, first
    private void getUserLocation() {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null)
                        {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();

                            goToUserLocation();
                        }
                    }
                });
    }

    // Call dialer intent
    private void phoneUser(String number) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    // Move camera to the user's position
    private void goToUserLocation() {
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .draggable(true)
            .title("Your Location"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        String userLocation = (latitude + "," + longitude); // Create string to save to database
        saveUserCoordinates(userLocation); // Save users last coordinate to the database
    }

    // Save user's coordinates to the database
    private void saveUserCoordinates(String coordinates) {
        // Save user details on opening map
        HashMap userMap = new HashMap();
            userMap.put("lastLocation", coordinates);
        usersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    return;
                } else {
                    Toast.makeText(MapsActivity.this, "Error Updating", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
