package com.example.wordquarium.data.repository;

import android.content.ContentValues;

@FunctionalInterface
public interface OnDataUpdateListener {
    public void onUpdate(ContentValues values);
}
