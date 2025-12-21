package com.example.wordquarium.data.network;

public interface CallbackWord {
    void onSuccess(String explanation);
    void onError(Throwable throwable);
}
