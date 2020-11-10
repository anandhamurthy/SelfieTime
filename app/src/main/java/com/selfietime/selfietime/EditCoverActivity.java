package com.selfietime.selfietime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditCoverActivity extends AppCompatActivity {

    private ImageButton Change_Cover_Image;
    private CircleImageView Profile_Image;

    private ImageView Cover_Image, Cover_Back;

    private Uri mImageUri;

    private Bitmap compressedImage;

    private StorageTask mUploadTask;

    private StorageReference mProfileImageStorage;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cover);

        Change_Cover_Image = findViewById(R.id.change_profile_cover);
        Profile_Image = findViewById(R.id.profile_image);
        Cover_Image = findViewById(R.id.cover_image);
        Cover_Back = findViewById(R.id.profile_edit_back);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        UserInformation(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mProfileImageStorage = FirebaseStorage.getInstance().getReference("cover_images");

        Change_Cover_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(2, 1)
                        .start(EditCoverActivity.this);
            }
        });

        Cover_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {


            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            File file = new File(mImageUri.getPath());
            compressedImage = new Compressor(EditCoverActivity.this).setQuality(70).compressToBitmap(file);
            Upload();

        } else {
            Toast.makeText(EditCoverActivity.this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
        }
    }


    private void Upload() {
        final ProgressDialog pd = new ProgressDialog(EditCoverActivity.this);
        pd.setMessage("Loading");
        pd.show();
        if (compressedImage != null) {
            final StorageReference fileReference = mProfileImageStorage.child(System.currentTimeMillis()
                    + ".jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            mUploadTask = fileReference.putBytes(data);
            mUploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {


                        Uri downloadUri = task.getResult();
                        String miUrlOk = downloadUri.toString();

                        HashMap<String, Object> map1 = new HashMap<>();
                        map1.put("cover_image", miUrlOk);
                        mUsersDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(map1).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                pd.dismiss();
                            }
                        });

                        pd.dismiss();

                    } else {
                        Toast.makeText(EditCoverActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditCoverActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(EditCoverActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void UserInformation(String profileid) {
        mUsersDatabase.child(profileid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user.getCover_image() != null) {
                    Glide.with(EditCoverActivity.this).load(user.getCover_image()).into(Cover_Image);
                }
                Glide.with(EditCoverActivity.this).load(user.getProfile_image()).into(Profile_Image);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}