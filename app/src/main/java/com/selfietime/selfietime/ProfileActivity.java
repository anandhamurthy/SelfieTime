package com.selfietime.selfietime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
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
import com.selfietime.selfietime.Fragments.AddImageFragment;
import com.selfietime.selfietime.Fragments.MoreOptionsFragment;
import com.selfietime.selfietime.Fragments.ProfileFragment;
import com.selfietime.selfietime.Fragments.UserSavedSelfiesFragment;
import com.selfietime.selfietime.Fragments.UserSelfiesFragment;
import com.selfietime.selfietime.Model.Selfie;
import com.selfietime.selfietime.Model.User;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    protected TabLayout Profile_Tab_Layout;
    protected ViewPager Profile_Pager;
    RelativeLayout top_layout, tabs_main_layout;
    private ImageView Profile_Cover_Image, Profile_Back;
    private TextView Profile_Selfie_Count, Profile_Followings_Count, Profile_Followers_Count, Profile_User_Bio, Profile_User_Name, Profile_Toolbar_User_Name;
    private CircleImageView Profile_Image;
    private Button Profile_Button;
    private LinearLayout Profile_Privacy;
    private String user_id;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private DatabaseReference mFollowersDatabase, mUsersDatabase, mSelfieDatabase, mSavedDatabase, mFollowingDatabase;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mFollowersDatabase = FirebaseDatabase.getInstance().getReference("Follow").child(user_id).child("followers");
        mFollowersDatabase.keepSynced(true);

        mFollowingDatabase = FirebaseDatabase.getInstance().getReference("Follow").child(user_id).child("following");
        mFollowingDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mSelfieDatabase = FirebaseDatabase.getInstance().getReference().child("Selfies");
        mSelfieDatabase.keepSynced(true);

        mSavedDatabase = FirebaseDatabase.getInstance().getReference().child("Saves");
        mSavedDatabase.keepSynced(true);

        top_layout = findViewById(R.id.top_layout);
        tabs_main_layout = findViewById(R.id.tabs_main_layout);

        Profile_Tab_Layout = findViewById(R.id.tabs);
        Profile_Pager = findViewById(R.id.pager);

        Profile_Selfie_Count = findViewById(R.id.profile_wishes_count);
        Profile_Followers_Count = findViewById(R.id.profile_followers_count);
        Profile_Followings_Count = findViewById(R.id.profile_followings_count);
        Profile_Image = findViewById(R.id.profile_image);
        Profile_Cover_Image = findViewById(R.id.cover_image);
        Profile_Back = findViewById(R.id.profile_edit_back);

        Profile_Button = findViewById(R.id.profile_button);
        Profile_User_Name = findViewById(R.id.profile_name);
        Profile_User_Bio = findViewById(R.id.profile_bio);
        Profile_Toolbar_User_Name = findViewById(R.id.profile_user_name);
        Profile_Privacy = findViewById(R.id.profile_privacy);

        Profile_Pager.setOffscreenPageLimit(2);
        viewPagerAdapter = new ViewPagerAdapter(getResources(), getSupportFragmentManager());
        Profile_Pager.setAdapter(viewPagerAdapter);
        Profile_Tab_Layout.setupWithViewPager(Profile_Pager);

        Profile_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setTabIcons();


        if (!user_id.equals(mCurrentUserId)) {
            UserInformation(user_id);
            getFollowers();
            getSelfies(user_id);

            mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);
                    if (user.getTerms().equals("True")) {
                        Profile_Privacy.setVisibility(View.VISIBLE);
                        Profile_Followers_Count.setEnabled(false);
                        Profile_Followings_Count.setEnabled(false);
                    } else {
                        Profile_Privacy.setVisibility(View.GONE);
                        Profile_Followers_Count.setEnabled(true);
                        Profile_Followings_Count.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Profile_Button.setVisibility(View.VISIBLE);
            checkFollow();


        } else {
            UserInformation(user_id);
            getFollowers();
            getSelfies(user_id);
            Profile_Button.setVisibility(View.GONE);
        }

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


        Profile_Followers_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
                intent.putExtra("id", user_id);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        Profile_Followings_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, FollowingsActivity.class);
                intent.putExtra("id", user_id);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        Profile_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btn = Profile_Button.getText().toString();

                if (btn.equals("Follow")) {

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(mCurrentUserId)
                            .child("following").child(user_id).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user_id)
                            .child("followers").child(mCurrentUserId).setValue(true);
                    addNotification();
                } else if (btn.equals("Following")) {

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(mCurrentUserId)
                            .child("following").child(user_id).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user_id)
                            .child("followers").child(mCurrentUserId).removeValue();

                }
            }
        });

    }

    private void setTabIcons() {

        View view1 = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.single_profile_tab, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.selfies));
        Profile_Tab_Layout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.single_profile_tab, null);
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

    private void addNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(user_id);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", mCurrentUserId);
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    private void UserInformation(String profileid) {
        mUsersDatabase.child(profileid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (getApplicationContext() == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);

                if (user.getCover_image() != null) {
                    Glide.with(ProfileActivity.this).load(user.getCover_image()).into(Profile_Cover_Image);
                }
                Glide.with(ProfileActivity.this).load(user.getProfile_image()).into(Profile_Image);
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

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(mCurrentUserId).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(user_id).exists()) {
                    Profile_Button.setText("Following");
                } else {
                    Profile_Button.setText("Follow");
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
                    result = new UserSelfiesFragment(user_id);
                    break;

                case 1:
                    result = new UserSavedSelfiesFragment(user_id);
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