package com.selfietime.selfietime.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.Adapter.UserAdapter;
import com.selfietime.selfietime.Model.User;
import com.selfietime.selfietime.R;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView Search_List;
    private UserAdapter userAdapter;
    private List<User> userList;

    private DatabaseReference mUsersDatabase;
    private EditText Search_Edit_Text;
    private ImageView Search_Button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);


        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mUsersDatabase.keepSynced(true);

        Search_List = view.findViewById(R.id.search_list);
        Search_Edit_Text = view.findViewById(R.id.search_edit_text);

        Search_List.setHasFixedSize(true);
        Search_List.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), userList, true, false);
        Search_List.setAdapter(userAdapter);

        Search_Button = view.findViewById(R.id.search_search_icon);

        Search_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        Search_Edit_Text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString().toUpperCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //  readUsers();


        return view;
    }

    private void searchUsers(String s) {
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("user_name").startAt(s).endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userList.add(user);
                }


                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsers() {

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (Search_Edit_Text.getText().toString().equals("")) {
                    userList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        userList.add(user);

                    }

                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
