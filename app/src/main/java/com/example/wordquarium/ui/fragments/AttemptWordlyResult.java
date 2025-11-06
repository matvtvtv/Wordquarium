package com.example.wordquarium.ui.fragments;

import com.example.wordquarium.logic.adapters.LetterStatus;

public class AttemptWordlyResult {
    private String guess;
    private LetterStatus[] statuses;

    public AttemptWordlyResult(String guess, LetterStatus[] statuses) {
        this.guess = guess;
        this.statuses = statuses;
    }

    public String getGuess() {
        return guess;
    }

    public LetterStatus[] getStatuses() {
        return statuses;
    }
}
