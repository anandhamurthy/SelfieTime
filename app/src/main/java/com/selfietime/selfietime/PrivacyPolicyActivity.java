package com.selfietime.selfietime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;


public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        startActivity(new Intent(PrivacyPolicyActivity.this, InterstitialAdActivity.class));
        WebView view = new WebView(this);
        view.getSettings().setJavaScriptEnabled(true);
        view.loadUrl("file:///android_asset/private_policy.html");
        setContentView(view);
    }

}
