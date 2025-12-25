package com.example.wordquarium.data.repository;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.wordquarium.logic.adapters.Item;

import java.util.ArrayList;
import java.util.List;

public class AdminRepository {

    private final SQLiteDatabase db;

    public AdminRepository(Context context) {
        db = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    // -------- СЛОВА --------

    public List<Item> getAllWords() {
        List<Item> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT id, word FROM words", null);
        while (c.moveToNext()) {
            list.add(new Item(c.getInt(0), c.getString(1)));
        }
        c.close();
        return list;
    }

    public void insertWord(String text) {
        ContentValues cv = new ContentValues();
        cv.put("word", text);
        cv.put("length", text.length());
        db.insert("words", null, cv);
    }

    public void updateWord(int id, String text) {
        ContentValues cv = new ContentValues();
        cv.put("word", text);
        cv.put("length", text.length());
        db.update("words", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteWord(int id) {
        db.delete("words", "id=?", new String[]{String.valueOf(id)});
    }

    // -------- ФРАЗЫ --------

    public List<Item> getAllPhrases() {
        List<Item> list = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT road_text_id, road_text FROM road_text_table", null);
        while (c.moveToNext()) {
            list.add(new Item(c.getInt(0), c.getString(1)));
        }
        c.close();
        return list;
    }

    public void insertPhrase(String text) {
        ContentValues cv = new ContentValues();
        cv.put("road_text", text);
        db.insert("road_text_table", null, cv);
    }

    public void updatePhrase(int id, String text) {
        ContentValues cv = new ContentValues();
        cv.put("road_text", text);
        db.update("road_text_table", cv, "road_text_id=?", new String[]{String.valueOf(id)});
    }

    public void deletePhrase(int id) {
        db.delete("road_text_table", "road_text_id=?", new String[]{String.valueOf(id)});
    }
}
