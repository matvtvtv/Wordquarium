package com.example.wordquarium.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.wordquarium.R;
import com.example.wordquarium.logic.adapters.ViewPagerAdapter;
import com.example.wordquarium.logic.viewmodels.MainViewModel;
import com.example.wordquarium.ui.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
import android.widget.ImageView;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private SharedPreferences preferences;


    private ViewPager2 viewPager;
    private MainViewModel mainViewModel;
    private ViewPagerAdapter adapter;
    private BottomNavigationView bottomNavigationView;
    private ImageView settsButton;
    private ImageView statButton;

    private int currentSelectedItemId = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int isUserLoggedIn = preferences.getInt("userId", -1); // Проверка авторизации
        boolean isFirstRun = preferences.getBoolean("isFirstRun", true);


        if (isUserLoggedIn == -1) {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        getAllId();
        //в активности  this, а в фрагментах обязоательно  requareActivity()

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(true);
        viewPager.setOffscreenPageLimit(2);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == currentSelectedItemId) {
                return false;
            }
            currentSelectedItemId = itemId;
            switch (Objects.requireNonNull(item.getTitle()).toString()) {
                case "WordGameFragment":
                    viewPager.setCurrentItem(0);
                    return true;
                case "WordlyFragment":
                    viewPager.setCurrentItem(1);
                    return true;
                case "CryptogramFragment":
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        });
        if (savedInstanceState == null) {
            viewPager.setCurrentItem(1, false);
        }


        settsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        });
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        statButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void getAllId() {

        bottomNavigationView=findViewById(R.id.navig_menu);
        viewPager=findViewById(R.id.viewPager);
        settsButton=findViewById(R.id.setts_button);
        statButton=findViewById(R.id.stat_button);


    }


}