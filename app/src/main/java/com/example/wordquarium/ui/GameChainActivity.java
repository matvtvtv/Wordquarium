package com.example.wordquarium.ui;

import static com.example.wordquarium.logic.adapters.LetterStatus.GRAY;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.model.PlayerSettingsModel;
import com.example.wordquarium.data.model.WordsModel;
import com.example.wordquarium.data.repository.DatabaseHelper;
import com.example.wordquarium.data.repository.PlayerRepository;
import com.example.wordquarium.data.repository.PlayerSettingsRepository;
import com.example.wordquarium.data.repository.WordsRepository;
import com.example.wordquarium.databinding.ActivityGameChainBinding;
import com.example.wordquarium.logic.adapters.Keyboard;
import com.example.wordquarium.logic.adapters.LetterStatus;
import com.example.wordquarium.ui.fragments.GameChainLogic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameChainActivity extends AppCompatActivity {
    private ActivityGameChainBinding binding;

    private TextView appWordView;
    private TextView hintView;
    private EditText inputField;
    private Button btnCheck;
    private Button btnSkip;

    private WordsRepository wordsRepository;
    private PlayerSettingsRepository playerSettingsRepository;

    private final Set<String> usedWords = new HashSet<>();
    private final Random rnd = new Random();

    private GameChainLogic logic;

    // клавиши (как у тебя были)
    private final List<Keyboard.Key> keyList = java.util.Arrays.asList(
            new Keyboard.Key("Й"), new Keyboard.Key("Ц"), new Keyboard.Key("У"), new Keyboard.Key("К"),
            new Keyboard.Key("Е"), new Keyboard.Key("Н"), new Keyboard.Key("Г"), new Keyboard.Key("Ш"),
            new Keyboard.Key("Щ"), new Keyboard.Key("З"), new Keyboard.Key("Х"), new Keyboard.Key("Ъ"),
            new Keyboard.Key("Ф"), new Keyboard.Key("Ы"), new Keyboard.Key("В"), new Keyboard.Key("А"),
            new Keyboard.Key("П"), new Keyboard.Key("Р"), new Keyboard.Key("О"), new Keyboard.Key("Л"),
            new Keyboard.Key("Д"), new Keyboard.Key("Ж"), new Keyboard.Key("Э"), new Keyboard.Key("Я"),
            new Keyboard.Key("Ч"), new Keyboard.Key("С"), new Keyboard.Key("М"), new Keyboard.Key("И"),
            new Keyboard.Key("Т"), new Keyboard.Key("Б"), new Keyboard.Key("Ю"), new Keyboard.Key("Ь"),
            new Keyboard.Key("Del", GRAY)
    );

    // запомним текущую подсвеченную клавишу, чтобы убирать подсветку при обновлении
    private Keyboard.Key currentHighlightedKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameChainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        wordsRepository = new WordsRepository(db);

        PlayerRepository playerRepository = PlayerRepository.getInstance(getApplicationContext());
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);

        playerSettingsRepository = PlayerSettingsRepository.getInstance(getApplicationContext());
        int user_Id = playerSettingsRepository.getCurrentUserId();
        PlayerSettingsModel user_Ac = playerSettingsRepository.getUserData(user_Id);

        // Инициализация view из binding
        getAllId();

        // Инициализация клавиатуры (используем binding.keyboard)
        Keyboard keyboard = new Keyboard(binding.keyboard, keyList);
        keyboard.setOnKeyClickListener(v -> {
            // звук/вибрация
            if (user_Ac != null && user_Ac.getSound() == 1) {
                MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.keyboard_sound);
                mediaPlayer.start();
            }
            if (user_Ac != null && user_Ac.getVibration() == 1) {
                vibrateDevice(this, 80);
            }

            // Обработка нажатой клавиши
            Button btn = (Button) v;
            String keyText = btn.getText().toString();

            if ("Del".equals(keyText)) {
                // удалить последний символ в EditText
                String cur = inputField.getText().toString();
                if (!cur.isEmpty()) {
                    inputField.setText(cur.substring(0, cur.length() - 1));
                    inputField.setSelection(inputField.getText().length());
                }
            } else {
                // добавить букву в EditText
                inputField.append(keyText);
            }
        });
        keyboard.create(this, binding.getRoot());

        logic = new GameChainLogic();

        // Запуск новой игры
        startNewGame();

        // слушатели кнопок
        btnCheck.setOnClickListener(v -> onPlayerSubmit());
        btnSkip.setOnClickListener(v -> onPlayerSkip());
    }

    private void startNewGame() {
        List<WordsModel> all = wordsRepository.getAllWords();
        if (all == null || all.isEmpty()) {
            Toast.makeText(this, "Словарь пуст — добавьте слова в БД", Toast.LENGTH_LONG).show();
            return;
        }
        WordsModel start = all.get(rnd.nextInt(all.size()));
        String startWord = start.getWord().toUpperCase();
        logic.startNewGame(startWord);
        usedWords.clear();
        usedWords.add(startWord);

        updateUIForAppWord(startWord);
    }

    private void updateUIForAppWord(String word) {
        appWordView.setText(word);
        char required = logic.getRequiredStartLetter();
        hintView.setText("Нужно слово на: " + required);

        // сброс предыдущей подсветки
        if (currentHighlightedKey != null) {
            currentHighlightedKey.setStatus(LetterStatus.UNDEFINED);
            if (binding.keyboard != null) {
                // уведомить адаптер об изменении
                Keyboard kb = new Keyboard(binding.keyboard, keyList);
                // лучше использовать существующий экземпляр keyboard — у нас его нет в поле,
                // поэтому вместо этого пройдём по keyList и вызовем notify через временный объект.
                // В твоём проекте keyboard хранится локально — если хочешь, можно сохранить как поле.
            }
        }

        // Найти клавишу по тексту и подсветить её как GREEN
        String requiredStr = String.valueOf(required);
        for (Keyboard.Key k : keyList) {
            if (requiredStr.equalsIgnoreCase(k.getKeyText())) {
                // сброс предыдущего
                if (currentHighlightedKey != null && currentHighlightedKey != k) {
                    currentHighlightedKey.setStatus(LetterStatus.UNDEFINED);
                }
                k.setStatus(LetterStatus.GREEN);
                currentHighlightedKey = k;
                break;
            }
        }

        // уведомляем адаптер об изменениях: удобней хранить keyboard как поле — сделаем это простым способом:
        if (binding.keyboard != null && binding.keyboard.getAdapter() != null) {
            binding.keyboard.getAdapter().notifyDataSetChanged();
        }
    }

    private void onPlayerSubmit() {
        String playerWord = inputField.getText().toString().trim().toUpperCase();
        if (playerWord.isEmpty()) {
            Toast.makeText(this, "Введите слово", Toast.LENGTH_SHORT).show();
            return;
        }

        // проверка первой буквы
        char required = logic.getRequiredStartLetter();
        if (playerWord.charAt(0) != required) {
            Toast.makeText(this, "Слово должно начинаться с буквы: " + required, Toast.LENGTH_SHORT).show();
            return;
        }

        // проверка на повтор
        if (usedWords.contains(playerWord)) {
            Toast.makeText(this, "Это слово уже использовано", Toast.LENGTH_SHORT).show();
            return;
        }


        if (wordsRepository.isValidWord(playerWord)) {
            Toast.makeText(this, "Похоже, я не знаю такого слова", Toast.LENGTH_SHORT).show();
            return;
        }

        // добавляем слово игрока в использованные
        usedWords.add(playerWord);

        // получаем ответ приложения — слово, начинающееся на последнюю букву игрока
        char last = playerWord.charAt(playerWord.length() - 1);
        List<WordsModel> candidates = wordsRepository.getWordsStartingWith(last);

        // фильтруем уже использованные слова
        List<WordsModel> filtered = new ArrayList<>();
        for (WordsModel w : candidates) {
            String up = w.getWord().toUpperCase();
            if (!usedWords.contains(up)) filtered.add(w);
        }

        if (filtered.isEmpty()) {
            // Игра: приложение не может ответить — победа игрока
            showEndDialog(true, "Приложение не нашло слово — вы победили!");
            return;
        }

        WordsModel response = filtered.get(rnd.nextInt(filtered.size()));
        String appWord = response.getWord().toUpperCase();
        usedWords.add(appWord);

        // обновляем логику и UI — подсветка буквы сбросится и появится новая
        logic.setAppWord(appWord);
        updateUIForAppWord(appWord);

        // очищаем поле ввода
        inputField.setText("");
    }

    private void onPlayerSkip() {
        Toast.makeText(this, "Пропуск хода (можно реализовать штраф)", Toast.LENGTH_SHORT).show();

        char last = logic.getRequiredStartLetter();
        List<WordsModel> candidates = wordsRepository.getWordsStartingWith(last);
        List<WordsModel> filtered = new ArrayList<>();
        for (WordsModel w : candidates) {
            String up = w.getWord().toUpperCase();
            if (!usedWords.contains(up)) filtered.add(w);
        }
        if (filtered.isEmpty()) {
            showEndDialog(true, "Никто не может найти слово — игра окончена");
            return;
        }
        WordsModel response = filtered.get(rnd.nextInt(filtered.size()));
        String appWord = response.getWord().toUpperCase();
        usedWords.add(appWord);
        logic.setAppWord(appWord);
        updateUIForAppWord(appWord);
    }

    private void showEndDialog(boolean playerWon, String message) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void getAllId() {
        appWordView = binding.appWord;
        hintView = binding.hintStartLetter;
        inputField = binding.inputField;
        btnCheck = binding.btnCheck;
        btnSkip = binding.btnSkip;
    }

    public void vibrateDevice(Context context, long milliseconds) {
        android.os.Vibrator vibrator = (android.os.Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(milliseconds, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }
}
