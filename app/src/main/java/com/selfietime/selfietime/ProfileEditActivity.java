package com.selfietime.selfietime;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.selfietime.selfietime.Model.User;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class ProfileEditActivity extends AppCompatActivity {

    private ImageView Profile_Edit_Back;
    private TextView Profile_Edit_Email_Address;
    private FloatingActionButton Profile_Edit_Save;
    private EditText Profile_Edit_User_Name, Profile_Edit_Place, Profile_Edit_Bio, Profile_Edit_DOB;
    private EditText Profile_Edit_Gender;
    private Switch Profile_Privacy;

    private String Privacy = "False";

    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        Profile_Edit_Back = findViewById(R.id.profile_edit_back);
        Profile_Edit_Save = findViewById(R.id.profile_edit_save);
        Profile_Edit_User_Name = findViewById(R.id.profile_edit_user_name);
        Profile_Edit_Place = findViewById(R.id.profile_edit_place);
        Profile_Edit_Bio = findViewById(R.id.profile_edit_bio);
        Profile_Edit_DOB = findViewById(R.id.profile_edit_dob);
        Profile_Edit_Email_Address = findViewById(R.id.profile_edit_email_address);
        Profile_Edit_DOB = findViewById(R.id.profile_edit_dob);
        Profile_Edit_Gender = findViewById(R.id.profile_edit_gender);
        Profile_Privacy = findViewById(R.id.profile_privacy);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUserId);
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Profile_Edit_User_Name.setText(user.getUser_name());
                Profile_Edit_Bio.setText(user.getBio());
                Profile_Edit_Place.setText(user.getPlace());
                Profile_Edit_Gender.setText(user.getGender());
                Profile_Edit_DOB.setText(user.getDate_of_birth());
                Profile_Edit_Email_Address.setText(user.getEmail_id());
                Profile_Privacy.setChecked(user.getTerms().equals("True"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Profile_Edit_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Profile_Edit_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(ProfileEditActivity.this, "fe", Toast.LENGTH_SHORT).show();
                if (Profile_Privacy.isChecked()) {
                    Privacy = "True";
                } else {
                    Privacy = "False";
                }
                if (isEmpty(Profile_Edit_User_Name.getText().toString())) {

                    UpdateProfile(Profile_Edit_User_Name.getText().toString(),
                            Profile_Edit_Gender.getText().toString(),
                            Profile_Edit_Place.getText().toString(), Profile_Edit_DOB.getText().toString(), Privacy, Profile_Edit_Bio.getText().toString());
                }

            }
        });

    }

    private void UpdateProfile(String name, String gender, String place, String dob, String terms, String bio) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("user_name", name);
        map.put("gender", gender);
        map.put("place", place);
        map.put("terms", terms);
        map.put("date_of_birth", dob);
        map.put("bio", bio);
        map.put("user_id", mCurrentUserId);

        mUsersDatabase.updateChildren(map);

        Toast.makeText(ProfileEditActivity.this, "Successfully Updated!", Toast.LENGTH_SHORT).show();
    }

    private boolean isEmpty(String name) {
        if (name.isEmpty()) {
            Toast.makeText(ProfileEditActivity.this, "User Name Should Not Be Empty!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
