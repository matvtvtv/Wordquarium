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
    private int success = 0;

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

    // fallback phrase (на случай, если чтение файла не получилось)
    private String phrase = "Сидел петух на лавочке, считал свои булавочки, раз, два, три";

    // mapping letter -> number
    private final HashMap<Character, Integer> mapLetterToNumber = new HashMap<>();
    private final HashSet<Integer> usedNumbers = new HashSet<>();

    private final Random rnd = new Random(System.currentTimeMillis());
    private final int REVEAL_LETTERS_COUNT = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCryptogramBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getAllId();

        // Попробуем получить случайную фразу из assets/Cryptogram.txt
        String fromAssets = getRandomPhraseFromAssets("Cryptogram.txt");
        if (fromAssets != null && !fromAssets.trim().isEmpty()) {
            phrase = fromAssets.trim();
        } // иначе остаётся fallback

        setupGame();

        // Обновляем счетчик ошибок на экране
        errorsText.setText("Количество ошибок: " + errors + "/5");

        btnExit.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // Подсказка уже проинициализирована в getAllId()
        tvHint.setText("Нажми на квадрат с буквой → он выделится → нажми букву на клавиатуре");
    }

    /**
     * Читает все непустые строки из assets и выбирает одну случайную.
     * Возвращает null в случае ошибки / если файл пуст.
     */
    private String getRandomPhraseFromAssets(String filename) {
        List<String> lines = readLinesFromAssets(filename);
        if (lines == null || lines.isEmpty()) return null;
        Collections.shuffle(lines, rnd);
        return lines.get(0);
    }

    /**
     * Читает все непустые строки из assets/<filename> в кодировке UTF-8.
     */
    private List<String> readLinesFromAssets(String filename) {
        List<String> list = new ArrayList<>();
        try (InputStream is = getAssets().open(filename);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line == null) continue;
                line = line.trim();
                if (!line.isEmpty()) list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Логируем и возвращаем пустой список
            return new ArrayList<>();
        }
        return list;
    }

    private void setupGame() {
        prepareMappingForPhrase(phrase);
        buildCellsForPhrase(phrase);

        int columns = 10;
        rvCrypt.setLayoutManager(new GridLayoutManager(this, columns));

        adapter = new CryptogramAdapter(this, cells);
        // Вешаем обработчик выбора клетки: адаптер сам хранит selectedIndex
        adapter.setOnCellClickListener(position -> adapter.setSelectedIndex(position));
        rvCrypt.setAdapter(adapter);

        // Используем binding для клавиатуры view
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
        for (int i = 0; i < k && i < uniques.size(); i++) {
            res.add(uniques.get(i));
        }
        return res;
    }

    private void handleKeyPress(String keyText) {
        if (keyText == null || keyText.isEmpty()) return;
        char pressed = keyText.charAt(0);

        int sel = adapter.getSelectedIndex();
        if (sel == -1) {
            Toast.makeText(this, "Сначала выберите клетку в фразе", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sel < 0 || sel >= cells.size()) return;
        CharCell selectedCell = cells.get(sel);
        if (!selectedCell.isLetter()) {
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

        // объект клавиши, чтобы менять её статус
        Keyboard.Key kKey = keyboard.findByKeyText(String.valueOf(pressed));

        if (attempt == real) {
            // правильная — раскрываем ТОЛЬКО выбранную ячейку
            adapter.revealAtPosition(sel);
            success++;

            if (kKey != null) {
                kKey.setStatus(LetterStatus.GREEN);
                keyboard.notifyKeyChanged(kKey);
            }
        } else {
            // неверная попытка — показываем введённую букву в клетке как WRONG
            adapter.markCellWrongWithAttempt(sel, attempt);
            errors++;
            errorsText.setText("Количество ошибок: " + errors + "/5");
            if (errors >= 5) {
                showEndDialog(false);
            }
            if (kKey != null) {
                if (presentAnywhere) kKey.setStatus(LetterStatus.YELLOW);
                else kKey.setStatus(LetterStatus.GRAY);
                keyboard.notifyKeyChanged(kKey);
            }
        }
    }

    private void showEndDialog(boolean playerWon) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_game_win);
        dialog.setCancelable(true);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView popupGameText = dialog.findViewById(R.id.textView9);
        TextView popupGameWin = dialog.findViewById(R.id.popupGameWinText);
        TextView popupGame = dialog.findViewById(R.id.resoult_win);

        Button btnRestart = dialog.findViewById(R.id.btnRestart);
        Button btnMainMenu = dialog.findViewById(R.id.btnMainMenu);

        if (popupGameText != null) popupGameText.setText(" ");
        if (popupGameWin != null) popupGameWin.setText(playerWon ? "Вы выиграли" : "Вы проиграли");
        if (popupGame != null) popupGame.setText(success + " букв");

        btnRestart.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
            startActivity(new Intent(this, CryptogramActivity.class));
        });

        btnMainMenu.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        });

        dialog.show();
    }

    private void getAllId() {
        errorsText = binding.levelGameText;
        btnExit = binding.exitButton;
        rvCrypt = binding.rvCryptogram;
        tvHint = binding.cryptHint;
        // проверка на null для раннего обнаружения ошибок в layout
        if (errorsText == null || rvCrypt == null) {
            throw new IllegalStateException("Не найден необходимый view в activity_cryptogram.xml (errorsText или rvCryptogram)");
        }
    }
}
