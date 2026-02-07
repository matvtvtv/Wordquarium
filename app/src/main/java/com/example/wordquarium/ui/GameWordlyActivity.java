// GameWordlyActivity_with_RulesDialog.java
// В этом файле — обновлённая версия вашего Activity с добавленной кнопкой вызова диалога правил
// Ниже также приложены два XML-файла: обновлённый activity_game_wordly.xml (с кнопкой) и popup_rules_wordly.xml

package com.example.wordquarium.ui;

import static com.example.wordquarium.logic.adapters.LetterStatus.GRAY;
import static com.example.wordquarium.logic.adapters.LetterStatus.GREEN;
import static com.example.wordquarium.logic.adapters.LetterStatus.YELLOW;

import android.annotation.SuppressLint;
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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
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
import com.example.wordquarium.databinding.ActivityGameWordlyBinding;
import com.example.wordquarium.logic.adapters.Keyboard;
import com.example.wordquarium.logic.adapters.LetterStatus;
import com.example.wordquarium.ui.fragments.AttemptWordlyResult;
import com.example.wordquarium.ui.fragments.GameWordlyLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameWordlyActivity extends AppCompatActivity {


    ActivityGameWordlyBinding binding;

    private GridLayout gridLetters;
    private List<TextView> letterCells;
    private int currentCellIndex = 0;
    private int currentAttemptIndex = 0;
    private int wordLength;
    private int game_mode;
    private int check_of_word;
    private String friend_word;
    private final int MAX_ATTEMPTS = 6;
    private GameWordlyLogic gameLogic;
    private WordsRepository wordsRepository;
    private TextView tvHint;
    private PlayerSettingsRepository playerSettingsRepository;

    private ImageView btnRules; // новая кнопка для вызова диалога правил


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


    @SuppressLint({"SetTextI18n", "CutPasteId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityGameWordlyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        PlayerRepository playerRepository = PlayerRepository.getInstance(getApplicationContext());
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);

        game_mode= getIntent().getIntExtra("GAME_MODE", 2);
        check_of_word= getIntent().getIntExtra("CHECK_OF_WORD", 1);
        friend_word= getIntent().getStringExtra("FRIEND_WORD");

        if(game_mode==2){
            switch ((user.getLevel()/10)) {

                case 0:wordLength=4;break;
                case 1:wordLength=5;break;
                case 2:wordLength=6;break;
                case 3:wordLength=7;break;
                default:wordLength=7;break;
            }
        }
        else {
            wordLength = getIntent().getIntExtra("WORD_LENGTH", wordLength);
        }
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        wordsRepository = new WordsRepository(db);
        if(game_mode!=4){
            List<WordsModel> validWords = wordsRepository.getFilteredWordsFree_2(wordLength);
            int randomIndex = (int) (Math.random() * validWords.size());
            WordsModel selectedWord = validWords.get(randomIndex);
            gameLogic = new GameWordlyLogic(MAX_ATTEMPTS);
            gameLogic.startNewGame(selectedWord.getWord());}
        else{

            gameLogic = new GameWordlyLogic(MAX_ATTEMPTS);
            gameLogic.startNewGame(friend_word);
        }



        gridLetters = findViewById(R.id.gridLetters);
        letterCells = new ArrayList<>();
        for (int i = 0; i < MAX_ATTEMPTS * wordLength; i++) {
            TextView cell = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;
            int screenWidth = displayMetrics.widthPixels;

            float availableHeight = screenHeight * 0.7f;
            float availableWidth = screenWidth * 0.9f;
            int cubeWidth;
            int cubeHeight = (int) (availableHeight / MAX_ATTEMPTS);
            if (4==wordLength){
                cubeWidth = (int) (availableWidth / (wordLength + 1));;}
            else{ cubeWidth = (int) (availableWidth / wordLength);}
            int cubeSize = Math.min(cubeWidth, cubeHeight);
            int padding = (int) (cubeSize * 0.05f);
            cubeSize -= padding;

            params.width = cubeSize;
            params.height = cubeSize;
            params.setMargins(6, 8, 6, 8);
            cell.setLayoutParams(params);
            cell.setGravity(Gravity.CENTER);
            cell.setTextSize(28);
            cell.setBackgroundResource(R.drawable.cell_background_undefined);
            gridLetters.addView(cell);
            letterCells.add(cell);
        }
        GridLayout layout = findViewById(R.id.gridLetters);
        layout.setColumnCount(wordLength);


        binding.exitButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameWordlyActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Инициализация кнопки правил (новое)
        btnRules = findViewById(R.id.btnRules);
        btnRules.setOnClickListener(v -> showRulesDialog());

        DataFromWordAPI dataFromWordAPI = new DataFromWordAPI();
        dataFromWordAPI.getWordHint(gameLogic.getHiddenWord(), new CallbackWord() {
            @Override
            public void onSuccess(String hint) {
                runOnUiThread(() -> {
                    binding.tvHint.setText(hint);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });

        binding.btnHint.setOnClickListener(v -> showHintDialog());

        TextView level=findViewById(R.id.levelGameText);
        level.setText("Level № "+user.getLevel());
        playerSettingsRepository = PlayerSettingsRepository.getInstance(getApplicationContext());
        int user_Id = playerSettingsRepository.getCurrentUserId();
        PlayerSettingsModel user_Ac = playerSettingsRepository.getUserData(user_Id);

        Keyboard keyboard = new Keyboard(binding.keyboard, keyList);
        keyboard.setOnKeyClickListener(v -> {
            if(user_Ac.getSound()==1){
                MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.keyboard_sound);
                mediaPlayer.start();}
            if(user_Ac.getVibration()==1){
                vibrateDevice(this,80);}
            Button btn = (Button) v;
            String letter = btn.getText().toString();
            if (letter.equals("Del")) {
                if (currentCellIndex > currentAttemptIndex * wordLength) {
                    currentCellIndex--;
                    letterCells.get(currentCellIndex).setText("");
                }
            } else {
                if (currentCellIndex < (currentAttemptIndex + 1) * wordLength) {
                    letterCells.get(currentCellIndex).setText(letter);
                    currentCellIndex++;
                }
            }
        });
        keyboard.create(this, binding.getRoot());

        Button btnCheck = binding.btnCheck;
        btnCheck.setOnClickListener(v -> {
            int start = currentAttemptIndex * wordLength;
            int end = start + wordLength;
            StringBuilder guessBuilder = new StringBuilder();
            for (int i = start; i < end; i++) {
                guessBuilder.append(letterCells.get(i).getText());
            }
            String guess = guessBuilder.toString().trim();

            if (guess.length() != wordLength) {
                Toast.makeText(getApplicationContext(), "Введите слово из " + wordLength + " букв!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Проверка: существует ли введённое слово в базе данных
            try {
                if(check_of_word==1) {
                    if (wordsRepository.isValidWord(guess)) {
                        Toast.makeText(getApplicationContext(), "Похоже, я не знаю такого слова", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                AttemptWordlyResult result = gameLogic.checkWord(guess);
                for (int i = 0; i < wordLength; i++) {
                    TextView cell = letterCells.get(start + i);
                    Keyboard.Key key = keyboard.findByKeyText(String.valueOf(result.getGuess().charAt(i)));
                    switch (result.getStatuses()[i]) {
                        case GREEN:
                            if (key != null && (key.getStatus() == LetterStatus.UNDEFINED || key.getStatus() == YELLOW)) {
                                key.setStatus(GREEN);
                                keyboard.notifyKeyChanged(key);
                            }
                            cell.setBackgroundResource(R.drawable.cell_background_green);
                            break;
                        case YELLOW:
                            if (key != null && key.getStatus() == LetterStatus.UNDEFINED) {
                                key.setStatus(YELLOW);
                                keyboard.notifyKeyChanged(key);
                            }
                            cell.setBackgroundResource(R.drawable.cell_background_yellow);
                            break;
                        case GRAY:
                            if (key != null && key.getStatus() == LetterStatus.UNDEFINED) {
                                key.setStatus(GRAY);
                                keyboard.notifyKeyChanged(key);
                            }
                            cell.setBackgroundResource(R.drawable.cell_background_grey);
                            break;
                    }
                }

                if (gameLogic.isGameWon()) {
                    playerWin();
                } else if (gameLogic.isGameOver()) {
                    playerLose();
                } else {
                    currentAttemptIndex++;
                }
            } catch (IllegalStateException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Метод для показа диалога с правилами режима Wordly
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

        // Динамический текст правил — используем текущее слово/параметры
        StringBuilder rules = new StringBuilder();
        rules.append("Правила режима Wordly:\n\n");
        rules.append("1) Угадайте скрытое слово длиной ").append(wordLength).append(" букв за ").append(MAX_ATTEMPTS).append(" попыток.\n");
        rules.append("2) Подсветка букв: Зеленая — буква на своём месте; Желтая — буква есть в слове, но не на этом месте; Серая — буквы нет в слове.\n");
        rules.append("3) Подсказки: можно открыть букву за монеты. Первая подсказка стоит 5 монет, далее цена растёт.\n");
        rules.append("4) За победу вы получаете монеты и/или повышение уровня в зависимости от режима.\n");
        rules.append("Удачи! ");

        tvTitle.setText("Правила Wordly");
        tvRules.setText(rules.toString());

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void playerWin() {

        PlayerRepository playerRepository = PlayerRepository.getInstance(getApplicationContext());
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);

        // Повышение уровня для Wordly
        if (game_mode == 2) {
            user.setLevel(user.getLevel() + 1);
        }

        // Награда за победу
        switch (game_mode) {
            case 1:
                user.setMoney(user.getMoney() + 20);
                break;
            case 2:
                user.setMoney(user.getMoney() + 10);
                break;
            case 3:
            case 4:
                user.setMoney(user.getMoney() + 5);
                break;
        }

        // Wordly статистика
        if (game_mode == 2) {
            user.setGamesWinWordly(user.getGamesWinWordly() + 1);
            user.setCurrentSeriesWinsWordly(user.getCurrentSeriesWinsWordly() + 1);

            // обновляем рекорд серии
            if (user.getCurrentSeriesWinsWordly() > user.getMaxSeriesWinsWordly()) {
                user.setMaxSeriesWinsWordly(user.getCurrentSeriesWinsWordly());
            }
        }


        playerSettingsRepository = PlayerSettingsRepository.getInstance(getApplicationContext());
        int userSettingsId = playerSettingsRepository.getCurrentUserId();
        PlayerSettingsModel settings = playerSettingsRepository.getUserData(userSettingsId);

        if (settings.getSound() == 1) {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.game_win_sound);
            mediaPlayer.start();
        }


        ContentValues values = new ContentValues();

        values.put("level", user.getLevel());
        values.put("money", user.getMoney());

        values.put("gamesWinWordly", user.getGamesWinWordly());
        values.put("currentSeriesWinsWordly", user.getCurrentSeriesWinsWordly());
        values.put("maxSeriesWinsWordly", user.getMaxSeriesWinsWordly());

        if (values.size() > 0) {
            playerRepository.updateUserData(userId, values);

            // Синхронизация с сервером только если есть изменения
            DataFromUserAPI dataFromUserAPI = new DataFromUserAPI();
            dataFromUserAPI.updateUser(user, new CallbackUser() {
                @Override
                public void onSuccess(PlayerModel playerModel) { }

                @Override
                public void onError(Throwable throwable) { }
            });
        }

        showGameWinDialog();
    }

    private void playerLose() {

        PlayerRepository playerRepository = PlayerRepository.getInstance(getApplicationContext());
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);


        if (game_mode == 2) {   // Wordly mode
            // level cannot be negative
            if (user.getLevel() > 0) {
                user.setLevel(user.getLevel() - 1);
            } else {
                user.setLevel(0);
            }

            // reset win streak
            user.setCurrentSeriesWinsWordly(0);
        }


        playerSettingsRepository = PlayerSettingsRepository.getInstance(getApplicationContext());
        int userSettingsId = playerSettingsRepository.getCurrentUserId();
        PlayerSettingsModel settings = playerSettingsRepository.getUserData(userSettingsId);

        if (settings.getSound() == 1) {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.game_lose_sound);
            mediaPlayer.start();
        }


        ContentValues values = new ContentValues();
        values.put("level", user.getLevel());
        if (game_mode == 2) {
            values.put("currentSeriesWinsWordly", 0);
        }

        values.put("money", user.getMoney());
        if (values.size() > 0) {
            playerRepository.updateUserData(userId, values);

            // Синхронизация с сервером только если есть изменения
            DataFromUserAPI dataFromUserAPI = new DataFromUserAPI();
            dataFromUserAPI.updateUser(user, new CallbackUser() {
                @Override
                public void onSuccess(PlayerModel playerModel) { }

                @Override
                public void onError(Throwable throwable) { }
            });
        }

        showGameOverDialog();
    }



    private Set<Integer> hintedIndexes = new HashSet<>();

    private void showGameOverDialog() {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_game_over);
        dialog.setCancelable(false);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.7), // 90% ширины экрана
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
        );
        TextView popupGameOver = dialog.findViewById(R.id.popupGameOverText);
        Button btnRestart = dialog.findViewById(R.id.btnRestart);
        Button btnMainMenu = dialog.findViewById(R.id.btnMainMenu);
        Button btnKnow = dialog.findViewById(R.id.btnKnow);

        popupGameOver.setText( gameLogic.getHiddenWord());
        TextView resoult_lose = dialog.findViewById(R.id.resoult_lose);

        resoult_lose.setText(" ");

        btnRestart.setOnClickListener(v -> {
            if(game_mode==1){
                Toast.makeText(this, "Вы уже играли слово дня", Toast.LENGTH_SHORT).show();

            }
            else{
                finish();
                Intent intent = new Intent(this, GameWordlyActivity.class);
                intent.putExtra("WORD_LENGTH", wordLength); // Передаем значение
                intent.putExtra("GAME_MODE", game_mode);

                startActivity(intent);
            }

            //надо изменить закрытите
        });


        btnMainMenu.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
        btnKnow.setOnClickListener(v -> {

            Dialog dialog_know = new Dialog(this);
            dialog_know.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog_know.setContentView(R.layout.popup_know);
            dialog_know.setCancelable(true);
            dialog_know.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView tvAnswer = dialog_know.findViewById(R.id.tvAnswer);
            ProgressBar progressBar = dialog_know.findViewById(R.id.progressBar);

            tvAnswer.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            dialog_know.show();

            DataFromWordAPI dataFromWordAPI = new DataFromWordAPI();

            dataFromWordAPI.getWordExplanation(
                    gameLogic.getHiddenWord(),
                    new CallbackWord() {

                        @Override
                        public void onSuccess(String explanation) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                tvAnswer.setVisibility(View.VISIBLE);
                                tvAnswer.setText(explanation);
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
        });

        dialog.show();
    }
    private void showGameWinDialog() {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_game_win);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupGameWin = dialog.findViewById(R.id.popupGameWinText);
        Button btnRestart = dialog.findViewById(R.id.btnRestart);
        Button btnMainMenu = dialog.findViewById(R.id.btnMainMenu);
        Button btnKnow = dialog.findViewById(R.id.btnKnow);

        popupGameWin.setText( gameLogic.getHiddenWord());
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.7), // 90% ширины экрана
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
        );

        TextView resoult_win = dialog.findViewById(R.id.resoult_win);
        switch (game_mode){
            case 1:resoult_win.setText("Вы получили : 20 монет");break;
            case 2:resoult_win.setText("Вы получили : 10 монет");break;
            case 3:resoult_win.setText("Вы получили : 5 монет");break;
            case 4:resoult_win.setText("Вы получили : 5 монет");break;
        }

        btnRestart.setOnClickListener(v -> {
            if(game_mode==1){
                Toast.makeText(this, "Вы уже играли слово дня", Toast.LENGTH_SHORT).show();

            }
            else{
                finish();
                Intent intent = new Intent(this, GameWordlyActivity.class);
                intent.putExtra("WORD_LENGTH", wordLength); // Передаем значение
                intent.putExtra("GAME_MODE", game_mode);

                startActivity(intent);
            }

            //надо изменить закрытите
        });

        btnMainMenu.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        btnKnow.setOnClickListener(v -> {

            Dialog dialog_know = new Dialog(this);
            dialog_know.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog_know.setContentView(R.layout.popup_know);
            dialog_know.setCancelable(true);
            dialog_know.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView tvAnswer = dialog_know.findViewById(R.id.tvAnswer);
            ProgressBar progressBar = dialog_know.findViewById(R.id.progressBar);

            tvAnswer.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            dialog_know.show();

            DataFromWordAPI dataFromWordAPI = new DataFromWordAPI();

            dataFromWordAPI.getWordExplanation(
                    gameLogic.getHiddenWord(),
                    new CallbackWord() {

                        @Override
                        public void onSuccess(String explanation) {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                tvAnswer.setVisibility(View.VISIBLE);
                                tvAnswer.setText(explanation);
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
        });


        dialog.show();
    }



    private char[] hintWord;

    private int hintCount=0;
    private void initializeHintWord(int wordLength) {
        if (wordLength > 0) {
            hintWord = new char[wordLength];
            Arrays.fill(hintWord, '*');
            hintedIndexes.clear(); // очистим историю подсказок — если нужно сохранить между играми, уберите эту строку
        } else {
            Log.e("GameActivity", "Invalid word length: " + wordLength);
            hintWord = null;
        }
    }

    private void showHintDialog() {
        if (hintWord == null) {
            initializeHintWord(wordLength);
        }

        Dialog hintDialog = new Dialog(this);
        hintDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        hintDialog.setContentView(R.layout.popup_hint);
        hintDialog.setCancelable(true);
        hintDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupHint = hintDialog.findViewById(R.id.popupHintText);
        TextView moneyCostView = hintDialog.findViewById(R.id.moneyCost); // стоимость следующей подсказки
        TextView moneyTextView = hintDialog.findViewById(R.id.money_text); // ваш id в последней версии — money_text

        // Инициализация отображения
        popupHint.setText("Подсказка: " + new String(hintWord));
        int nextCost = (hintCount + 1) * 5;
        moneyCostView.setText("Стоимость подсказки : " + nextCost);
        PlayerRepository playerRepository = PlayerRepository.getInstance(getApplicationContext());
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);
        moneyTextView.setText(user.getMoney() + "X");

        hintDialog.findViewById(R.id.btnUpdateHint).setOnClickListener(v -> {
            // при клике пытаемся выполнить подсказку (внутри updateHint мы обновим hintCount при успехе)
            updateHint(popupHint, moneyCostView, moneyTextView);
        });

        hintDialog.findViewById(R.id.btnCloseHint).setOnClickListener(v -> hintDialog.dismiss());

        hintDialog.show();
    }


    @SuppressLint("SetTextI18n")
    private void updateHint(TextView popupHint, TextView moneyCost, TextView moneyText) {
        if (hintWord == null) {
            Log.e("GameActivity", "updateHint called but hintWord == null");
            initializeHintWord(wordLength);
        }

        // Список индексов, которые ещё можно открыть
        List<Integer> unopenedIndexes = computeUnopenedIndexes();
        if (unopenedIndexes.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Все возможные подсказки использованы!", Toast.LENGTH_SHORT).show();
            return;
        }

        PlayerRepository playerRepository = PlayerRepository.getInstance(getApplicationContext());
        int userId = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(userId);

        int cost = (hintCount + 1) * 5; // стоимость следующей подсказки
        if (user.getMoney() < cost) {
            Toast.makeText(getApplicationContext(), "Недостаточно монет для получения подсказки!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Выбираем случайный индекс из оставшихся и открываем букву
        int hintIndex = unopenedIndexes.get((int) (Math.random() * unopenedIndexes.size()));
        char letterToReveal = gameLogic.getHiddenWord().charAt(hintIndex);
        hintWord[hintIndex] = letterToReveal;
        hintedIndexes.add(hintIndex);

        // Списываем монеты
        user.setMoney(user.getMoney() - cost);
        // Увеличиваем счетчик использованных подсказок (после успешной покупки)
        hintCount++;

        // Обновляем локальную БД (и при необходимости — сервер)
        ContentValues values = new ContentValues();
        values.put("money", user.getMoney());
        if (values.size() > 0) {
            playerRepository.updateUserData(userId, values);

            // Синхронизация с сервером только если есть изменения
            DataFromUserAPI dataFromUserAPI = new DataFromUserAPI();
            dataFromUserAPI.updateUser(user, new CallbackUser() {
                @Override
                public void onSuccess(PlayerModel playerModel) { }

                @Override
                public void onError(Throwable throwable) { }
            });
        }



        // Обновляем UI
        popupHint.setText("Подсказка: " + new String(hintWord));
        moneyText.setText(user.getMoney() + "X");
        int nextCost = (hintCount + 1) * 5;
        moneyCost.setText("Стоимость подсказки : " + nextCost);
    }



    private List<Integer> computeUnopenedIndexes() {
        List<Integer> unopenedIndexes = new ArrayList<>();
        for (int i = 0; i < wordLength; i++) {
            if (!hintedIndexes.contains(i) && !isLetterGuessed(i)) {
                unopenedIndexes.add(i);
            }
        }
        return unopenedIndexes;
    }
    private boolean isLetterGuessed(int index) {
        return hintWord[index] != '*';
    }


    public void vibrateDevice(Context context, long milliseconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }




}




/* popup_rules_wordly.xml */


