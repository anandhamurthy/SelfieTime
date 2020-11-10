package com.selfietime.selfietime.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.Adapter.MyPhotoAdapter;
import com.selfietime.selfietime.Model.Selfie;
import com.selfietime.selfietime.R;

import java.util.ArrayList;
import java.util.List;


public class UserSavedSelfiesFragment extends Fragment {


    private final String user_id;
    private List<String> saveList;
    private MyPhotoAdapter myPhotoAdapter;
    private List<Selfie> selfieList;
    private RecyclerView Saved_Selfie_List;
    private DatabaseReference mSelfieDatabase, mSavedDatabase;

    public UserSavedSelfiesFragment(String user_id) {
        this.user_id = user_id;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_saved_selfies, container, false);


        mSelfieDatabase = FirebaseDatabase.getInstance().getReference().child("Selfies");
        mSelfieDatabase.keepSynced(true);

        mSavedDatabase = FirebaseDatabase.getInstance().getReference().child("Saves");
        mSavedDatabase.keepSynced(true);

        Saved_Selfie_List = view.findViewById(R.id.saved_selfie_list);

        Saved_Selfie_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManagers = new GridLayoutManager(getContext(), 3);
        Saved_Selfie_List.setLayoutManager(mLayoutManagers);
        selfieList = new ArrayList<>();
        myPhotoAdapter = new MyPhotoAdapter(getContext(), selfieList);
        Saved_Selfie_List.setAdapter(myPhotoAdapter);

        getSavedSelfies(user_id);

        return view;
    }

    private void getSavedSelfies(String user_id) {
        saveList = new ArrayList<>();
        mSavedDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    saveList.add(snapshot.getKey());
                }
                getSelfies();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getSelfies() {
        mSelfieDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selfieList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Selfie selfie = snapshot.getValue(Selfie.class);

                    for (String id : saveList) {
                        if (selfie.getSelfie_id().equals(id)) {
                            selfieList.add(selfie);
                        }
                    }
                }
                myPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}