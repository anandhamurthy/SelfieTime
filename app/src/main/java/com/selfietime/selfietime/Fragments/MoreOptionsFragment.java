package com.selfietime.selfietime.Fragments;

import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.selfietime.selfietime.LoginActivity;
import com.selfietime.selfietime.OpenSourcesActivity;
import com.selfietime.selfietime.PrivacyPolicyActivity;
import com.selfietime.selfietime.ProfileEditActivity;
import com.selfietime.selfietime.R;

public class MoreOptionsFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private TextView Open_Source, Private_Policy, Logout, Edit_Profile;

    public MoreOptionsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more_options, container, false);


        Open_Source = view.findViewById(R.id.open_source_library);
        Private_Policy = view.findViewById(R.id.private_policy);
        Logout = view.findViewById(R.id.logout);
        Edit_Profile = view.findViewById(R.id.edit_profile);

        Edit_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ProfileEditActivity.class));
            }
        });

        Open_Source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), OpenSourcesActivity.class));
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                getActivity().finish();
            }
        });

        Private_Policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), PrivacyPolicyActivity.class));
            }
        });

        return view;
    }
}