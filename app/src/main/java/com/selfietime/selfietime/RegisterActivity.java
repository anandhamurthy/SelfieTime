package com.selfietime.selfietime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class RegisterActivity extends AppCompatActivity {

    private EditText Register_Email_Address;
    private EditText Register_Password;
    private EditText Register_Confirm_Password;
    private EditText Register_Name;
    private CircleImageView Register_Profile_Image;
    private Button Register_Button;
    private TextView Register_Login_Button;

    private Uri ProfileImageURI = null;

    private boolean isChanged = false;

    private StorageReference mProfileImageStorageReference;

    private Bitmap compressedProfileImageFile;

    private FirebaseAuth mAuth;

    private DatabaseReference mUsersDatabase;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mProfileImageStorageReference = FirebaseStorage.getInstance().getReference();

        Register_Email_Address = findViewById(R.id.register_email);
        Register_Password = findViewById(R.id.register_password);
        Register_Confirm_Password = findViewById(R.id.register_confirm_password);
        Register_Name = findViewById(R.id.register_name);
        Register_Profile_Image = findViewById(R.id.register_profile_image);
        Register_Button = findViewById(R.id.register_button);
        Register_Login_Button = findViewById(R.id.register_login_text);


        Register_Profile_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(RegisterActivity.this);

            }
        });

        Register_Login_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();

            }
        });

        Register_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = Register_Email_Address.getText().toString();
                final String pass = Register_Password.getText().toString();
                String confirm_pass = Register_Confirm_Password.getText().toString();
                final String name = Register_Name.getText().toString();

                if (isEmpty(email, name, pass, confirm_pass)) {

                    if (ProfileImageURI != null) {

                        if (pass.length() > 5 && confirm_pass.length() > 5) {

                            if (pass.equals(confirm_pass)) {

                                if (isChanged) {

                                    mProgressDialog.setTitle("Registering");
                                    mProgressDialog.setMessage("Creating User..");
                                    mProgressDialog.show();
                                    mProgressDialog.setCanceledOnTouchOutside(false);

                                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if (task.isSuccessful()) {

                                                File newImageFile = new File(ProfileImageURI.getPath());

                                                compressedProfileImageFile = new Compressor(RegisterActivity.this)
                                                        .setMaxHeight(125)
                                                        .setMaxWidth(125)
                                                        .setQuality(50)
                                                        .compressToBitmap(newImageFile);

                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                compressedProfileImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                byte[] thumbData = baos.toByteArray();

                                                final String randomName = UUID.randomUUID().toString();

                                                UploadTask image_path = mProfileImageStorageReference.child("profile_images").child(randomName + ".jpg").putBytes(thumbData);

                                                image_path.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                                        if (task.isSuccessful()) {

                                                            mProgressDialog.setMessage("Creating The Profile..");

                                                            final String download_url = task.getResult().getDownloadUrl().toString();

                                                            String device_token = FirebaseInstanceId.getInstance().getToken();

                                                            HashMap userMap = new HashMap<>();
                                                            userMap.put("email_id", email);
                                                            userMap.put("user_name", name);
                                                            userMap.put("place", "");
                                                            userMap.put("date_of_birth", "");
                                                            userMap.put("gender", "");
                                                            userMap.put("terms", "");
                                                            userMap.put("bio", "");
                                                            userMap.put("user_id", mAuth.getCurrentUser().getUid());
                                                            userMap.put("profile_image", download_url);
                                                            userMap.put("device_token", device_token);

                                                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                                                            String uid = current_user.getUid();

                                                            mUsersDatabase.child(uid).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        mProgressDialog.dismiss();

                                                                        Toast.makeText(RegisterActivity.this, "Account Created & Logging in Sucessfully", Toast.LENGTH_LONG).show();
                                                                        Intent setupIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(setupIntent);
                                                                        finish();

                                                                    }

                                                                }
                                                            });

                                                        } else {

                                                            mProgressDialog.dismiss();
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(RegisterActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();


                                                        }
                                                    }
                                                });


                                            } else {
                                                mProgressDialog.dismiss();
                                                String errorMessage = task.getException().getMessage();
                                                Toast.makeText(RegisterActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();

                                            }

                                        }
                                    });

                                } else {

                                    Toast.makeText(RegisterActivity.this, "Add Profile Image.", Toast.LENGTH_LONG).show();

                                }


                            } else {

                                Toast.makeText(RegisterActivity.this, "Confirm Password and Password Field doesn't match.", Toast.LENGTH_LONG).show();

                            }

                        } else {

                            Register_Password.setError("Atleast 6 Characters");
                            Register_Confirm_Password.setError("Atleast 6 Characters");
                            Toast.makeText(RegisterActivity.this, "Password must contain atleast 6 characters.", Toast.LENGTH_LONG).show();

                        }
                    } else {

                        Toast.makeText(RegisterActivity.this, "Add Profile Image.", Toast.LENGTH_LONG).show();

                    }

                } else {

                    Toast.makeText(RegisterActivity.this, "Enter all the details.", Toast.LENGTH_LONG).show();

                }


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            Intent mainIntent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(mainIntent);
            finish();

        }

    }

    private boolean isEmpty(String email, String name, String password, String confirm_pass) {
        if (email.isEmpty() || name.isEmpty() || password.isEmpty() || confirm_pass.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Complete All the Details", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                ProfileImageURI = result.getUri();
                Register_Profile_Image.setImageURI(ProfileImageURI);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

}
