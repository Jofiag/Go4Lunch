package com.example.go4lunch.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.go4lunch.R;

public class SettingsActivity extends AppCompatActivity {
    private TextView notificationsOnTextView;
    private TextView notificationsOffTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setReferences();

        manageNotifications();
    }

    private void setReferences() {
        notificationsOnTextView = findViewById(R.id.ntf_on_text_view);
        notificationsOffTextView = findViewById(R.id.ntf_off_text_view);
    }

    private void manageNotifications(){
        notificationsOnTextView.setOnClickListener(v -> {
            //TODO: turn off notifications
            Toast.makeText(this, "Notification ON", Toast.LENGTH_SHORT).show();
        });

        notificationsOffTextView.setOnClickListener(v -> {
            //TODO: turn off notifications
            Toast.makeText(this, "Notification OFF", Toast.LENGTH_SHORT).show();
        });
    }
}