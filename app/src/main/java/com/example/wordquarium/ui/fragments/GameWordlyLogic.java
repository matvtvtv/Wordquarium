package com.example.wordquarium.ui.fragments;

import com.example.wordquarium.logic.adapters.LetterStatus;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class GameWordlyLogic {

        @Getter
        private String hiddenWord;
        @Getter private int maxAttempts;
        @Getter private int currentAttempt;
        @Getter private List<AttemptWordlyResult> history;

        public GameWordlyLogic(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            this.history = new ArrayList<>();
            this.currentAttempt = 0;
        }

        public void startNewGame(String newHiddenWord) {
            this.hiddenWord = newHiddenWord.toUpperCase();
            this.currentAttempt = 0;
            this.history.clear();
        }

        public AttemptWordlyResult checkWord(String guess) {
            if (isGameOver()) {
                throw new IllegalStateException("Игра уже завершена!");
            }
            guess = guess.toUpperCase();
            currentAttempt++;
            LetterStatus[] statuses = compareWords(hiddenWord, guess);
            AttemptWordlyResult result = new AttemptWordlyResult(guess, statuses);
            history.add(result);
            return result;
        }

        public boolean isGameWon() {
            if (history.isEmpty()) return false;
            AttemptWordlyResult lastResult = history.get(history.size() - 1);
            for (LetterStatus status : lastResult.getStatuses()) {
                if (status != LetterStatus.GREEN) {
                    return false;
                }
            }
            return true;
        }

        public boolean isGameOver() {
            return isGameWon() || (currentAttempt >= maxAttempts);
        }

        private LetterStatus[] compareWords(String hidden, String guess) {
            int length = hidden.length();
            LetterStatus[] result = new LetterStatus[length];
            char[] hiddenChars = hidden.toCharArray();
            char[] guessChars = guess.toCharArray();
            boolean[] used = new boolean[length];

            for (int i = 0; i < length; i++) {
                if (guessChars[i] == hiddenChars[i]) {
                    result[i] = LetterStatus.GREEN;
                    used[i] = true;
                } else {
                    result[i] = LetterStatus.GRAY;
                }
            }

            for (int i = 0; i < length; i++) {
                if (result[i] == LetterStatus.GREEN) continue;
                for (int j = 0; j < length; j++) {
                    if (!used[j] && guessChars[i] == hiddenChars[j]) {
                        result[i] = LetterStatus.YELLOW;
                        used[j] = true;
                        break;
                    }
                }
            }
            return result;
        }
    }

