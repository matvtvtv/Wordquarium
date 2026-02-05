package com.example.wordquarium.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wordquarium.R;
import com.example.wordquarium.databinding.FragmentChainBinding;
import com.example.wordquarium.databinding.FragmentCryptogramBinding;
import com.example.wordquarium.ui.CryptogramActivity;
import com.example.wordquarium.ui.GameChainActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CryptogramFragment extends Fragment {


    private FragmentCryptogramBinding binding;
    private CardView btn_Eazy;
    private CardView btn_Medium;
    private CardView btn_Hard;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCryptogramBinding.inflate(inflater, container, false);
        getAllId();
        btn_Eazy.setOnClickListener(v -> {
            setDiff(8);

        });
        btn_Medium.setOnClickListener(v -> {

            setDiff(6);

        });
        btn_Hard.setOnClickListener(v -> {

            setDiff(4);

        });

        return binding.getRoot(); // возвращаем корень

    }

    private void getAllId() {
        btn_Eazy=binding.cardBtn4;
        btn_Medium=binding.cardBtn5;
        btn_Hard=binding.cardBtn6;

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // освобождаем binding
    }
    private void setDiff(int diff) {
        Intent intent = new Intent(getContext(), CryptogramActivity.class);
        intent.putExtra("DIFF", diff);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        requireActivity().finish(); // finish() ПОСЛЕ startActivity()
    }

}