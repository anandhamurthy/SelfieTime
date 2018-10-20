package com.selfietime.selfietime.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.selfietime.selfietime.Adapter.SelfieAdapter;
import com.selfietime.selfietime.Adapter.StoryAdapter;
import com.selfietime.selfietime.CompetitionActivity;
import com.selfietime.selfietime.GreetingsActivity;
import com.selfietime.selfietime.LikesActivity;
import com.selfietime.selfietime.Model.Selfie;
import com.selfietime.selfietime.Model.Story;
import com.selfietime.selfietime.Notification.Client;
import com.selfietime.selfietime.Notification.Token;
import com.selfietime.selfietime.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    APIService apiService;
    private RecyclerView Home_Selfie_List;
    private SelfieAdapter selfieAdapter;
    private List<Selfie> selfieList;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private DatabaseReference mSelfieDatabase, mFollowingDatabase, mUsersDatabase, mStoryDatabase;
    private RecyclerView Home_Story_List;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;
    private List<String> followingList;
    private ProgressBar Home_Progress_Bar;
    private ImageView Home_Greetings, Home_Context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Home_Selfie_List = view.findViewById(R.id.home_selfie_list);
        Home_Selfie_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        Home_Selfie_List.setLayoutManager(mLayoutManager);
        selfieList = new ArrayList<>();
        selfieAdapter = new SelfieAdapter(getContext(), selfieList);
        Home_Selfie_List.setAdapter(selfieAdapter);

        Home_Story_List = view.findViewById(R.id.home_story_list);
        Home_Story_List.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        Home_Story_List.setLayoutManager(linearLayoutManager);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        Home_Story_List.setAdapter(storyAdapter);

        Home_Progress_Bar = view.findViewById(R.id.home_progress_bar);
        Home_Greetings = view.findViewById(R.id.home_greetings);
        Home_Context = view.findViewById(R.id.home_context);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();


        updateToken(FirebaseInstanceId.getInstance().getToken());
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        mSelfieDatabase = FirebaseDatabase.getInstance().getReference().child("Selfies");
        mSelfieDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFollowingDatabase = FirebaseDatabase.getInstance().getReference("Follow").child(mCurrentUserId).child("following");
        mFollowingDatabase.keepSynced(true);
        mStoryDatabase = FirebaseDatabase.getInstance().getReference("Story");
        mStoryDatabase.keepSynced(true);

        Home_Greetings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GreetingsActivity.class);
                intent.putExtra("title", "Greetings");
                startActivity(intent);

            }
        });
        Home_Context.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), CompetitionActivity.class);
                intent.putExtra("title", "Competitions");
                startActivity(intent);

            }
        });
        checkFollowing();

        return view;
    }


    private void checkFollowing() {
        followingList = new ArrayList<>();

        mFollowingDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    followingList.add(snapshot.getKey());
                }

                readPosts();
                readStory();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readPosts() {

        mSelfieDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selfieList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Selfie selfie = snapshot.getValue(Selfie.class);
                    for (String id : followingList) {
                        if (selfie.getUser_id().equals(id)) {
                            selfieList.add(selfie);
                        }
                    }
                }

                selfieAdapter.notifyDataSetChanged();
                Home_Progress_Bar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateToken(String refreshToken) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Token token = new Token(refreshToken);
        reference.child(firebaseUser.getUid()).setValue(token);
    }

    private void readStory() {

        mStoryDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", 0, 0, "",
                        FirebaseAuth.getInstance().getCurrentUser().getUid()));
                for (String id : followingList) {
                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot snapshot : dataSnapshot.child(id).getChildren()) {
                        story = snapshot.getValue(Story.class);
                        if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                            countStory++;
                        }
                    }
                    if (countStory > 0) {
                        storyList.add(story);
                    }
                }

                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
