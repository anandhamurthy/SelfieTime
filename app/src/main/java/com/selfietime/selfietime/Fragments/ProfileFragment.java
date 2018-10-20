package com.selfietime.selfietime.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.Adapter.MyPhotoAdapter;
import com.selfietime.selfietime.FollowingsActivity;
import com.selfietime.selfietime.ProfileEditActivity;
import com.selfietime.selfietime.FollowersActivity;
import com.selfietime.selfietime.Model.Selfie;
import com.selfietime.selfietime.Model.User;
import com.selfietime.selfietime.SettingsActivity;
import com.selfietime.selfietime.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private ImageView Profile_Image_Edit, Profile_Menu, Profile_Selfies, Profile_Saved_Photos;
    private TextView Profile_Wishes_Count, Profile_Followings_Count, Profile_Followers_Count, Profile_User_Bio, Profile_User_Name, Profile_Toolbar_User_Name;
    private CircleImageView Profile_Image;
    private Button Profile_Button;
    private RecyclerView Profile_Selfies_List, Profile_Saved_Photos_List;

    private LinearLayout Profile_Bottom_Bar;
    private LinearLayout Profile_Privacy;

    private List<String> mySaves;

    private FirebaseUser mFirebaseUser;
    private String profileid;
    private String mCurrentUserId;
    private DatabaseReference mFollowersDatabase, mUsersDatabase, mSelfieDatabase, mSavedDatabase, mFollowingDatabase;

    private MyPhotoAdapter myFotosAdapter;
    private List<Selfie> postList;

    private MyPhotoAdapter myFotosAdapter_saves;
    private List<Selfie> postList_saves;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");

        mFollowersDatabase = FirebaseDatabase.getInstance().getReference("Follow").child(profileid).child("followers");
        mFollowersDatabase.keepSynced(true);

        mFollowingDatabase = FirebaseDatabase.getInstance().getReference("Follow").child(profileid).child("following");
        mFollowingDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mSelfieDatabase = FirebaseDatabase.getInstance().getReference().child("Selfies");
        mSelfieDatabase.keepSynced(true);

        mSavedDatabase = FirebaseDatabase.getInstance().getReference().child("Saves");
        mSavedDatabase.keepSynced(true);

        Profile_Image_Edit = view.findViewById(R.id.profile_image_edit);
        Profile_Menu = view.findViewById(R.id.profile_menu);
        Profile_Selfies = view.findViewById(R.id.profile_selfies);
        Profile_Saved_Photos = view.findViewById(R.id.profile_saved_photos);
        Profile_Wishes_Count = view.findViewById(R.id.profile_wishes_count);
        Profile_Followers_Count = view.findViewById(R.id.profile_followers_count);
        Profile_Followings_Count = view.findViewById(R.id.profile_followings_count);
        Profile_Image = view.findViewById(R.id.profile_image);
        Profile_Selfies_List = view.findViewById(R.id.profile_selfies_list);
        Profile_Saved_Photos_List = view.findViewById(R.id.profile_saved_photos_list);
        Profile_Button = view.findViewById(R.id.profile_button);
        Profile_User_Name = view.findViewById(R.id.profile_name);
        Profile_User_Bio = view.findViewById(R.id.profile_bio);
        Profile_Toolbar_User_Name = view.findViewById(R.id.profile_user_name);
        Profile_Bottom_Bar = view.findViewById(R.id.profile_bottom_bar);
        Profile_Privacy = view.findViewById(R.id.profile_privacy);

        Profile_Selfies_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 3);
        Profile_Selfies_List.setLayoutManager(mLayoutManager);
        postList = new ArrayList<>();
        myFotosAdapter = new MyPhotoAdapter(getContext(), postList);
        Profile_Selfies_List.setAdapter(myFotosAdapter);

        Profile_Saved_Photos_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManagers = new GridLayoutManager(getContext(), 3);
        Profile_Saved_Photos_List.setLayoutManager(mLayoutManagers);
        postList_saves = new ArrayList<>();
        myFotosAdapter_saves = new MyPhotoAdapter(getContext(), postList_saves);
        Profile_Saved_Photos_List.setAdapter(myFotosAdapter_saves);

        Profile_Selfies_List.setVisibility(View.VISIBLE);
        Profile_Saved_Photos_List.setVisibility(View.GONE);

        if (!profileid.equals(mCurrentUserId)) {

            UserInformation(profileid);
            getFollowers(profileid);
            getSelfies(profileid);
            getPhotos(profileid);
            getPhotoSaved(profileid);

            mUsersDatabase.child(profileid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);
                    if (user.getTerms().equals("True")) {
                        Profile_Bottom_Bar.setVisibility(View.GONE);
                        Profile_Privacy.setVisibility(View.VISIBLE);
                        Profile_Followers_Count.setEnabled(false);
                        Profile_Followings_Count.setEnabled(false);
                        Profile_Selfies_List.setVisibility(View.GONE);
                        Profile_Saved_Photos_List.setVisibility(View.GONE);
                    } else {
                        Profile_Privacy.setVisibility(View.GONE);
                        Profile_Bottom_Bar.setVisibility(View.VISIBLE);
                        Profile_Followers_Count.setEnabled(true);
                        Profile_Followings_Count.setEnabled(true);
                        Profile_Selfies_List.setVisibility(View.VISIBLE);
                        Profile_Saved_Photos_List.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            checkFollow();
            Profile_Button.setVisibility(View.GONE);
            Profile_Saved_Photos.setVisibility(View.VISIBLE);
            Profile_Image_Edit.setVisibility(View.GONE);
            Profile_Menu.setVisibility(View.GONE);

        } else {
            UserInformation(mCurrentUserId);
            getFollowers(mCurrentUserId);
            getSelfies(mCurrentUserId);
            getPhotos(mCurrentUserId);
            getPhotoSaved(mCurrentUserId);
            Profile_Button.setVisibility(View.GONE);
        }

        Profile_Image_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ProfileEditActivity.class));
            }
        });

        Profile_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btn = Profile_Button.getText().toString();

                if (btn.equals("Follow")) {

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(mCurrentUserId)
                            .child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(mCurrentUserId).setValue(true);
                    addNotification();
                } else if (btn.equals("Following")) {

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(mCurrentUserId)
                            .child("following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(mCurrentUserId).removeValue();

                }
            }
        });

        Profile_Menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
            }
        });

        Profile_Selfies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Profile_Selfies_List.setVisibility(View.VISIBLE);
                Profile_Saved_Photos_List.setVisibility(View.GONE);
            }
        });

        Profile_Saved_Photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Profile_Selfies_List.setVisibility(View.GONE);
                Profile_Saved_Photos_List.setVisibility(View.VISIBLE);
            }
        });


        Profile_Followers_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });

        Profile_Followings_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowingsActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });

        return view;
    }

    private void addNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);

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
                if (getContext() == null) {
                    return;
                }
                User user = dataSnapshot.getValue(User.class);

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

    private void checkFollow() {
        mFollowingDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(profileid).exists()) {
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

    private void getFollowers(String profileid) {
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
                Profile_Wishes_Count.setText("" + i);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPhotos(final String profileid) {
        mSelfieDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Selfie selfie = snapshot.getValue(Selfie.class);
                    if (selfie.getUser_id().equals(profileid)) {
                        postList.add(selfie);
                    }
                }
                Collections.reverse(postList);
                myFotosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPhotoSaved(String profileid) {
        mySaves = new ArrayList<>();
        mSavedDatabase.child(profileid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mySaves.add(snapshot.getKey());
                }
                readSaves();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readSaves() {
        mSelfieDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList_saves.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Selfie selfie = snapshot.getValue(Selfie.class);

                    for (String id : mySaves) {
                        if (selfie.getSelfie_id().equals(id)) {
                            postList_saves.add(selfie);
                        }
                    }
                }
                myFotosAdapter_saves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
