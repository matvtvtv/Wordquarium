package com.example.wordquarium.ui;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.network.CallbackUser;
import com.example.wordquarium.data.network.DataFromUserAPI;
import com.example.wordquarium.data.repository.PlayerRepository;
import com.example.wordquarium.data.repository.PlayerSettingsRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextLogin, editTextPassword;
    private TextView textViewMessage;
    private ProgressBar progressBar;

    private PlayerRepository playerRepository;
    private PlayerSettingsRepository playerSettingsRepository;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private DataFromUserAPI dataFromUserAPI = new DataFromUserAPI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewMessage = findViewById(R.id.textViewMessage);
        progressBar = findViewById(R.id.progressBar);

        CardView btnRegister = findViewById(R.id.registation_card);
        CardView btnLog = findViewById(R.id.entarance_card);

        playerRepository = PlayerRepository.getInstance(this);
        playerSettingsRepository = PlayerSettingsRepository.getInstance(this);

        btnRegister.setOnClickListener(v -> registerUser());
        btnLog.setOnClickListener(v -> loginUser());
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> progressBar.setVisibility(show ? View.VISIBLE : View.GONE));
    }

    private void registerUser() {
        String login = editTextLogin.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            textViewMessage.setText("Введите логин и пароль!");
            return;
        }

        showLoading(true);

        executor.execute(() ->
                dataFromUserAPI.getRegistration(login, password, new CallbackUser() {
                    @Override
                    public void onSuccess(PlayerModel playerModel) {
                        showLoading(false);
                        saveToRepository(playerModel);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        showLoading(false);
                        runOnUiThread(() ->
                                textViewMessage.setText("Логин уже существует!")
                        );
                    }
                })
        );
    }

    private void loginUser() {
        String login = editTextLogin.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            textViewMessage.setText("Введите логин и пароль!");
            return;
        }

        showLoading(true);

        executor.execute(() ->
                dataFromUserAPI.getEnter(login, password, new CallbackUser() {
                    @Override
                    public void onSuccess(PlayerModel playerModel) {
                        showLoading(false);
                        saveToRepository(playerModel);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        showLoading(false);
                        runOnUiThread(() ->
                                textViewMessage.setText("Аккаунт не найден!")
                        );
                    }
                })
        );
    }

    private void saveToRepository(PlayerModel playerModel) {
        playerRepository.userRegistration(playerModel, this);
        int userId = playerRepository.getCurrentUserId();
        playerSettingsRepository.userSettingsRegistration(userId, this);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

