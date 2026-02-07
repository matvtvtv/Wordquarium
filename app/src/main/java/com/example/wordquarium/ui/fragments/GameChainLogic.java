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
        if ((last == 'ъ' || last == 'ь'|| last == 'ы') && lastIndex > 0) {
            return appWord.charAt(lastIndex - 1);
        }

        return last;
    }
    public char getRequiredStartLetterForWord(String word) {
        if (word == null || word.isEmpty()) return '\0';

        int lastIndex = word.length() - 1;
        char last = word.charAt(lastIndex);

        if ((last == 'ъ' || last == 'ь' || last == 'ы' ||
                last == 'Ъ' || last == 'Ь' || last == 'Ы') && lastIndex > 0) {
            return word.charAt(lastIndex - 1);
        }
        return last;
    }


}