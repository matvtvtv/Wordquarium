package com.example.wordquarium.ui;

import static android.view.Window.FEATURE_NO_TITLE;
import static com.example.wordquarium.logic.adapters.LetterStatus.GRAY;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    private TextView appWordView1;
    private TextView appWordView2;
    private TextView appWordView3;
    private TextView hintView;
    private TextView textTimer;
    private EditText inputField;
    private Button btnCheck;
    private Button btnSkip;
    private TextView NumberText;
    private ImageView btnExit;

    private WordsRepository wordsRepository;
    private PlayerSettingsRepository playerSettingsRepository;

    private final Set<String> usedWords = new HashSet<>();
    private final Random rnd = new Random();
    private GameChainLogic logic;

    private String WordView1 = "...";
    private String WordView2 = "...";
    private String WordView3 = "...";
    private int Number = 0;

    private CountdownTimer countdownTimer;
    private boolean gamemode;
    private int time;

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
        PlayerModel user = playerRepository.getUserData(playerRepository.getCurrentUserId());

        playerSettingsRepository = PlayerSettingsRepository.getInstance(getApplicationContext());
        PlayerSettingsModel userSettings = playerSettingsRepository.getUserData(playerSettingsRepository.getCurrentUserId());

        getAllId();
        NumberText.setText("0");

        gamemode = getIntent().getBooleanExtra("GAMEMOD", false);
        time = getIntent().getIntExtra("TIME", 50);

        Keyboard keyboard = new Keyboard(binding.keyboard, keyList);
        keyboard.setOnKeyClickListener(v -> {
            if (userSettings != null && userSettings.getSound() == 1) {
                MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.keyboard_sound);
                mediaPlayer.start();
            }
            if (userSettings != null && userSettings.getVibration() == 1) {
                vibrateDevice(this, 80);
            }

            String keyText = ((Button) v).getText().toString();
            if ("Del".equals(keyText)) {
                String cur = inputField.getText().toString();
                if (!cur.isEmpty()) {
                    inputField.setText(cur.substring(0, cur.length() - 1));
                    inputField.setSelection(inputField.getText().length());
                }
            } else {
                inputField.append(keyText);
            }
        });
        keyboard.create(this, binding.getRoot());

        logic = new GameChainLogic();

        if (time != 66) {
            countdownTimer = new CountdownTimer(textTimer, time);
            countdownTimer.start();
        } else {
            textTimer.setText("∞");
        }

        startNewGame();

        btnCheck.setOnClickListener(v -> onPlayerSubmit());
        btnSkip.setOnClickListener(v -> onPlayerSkip());
        btnExit.setOnClickListener(v -> {
            if (countdownTimer != null) countdownTimer.stop();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
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
        appWordView1.setText(word);
        appWordView2.setText(WordView2);
        appWordView3.setText(WordView3);
        WordView3 = WordView2;
        WordView2 = word;

        char required = logic.getRequiredStartLetter();
        hintView.setText("Нужно слово на: " + required);

        if (currentHighlightedKey != null) currentHighlightedKey.setStatus(LetterStatus.UNDEFINED);

        for (Keyboard.Key k : keyList) {
            if (String.valueOf(required).equalsIgnoreCase(k.getKeyText())) {
                k.setStatus(LetterStatus.GREEN);
                currentHighlightedKey = k;
                break;
            }
        }

        if (binding.keyboard != null && binding.keyboard.getAdapter() != null)
            binding.keyboard.getAdapter().notifyDataSetChanged();
    }

    private void onPlayerSubmit() {
        String playerWord = inputField.getText().toString().trim().toUpperCase();
        if (playerWord.isEmpty()) {
            Toast.makeText(this, "Введите слово", Toast.LENGTH_SHORT).show();
            return;
        }

        char required = logic.getRequiredStartLetter();
        if (playerWord.charAt(0) != required) {
            Toast.makeText(this, "Слово должно начинаться с буквы: " + required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (usedWords.contains(playerWord)) {
            Toast.makeText(this, "Это слово уже использовано", Toast.LENGTH_SHORT).show();
            return;
        }

        if (wordsRepository.isValidWord(playerWord)) {
            Toast.makeText(this, "Похоже, я не знаю такого слова", Toast.LENGTH_SHORT).show();
            return;
        }

        usedWords.add(playerWord);

        char last = playerWord.charAt(playerWord.length() - 1);
        List<WordsModel> candidates = wordsRepository.getWordsStartingWith(last);
        List<WordsModel> filtered = new ArrayList<>();
        for (WordsModel w : candidates) {
            String up = w.getWord().toUpperCase();
            if (!usedWords.contains(up)) filtered.add(w);
        }

        if (filtered.isEmpty()) {
            if (countdownTimer != null) countdownTimer.stop();
            showEndDialog(true, "Приложение не нашло слово — вы победили!");
            return;
        }

        WordsModel response = filtered.get(rnd.nextInt(filtered.size()));
        String appWord = response.getWord().toUpperCase();
        usedWords.add(appWord);

        Number++;
        NumberText.setText(String.valueOf(Number));

        WordView3 = WordView2;
        WordView2 = playerWord;
        logic.setAppWord(appWord);
        updateUIForAppWord(appWord);

        if (time != 66 && countdownTimer != null && gamemode) countdownTimer.reset(time);

        inputField.setText("");
    }

    private void onPlayerSkip() {
        Toast.makeText(this, "Пропуск хода", Toast.LENGTH_SHORT).show();

        char last = logic.getRequiredStartLetter();
        List<WordsModel> candidates = wordsRepository.getWordsStartingWith(last);
        List<WordsModel> filtered = new ArrayList<>();
        for (WordsModel w : candidates) {
            String up = w.getWord().toUpperCase();
            if (!usedWords.contains(up)) filtered.add(w);
        }

        if (filtered.isEmpty()) {
            if (countdownTimer != null) countdownTimer.stop();
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
        if (countdownTimer != null) countdownTimer.stop();

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_game_win);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupGameText = dialog.findViewById(R.id.textView9);
        TextView popupGameWin = dialog.findViewById(R.id.popupGameWinText);
        TextView popupGame = dialog.findViewById(R.id.resoult_win);
        TextView popupGameDia = dialog.findViewById(R.id.tvGameWin);

        Button btnRestart = dialog.findViewById(R.id.btnRestart);
        Button btnMainMenu = dialog.findViewById(R.id.btnMainMenu);

        if (popupGameText != null) popupGameText.setText(" ");
        if (popupGameWin != null) popupGameWin.setText("Вы ответили на");
        if (popupGame != null) popupGame.setText(String.valueOf(Number + 1) + " слов");
        if (playerWon && popupGameDia != null) popupGameDia.setText("Вы победили");

        btnRestart.setOnClickListener(v -> {
            if (countdownTimer != null) countdownTimer.stop();
            Intent intent = new Intent(this, GameChainActivity.class);
            intent.putExtra("TIME", time);
            intent.putExtra("GAMEMOD", gamemode);
            finish();
            startActivity(intent);
        });

        btnMainMenu.setOnClickListener(v -> {
            if (countdownTimer != null) countdownTimer.stop();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        });

        dialog.show();
    }

    private void getAllId() {
        appWordView1 = binding.appWord1;
        appWordView2 = binding.appWord2;
        appWordView3 = binding.appWord3;
        textTimer = binding.levelGameText;
        hintView = binding.hintStartLetter;
        inputField = binding.inputField;
        btnCheck = binding.btnCheck;
        btnSkip = binding.btnSkip;
        btnExit = binding.exitButton;
        NumberText = binding.numberText;
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

    public class CountdownTimer {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private Runnable runnable;
        private int remainingSeconds;
        private boolean running = false;
        private final TextView timerView;

        public CountdownTimer(TextView timerView, int startSeconds) {
            this.timerView = timerView;
            this.remainingSeconds = startSeconds;
            updateText();
        }

        private void updateText() {
            int min = remainingSeconds / 60;
            int sec = remainingSeconds % 60;
            timerView.setText(String.format("%02d:%02d", min, sec));
        }

        public void start() {
            if (running) return;
            running = true;
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (!running) return;
                    if (remainingSeconds > 0) {
                        remainingSeconds--;
                        updateText();
                        handler.postDelayed(this, 1000);
                    } else {
                        running = false;
                        if (!isFinishing()) showEndDialog(false, "Время вышло");
                    }
                }
            };
            handler.post(runnable);
        }

        public void stop() {
            running = false;
            if (runnable != null) handler.removeCallbacks(runnable);
        }

        public void reset(int newSeconds) {
            stop();
            this.remainingSeconds = newSeconds;
            updateText();
            start();
        }

        public int getRemainingSeconds() {
            return remainingSeconds;
        }

        public boolean isRunning() {
            return running;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
    }
}
