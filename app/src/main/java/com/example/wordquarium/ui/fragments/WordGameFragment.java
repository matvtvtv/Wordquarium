package com.example.wordquarium.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wordquarium.R;
import com.example.wordquarium.ui.GameChainActivity;
import com.example.wordquarium.ui.GameWordlyActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WordGameFragment extends Fragment {
    private CardView endless;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_word_game, container, false);
        getAllId(view);
        endless.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), GameChainActivity.class);
                startActivity(intent);
        });

        return view;
    }

    private void getAllId(View view) {
        endless = view.findViewById(R.id.endless);
    }
}
