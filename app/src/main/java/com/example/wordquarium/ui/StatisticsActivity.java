package com.example.wordquarium.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.wordquarium.R;
import com.example.wordquarium.ui.fragments.StatisticsFragment;

public class StatisticsActivity extends AppCompatActivity {

    private ImageView exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        initViews();
        setupExitButton();
        if (savedInstanceState == null) {
            StatisticsFragment fragment = new StatisticsFragment();
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