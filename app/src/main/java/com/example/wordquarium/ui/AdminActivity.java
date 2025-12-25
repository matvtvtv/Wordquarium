package com.example.wordquarium.ui;


import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.wordquarium.R;
import com.example.wordquarium.ui.fragments.AdminPhrasesFragment;
import com.example.wordquarium.ui.fragments.AdminWordsFragment;


public class AdminActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);


        openFragment(new AdminWordsFragment()); // по умолчанию слова


        findViewById(R.id.btnWords).setOnClickListener(v -> openFragment(new AdminWordsFragment()));
        findViewById(R.id.btnPhrases).setOnClickListener(v -> openFragment(new AdminPhrasesFragment()));
    }


    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.adminContainer, fragment)
                .commit();
    }
}