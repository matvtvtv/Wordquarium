package com.example.wordquarium.controllers;

import android.content.Context;

import com.example.wordquarium.models.PreferencesModel;

public class SettingsController {

    public interface ViewContract {
        void showToast(String message);
    }

    private final PreferencesModel model;
    private ViewContract view;

    public SettingsController(Context context) {
        this.model = new PreferencesModel(context);
    }

    public void setView(ViewContract view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }


}
