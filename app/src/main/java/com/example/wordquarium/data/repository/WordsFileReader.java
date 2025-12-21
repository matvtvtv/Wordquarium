package com.example.wordquarium.data.repository;


import android.content.Context;
import android.util.Log;

import com.example.wordquarium.data.model.WordsModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WordsFileReader {
    private List<WordsModel> wordsList = new ArrayList<>();

    public WordsFileReader(Context context) {
        loadWords(context);
    }
    public List<WordsModel> getWordsList() {
        return wordsList;
    }

    private void loadWords(Context context) {
        try (InputStream is = context.getAssets().open("words_with_length.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int id=0;
            while ((line = reader.readLine()) != null) {
                // Формат строки: id,слово,difficulty,length
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    id++;
                    String word = parts[0].trim();
                    int length = Integer.parseInt(parts[1].trim());
                    wordsList.add(new WordsModel(id, word, length));

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}