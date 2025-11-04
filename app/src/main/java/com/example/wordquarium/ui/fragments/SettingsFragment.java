package com.example.wordquarium.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.wordquarium.R;
import com.example.wordquarium.controllers.SettingsController;

public class SettingsFragment extends Fragment implements SettingsController.ViewContract {

    private SettingsController controller;
    private Switch switchNotifications;

    public SettingsFragment() {
        // Обязательный пустой конструктор
    }

    public void setController(SettingsController controller) {
        this.controller = controller;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (controller == null) {
            controller = new SettingsController(context);
        }
        controller.setView(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (controller != null) controller.detachView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
