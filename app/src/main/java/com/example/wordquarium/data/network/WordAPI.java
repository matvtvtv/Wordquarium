package com.example.wordquarium.data.network;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WordAPI {

    // используем тот же base URL
    public static final String Api = "https://t7lvb7zl-8080.euw.devtunnels.ms/api";
    //public static final String Api = "https://jds25q4d-8004.euw.devtunnels.ms/api";

    public void getWordExplanation(String word, CallbackWord callbackWord) {

        OkHttpClient client = new OkHttpClient();



        Request request = new Request.Builder()
                .url(Api + "/word_know/" + word)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("WordAPI", "Error onFailure", e);
                callbackWord.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {

                if (response.isSuccessful() && response.body() != null) {
                    String explanation = response.body().string();
                    Log.d("WordAPI", "Response: " + explanation);
                    callbackWord.onSuccess(explanation);
                } else {
                    Log.e("WordAPI", "Failed: " + response.code());
                    callbackWord.onError(
                            new Exception("Failed: " + response.code())
                    );
                }
            }
        });
    }

    public void getWordHint(String word, CallbackWord callbackWord) {

        OkHttpClient client = new OkHttpClient();



        Request request = new Request.Builder()
                .url(Api + "/word_hint/" + word)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("WordAPI", "Error onFailure", e);
                callbackWord.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {

                if (response.isSuccessful() && response.body() != null) {
                    String explanation = response.body().string();
                    Log.d("WordAPI", "Response: " + explanation);
                    callbackWord.onSuccess(explanation);
                } else {
                    Log.e("WordAPI", "Failed: " + response.code());
                    callbackWord.onError(
                            new Exception("Failed: " + response.code())
                    );
                }
            }
        });
    }

    public void getPhraseExplanation(String phrase, CallbackWord callbackWord) {

        OkHttpClient client = new OkHttpClient();



        Request request = new Request.Builder()
                .url(Api + "/explain_phrase/" + phrase)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("WordAPI", "Error onFailure", e);
                callbackWord.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call,
                                   @NonNull Response response) throws IOException {

                if (response.isSuccessful() && response.body() != null) {
                    String explanation = response.body().string();
                    Log.d("WordAPI", "Response: " + explanation);
                    callbackWord.onSuccess(explanation);
                } else {
                    Log.e("WordAPI", "Failed: " + response.code());
                    callbackWord.onError(
                            new Exception("Failed: " + response.code())
                    );
                }
            }
        });
    }
}
