package com.example.wordquarium.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.wordquarium.data.model.WordsModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WordsRepository {
    private static final String TAG = "WordsRepository";

    // кеш слов (опционально)
    private final List<WordsModel> wordsList = new ArrayList<>();

    private final SQLiteDatabase db;
    private final Random rnd = new Random();

    public WordsRepository(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Импорт слов из файла в БД.
     * Выполняется в транзакции для производительности.
     */
    public void importWordsFromFile(Context context) {
        WordsFileReader fileReader = new WordsFileReader(context);
        List<WordsModel> fileWords = fileReader.getWordsList(); // Загружаем слова из файла
        if (fileWords == null || fileWords.isEmpty()) return;

        db.beginTransaction();
        try {
            for (WordsModel word : fileWords) {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_ID_WORDS, word.getId());
                values.put(DatabaseHelper.COLUMN_WORD_WORDS, word.getWord());
                values.put(DatabaseHelper.COLUMN_LENGTH_WORDS, word.getLength());
                db.insertWithOnConflict(DatabaseHelper.WORD_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "importWordsFromFile error: " + e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        // Обновим кеш при необходимости
        reloadCache();
    }

    /**
     * Возвращает список слов заданной длины.
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
                    int len = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LENGTH_WORDS));
                    result.add(new WordsModel(id, word, len));
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
                    int len = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LENGTH_WORDS));
                    result.add(new WordsModel(id, word, len));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    /**
     * Получить слова, начинающиеся с указанной буквы.
     * Используем LIKE ? COLLATE NOCASE для игнорирования регистра.
     */
    public List<WordsModel> getWordsStartingWith(char startChar) {
        List<WordsModel> result = new ArrayList<>();
        String prefix = String.valueOf(startChar);
        String query = "SELECT * FROM " + DatabaseHelper.WORD_TABLE +
                " WHERE " + DatabaseHelper.COLUMN_WORD_WORDS + " LIKE ? COLLATE NOCASE";
        Cursor cursor = db.rawQuery(query, new String[]{prefix + "%"});

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID_WORDS));
                    String word = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WORD_WORDS));
                    int len = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LENGTH_WORDS));
                    result.add(new WordsModel(id, word, len));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return result;
    }



    public boolean isValidWord(String guess) {
        if (guess == null) return false;
        String normalized = guess.trim().toLowerCase();
        String query = "SELECT 1 FROM " + DatabaseHelper.WORD_TABLE +
                " WHERE LOWER(" + DatabaseHelper.COLUMN_WORD_WORDS + ") = ? LIMIT 1";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[]{normalized});
            boolean found = cursor.moveToFirst(); // <-- TRUE если слово нашлось
            return !found;
        } finally {
            if (cursor != null) cursor.close();
        }
    }


    public WordsModel getRandomWordByStartingLetter(char start, Set<String> excludedUppercase) {
        List<WordsModel> candidates = getWordsStartingWith(start);
        if (candidates.isEmpty()) return null;

        if (excludedUppercase != null && !excludedUppercase.isEmpty()) {
            List<WordsModel> filtered = new ArrayList<>();
            for (WordsModel w : candidates) {
                if (!excludedUppercase.contains(w.getWord().toUpperCase())) filtered.add(w);
            }
            if (filtered.isEmpty()) return null;
            return filtered.get(rnd.nextInt(filtered.size()));
        } else {
            return candidates.get(rnd.nextInt(candidates.size()));
        }
    }

    /**
     * Проверка, пуста ли таблица слов.
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
     * Перезагрузить кеш из БД.
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
     * Вернуть копию кеша.
     */
    public List<WordsModel> getCachedWords() {
        return new ArrayList<>(wordsList);
    }
}
