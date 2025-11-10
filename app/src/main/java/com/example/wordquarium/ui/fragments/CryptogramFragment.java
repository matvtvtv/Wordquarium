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
    private CardView cardBtn4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCryptogramBinding.inflate(inflater, container, false);
        getAllId();
        cardBtn4.setOnClickListener(v -> {

            Intent intent = new Intent(getContext(), CryptogramActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return binding.getRoot(); // возвращаем корень

    }

    private void getAllId() {
        cardBtn4 = binding.cardBtn4;

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // освобождаем binding
    }
}