package com.example.wordquarium.controllers;

import android.content.Context;

import com.example.wordquarium.models.PreferencesModel;


public class MainController {



    private final PreferencesModel prefsModel;
    private SettingsController.ViewContract view;

    public MainController(Context appContext) {
        prefsModel = new PreferencesModel(appContext);
    }

    public void attachView(SettingsController.ViewContract view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
    }


}
