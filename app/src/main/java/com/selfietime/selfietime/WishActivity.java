package com.selfietime.selfietime;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.selfietime.selfietime.Fragments.APIService;
import com.selfietime.selfietime.Notification.Client;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class WishActivity extends AppCompatActivity {

    private Spinner Day, Month;
    private ImageView Wish_Image, Wish_Close;
    private EditText Year, Name, Greeting;
    private FloatingActionButton Send;


    private Uri mImageUri;
    private String miUrlOk = "";
    private String mUser_Id;
    private StorageTask uploadTask;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserId;
    private DatabaseReference mUsersDatabase, mWishDatabase;

    private StorageReference mWishesStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);

        Bundle intent = getIntent().getExtras();
        mUser_Id = intent.getString("user_id");

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mCurrentUserId = mCurrentUser.getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);
        mUsersDatabase.keepSynced(true);

        mWishesStorage = FirebaseStorage.getInstance().getReference().child("Wishes");

        mWishDatabase = FirebaseDatabase.getInstance().getReference().child("Wishes");
        mWishDatabase.keepSynced(true);

        Day = findViewById(R.id.wish_day);
        Month = findViewById(R.id.wish_month);
        Year = findViewById(R.id.wish_year);
        Send = findViewById(R.id.wish_send);
        Wish_Image = findViewById(R.id.wish_image);
        Name = findViewById(R.id.wish_name);
        Greeting = findViewById(R.id.wish_greeting);
        Wish_Close = findViewById(R.id.wish_close);

        Wish_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(WishActivity.this);
            }
        });
        Wish_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WishActivity.this, MainActivity.class));
                finish();
            }
        });

        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String day = Day.getSelectedItem().toString();
                String month = Month.getSelectedItem().toString();
                String year = Year.getText().toString();
                String name = Name.getText().toString();
                String greeting = Greeting.getText().toString();


                if (isEmpty(name, greeting, day, month, year)) {
                    UploadImage(day, month, year, name, greeting);
                }
            }
        });
    }

    private boolean isEmpty(String name, String greet, String day, String month, String year) {
        if (name.isEmpty() || greet.isEmpty() || day.isEmpty() || month.isEmpty() || year.isEmpty()) {
            Toast.makeText(WishActivity.this, "Complete All the Details", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void UploadImage(final String day, final String month, final String year, final String name, final String greeting) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Sending");
        pd.show();
        if (mImageUri != null) {
            final StorageReference fileReference = mWishesStorage.child(System.currentTimeMillis()
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

                        String postid = mWishDatabase.push().getKey();

                        String rd = day + "-" + month + "-" + year;

                        HashMap wishMap = new HashMap<>();
                        wishMap.put("date", rd);
                        wishMap.put("from", mCurrentUserId);
                        wishMap.put("name", name);
                        wishMap.put("greeting", greeting);
                        wishMap.put("wish_id", postid);
                        wishMap.put("wish_image", miUrlOk);
                        wishMap.put("to", mUser_Id);

                        mWishDatabase.child(mUser_Id).child(postid).setValue(wishMap);

                        pd.dismiss();

                        startActivity(new Intent(WishActivity.this, MainActivity.class));
                        finish();
                        startActivity(new Intent(WishActivity.this, InterstitialAdActivity.class));
                    } else {
                        Toast.makeText(WishActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(WishActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(WishActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            Wish_Image.setImageURI(mImageUri);
        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(WishActivity.this, MainActivity.class));
            finish();
        }
    }

}
