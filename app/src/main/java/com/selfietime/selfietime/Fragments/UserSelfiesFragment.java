package com.selfietime.selfietime.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import java.util.Collections;
import java.util.List;

public class UserSelfiesFragment extends Fragment {

    private final String user_id;
    private MyPhotoAdapter myPhotoAdapter;
    private List<Selfie> selfieList;
    private RecyclerView Selfies_List;
    private DatabaseReference mSelfieDatabase;

    public UserSelfiesFragment(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_selfies, container, false);

        mSelfieDatabase = FirebaseDatabase.getInstance().getReference().child("Selfies");
        mSelfieDatabase.keepSynced(true);

        Selfies_List = view.findViewById(R.id.selfie_list);
        Selfies_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new GridLayoutManager(getContext(), 3);
        Selfies_List.setLayoutManager(mLayoutManager);
        selfieList = new ArrayList<>();
        myPhotoAdapter = new MyPhotoAdapter(getContext(), selfieList);
        Selfies_List.setAdapter(myPhotoAdapter);

        getSelfies(user_id);

        return view;
    }

    private void getSelfies(final String user_id) {
        mSelfieDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selfieList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Selfie selfie = snapshot.getValue(Selfie.class);
                    if (selfie.getUser_id().equals(user_id)) {
                        selfieList.add(selfie);
                    }
                }
                Collections.reverse(selfieList);
                myPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}