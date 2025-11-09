package com.example.wordquarium.ui.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wordquarium.R;
import com.example.wordquarium.databinding.ActivityGameChainBinding;
import com.example.wordquarium.databinding.FragmentChainBinding;
import com.example.wordquarium.ui.GameChainActivity;
import com.example.wordquarium.ui.GameWordlyActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChainFragment extends Fragment {
    private CardView endless;
    private CardView btn30_Speed;
    private CardView btn20_Speed;
    private CardView btn10_Speed;
    private CardView btn_Eazy;
    private CardView btn_Medium;
    private CardView btn_Hard;
    private FragmentChainBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        binding = FragmentChainBinding.inflate(inflater, container, false);

        getAllId();

        endless.setOnClickListener(v -> {
            setTIME_GAMEMODE(66,false);
            requireActivity().finish();
        });
        btn30_Speed.setOnClickListener(v -> {
                setTIME_GAMEMODE(30,true);
                requireActivity().finish();
        });
        btn20_Speed.setOnClickListener(v -> {
            setTIME_GAMEMODE(20,true);
            requireActivity().finish();
        });
        btn10_Speed.setOnClickListener(v -> {
            setTIME_GAMEMODE(10,true);
            requireActivity().finish();
        });
        btn_Eazy.setOnClickListener(v -> {
            setTIME_GAMEMODE(120,false);
            requireActivity().finish();
        });
        btn_Medium.setOnClickListener(v -> {
            setTIME_GAMEMODE(180,false);
            requireActivity().finish();
        });
        btn_Hard.setOnClickListener(v -> {
            setTIME_GAMEMODE(240,false);
            requireActivity().finish();
        });

        return binding.getRoot(); // возвращаем корень

    }

    private void getAllId() {
        endless = binding.endless;
        btn30_Speed = binding.cardBtn30Speed;
        btn20_Speed = binding.cardBtn20Speed;
        btn10_Speed = binding.cardBtn10Speed;
        btn_Eazy=binding.cardBtn4;
        btn_Medium=binding.cardBtn5;
        btn_Hard=binding.cardBtn6;
    }
    private void setTIME_GAMEMODE(int time, boolean gamemode){

        Intent intent = new Intent(getContext(), GameChainActivity.class);
        intent.putExtra("TIME",time);
        intent.putExtra("GAMEMOD",gamemode);
        startActivity(intent);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // освобождаем binding
    }
}
