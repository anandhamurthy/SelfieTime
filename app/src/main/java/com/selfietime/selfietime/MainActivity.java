package com.selfietime.selfietime;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.selfietime.selfietime.Fragments.MainFragment;


public class MainActivity extends AppCompatActivity {

    public static MainActivity mainMenuActivity;
    long mBackPressed;
    private MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                initScreen();
            }

        } else {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                mainFragment = (MainFragment) getSupportFragmentManager().getFragments().get(0);
            }
        }

    }

    private void initScreen() {
        mainFragment = new MainFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mainFragment)
                .commit();

        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }


}