package com.example.wordquarium.ui.fragments;




public class GameChainLogic {


    private String appWord;


    public void startNewGame(String startWord) {
        setAppWord(startWord);
    }


    public void setAppWord(String word) {
        this.appWord = word;
    }


    public String getAppWord() { return appWord; }


    // буква, с которой игрок должен начинать свое слово
    public char getRequiredStartLetter() {
        if (appWord == null || appWord.isEmpty()) return '\0';
        return appWord.charAt(appWord.length() - 1);
    }
}