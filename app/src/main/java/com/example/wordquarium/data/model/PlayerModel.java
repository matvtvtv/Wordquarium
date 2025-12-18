package com.example.wordquarium.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;



    @AllArgsConstructor
    @Data
    public class PlayerModel {

        private int userId;                        // COLUMN_USER_ID
        private String login;                      // COLUMN_USER_LOGIN
        private String password;                   // COLUMN_USER_PASSWORD

        // Wordly stats
        private int level;                         // COLUMN_USER_LEVEL_WORDLY
        private int gamesWinWordly;                // COLUMN_USER_GAMES_WIN_WORDLY
        private int maxSeriesWinsWordly;           // COLUMN_USER_MAX_SERIES_WINS_WORDLY
        private int currentSeriesWinsWordly;       // COLUMN_USER_CURRENT_SERIES_WINS_WORDLY

        // Endless / Speed / Time game modes
        private int bestChainEndless;              // COLUMN_USER_BEST_CHAIN_ENDLESS
        private int bestChainSpeed;                // COLUMN_USER_BEST_CHAIN_SPEED
        private int bestChainTime;                 // COLUMN_USER_BEST_CHAIN_TIME

        // Cryptogram wins
        private int cryptogramEasyWins;            // COLUMN_USER_CRYPTOGRAM_EASY_WINS
        private int cryptogramMiddleWins;          // COLUMN_USER_CRYPTOGRAM_MIDDLE_WINS
        private int cryptogramHardWins;            // COLUMN_USER_CRYPTOGRAM_HARD_WINS

        // Money
        private int money;                         // COLUMN_USER_MONEY

        // Daily word
        private String wordDay;                    // COLUMN_USER_WORDDAY
    }

