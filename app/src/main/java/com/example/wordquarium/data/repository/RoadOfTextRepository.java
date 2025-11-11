package com.example.wordquarium.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.wordquarium.data.model.RoadOfTextModel;

import java.util.ArrayList;
import java.util.List;

public class RoadOfTextRepository {
    private final DatabaseHelper dbHelper;

    public RoadOfTextRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Вставляет фразу в таблицу, если такой записи ещё нет.
     * Возвращает id вставленной записи или -1 при ошибке/если уже существует.
     */
    public long insertIfNotExists(String phrase) {
        if (phrase == null) return -1;

        // Проверка на существование
        if (exists(phrase)) return -1;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_ROAD_TEXT, phrase);
        long id = db.insert(DatabaseHelper.ROAD_TEXT_TABLE, null, cv);
        return id;
    }

    /**
     * Вставляет список фраз. Возвращает количество успешно вставленных.
     */
    public int insertAllIfNotExists(List<String> phrases) {
        if (phrases == null || phrases.isEmpty()) return 0;
        int inserted = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String p : phrases) {
                if (p == null) continue;
                if (!existsInternal(db, p)) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_ROAD_TEXT, p);
                    long id = db.insert(DatabaseHelper.ROAD_TEXT_TABLE, null, cv);
                    if (id != -1) inserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return inserted;
    }

    /**
     * Возвращает true, если фраза уже есть в таблице.
     */
    public boolean exists(String phrase) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return existsInternal(db, phrase);
    }

    private boolean existsInternal(SQLiteDatabase db, String phrase) {
        String sql = "SELECT 1 FROM " + DatabaseHelper.ROAD_TEXT_TABLE +
                " WHERE " + DatabaseHelper.COLUMN_ROAD_TEXT + " = ? LIMIT 1";
        try (Cursor c = db.rawQuery(sql, new String[]{phrase})) {
            return c != null && c.moveToFirst();
        }
    }

    /**
     * Возвращает все фразы из таблицы.
     */
    public List<RoadOfTextModel> getAll() {
        List<RoadOfTextModel> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] cols = new String[]{DatabaseHelper.COLUMN_ROAD_TEXT_ID, DatabaseHelper.COLUMN_ROAD_TEXT};
        try (Cursor c = db.query(DatabaseHelper.ROAD_TEXT_TABLE, cols, null, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idIdx = c.getColumnIndex(DatabaseHelper.COLUMN_ROAD_TEXT_ID);
                int textIdx = c.getColumnIndex(DatabaseHelper.COLUMN_ROAD_TEXT);
                do {
                    int id = c.getInt(idIdx);
                    String text = c.getString(textIdx);
                    list.add(new RoadOfTextModel(id, text));
                } while (c.moveToNext());
            }
        }
        return list;
    }


    public boolean isEmpty() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT COUNT(1) FROM " + DatabaseHelper.ROAD_TEXT_TABLE;
        try (Cursor c = db.rawQuery(sql, null)) {
            if (c != null && c.moveToFirst()) {
                return c.getInt(0) == 0;
            }
        }
        return true;
    }
}
