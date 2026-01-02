package com.example.wordquarium.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.wordquarium.R;
import com.example.wordquarium.data.repository.AdminRepository;
import com.example.wordquarium.logic.adapters.Item;

public class AdminEditDialog extends DialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_TEXT = "text";
    private static final String ARG_TYPE = "type"; // word | phrase
    private static final String ARG_IS_NEW = "is_new";

    private AdminRepository repository;

    // ====== OPENERS ======

    public static AdminEditDialog openWord(@Nullable Item item) {
        return open(item, "word");
    }

    public static AdminEditDialog openPhrase(@Nullable Item item) {
        return open(item, "phrase");
    }

    private static AdminEditDialog open(@Nullable Item item, String type) {
        Bundle b = new Bundle();
        b.putString(ARG_TYPE, type);

        if (item != null) {
            b.putInt(ARG_ID, item.id);
            b.putString(ARG_TEXT, item.text);
            b.putBoolean(ARG_IS_NEW, false);
        } else {
            b.putBoolean(ARG_IS_NEW, true);
        }

        AdminEditDialog d = new AdminEditDialog();
        d.setArguments(b);
        return d;
    }

    // ====== UI ======

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_admin_edit, container, false);
        repository = new AdminRepository(requireContext());

        EditText et = v.findViewById(R.id.etText);
        Button save = v.findViewById(R.id.btnSave);
        Button del = v.findViewById(R.id.btnDelete);

        Bundle args = getArguments();
        boolean isNew = args.getBoolean(ARG_IS_NEW);
        String type = args.getString(ARG_TYPE);

        if (!isNew) {
            et.setText(args.getString(ARG_TEXT));
        } else {
            del.setVisibility(View.GONE); // 👈 при добавлении удалять нечего
        }

        save.setOnClickListener(v1 -> {
            String text = et.getText().toString().trim();

            if (text.isEmpty()) {
                et.setError("Поле не может быть пустым");
                return;
            }

            if ("word".equals(type)) {
                if (isNew) {
                    repository.insertWord(text);
                } else {
                    repository.updateWord(args.getInt(ARG_ID), text);
                }
            } else {
                if (isNew) {
                    repository.insertPhrase(text);
                } else {
                    repository.updatePhrase(args.getInt(ARG_ID), text);
                }
            }

            dismiss();
        });

        del.setOnClickListener(v12 -> {
            int id = args.getInt(ARG_ID);
            if ("word".equals(type)) {
                repository.deleteWord(id);
            } else {
                repository.deletePhrase(id);
            }
            dismiss();
        });

        return v;
    }
}
