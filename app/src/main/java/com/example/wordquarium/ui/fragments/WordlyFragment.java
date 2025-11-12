package com.example.wordquarium.ui.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.repository.PlayerRepository;
import com.example.wordquarium.ui.GameWordlyActivity;
import com.example.wordquarium.ui.MultiUserActivity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WordlyFragment extends Fragment {

    private CardView word_day;
    private CardView travel_button;
    private CardView letter_4_free;
    private CardView letter_5_free;
    private CardView letter_6_free;
    private CardView letter_7_free;
    private ProgressBar level;
    private CardView multiUser;


    private int GAME_MODE = 2;
    private int WORD_LENGTH = 0;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wordly, container, false);
        getAllId(view);
        PlayerRepository playerRepository = PlayerRepository.getInstance(getContext());
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);

        level.setProgress((user.getLevel()%5)*10,true);
        word_day.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedDate = now.format(formatter);
            if (!Objects.equals(user.getWordDay(), formattedDate)) {
                values.put("wordDay", formattedDate);
                playerRepository.updateUserData(userId, values);
                WORD_LENGTH = 5;
                Intent intent = new Intent(getContext(), GameWordlyActivity.class);
                intent.putExtra("WORD_LENGTH", WORD_LENGTH);
                GAME_MODE = 1;
                intent.putExtra("GAME_MODE", GAME_MODE);

                requireActivity().finish();
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Вы уже играли слово дня", Toast.LENGTH_SHORT).show();
            }
        });

        travel_button.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), GameWordlyActivity.class);
            GAME_MODE = 2;
            intent.putExtra("GAME_MODE", GAME_MODE);
            requireActivity().finish();
            startActivity(intent);
        });

        letter_4_free.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), GameWordlyActivity.class);
            WORD_LENGTH = 4;
            intent.putExtra("WORD_LENGTH", WORD_LENGTH);
            GAME_MODE = 3;
            intent.putExtra("GAME_MODE", GAME_MODE);
            requireActivity().finish();
            startActivity(intent);
        });

        letter_5_free.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), GameWordlyActivity.class);
            WORD_LENGTH = 5;
            intent.putExtra("WORD_LENGTH", WORD_LENGTH);
            GAME_MODE = 3;
            intent.putExtra("GAME_MODE", GAME_MODE);
            requireActivity().finish();
            startActivity(intent);
        });

        letter_6_free.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), GameWordlyActivity.class);
            WORD_LENGTH = 6;
            intent.putExtra("WORD_LENGTH", WORD_LENGTH);
            GAME_MODE = 3;
            intent.putExtra("GAME_MODE", GAME_MODE);
            requireActivity().finish();
            startActivity(intent);
        });

        letter_7_free.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), GameWordlyActivity.class);
            WORD_LENGTH = 7;
            intent.putExtra("WORD_LENGTH", WORD_LENGTH);
            GAME_MODE = 3;
            intent.putExtra("GAME_MODE", GAME_MODE);
            requireActivity().finish();
            startActivity(intent);
        });

        multiUser.setOnClickListener(v -> {

            startActivity(new Intent(requireContext(), MultiUserActivity.class));
        });

        return view;
    }

    private void getAllId(View view) {
        travel_button = view.findViewById(R.id.travel);
        letter_4_free = view.findViewById(R.id.cardBtn4);
        letter_5_free = view.findViewById(R.id.cardBtn5);
        letter_6_free = view.findViewById(R.id.cardBtn6);
        letter_7_free = view.findViewById(R.id.cardBtn7);
        word_day = view.findViewById(R.id.word_of_day);
        multiUser = view.findViewById(R.id.multi_user);
        level= view.findViewById(R.id.progressBar);
    }
}
