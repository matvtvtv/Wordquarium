package com.example.wordquarium.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.wordquarium.R;
import com.example.wordquarium.logic.adapters.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;
    private BottomNavigationView bottomNavigationView;
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

        getAllId();
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

    }

    private void getAllId() {

        bottomNavigationView=findViewById(R.id.navig_menu);
        viewPager=findViewById(R.id.viewPager);
    }
}