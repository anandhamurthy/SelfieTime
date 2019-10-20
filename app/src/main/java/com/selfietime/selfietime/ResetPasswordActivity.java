package com.selfietime.selfietime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ResetPasswordActivity extends AppCompatActivity {

    private EditText Reset_Password_Email;
    private Button Reset_Password_Reset;
    private TextView Reset_Password_Login;
    private FirebaseAuth mAuth;
    private ProgressBar Reset_Password_Progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Reset_Password_Email = findViewById(R.id.reset_password_email);
        Reset_Password_Reset = findViewById(R.id.reset_password_reset_password);
        Reset_Password_Login = findViewById(R.id.reset_password_login);
        Reset_Password_Progress = findViewById(R.id.reset_password_progress_bar);

        mAuth = FirebaseAuth.getInstance();

        Reset_Password_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Reset_Password_Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email_address = Reset_Password_Email.getText().toString();

                if (isEmpty(email_address)) {

                    Reset_Password_Progress.setVisibility(View.VISIBLE);
                    mAuth.sendPasswordResetEmail(email_address)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                    }

                                    Reset_Password_Progress.setVisibility(View.INVISIBLE);
                                }
                            });
                }
            }
        });
    }

    private boolean isEmpty(String email_address) {
        if (email_address.isEmpty()) {
            Toast.makeText(ResetPasswordActivity.this, "Enter your Email Address.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}
