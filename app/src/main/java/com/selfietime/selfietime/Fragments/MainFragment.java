package com.selfietime.selfietime.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.gms.ads.AdView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.selfietime.selfietime.CurvedBottomNavigationView;
import com.selfietime.selfietime.Custom_ViewPager;
import com.selfietime.selfietime.EditorActivity;
import com.selfietime.selfietime.MainActivity;
import com.selfietime.selfietime.R;
import com.theartofdev.edmodo.cropper.CropImage;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class MainFragment extends Fragment {

    Fragment selectedfragment = null;

    private ImageView Add_Selfie;
    private Uri mImageUri;

    private CurvedBottomNavigationView curvedBottomNavigationView;

    public MainFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        curvedBottomNavigationView = view.findViewById(R.id.bottom_navigation);
        Add_Selfie = view.findViewById(R.id.add_selfie);
        curvedBottomNavigationView.inflateMenu(R.menu.bottom_navigation);
        CurvedBottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
                new CurvedBottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.navigation_home:
                                selectedfragment = new HomeFragment();
                                break;
                            case R.id.navigation_search:
                                selectedfragment = new SearchFragment();
                                break;
                            case R.id.navigation_notification:
                                selectedfragment = new NotificationFragment();
                                break;
                            case R.id.navigation_profile:
                                SharedPreferences.Editor editor = getActivity().getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                                editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                editor.apply();
                                selectedfragment = new ProfileFragment();
                                break;
                        }
                        if (selectedfragment != null) {
                            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                    selectedfragment).commit();
                        }

                        return true;
                    }
                };

        curvedBottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        Add_Selfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .start(getContext(), MainFragment.this);
            }
        });

        Bundle intent = getActivity().getIntent().getExtras();
        if (intent != null) {
            String publisher = intent.getString("publisherid");

            SharedPreferences.Editor editor = getActivity().getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ProfileFragment()).commit();
        } else {
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            Intent SelfieIntent = new Intent(getContext(), EditorActivity.class);
            SelfieIntent.putExtra("image_url", mImageUri.toString());
            startActivity(SelfieIntent);

        } else {
            Toast.makeText(getContext(), "Something gone wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}