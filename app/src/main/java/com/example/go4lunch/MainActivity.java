package com.example.go4lunch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.go4lunch.controller.HomepageActivity;

public class MainActivity extends AppCompatActivity {
    private Button signInFacebookButton;
    private Button signInGoogleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setReferences();
        signInWithFacebook();
        signInWithGoogle();
    }

    private void setReferences(){
        signInFacebookButton = findViewById(R.id.sign_in_facebook_button);
        signInGoogleButton = findViewById(R.id.sign_in_google_button);
    }


    private void signInWithFacebook(){
        signInFacebookButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HomepageActivity.class)));
    }

    private void signInWithGoogle(){
        signInGoogleButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HomepageActivity.class)));
    }
}