package com.example.wordquarium.data.repository;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "words.db";
    private static final int DATABASE_VERSION = 1;

    public static final String WORD_TABLE = "words";
    public static final String COLUMN_ID_WORDS = "id";
    public static final String COLUMN_WORD_WORDS = "word";
    public static final String COLUMN_LENGTH_WORDS = "length";





    public static final String USER_TABLE = "user";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_USER_LOGIN = "login";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_LEVEL_WORDLY = "level";
    public static final String COLUMN_USER_GAMES_WIN_WORDLY = "gamesWinWordly";
    public static final String COLUMN_USER_MAX_SERIES_WINS_WORDLY = "maxSeriesWinsWordly";
    public static final String COLUMN_USER_CURRENT_SERIES_WINS_WORDLY = "currentSeriesWinsWordly";
    public static final String COLUMN_USER_BEST_CHAIN_ENDLESS = "bestChainEndless";
    public static final String COLUMN_USER_BEST_CHAIN_SPEED = "bestChainSpeed";
    public static final String COLUMN_USER_BEST_CHAIN_TIME = "bestChainTime";
    public static final String COLUMN_USER_CRYPTOGRAM_EASY_WINS = "cryptogramEasyWins";
    public static final String COLUMN_USER_CRYPTOGRAM_MIDDLE_WINS = "cryptogramMidWins";
    public static final String COLUMN_USER_CRYPTOGRAM_HARD_WINS = "cryptogramHardWins";
    public static final String COLUMN_USER_MONEY = "money";
    public static final String COLUMN_USER_WORDDAY = "wordDay";


    public static final String USER_SETTINGS_TABLE="settings";
    public static final String COLUMN_USER_SETTINGS_ID = "userId";
    public static final String COLUMN_USER_SOUND = "sound";
    public static final String COLUMN_USER_VIBRATION = "vibration";
    public static final String COLUMN_USER_PICTURE = "profileImage";
    public static final String COLUMN_USER_NOTIFICATION = "notification";


    public static final String ROAD_TEXT_TABLE = "road_text_table";
    public static final String COLUMN_ROAD_TEXT_ID = "road_text_id";
    public static final String COLUMN_ROAD_TEXT = "road_text";



    private static DatabaseHelper instance;
    private static final String CREATE_USER_TABLE = "CREATE TABLE " +  USER_TABLE+ " ( " +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_LOGIN + " TEXT, " +
            COLUMN_USER_PASSWORD + " TEXT, " +
            COLUMN_USER_LEVEL_WORDLY + " INTEGER, " +
            COLUMN_USER_GAMES_WIN_WORDLY + " INTEGER, " +
            COLUMN_USER_MAX_SERIES_WINS_WORDLY + " INTEGER, " +
            COLUMN_USER_CURRENT_SERIES_WINS_WORDLY + " INTEGER, " +
            COLUMN_USER_BEST_CHAIN_ENDLESS + " INTEGER, " +
            COLUMN_USER_BEST_CHAIN_SPEED + " INTEGER, " +
            COLUMN_USER_BEST_CHAIN_TIME + " INTEGER, " +
            COLUMN_USER_CRYPTOGRAM_EASY_WINS + " INTEGER, " +
            COLUMN_USER_CRYPTOGRAM_MIDDLE_WINS + " INTEGER, " +
            COLUMN_USER_CRYPTOGRAM_HARD_WINS + " INTEGER, " +
            COLUMN_USER_MONEY + " INTEGER, " +

            COLUMN_USER_WORDDAY + " INTEGER)";

    private static final String CREATE_TABLE_WORDS= "CREATE TABLE " + WORD_TABLE + " ( " +
            COLUMN_ID_WORDS + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_WORD_WORDS + " TEXT, " +
            COLUMN_LENGTH_WORDS + " INTEGER )";

    private static final String CREATE_USER_SETTINGS_TABLE = "CREATE TABLE " +  USER_SETTINGS_TABLE+ " ( " +
            COLUMN_USER_SETTINGS_ID + " INTEGER, " +
            COLUMN_USER_SOUND + " INTEGER, " +
            COLUMN_USER_VIBRATION + " INTEGER, " +
            COLUMN_USER_PICTURE + " BLOB ,"+
            COLUMN_USER_NOTIFICATION + " INTEGER )";

    private static final String CREATE_ROAD_TEXT_TABLE = "CREATE TABLE " + ROAD_TEXT_TABLE + " ( " +
            COLUMN_ROAD_TEXT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ROAD_TEXT + " TEXT )";
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WORDS);
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_USER_SETTINGS_TABLE);
        db.execSQL(CREATE_ROAD_TEXT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WORD_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USER_SETTINGS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ROAD_TEXT_TABLE);
        onCreate(db);
    }
}
