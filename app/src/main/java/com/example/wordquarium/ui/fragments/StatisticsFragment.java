package com.example.wordquarium.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.repository.PlayerRepository;
import com.example.wordquarium.ui.StatisticsActivity;


public class StatisticsFragment extends Fragment {
    private TextView gamesWinWordlyValue;
    private TextView maxSeriesWinsWordlyValue;
    private TextView currentSeriesWinsWordlyValue;
    private TextView bestChainEndlessValue;
    private TextView bestChainSpeedValue;
    private TextView bestChainTimeValue;
    private TextView cryptogramEasyWinsValue;
    private TextView cryptogramMiddleWinsValue;
    private TextView cryptogramHardWinsValue;
    private PlayerRepository playerRepository;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        getAllId(view);
        PlayerRepository playerRepository = PlayerRepository.getInstance(getContext());
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);
        gamesWinWordlyValue.setText(String.valueOf(user.getGamesWinWordly()));
        maxSeriesWinsWordlyValue.setText(String.valueOf(user.getMaxSeriesWinsWordly()));
        currentSeriesWinsWordlyValue.setText(String.valueOf(user.getCurrentSeriesWinsWordly()));
        bestChainEndlessValue.setText(String.valueOf(user.getBestChainEndless()));
        bestChainSpeedValue.setText(String.valueOf(user.getBestChainSpeed()));
        bestChainTimeValue.setText(String.valueOf(user.getBestChainTime()));
        cryptogramEasyWinsValue.setText(String.valueOf(user.getCryptogramEasyWins()));
        cryptogramMiddleWinsValue.setText(String.valueOf(user.getCryptogramMiddleWins()));
        cryptogramHardWinsValue.setText(String.valueOf(user.getCryptogramHardWins()));
        return view;

    }

    void getAllId(View view){
        gamesWinWordlyValue = view.findViewById(R.id.gamesWinWordlyValue);
        maxSeriesWinsWordlyValue = view.findViewById(R.id.maxSeriesWinsWordlyValue);
        currentSeriesWinsWordlyValue = view.findViewById(R.id.currentSeriesWinsWordlyValue);

        bestChainEndlessValue = view.findViewById(R.id.bestChainEndlessValue);
        bestChainSpeedValue = view.findViewById(R.id.bestChainSpeedValue);
        bestChainTimeValue = view.findViewById(R.id.bestChainTimeValue);

        cryptogramEasyWinsValue = view.findViewById(R.id.cryptogramEasyWinsValue);
        cryptogramMiddleWinsValue = view.findViewById(R.id.cryptogramMiddleWinsValue);
        cryptogramHardWinsValue = view.findViewById(R.id.cryptogramHardWinsValue);

    }
}