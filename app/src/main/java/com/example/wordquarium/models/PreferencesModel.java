package com.example.wordquarium.models;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesModel {
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_FIRST_RUN = "isFirstRun"; // оставляем при желании

    private final SharedPreferences prefs;

    public PreferencesModel(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // user id (auth)
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void setUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    // first run (по желанию)
    public boolean isFirstRun() {
        return prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    public void setFirstRunFalse() {
        prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
    }
}
