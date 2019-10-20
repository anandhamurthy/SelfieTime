package com.selfietime.selfietime;

import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;

public class GreetingActivity extends AppCompatActivity {

    private KonfettiView Greeting_Glitters;
    private ImageView Greeting_Image;
    private TextView Greeting_Greet, Greeting_Name;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greeting);

        String image = getIntent().getStringExtra("image");
        String name = getIntent().getStringExtra("name");
        String greet = getIntent().getStringExtra("greetings");
        String from = getIntent().getStringExtra("from_name");

        Greeting_Image = findViewById(R.id.greeting_image);
        Greeting_Name = findViewById(R.id.greeting_name);
        Greeting_Greet = findViewById(R.id.greeting_greet);
        Greeting_Name.setText(name);
        Greeting_Greet.setText(from + "\n" + "Wishes You " + "\n" + greet);
        Glide.with(getApplicationContext()).load(image).into(Greeting_Image);

        Greeting_Glitters = findViewById(R.id.greeting_glitters);
        Greeting_Glitters.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(5000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .setPosition(-50f, Greeting_Glitters.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 10000L);
    }
}
