package com.selfietime.selfietime;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class SelfieTime extends Application {

    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();


        MobileAds.initialize(this, getString(R.string.admob_app_id));

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            mUsersDatabase = FirebaseDatabase.getInstance()
                    .getReference().child("Users").child(mAuth.getCurrentUser().getUid());

            mUsersDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot != null) {

                        mUsersDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


    }


}
