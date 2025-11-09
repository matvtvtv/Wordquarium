package com.example.wordquarium.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.wordquarium.data.model.WordsModel;

import java.util.ArrayList;
import java.util.List;

public class WordsRepository {
    // кеш слов (не обязателен — можно загружать по запросу)
    private final List<WordsModel> wordsList = new ArrayList<>();

    private final SQLiteDatabase db;

    public WordsRepository(SQLiteDatabase db) {
        this.db = db;
    }

    // Импорт слов из файла в БД (не менял логику — оставил как было)
    public void importWordsFromFile(Context context) {
        WordsFileReader fileReader = new WordsFileReader(context);
        List<WordsModel> fileWords = fileReader.getWordsList(); // Загружаем слова из файла

        for (WordsModel word : fileWords) {
            ContentValues values = new ContentValues();
            values.put("id", word.getId());
            values.put("word", word.getWord());
            values.put("difficulty", word.getDifficulty());
            values.put("length", word.getLength());

            db.insert("words", null, values);
        }

        // обновим кеш
        reloadCache();
    }

    /**
     * Возвращает список слов определённой длины (корректная версия).
     * Здесь возвращается новый список, а не мутируется поле wordsList.
     */
    public List<WordsModel> getFilteredWordsFree(int length) {
        List<WordsModel> result = new ArrayList<>();
        String query = "SELECT * FROM " + DatabaseHelper.WORD_TABLE +
                " WHERE " + DatabaseHelper.COLUMN_LENGTH_WORDS + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(length)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_WORDS));
                    String word = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WORD_WORDS));
                    int diff = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIFFICULT_WORDS));
                    int len = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LENGTH_WORDS));

                    result.add(new WordsModel(id, word, diff, len));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    /**
     * Получить все слова из таблицы.
     */
    public List<WordsModel> getAllWords() {
        List<WordsModel> result = new ArrayList<>();
        String query = "SELECT * FROM " + DatabaseHelper.WORD_TABLE;
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_WORDS));
                    String word = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WORD_WORDS));
                    int diff = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIFFICULT_WORDS));
                    int len = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LENGTH_WORDS));

                    result.add(new WordsModel(id, word, diff, len));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    /**
     * Получить слова, начинающиеся с указанной буквы.
     * Используем COLLATE NOCASE чтобы игнорировать регистр.
     */
    public List<WordsModel> getWordsStartingWith(char startChar) {
        List<WordsModel> result = new ArrayList<>();
        String prefix = String.valueOf(startChar).toUpperCase();
        String query = "SELECT * FROM " + DatabaseHelper.WORD_TABLE +
                " WHERE " + DatabaseHelper.COLUMN_WORD_WORDS + " LIKE ? COLLATE NOCASE";
        Cursor cursor = db.rawQuery(query, new String[]{prefix + "%"});

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_WORDS));
                    String word = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WORD_WORDS));
                    int diff = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIFFICULT_WORDS));
                    int len = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LENGTH_WORDS));
                    result.add(new WordsModel(id, word, diff, len));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    /**
     * Получить слова по длине и сложности.
     */
    public List<WordsModel> getWordsByLengthAndDifficulty(int difficulty, int length) {
        List<WordsModel> result = new ArrayList<>();
        String query = "SELECT * FROM " + DatabaseHelper.WORD_TABLE +
                " WHERE " + DatabaseHelper.COLUMN_DIFFICULT_WORDS + " = ? AND " +
                DatabaseHelper.COLUMN_LENGTH_WORDS + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(difficulty), String.valueOf(length)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_WORDS));
                    String word = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WORD_WORDS));
                    int diff = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIFFICULT_WORDS));
                    int len = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LENGTH_WORDS));
                    result.add(new WordsModel(id, word, diff, len));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return result;
    }


    public boolean isValidWord(String guess) {

        for(int i = 0; i<wordsList.size(); i++){
            if(guess.equalsIgnoreCase(wordsList.get(i).getWord())) {
                return true;
            }
        }

        return false;
    }




    /**
     * Проверяет, пуста ли таблица слов.
     */
    public boolean isTableEmpty() {
        String query = "SELECT COUNT(1) AS cnt FROM " + DatabaseHelper.WORD_TABLE;
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor.moveToFirst()) {
                int cnt = cursor.getInt(cursor.getColumnIndexOrThrow("cnt"));
                return cnt == 0;
            }
            return true;
        } finally {
            cursor.close();
        }
    }

    /**
     * Перезагрузить кеш из БД (если используешь кеширование).
     */
    public void reloadCache() {
        wordsList.clear();
        wordsList.addAll(getAllWords());
    }

    /**
     * Очистить локальный кеш.
     */
    public void clearCache() {
        wordsList.clear();
    }

    /**
     * Возвращает кеш (может быть пустым, если не загружен).
     */
    public List<WordsModel> getCachedWords() {
        return new ArrayList<>(wordsList);
    }
}
