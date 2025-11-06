package com.example.wordquarium.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;



    @AllArgsConstructor
    @Data
    public class PlayerModel {

        private int userId;
        private String login;
        private String password;
        private int level;
        private int allGames;

        private int gamesWin;
        private int maxSeriesWins;
        private int currentSeriesWins;
        private int bestAttempt;
        private int oneAttempt;
        private int twoAttempt;
        private int threeAttempt;
        private int fourAttempt;
        private int fiveAttempt;
        private int sixAttempt;
        private int money;

        private String wordDay;
}
