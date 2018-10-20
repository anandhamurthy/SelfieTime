package com.selfietime.selfietime.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.Adapter.SelfieAdapter;
import com.selfietime.selfietime.Model.Selfie;
import com.selfietime.selfietime.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class PostDetailFragment extends Fragment {

    String postid;

    private RecyclerView recyclerView;
    private SelfieAdapter selfieAdapter;
    private List<Selfie> selfieList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", MODE_PRIVATE);
        postid = prefs.getString("postid", "none");

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        selfieList = new ArrayList<>();
        selfieAdapter = new SelfieAdapter(getContext(), selfieList);
        recyclerView.setAdapter(selfieAdapter);

        readPost();

        return view;
    }

    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Selfies").child(postid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selfieList.clear();
                Selfie selfie = dataSnapshot.getValue(Selfie.class);
                selfieList.add(selfie);

                selfieAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
