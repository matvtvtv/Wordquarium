package com.example.wordquarium.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordquarium.R;
import com.example.wordquarium.data.model.CharCell;
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

        String fromAssets = getRandomPhraseFromAssets("Cryptogram.txt");
        if (fromAssets != null && !fromAssets.trim().isEmpty()) phrase = fromAssets.trim();
        REVEAL_LETTERS_COUNT = getIntent().getIntExtra("DIFF", 1);

        setupGame();
        errorsText.setText("Количество ошибок: " + errors + "/5");

        btnExit.setOnClickListener(v -> {
            closeEndDialog();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        tvHint.setText("Нажми на квадрат с буквой → он выделится → нажми букву на клавиатуре");
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

        rvCrypt.setLayoutManager(new GridLayoutManager(this, 10));
        adapter = new CryptogramAdapter(this, cells);
        adapter.setOnCellClickListener(position -> {
            if (position >= 0 && position < cells.size()) {
                CharCell cell = cells.get(position);
                if (cell.isLetter() && !cell.isRevealed()) {
                    adapter.setSelectedIndex(position);
                }
            }
        });
        rvCrypt.setAdapter(adapter);

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

        for (char ch : phrase.toCharArray()) {
            if (Character.isLetter(ch)) {
                char up = Character.toUpperCase(ch);
                int num = mapLetterToNumber.getOrDefault(up, 0);
                boolean revealed = revealSet.contains(up);
                cells.add(new CharCell(ch, true, num, revealed));
            } else {
                cells.add(new CharCell(ch, false, 0, true));
            }
        }
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
        } else {
            adapter.markCellWrongWithAttempt(sel, attempt);
            errors++;
            errorsText.setText("Количество ошибок: " + errors + "/5");
            if (errors >= 5) showEndDialog(false);
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
        endDialog.setCancelable(true);
        if (endDialog.getWindow() != null) endDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView popupGameText = endDialog.findViewById(R.id.textView9);
        TextView popupGameWin = endDialog.findViewById(R.id.popupGameWinText);
        TextView popupGame = endDialog.findViewById(R.id.resoult_win);
        Button btnRestart = endDialog.findViewById(R.id.btnRestart);
        Button btnMainMenu = endDialog.findViewById(R.id.btnMainMenu);

        if (popupGameText != null) popupGameText.setText(" ");
        if (popupGameWin != null) popupGameWin.setText(playerWon ? "Вы выиграли" : "Вы проиграли");

        int totalLetters = 0;
        for (CharCell c : cells) if (c.isLetter()) totalLetters++;
        if (popupGame != null) popupGame.setText(totalLetters + " букв");

        btnRestart.setOnClickListener(v -> restartGame());
        btnMainMenu.setOnClickListener(v -> {
            closeEndDialog();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

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
        if (errorsText == null || rvCrypt == null) throw new IllegalStateException("Не найден необходимый view");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeEndDialog();
    }
}
