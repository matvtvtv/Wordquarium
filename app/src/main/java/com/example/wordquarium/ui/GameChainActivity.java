package com.example.wordquarium.ui;

import static android.view.Window.FEATURE_NO_TITLE;
import static com.example.wordquarium.logic.adapters.LetterStatus.GRAY;

import android.app.Dialog;
import android.content.ContentValues;
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
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.model.PlayerSettingsModel;
import com.example.wordquarium.data.model.WordsModel;
import com.example.wordquarium.data.network.CallbackUser;
import com.example.wordquarium.data.network.CallbackWord;
import com.example.wordquarium.data.network.DataFromUserAPI;
import com.example.wordquarium.data.network.DataFromWordAPI;
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
    private ImageView  btnRules;
    private TextView NumberText;
    private TextView wordKnow;
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

    // Добавляем поле для хранения диалога окончания
    private Dialog endDialog;

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
        btnRules.setOnClickListener(v -> showRulesDialog());
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
            closeEndDialog(); // Закрываем диалог при выходе
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        wordKnow.setOnClickListener(v -> {
            String wordToExplain = logic.getAppWord();
            if (wordToExplain != null && !wordToExplain.isEmpty()) {
                wordKnowDialog(wordToExplain);
            } else {
                Toast.makeText(this, "Нет слова для объяснения", Toast.LENGTH_SHORT).show();
            }
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
    // Замените существующий метод showRulesDialog() на этот:

    private void showRulesDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_rules_wordly);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        TextView tvTitle = dialog.findViewById(R.id.tvRulesTitle);
        TextView tvRules = dialog.findViewById(R.id.tvRulesText);
        Button btnClose = dialog.findViewById(R.id.btnCloseRules);

        // Динамический текст правил
        StringBuilder rules = new StringBuilder();
        rules.append("Правила режима Цепочка слов:\n\n");

        if (gamemode) {
            if (time == 66) {
                rules.append("Режим: Бесконечный\n");
                rules.append("1) У вас неограниченное время на размышление.\n");
                rules.append("2) Приложение загадывает слово, вы должны назвать слово, начинающееся на последнюю букву предыдущего.\n");
            } else {
                countdownTimer.stop();
                rules.append("Режим: На время (").append(time).append(" сек)\n");
                rules.append("1) На каждый ответ у вас ").append(time).append(" секунд.\n");
                rules.append("2) Таймер сбрасывается после каждого правильного ответа.\n");
                rules.append("3) Приложение загадывает слово, вы должны назвать слово, начинающееся на последнюю букву предыдущего.\n");
            }
        } else {
            rules.append("Режим: На жизни\n");
            rules.append("1) У вас 3 жизни (ошибки/пропуска снимают по одной жизни).\n");
            rules.append("2) Время не ограничено.\n");
            rules.append("3) Приложение загадывает слово, вы должны назвать слово, начинающееся на последнюю букву предыдущего.\n");
        }

        rules.append("4) Слово должно существовать в словаре и не повторяться.\n");
        rules.append("5) Зелёная подсветка на клавиатуре показывает, на какую букву должно начинаться ваше слово.\n");
        rules.append("6) Кнопка 'Пропустить' позволяет пропустить ход (в режиме на жизни — минус жизнь).\n");
        rules.append("7) Игра заканчивается, когда приложение не может найти подходящее слово — вы побеждаете!\n");
        rules.append("8) Рекорды сохраняются для каждого режима отдельно.");

        // УСТАНАВЛИВАЕМ ТЕКСТЫ
        tvTitle.setText("Правила Цепочки");
        tvRules.setText(rules.toString());

        // ОБРАБОТЧИК ЗАКРЫТИЯ
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            if (time == 66) {
            countdownTimer.start();}
        });



        // ПОКАЗЫВАЕМ ДИАЛОГ
        dialog.show();
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
        closeEndDialog(); // Закрываем предыдущий если есть

        if (countdownTimer != null) countdownTimer.stop();

        endDialog = new Dialog(this);
        endDialog.requestWindowFeature(FEATURE_NO_TITLE);
        endDialog.setContentView(R.layout.popup_game_win);
        endDialog.setCancelable(false);

        endDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupGameText = endDialog.findViewById(R.id.textView9);
        TextView popupGameWin = endDialog.findViewById(R.id.popupGameWinText);
        TextView popupGame = endDialog.findViewById(R.id.resoult_win);
        TextView popupGameDia = endDialog.findViewById(R.id.tvGameWin);

        Button btnRestart = endDialog.findViewById(R.id.btnRestart);
        Button btnMainMenu = endDialog.findViewById(R.id.btnMainMenu);
        Button btnKnow = endDialog.findViewById(R.id.btnKnow);

        if (popupGameText != null) popupGameText.setText("Вы ответили на");
        if (popupGameWin != null) popupGameWin.setText(" ");
        if (popupGame != null) popupGame.setText(String.valueOf(Number) + " слов");
        if (popupGameDia != null) popupGameDia.setText("");
        endDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.7), // 90% ширины экрана
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
        );
        PlayerRepository playerRepository = PlayerRepository.getInstance(this);
        int user_Id = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(user_Id);
        ContentValues values = new ContentValues();

        if(gamemode){
            if(time==66){
                if(user.getBestChainEndless() < Number){
                    values.put("bestChainEndless", Number);
                }
            } else if(user.getBestChainSpeed() < Number){
                values.put("bestChainSpeed", Number);
            }
        } else if(user.getBestChainTime() < Number){
            values.put("bestChainTime", Number);
        }

        if (values.size() > 0) {
            playerRepository.updateUserData(user_Id, values);

            DataFromUserAPI dataFromUserAPI = new DataFromUserAPI();
            dataFromUserAPI.updateUser(user, new CallbackUser() {
                @Override
                public void onSuccess(PlayerModel playerModel) { }

                @Override
                public void onError(Throwable throwable) { }
            });
        }

        btnRestart.setOnClickListener(v -> {
            closeEndDialog();
            if (countdownTimer != null) countdownTimer.stop();
            Intent intent = new Intent(this, GameChainActivity.class);
            intent.putExtra("TIME", time);
            intent.putExtra("GAMEMOD", gamemode);
            finish();
            startActivity(intent);
        });

        btnMainMenu.setOnClickListener(v -> {
            closeEndDialog();
            if (countdownTimer != null) countdownTimer.stop();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        });

        // ИСПРАВЛЕНИЕ: НЕ закрываем endDialog при открытии wordKnowDialog
        btnKnow.setOnClickListener(v -> {
            String wordToExplain = logic.getAppWord();
            if (wordToExplain != null && !wordToExplain.isEmpty()) {
                wordKnowDialog(wordToExplain);
            }
        });

        endDialog.show();
    }

    private void closeEndDialog() {
        if (endDialog != null && endDialog.isShowing()) {
            endDialog.dismiss();
            endDialog = null;
        }
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
        wordKnow = binding.wordKnow;
        btnRules = binding.btnRules;
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
        closeEndDialog();
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
    }

    public void wordKnowDialog(String word){
        if (countdownTimer != null && time != 66) {
            countdownTimer.stop();
        }

        Dialog dialog_know = new Dialog(this);
        dialog_know.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_know.setContentView(R.layout.popup_know);
        dialog_know.setCancelable(true);

        if (dialog_know.getWindow() != null) {
            dialog_know.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        }

        TextView tvAnswer = dialog_know.findViewById(R.id.tvAnswer);
        ProgressBar progressBar = dialog_know.findViewById(R.id.progressBar);

        if (tvAnswer != null) tvAnswer.setVisibility(View.GONE);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        dialog_know.setOnDismissListener(dialog -> {
            if (time != 66 && countdownTimer != null) {
                countdownTimer.start();
            }
        });

        dialog_know.show();



        DataFromWordAPI dataFromWordAPI = new DataFromWordAPI();

        dataFromWordAPI.getWordExplanation(
                word,
                new CallbackWord() {
                    @Override
                    public void onSuccess(String explanation) {
                        runOnUiThread(() -> {
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            if (tvAnswer != null) {
                                tvAnswer.setVisibility(View.VISIBLE);
                                tvAnswer.setText(explanation);
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        runOnUiThread(() -> {
                            dialog_know.dismiss();
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Ошибка загрузки",
                                    Toast.LENGTH_SHORT
                            ).show();
                        });
                    }
                }
        );
    }
}