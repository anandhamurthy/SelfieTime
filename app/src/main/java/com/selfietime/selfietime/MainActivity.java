package com.selfietime.selfietime;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.selfietime.selfietime.Fragments.HomeFragment;
import com.selfietime.selfietime.Fragments.NotificationFragment;
import com.selfietime.selfietime.Fragments.ProfileFragment;
import com.selfietime.selfietime.Fragments.SearchFragment;
import com.theartofdev.edmodo.cropper.CropImage;


public class MainActivity extends AppCompatActivity {

    Fragment selectedfragment = null;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private BottomNavigationView mBottomNavigation;
    private Uri mImageUri;
    private AdView mAdView;
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedfragment = new HomeFragment();
                            break;
                        case R.id.nav_search:
                            selectedfragment = new SearchFragment();
                            break;
                        case R.id.nav_add:
                            selectedfragment = null;
                            CropImage.activity()
                                    .start(MainActivity.this);
                            break;
                        case R.id.nav_heart:
                            selectedfragment = new NotificationFragment();
                            break;
                        case R.id.nav_profile:
                            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                            editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.apply();
                            selectedfragment = new ProfileFragment();
                            break;
                    }
                    if (selectedfragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                selectedfragment).commit();
                    }

                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUsersDatabase.keepSynced(true);


        }

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
        mBottomNavigation = findViewById(R.id.bottom_navigation);
        mBottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();


            CharSequence options[] = new CharSequence[]{"Add Selfie"};

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Options");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position) {

                    if (position == 0) {
                        Intent SelfieIntent = new Intent(MainActivity.this, NewSelfieActivity.class);
                        SelfieIntent.putExtra("image_url", mImageUri.toString());
                        startActivity(SelfieIntent);
                    }

                }
            });

            builder.show();

        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
