package com.example.wordquarium.data.repository;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RoadOfTextFileReader {

    /**
     * Читает строки из файла в assets (UTF-8), игнорирует пустые строки.
     */
    public static List<String> readFromAssets(Context ctx, String assetFileName) {
        List<String> list = new ArrayList<>();
        try (InputStream is = ctx.getAssets().open(assetFileName);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    list.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


}
