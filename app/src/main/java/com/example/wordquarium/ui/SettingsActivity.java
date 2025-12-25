package com.example.wordquarium.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.wordquarium.R;
import com.example.wordquarium.controllers.SettingsController;
import com.example.wordquarium.ui.fragments.SettingsFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {

    private ImageView exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupExitButton();

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.settings_container, fragment);
            ft.commit();
        }
    }

    private void initViews() {
        exit = findViewById(R.id.button); // Убедитесь, что ID соответствует XML
    }

    private void setupExitButton() {
        if (exit != null) {
            exit.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }
    }


}
