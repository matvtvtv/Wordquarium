package com.example.wordquarium.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.repository.DatabaseHelper;
import com.example.wordquarium.data.repository.PlayerRepository;
import com.example.wordquarium.data.repository.RoadOfTextFileReader;
import com.example.wordquarium.data.repository.RoadOfTextRepository;
import com.example.wordquarium.data.repository.WordsRepository;
import com.example.wordquarium.logic.adapters.ViewPagerAdapter;
import com.example.wordquarium.logic.viewmodels.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
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
    private TextView money_text;

    private int currentSelectedItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
        );
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int isUserLoggedIn = preferences.getInt("userId", -1);
        boolean isFirstRun = preferences.getBoolean("isFirstRun", true);

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        WordsRepository wordsRepository = new WordsRepository(db);
        RoadOfTextRepository repo = new RoadOfTextRepository(this);

        if (isFirstRun) {
            wordsRepository.importWordsFromFile(this);
            SharedPreferences.Editor editor = preferences.edit();
            List<String> phrases = RoadOfTextFileReader.readFromAssets(this, "Cryptogram.txt");
            int added = repo.insertAllIfNotExists(phrases);
            //Toast.makeText(this, "Импортировано фраз: " + added, Toast.LENGTH_LONG).show();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        }

        if (isUserLoggedIn == -1) {
            startActivity(new Intent(this, RegistrationActivity.class));
            finish();
            return;
        }

        PlayerRepository playerRepository = PlayerRepository.getInstance(this);
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);

        getAllId();
        money_text.setText(String.valueOf(user.getMoney()));

        playerRepository.addOnDataUpdateListener(values -> {
            Integer newMoney = (Integer) values.get("money");
            if (newMoney != null) {
                money_text.setText(String.valueOf(newMoney));
            }
        });

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(true);
        viewPager.setOffscreenPageLimit(2);

        // Восстановление последнего слайда
        int lastSlide = preferences.getInt("lastSlide", 1); // по умолчанию 1
        viewPager.setCurrentItem(lastSlide, false);
        bottomNavigationView.getMenu().getItem(lastSlide).setChecked(true);

        // Сохраняем текущий слайд при смене
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("lastSlide", position);
                editor.apply();
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
            viewPager.setCurrentItem(lastSlide, false);
        }

        settsButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });

        statButton.setOnClickListener(v -> {
            startActivity(new Intent(this, StatisticsActivity.class));
            finish();
        });
    }

    private void getAllId() {
        bottomNavigationView = findViewById(R.id.navig_menu);
        viewPager = findViewById(R.id.viewPager);
        settsButton = findViewById(R.id.setts_button);
        statButton = findViewById(R.id.stat_button);
        money_text = findViewById(R.id.money_text);
    }
}
