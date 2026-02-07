package com.example.wordquarium.ui;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.CharCell;
import com.example.wordquarium.data.model.PlayerModel;
import com.example.wordquarium.data.network.CallbackUser;
import com.example.wordquarium.data.network.CallbackWord;
import com.example.wordquarium.data.network.DataFromUserAPI;
import com.example.wordquarium.data.network.DataFromWordAPI;
import com.example.wordquarium.data.repository.PlayerRepository;
import com.example.wordquarium.databinding.ActivityCryptogramBinding;
import com.example.wordquarium.logic.adapters.CryptogramAdapter;
import com.example.wordquarium.logic.adapters.Keyboard;
import com.example.wordquarium.logic.adapters.LetterStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class CryptogramActivity extends AppCompatActivity {

    private ActivityCryptogramBinding binding;

    private RecyclerView rvCrypt;
    private TextView tvHint;
    private TextView errorsText;
    private ImageView btnExit;
    private CryptogramAdapter adapter;
    private List<CharCell> cells;
    private int errors = 0;
    private Dialog endDialog;

    // Кнопка правил
    private ImageView btnRules;

    private final List<Keyboard.Key> keyList = java.util.Arrays.asList(
            new Keyboard.Key("Й"), new Keyboard.Key("Ц"), new Keyboard.Key("У"), new Keyboard.Key("К"),
            new Keyboard.Key("Е"), new Keyboard.Key("Н"), new Keyboard.Key("Г"), new Keyboard.Key("Ш"),
            new Keyboard.Key("Щ"), new Keyboard.Key("З"), new Keyboard.Key("Х"), new Keyboard.Key("Ъ"),
            new Keyboard.Key("Ф"), new Keyboard.Key("Ы"), new Keyboard.Key("В"), new Keyboard.Key("А"),
            new Keyboard.Key("П"), new Keyboard.Key("Р"), new Keyboard.Key("О"), new Keyboard.Key("Л"),
            new Keyboard.Key("Д"), new Keyboard.Key("Ж"), new Keyboard.Key("Э"), new Keyboard.Key("Я"),
            new Keyboard.Key("Ч"), new Keyboard.Key("С"), new Keyboard.Key("М"), new Keyboard.Key("И"),
            new Keyboard.Key("Т"), new Keyboard.Key("Б"), new Keyboard.Key("Ю"), new Keyboard.Key("Ь")
    );

    private Keyboard keyboard;
    private String phrase = "Сидел петух на лавочке, считал свои булавочки, раз, два, три";
    private String fromAssets;
    private final HashMap<Character, Integer> mapLetterToNumber = new HashMap<>();
    private final HashSet<Integer> usedNumbers = new HashSet<>();
    private final Random rnd = new Random(System.currentTimeMillis());
    private int REVEAL_LETTERS_COUNT = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCryptogramBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getAllId();

        // Инициализация кнопки правил
        btnRules = findViewById(R.id.btnRules);
        btnRules.setOnClickListener(v -> showRulesDialog());

        fromAssets = getRandomPhraseFromAssets("Cryptogram.txt");
        if (fromAssets != null && !fromAssets.trim().isEmpty()) phrase = fromAssets.trim();
        REVEAL_LETTERS_COUNT = getIntent().getIntExtra("DIFF", 1);

        setupGame();
        if (errorsText != null) errorsText.setText("Количество ошибок: " + errors + "/5");

        if (btnExit != null) btnExit.setOnClickListener(v -> {
            closeEndDialog();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        if (tvHint != null) tvHint.setText("Нажми на квадрат с буквой → он выделится → нажми букву на клавиатуре");
    }

    // Метод для показа диалога с правилами режима Криптограмма
    private void showRulesDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_rules_wordly); // Используем тот же layout
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
        rules.append("Правила режима Криптограмма:\n\n");
        rules.append("1) Ваша задача — расшифровать загаданную фразу, заменив числа на буквы.\n");
        rules.append("2) Каждому числу соответствует определённая буква русского алфавита.\n");
        rules.append("3) Открыты буквы в зависимости от сложности: Легко — 8 букв, Средне — 6 букв, Сложно — 4 буквы.\n");
        rules.append("4) Нажмите на клетку с числом, затем выберите букву на клавиатуре.\n");
        rules.append("5) Зелёная подсветка клавиши — правильная буква; Жёлтая — буква есть в фразе, но в другом месте; Серая — буквы нет в фразе.\n");
        rules.append("6) Допускается максимум 5 ошибок. При превышении игра окончена.\n");
        rules.append("7) За победу начисляются монеты и обновляется статистика в зависимости от сложности.");

        tvTitle.setText("Правила Криптограммы");
        tvRules.setText(rules.toString());

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String getRandomPhraseFromAssets(String filename) {
        List<String> lines = readLinesFromAssets(filename);
        if (lines == null || lines.isEmpty()) return null;
        Collections.shuffle(lines, rnd);
        return lines.get(0);
    }

    private List<String> readLinesFromAssets(String filename) {
        List<String> list = new ArrayList<>();
        try (InputStream is = getAssets().open(filename);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return list;
    }

    private void setupGame() {
        prepareMappingForPhrase(phrase);
        buildCellsForPhrase(phrase);

        if (rvCrypt != null) rvCrypt.setLayoutManager(new GridLayoutManager(this, 10));
        adapter = new CryptogramAdapter(this, cells);
        adapter.setOnCellClickListener(position -> {
            if (position >= 0 && position < cells.size()) {
                CharCell cell = cells.get(position);
                if (cell.isLetter() && !cell.isRevealed()) {
                    adapter.setSelectedIndex(position);
                }
            }
        });
        if (rvCrypt != null) rvCrypt.setAdapter(adapter);

        keyboard = new Keyboard(binding.keyboard, keyList);
        keyboard.setOnKeyClickListener(v -> {
            String keyText = ((android.widget.Button) v).getText().toString();
            if ("Del".equalsIgnoreCase(keyText)) {
                adapter.setSelectedIndex(-1);
                return;
            }
            handleKeyPress(keyText);
        });
        keyboard.create(this, binding.getRoot());
    }

    private void prepareMappingForPhrase(String phrase) {
        mapLetterToNumber.clear();
        usedNumbers.clear();
        List<Character> uniqueLetters = new ArrayList<>();
        for (char ch : phrase.toCharArray()) {
            if (Character.isLetter(ch)) {
                char up = Character.toUpperCase(ch);
                if (!uniqueLetters.contains(up)) uniqueLetters.add(up);
            }
        }
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 32; i++) numbers.add(i);
        Collections.shuffle(numbers, rnd);

        for (int i = 0; i < uniqueLetters.size(); i++) {
            int num = numbers.get(i % numbers.size());
            mapLetterToNumber.put(uniqueLetters.get(i), num);
            usedNumbers.add(num);
        }
    }

    private void buildCellsForPhrase(String phrase) {
        cells = new ArrayList<>();
        HashSet<Character> revealSet = chooseInitialRevealSet(REVEAL_LETTERS_COUNT);

        final int COLUMNS = 10;

        String[] words = phrase.split(" ");
        List<CharCell> currentLine = new ArrayList<>();
        int currentLineWidth = 0;

        for (int wi = 0; wi < words.length; wi++) {
            String token = words[wi];

            // отделяем пунктуацию
            String trailingPunct = "";
            while (!token.isEmpty() && !Character.isLetter(token.charAt(token.length() - 1))) {
                trailingPunct = token.charAt(token.length() - 1) + trailingPunct;
                token = token.substring(0, token.length() - 1);
            }

            int wordLength = token.length();
            int spaceBefore = currentLineWidth > 0 ? 1 : 0;
            if (wordLength + spaceBefore > COLUMNS) {
                addCenteredLine(currentLine, COLUMNS);
                currentLine.clear();
                currentLineWidth = 0;
                spaceBefore = 0;
            }

            if (spaceBefore > 0) {
                currentLine.add(new CharCell(' ', false, 0, true));
                currentLineWidth++;
            }

            for (char ch : token.toCharArray()) {
                char up = Character.toUpperCase(ch);
                int num = mapLetterToNumber.getOrDefault(up, 0);
                boolean revealed = revealSet.contains(up);
                currentLine.add(new CharCell(ch, true, num, revealed));
                currentLineWidth++;
            }

            for (char p : trailingPunct.toCharArray()) {
                currentLine.add(new CharCell(p, false, 0, true));
                currentLineWidth++;
            }
        }

        if (!currentLine.isEmpty()) addCenteredLine(currentLine, COLUMNS);
    }

    private void addCenteredLine(List<CharCell> line, int columns) {
        int lineWidth = line.size();
        int leftPad = (columns - lineWidth) / 2;
        List<CharCell> paddedLine = new ArrayList<>();

        for (int i = 0; i < leftPad; i++) {
            paddedLine.add(new CharCell(' ', false, 0, true));
        }
        paddedLine.addAll(line);
        while (paddedLine.size() < columns) {
            paddedLine.add(new CharCell(' ', false, 0, true));
        }

        cells.addAll(paddedLine);
    }

    private HashSet<Character> chooseInitialRevealSet(int k) {
        List<Character> uniques = new ArrayList<>(mapLetterToNumber.keySet());
        Collections.shuffle(uniques, rnd);
        HashSet<Character> res = new HashSet<>();
        for (int i = 0; i < k && i < uniques.size(); i++) res.add(uniques.get(i));
        return res;
    }

    private void handleKeyPress(String keyText) {
        if (keyText == null || keyText.isEmpty()) return;
        char pressed = keyText.charAt(0);

        int sel = adapter.getSelectedIndex();
        if (sel == -1) {
            Toast.makeText(this, "Сначала выберите клетку", Toast.LENGTH_SHORT).show();
            return;
        }

        CharCell selectedCell = cells.get(sel);
        if (!selectedCell.isLetter() || selectedCell.isRevealed()) {
            adapter.setSelectedIndex(-1);
            return;
        }

        char real = Character.toUpperCase(selectedCell.getCh());
        char attempt = Character.toUpperCase(pressed);

        boolean presentAnywhere = false;
        for (CharCell c : cells) {
            if (c.isLetter() && Character.toUpperCase(c.getCh()) == attempt) {
                presentAnywhere = true;
                break;
            }
        }

        Keyboard.Key kKey = keyboard.findByKeyText(String.valueOf(pressed));

        if (attempt == real) {
            adapter.revealAtPosition(sel);
            if (kKey != null) {
                kKey.setStatus(LetterStatus.GREEN);
                keyboard.notifyKeyChanged(kKey);
            }
            checkWin();
        }
        else {
            adapter.markCellWrongWithAttempt(sel, attempt);
            errors++;
            if (errorsText != null) errorsText.setText("Количество ошибок: " + errors + "/5");
            if (errors >= 5) {
                showEndDialog(false);
            }
            if (kKey != null) {
                kKey.setStatus(presentAnywhere ? LetterStatus.YELLOW : LetterStatus.GRAY);
                keyboard.notifyKeyChanged(kKey);
            }
        }
    }

    private void checkWin() {
        for (CharCell c : cells) {
            if (c.isLetter() && !c.isRevealed()) return;
        }
        showEndDialog(true);
    }

    private void showEndDialog(boolean playerWon) {
        closeEndDialog();

        endDialog = new Dialog(this);
        endDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        endDialog.setContentView(R.layout.popup_game_win);
        endDialog.setCancelable(false);
        if (endDialog.getWindow() != null) endDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupGameText = endDialog.findViewById(R.id.textView9);
        TextView tvGameWin = endDialog.findViewById(R.id.tvGameWin);
        TextView popupGame = endDialog.findViewById(R.id.resoult_win);
        TextView popupGameWinText = endDialog.findViewById(R.id.popupGameWinText);
        TextView textView9 = endDialog.findViewById(R.id.textView9);
        Button btnRestart = endDialog.findViewById(R.id.btnRestart);
        Button btnMainMenu = endDialog.findViewById(R.id.btnMainMenu);
        Button btnKnow = endDialog.findViewById(R.id.btnKnow);
        endDialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.7),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
        );
        PlayerRepository playerRepository = PlayerRepository.getInstance(this);
        int user_Id = playerRepository.getCurrentUserId();
        PlayerModel user = playerRepository.getUserData(user_Id);
        ContentValues values = new ContentValues();
        if (playerWon) {
            if (popupGameText != null) tvGameWin.setText("Вы выиграли");
        }
        else {
            if (popupGameText != null) tvGameWin.setText("Вы проиграли");
        }
        textView9.setText("Загаданная фраза:");
        popupGameWinText.setText(fromAssets);
        if (playerWon) {
            switch (REVEAL_LETTERS_COUNT) {
                case 4:
                    values.put("cryptogramHardWins", user.getCryptogramHardWins() + 1);
                    break;
                case 6:
                    values.put("cryptogramMidWins", user.getCryptogramMiddleWins() + 1);
                    break;
                case 8:
                    values.put("cryptogramEasyWins", user.getCryptogramEasyWins() + 1);
                    break;
            }
        }

        if (values.size() > 0) {
            playerRepository.updateUserData(user_Id, values);
        }

        DataFromUserAPI dataFromUserAPI = new DataFromUserAPI();
        dataFromUserAPI.updateUser(user, new CallbackUser() {
            @Override
            public void onSuccess(PlayerModel playerModel) { }

            @Override
            public void onError(Throwable throwable) { }
        });

        int totalLetters = 0;
        for (CharCell c : cells) if (c.isLetter()) totalLetters++;
        if (popupGame != null) popupGame.setText("отгадано " + totalLetters + " букв");

        if (btnRestart != null) btnRestart.setOnClickListener(v -> restartGame());

        if (btnMainMenu != null) btnMainMenu.setOnClickListener(v -> {
            closeEndDialog();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        if (btnKnow != null) {
            if (fromAssets != null && !fromAssets.trim().isEmpty()) {
                btnKnow.setVisibility(View.VISIBLE);
                btnKnow.setText("Узнать значение фразы");
                btnKnow.setOnClickListener(v -> wordPhraseDialog(fromAssets));
            } else {
                btnKnow.setVisibility(View.GONE);
            }
        }

        endDialog.show();
    }

    private void restartGame() {
        closeEndDialog();
        Intent intent = new Intent(this, CryptogramActivity.class);
        intent.putExtra("DIFF", REVEAL_LETTERS_COUNT);
        finish();
        startActivity(intent);
    }

    private void closeEndDialog() {
        if (endDialog != null && endDialog.isShowing()) {
            endDialog.dismiss();
            endDialog = null;
        }
    }

    private void getAllId() {
        errorsText = binding.levelGameText;
        btnExit = binding.exitButton;
        rvCrypt = binding.rvCryptogram;
        tvHint = binding.cryptHint;
        // Инициализация кнопки правил из binding
        btnRules = binding.btnRules;
        if (errorsText == null || rvCrypt == null) throw new IllegalStateException("Не найден необходимый view");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeEndDialog();
    }

    public void wordPhraseDialog(String phrase) {
        final Dialog dialog_know = new Dialog(this);
        dialog_know.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_know.setContentView(R.layout.popup_know);
        dialog_know.setCancelable(true);
        if (dialog_know.getWindow() != null) dialog_know.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvAnswer = dialog_know.findViewById(R.id.tvAnswer);
        ProgressBar progressBar = dialog_know.findViewById(R.id.progressBar);

        if (tvAnswer != null) tvAnswer.setVisibility(View.GONE);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        dialog_know.show();

        DataFromWordAPI dataFromWordAPI = new DataFromWordAPI();

        dataFromWordAPI.getPhraseExplanation(
                phrase,
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