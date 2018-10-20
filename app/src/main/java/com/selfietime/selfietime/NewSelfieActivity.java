package com.selfietime.selfietime;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewSelfieActivity extends AppCompatActivity {

    private Uri mImageUri;
    private String miUrlOk = "";
    private StorageTask uploadTask;
    private StorageReference mSelfieStorageReference;
    private String mCurrentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference mSelfieDatabase;

    private ImageView New_Selfie_Close, New_Selfie_Added;
    private EditText New_Selfie_Description;
    private FloatingActionButton New_Selfie_Add;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_selfie);

        Bundle intent = getIntent().getExtras();
        mImageUri = Uri.parse(intent.getString("image_url"));


        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        New_Selfie_Close = findViewById(R.id.new_selfie_close);
        New_Selfie_Added = findViewById(R.id.new_selfie_added);
        New_Selfie_Description = findViewById(R.id.new_selfie_description);
        New_Selfie_Add = findViewById(R.id.new_selfie_add);

        mSelfieStorageReference = FirebaseStorage.getInstance().getReference("selfies");
        mSelfieDatabase = FirebaseDatabase.getInstance().getReference().child("Selfies");

        New_Selfie_Added.setImageURI(mImageUri);

        New_Selfie_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NewSelfieActivity.this, MainActivity.class));
                finish();
            }
        });

        New_Selfie_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadImage();
            }
        });

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void UploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();
        if (mImageUri != null) {
            final StorageReference fileReference = mSelfieStorageReference.child(System.currentTimeMillis()
                    + ".jpg");

            uploadTask = fileReference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                        miUrlOk = downloadUri.toString();

                        String postid = mSelfieDatabase.push().getKey();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("selfie_id", postid);
                        hashMap.put("selfie_image", miUrlOk);
                        hashMap.put("selfie_description", New_Selfie_Description.getText().toString());
                        hashMap.put("user_id", mCurrentUserId);

                        mSelfieDatabase.child(postid).setValue(hashMap);

                        pd.dismiss();

                        startActivity(new Intent(NewSelfieActivity.this, MainActivity.class));
                        finish();

                    } else {
                        Toast.makeText(NewSelfieActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(NewSelfieActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(NewSelfieActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }
}
