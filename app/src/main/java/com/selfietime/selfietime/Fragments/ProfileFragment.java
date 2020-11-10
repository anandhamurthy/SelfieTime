package com.selfietime.selfietime.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.FollowingsActivity;
import com.selfietime.selfietime.FollowersActivity;
import com.selfietime.selfietime.Model.Selfie;
import com.selfietime.selfietime.Model.User;
import com.selfietime.selfietime.R;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {


    protected TabLayout Profile_Tab_Layout;
    protected ViewPager Profile_Pager;
    private CircleImageView Profile_Image;
    private Button Profile_Button;
    RelativeLayout top_layout, tabs_main_layout;

    private LinearLayout Profile_Privacy;

    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private DatabaseReference mFollowersDatabase, mUsersDatabase, mSelfieDatabase, mSavedDatabase, mFollowingDatabase;
    private ImageView Profile_Menu, Profile_Cover_Image;
    private TextView Profile_Selfie_Count, Profile_Followings_Count, Profile_Followers_Count, Profile_User_Bio, Profile_User_Name, Profile_Toolbar_User_Name;
    private ImageButton Profile_Edit_Image, Profile_Edit_Cover_Image;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mFollowersDatabase = FirebaseDatabase.getInstance().getReference("Follow").child(mCurrentUserId).child("followers");
        mFollowersDatabase.keepSynced(true);

        mFollowingDatabase = FirebaseDatabase.getInstance().getReference("Follow").child(mCurrentUserId).child("following");
        mFollowingDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mSelfieDatabase = FirebaseDatabase.getInstance().getReference().child("Selfies");
        mSelfieDatabase.keepSynced(true);

        mSavedDatabase = FirebaseDatabase.getInstance().getReference().child("Saves");
        mSavedDatabase.keepSynced(true);

        top_layout = view.findViewById(R.id.top_layout);
        tabs_main_layout = view.findViewById(R.id.tabs_main_layout);

        Profile_Tab_Layout = view.findViewById(R.id.tabs);
        Profile_Pager = view.findViewById(R.id.pager);

        Profile_Menu = view.findViewById(R.id.profile_menu);
        Profile_Selfie_Count = view.findViewById(R.id.profile_wishes_count);
        Profile_Followers_Count = view.findViewById(R.id.profile_followers_count);
        Profile_Followings_Count = view.findViewById(R.id.profile_followings_count);
        Profile_Image = view.findViewById(R.id.profile_image);
        Profile_Cover_Image = view.findViewById(R.id.cover_image);

        Profile_Edit_Image = view.findViewById(R.id.profile_edit_profile);
        Profile_Edit_Cover_Image = view.findViewById(R.id.profile_edit_profile_cover);
        Profile_Button = view.findViewById(R.id.profile_button);
        Profile_User_Name = view.findViewById(R.id.profile_name);
        Profile_User_Bio = view.findViewById(R.id.profile_bio);
        Profile_Toolbar_User_Name = view.findViewById(R.id.profile_user_name);
        Profile_Privacy = view.findViewById(R.id.profile_privacy);

        Profile_Pager.setOffscreenPageLimit(2);
        viewPagerAdapter = new ViewPagerAdapter(getResources(), getChildFragmentManager());
        Profile_Pager.setAdapter(viewPagerAdapter);
        Profile_Tab_Layout.setupWithViewPager(Profile_Pager);

        setTabIcons();

        UserInformation(mCurrentUserId);
        getFollowers();
        getSelfies(mCurrentUserId);
        Profile_Button.setVisibility(View.GONE);

        ViewTreeObserver observer = top_layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                final int height = top_layout.getMeasuredHeight();

                top_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);

                ViewTreeObserver observer = tabs_main_layout.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tabs_main_layout.getLayoutParams();
                        params.height = tabs_main_layout.getMeasuredHeight() + height;
                        tabs_main_layout.setLayoutParams(params);
                        tabs_main_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                                this);

                    }
                });

            }
        });


        Profile_Edit_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddImageFragment addPhotoBottomDialogFragment =
                        new AddImageFragment("profile_image");
                addPhotoBottomDialogFragment.show(getFragmentManager(),
                        AddImageFragment.TAG);

            }
        });

        Profile_Edit_Cover_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddImageFragment addPhotoBottomDialogFragment =
                        new AddImageFragment("cover_image");
                addPhotoBottomDialogFragment.show(getFragmentManager(),
                        AddImageFragment.TAG);
            }
        });


        Profile_Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MoreOptionsFragment moreOptionsFragment =
                        new MoreOptionsFragment();
                moreOptionsFragment.show(getFragmentManager(),
                        AddImageFragment.TAG);
            }
        });


        Profile_Followers_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", mCurrentUserId);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        Profile_Followings_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowingsActivity.class);
                intent.putExtra("id", mCurrentUserId);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        return view;
    }

    private void setTabIcons() {

        View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.single_profile_tab, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.selfies));
        Profile_Tab_Layout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.single_profile_tab, null);
        ImageView imageView2 = view2.findViewById(R.id.image);
        imageView2.setImageDrawable(getResources().getDrawable(R.drawable.saved));
        Profile_Tab_Layout.getTabAt(1).setCustomView(view2);


        Profile_Tab_Layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);
                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.selfies));
                        break;

                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.saved));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);
                switch (tab.getPosition()) {
                    case 0:

                        image.setImageDrawable(getResources().getDrawable(R.drawable.selfies));
                        break;
                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.saved));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
    private void UserInformation(String profileid) {
        mUsersDatabase.child(profileid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getContext() == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);

                if (user.getCover_image() != null) {
                    Glide.with(getContext()).load(user.getCover_image()).into(Profile_Cover_Image);
                }
                Glide.with(getContext()).load(user.getProfile_image()).into(Profile_Image);
                Profile_User_Name.setText(user.getUser_name());
                Profile_Toolbar_User_Name.setText(user.getUser_name());
                if (user.getBio().isEmpty()) {
                    Profile_User_Bio.setVisibility(View.GONE);
                } else {
                    Profile_User_Bio.setVisibility(View.VISIBLE);
                    Profile_User_Bio.setText(user.getBio());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getFollowers() {
        mFollowersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile_Followers_Count.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFollowingDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile_Followings_Count.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getSelfies(final String profileid) {
        mSelfieDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Selfie selfie = snapshot.getValue(Selfie.class);
                    if (selfie.getUser_id().equals(profileid)) {
                        i++;
                    }
                }
                Profile_Selfie_Count.setText("" + i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private final Resources resources;

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {
            super(fm);
            this.resources = resources;
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new UserSelfiesFragment(mCurrentUserId);
                    break;

                case 1:
                    result = new UserSavedSelfiesFragment(mCurrentUserId);
                    break;

                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }


    }

}
