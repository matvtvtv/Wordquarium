package com.example.wordquarium.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.MultiUserModel;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.network.CallbackMultiUserS;
import com.example.wordquarium.data.network.DataFromMultiUserAPI;
import com.example.wordquarium.data.repository.PlayerRepository;
import com.example.wordquarium.databinding.FragmentChainBinding;
import com.example.wordquarium.databinding.FragmentMultiUserListBinding;
import com.example.wordquarium.logic.adapters.MultiUserAdapter;
import com.example.wordquarium.ui.GameWordlyActivity;
import com.example.wordquarium.ui.MainActivity;
import com.example.wordquarium.ui.MultiUserActivity;
import com.example.wordquarium.ui.RegistrationActivity;

public class MultiUserList extends Fragment {

    private ImageView plus;
    private ImageView exit_m;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private FragmentMultiUserListBinding binding;

    public MultiUserList() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentMultiUserListBinding.inflate(inflater, container, false);

        getAllId();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        plus.setOnClickListener(v -> {
            MultiUserNewWordForFriend newFrag = new MultiUserNewWordForFriend();
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.lay_multi_user_list, newFrag)
                    .addToBackStack(null)
                    .commit();
        });

        exit_m.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
        });

        loadGames();

        return binding.getRoot();
    }

    private void loadGames() {
        showLoading(true);

        PlayerRepository playerRepository = PlayerRepository.getInstance(requireContext());
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);

        DataFromMultiUserAPI api = new DataFromMultiUserAPI();

        api.getMultiUserGames(user.getLogin(), new CallbackMultiUserS() {
            @Override
            public void onSuccess(MultiUserModel[] multiUsers) {
                if (getActivity() == null) return;

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);

                    MultiUserAdapter adapter =
                            new MultiUserAdapter(multiUsers, item -> openGameActivity(item));

                    recyclerView.setAdapter(adapter);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                if (getActivity() == null) return;

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    throwable.printStackTrace();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void openGameActivity(MultiUserModel chosenGame) {
        Intent intent = new Intent(requireContext(), GameWordlyActivity.class);

        intent.putExtra("GAME_MODE", 4);
        intent.putExtra("CHECK_OF_WORD", chosenGame.getFlagOfCheck());
        intent.putExtra("WORD_LENGTH", chosenGame.getWord().length());
        intent.putExtra("FRIEND_WORD", chosenGame.getWord());

        startActivity(intent);
    }

    private void getAllId() {
        plus = binding.plus;
        exit_m = binding.exitM;
        recyclerView = binding.recyclerMultiUser;
        progressBar = binding.progressBarMulti;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
