package com.example.wordquarium.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordquarium.R;
import com.example.wordquarium.data.repository.AdminRepository;
import com.example.wordquarium.logic.adapters.AdminAdapter;
import com.example.wordquarium.ui.AdminEditDialog;

public class AdminPhrasesFragment extends Fragment {

    private AdminRepository repository;
    private AdminAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_phrases, container, false);

        repository = new AdminRepository(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdminAdapter(item ->
                AdminEditDialog
                        .openPhrase(item)
                        .show(getParentFragmentManager(), "edit_phrase")
        );

        recyclerView.setAdapter(adapter);
        loadData();

        view.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            AdminEditDialog
                    .openPhrase(null)
                    .show(getParentFragmentManager(), "add_phrase");
        });

        return view;
    }

    private void loadData() {
        adapter.setData(repository.getAllPhrases());
    }
}
