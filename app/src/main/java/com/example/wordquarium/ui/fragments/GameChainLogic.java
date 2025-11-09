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




    public char getRequiredStartLetter() {
        if (appWord == null || appWord.isEmpty()) return '\0';


        // Берём последнюю реальную букву
        int lastIndex = appWord.length() - 1;
        char last = appWord.charAt(lastIndex);

        // Если последний символ Ъ или Ь — берём предыдущий
        if ((last == 'Ъ' || last == 'Ь') && lastIndex > 0) {
            return appWord.charAt(lastIndex - 1);
        }

        return last;
    }

}