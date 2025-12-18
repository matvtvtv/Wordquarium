package com.example.wordquarium.data.repository;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;


public class PlayerRepository {
    private static PlayerRepository instance;
    private SQLiteDatabase db;
    private Context context;
    List<OnDataUpdateListener> onDataUpdateListeners = new ArrayList<>();

    private PlayerRepository(Context context) {
        this.context = context;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        this.db = dbHelper.getWritableDatabase();
    }

    public static synchronized PlayerRepository getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerRepository(context.getApplicationContext());
        }
        return instance;
    }

    // Регистрация нового пользователя
    public void userRegistration(PlayerModel playerModel, Context context) {
        ContentValues values = new ContentValues();

        values.put("login", playerModel.getLogin());
        values.put("password", playerModel.getPassword());

        // Wordly stats
        values.put("level", playerModel.getLevel());
        values.put("gamesWinWordly", playerModel.getGamesWinWordly());
        values.put("maxSeriesWinsWordly", playerModel.getMaxSeriesWinsWordly());
        values.put("currentSeriesWinsWordly", playerModel.getCurrentSeriesWinsWordly());

        // Game modes
        values.put("bestChainEndless", playerModel.getBestChainEndless());
        values.put("bestChainSpeed", playerModel.getBestChainSpeed());
        values.put("bestChainTime", playerModel.getBestChainTime());

        // Cryptogram wins
        values.put("cryptogramEasyWins", playerModel.getCryptogramEasyWins());
        values.put("cryptogramMidWins", playerModel.getCryptogramMiddleWins());
        values.put("cryptogramHardWins", playerModel.getCryptogramHardWins());

        // Money + Word of the day
        values.put("money", playerModel.getMoney());
        values.put("wordDay", playerModel.getWordDay());

        int userId = (int) db.insert("user", null, values);

        if (userId != -1) {
            saveUserId(userId);
        }
    }


    // Проверка, существует ли пользователь
    public boolean isValidUser(String login) {
        //  String query = "SELECT * FROM " + DatabaseHelper.WORD_TABLE + " WHERE " + DatabaseHelper.COLUMN_LENGTH_WORDS + " = ?";
        String query = "SELECT * FROM " + DatabaseHelper.USER_TABLE + " WHERE "+ DatabaseHelper.COLUMN_USER_LOGIN + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{login});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    // Получение userId по логину (например, при авторизации)
    public void getUserIdByLogin(String login) {
        int userId = -1;


        String query = "SELECT * FROM " + DatabaseHelper.USER_TABLE + " WHERE "+ DatabaseHelper.COLUMN_USER_LOGIN + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{login});
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        else {userId = -1;}

        saveUserId(userId); // Сохраняем ID нового пользователя
        cursor.close();
    }

    // Сохранение userId в SharedPreferences
    private void saveUserId(int userId) {
        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("userId", userId);
        editor.apply();
    }

    // Получение сохраненного userId
    public int getCurrentUserId() {
        SharedPreferences preferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return preferences.getInt("userId", -1);
    }

    // Получение данных пользователя
    public PlayerModel getUserData(int userId) {
        PlayerModel player = null;

        String query = "SELECT * FROM " + DatabaseHelper.USER_TABLE +
                " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {

            player = new PlayerModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_LOGIN)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_LEVEL_WORDLY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_GAMES_WIN_WORDLY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_MAX_SERIES_WINS_WORDLY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_CURRENT_SERIES_WINS_WORDLY)),

                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_BEST_CHAIN_ENDLESS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_BEST_CHAIN_SPEED)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_BEST_CHAIN_TIME)),

                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_CRYPTOGRAM_EASY_WINS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_CRYPTOGRAM_MIDDLE_WINS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_CRYPTOGRAM_HARD_WINS)),

                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_MONEY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_WORDDAY))
            );
        }

        cursor.close();
        return player;
    }


    // Обновление данных пользователя
    public void updateUserData(int userId, ContentValues values) {
        db.update("user", values,  DatabaseHelper.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        notifyDataUpdated(values);
    }

    public void addOnDataUpdateListener(OnDataUpdateListener listener) {
        onDataUpdateListeners.add(listener);
    }

    public void notifyDataUpdated(ContentValues values) {
        for (OnDataUpdateListener listener: onDataUpdateListeners) {
            listener.onUpdate(values);
        }
    }

}
