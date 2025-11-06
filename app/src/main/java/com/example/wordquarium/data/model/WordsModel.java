package com.example.wordquarium.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class WordsModel {
    private int id;
    private String word;
    private int length;
    private int difficulty;
}

