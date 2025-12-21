package com.example.wordquarium.data.network;

public class DataFromWordAPI {

    private final WordAPI wordAPI;

    public DataFromWordAPI() {
        this.wordAPI = new WordAPI();
    }

    public void getWordExplanation(String word, CallbackWord callbackWord) {
        wordAPI.getWordExplanation(word, new CallbackWord() {
            @Override
            public void onSuccess(String explanation) {
                callbackWord.onSuccess(explanation);
            }

            @Override
            public void onError(Throwable throwable) {
                callbackWord.onError(throwable);
            }
        });
    }
}
