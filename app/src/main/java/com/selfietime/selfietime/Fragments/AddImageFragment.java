package com.selfietime.selfietime.Fragments;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.selfietime.selfietime.EditCoverActivity;
import com.selfietime.selfietime.EditProfileActivity;
import com.selfietime.selfietime.ProfileEditActivity;
import com.selfietime.selfietime.R;
import com.theartofdev.edmodo.cropper.CropImage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


public class AddImageFragment extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    Context context;
    private String key;
    private TextView Add_Image;

    public AddImageFragment() {
    }

    public AddImageFragment(String key) {
        this.key = key;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_image, container, false);
        context = getContext();


        Add_Image = view.findViewById(R.id.add_image);

        if (key.equals("cover_image")) {
            Add_Image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), EditCoverActivity.class));
                }
            });
        } else {
            Add_Image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                }
            });
        }


        return view;
    }

}
