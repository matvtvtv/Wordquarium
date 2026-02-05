package com.example.wordquarium.ui.fragments;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.wordquarium.data.model.MultiUserModel;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.model.WordsModel;
import com.example.wordquarium.data.network.CallbackMultiUser;
import com.example.wordquarium.data.network.DataFromMultiUserAPI;
import com.example.wordquarium.data.repository.DatabaseHelper;
import com.example.wordquarium.data.repository.PlayerRepository;
import com.example.wordquarium.data.repository.WordsRepository;
import com.example.wordquarium.databinding.FragmentMultiUserNewWordForFriendBinding;

import java.util.List;

public class MultiUserNewWordForFriend extends Fragment {

    private FragmentMultiUserNewWordForFriendBinding binding;
    private WordsRepository wordsRepository;
    public  EditText loginGuessing;

    public  EditText word;
    public  CheckBox radioButtonFlagOfCheck;
    public Button buttonNew;
    public  TextView textError;
    public  ImageView exit;


    private static final String RUSSIAN_WORD_PATTERN = "^[А-Яа-яЁё]{4,7}$";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMultiUserNewWordForFriendBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAllId();
        // Обработчик кнопки "назад"
        binding.exit.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // Обработчик кнопки "создать"
        binding.buttonNew.setOnClickListener(v -> {
            binding.textError.setText(""); // сброс ошибки

            PlayerRepository playerRepository = PlayerRepository.getInstance(requireContext());
            int userId = playerRepository.getCurrentUserId();
            PlayerModel user = playerRepository.getUserData(userId);
            if (user == null) {
                Toast.makeText(requireContext(), "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
                return;
            }

            String loginText = binding.loginGuessing.getText().toString().trim();
            String wordText = binding.word.getText().toString().trim();

            // Проверки
            if (TextUtils.isEmpty(loginText)) {
                binding.textError.setText("Введите логин.");
                return;
            }
            if (user.getLogin().equals(loginText)) {
                binding.textError.setText("Нельзя загадывать самому себе");
                return;
            }


            if (!wordText.matches(RUSSIAN_WORD_PATTERN)) {
                binding.textError.setText("Слово должно состоять из 4–7 русских букв без пробелов.");
                return;
            }

            boolean shouldCheckExistence = binding.radioButtonflagofcheck.isChecked();

            if (shouldCheckExistence) {
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(requireContext());
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                wordsRepository = new WordsRepository(db);

                if (wordsRepository.isValidWord(wordText)) {
                    Toast.makeText(requireContext(),
                            "Похоже, я не знаю такого слова", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Формируем объект для API
            MultiUserModel newEntry = new MultiUserModel(
                    null,
                    user.getLogin(),     // кто загадывает
                    loginText,           // кому
                    wordText,
                    shouldCheckExistence ? 1 : 0
            );

            // Отправляем запрос
            DataFromMultiUserAPI api = new DataFromMultiUserAPI();
            api.saveMultiUser(newEntry, new CallbackMultiUser() {
                @Override
                public void onSuccess(MultiUserModel savedMultiUser) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(),
                                "Слово успешно отправлено!", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    });
                }

                @Override
                public void onError(Throwable t) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(),
                                    "Ошибка при отправке: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
                }
            });
        });
    }
    private void getAllId() {
        loginGuessing = binding.loginGuessing;
        word = binding.word;
        radioButtonFlagOfCheck = binding.radioButtonflagofcheck;
        buttonNew = binding.buttonNew;
        textError=binding.textError;
        exit=binding.exit;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
