package com.selfietime.selfietime.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.Adapter.NotificationAdapter;
import com.selfietime.selfietime.Model.Notification;
import com.selfietime.selfietime.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NotificationFragment extends Fragment {

    private RecyclerView mNotificationList;
    private RelativeLayout mNoNotification;

    private DatabaseReference mNotificationDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;

    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);


        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mNotificationDatabase = FirebaseDatabase.getInstance().getReference("Notifications").child(mCurrentUserId);
        mNotificationDatabase.keepSynced(true);
        mNoNotification = view.findViewById(R.id.no_notification);
        mNotificationList = view.findViewById(R.id.notification_list);
        mNotificationList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mNotificationList.setLayoutManager(mLayoutManager);
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        mNotificationList.setAdapter(notificationAdapter);

        readNotifications();

        return view;
    }

    private void readNotifications() {

        mNotificationDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mNotificationList.setVisibility(View.VISIBLE);
                    mNoNotification.setVisibility(View.GONE);
                    notificationList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Notification notification = snapshot.getValue(Notification.class);
                        notificationList.add(notification);
                    }

                    Collections.reverse(notificationList);
                    notificationAdapter.notifyDataSetChanged();
                } else {
                    mNoNotification.setVisibility(View.VISIBLE);
                    mNotificationList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
