package com.example.wordquarium.ui;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.DialogFragment;

import com.example.wordquarium.R;
import com.example.wordquarium.data.repository.AdminRepository;
import com.example.wordquarium.logic.adapters.Item;

public class AdminEditDialog extends DialogFragment {

    private static final String ARG_ID = "id";
    private static final String ARG_TEXT = "text";
    private static final String ARG_TYPE = "type"; // word или phrase

    private AdminRepository repository;

    public static AdminEditDialog openWord(Item item) {
        return open(item, "word");
    }

    public static AdminEditDialog openPhrase(Item item) {
        return open(item, "phrase");
    }

    private static AdminEditDialog open(Item item, String type) {
        Bundle b = new Bundle();
        b.putInt(ARG_ID, item.id);
        b.putString(ARG_TEXT, item.text);
        b.putString(ARG_TYPE, type);
        AdminEditDialog d = new AdminEditDialog();
        d.setArguments(b);
        return d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_admin_edit, container, false);

        repository = new AdminRepository(requireContext());

        EditText et = v.findViewById(R.id.etText);
        Button save = v.findViewById(R.id.btnSave);
        Button del = v.findViewById(R.id.btnDelete);

        int id = getArguments().getInt(ARG_ID);
        String type = getArguments().getString(ARG_TYPE);

        et.setText(getArguments().getString(ARG_TEXT));

        save.setOnClickListener(x -> {
            String text = et.getText().toString();
            if ("word".equals(type)) {
                repository.updateWord(id, text);
            } else {
                repository.updatePhrase(id, text);
            }
            dismiss();
        });

        del.setOnClickListener(x -> {
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
