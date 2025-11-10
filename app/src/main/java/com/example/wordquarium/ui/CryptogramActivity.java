package com.example.wordquarium.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wordquarium.R;
import com.example.wordquarium.data.model.CharCell;
import com.example.wordquarium.logic.adapters.CryptogramAdapter;
import com.example.wordquarium.logic.adapters.Keyboard;
import com.example.wordquarium.logic.adapters.LetterStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class CryptogramActivity extends AppCompatActivity {

    private RecyclerView rvCrypt;
    private TextView tvHint;
    private Button btnReset;

    private CryptogramAdapter adapter;
    private List<CharCell> cells;

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

    // фразу можно менять
    private String phrase = "Шла Саша по шоссе";

    // mapping letter -> number
    private final HashMap<Character, Integer> mapLetterToNumber = new HashMap<>();
    private final HashSet<Integer> usedNumbers = new HashSet<>();

    private final Random rnd = new Random(System.currentTimeMillis());
    private final int REVEAL_LETTERS_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cryptogram);

        rvCrypt = findViewById(R.id.rvCryptogram);
        tvHint = findViewById(R.id.crypt_hint);
        btnReset = findViewById(R.id.btnResetCrypt);

        setupGame();

        tvHint.setText("Нажми на квадрат с буквой → он выделится → нажми букву на клавиатуре");
    }

    private void setupGame() {
        prepareMappingForPhrase(phrase);
        buildCellsForPhrase(phrase);

        int columns = 8;
        rvCrypt.setLayoutManager(new GridLayoutManager(this, columns));

        adapter = new CryptogramAdapter(this, cells);
        adapter.setOnCellClickListener(position -> {
            // оставляем логику выбора в адаптере — activity ничего больше не делает
        });
        rvCrypt.setAdapter(adapter);

        keyboard = new Keyboard(findViewById(R.id.keyboard), keyList);
        keyboard.setOnKeyClickListener(v -> {
            String keyText = ((android.widget.Button) v).getText().toString();
            if ("Del".equals(keyText)) {
                adapter.setSelectedIndex(-1);
                return;
            }
            handleKeyPress(keyText);
        });
        keyboard.create(this, findViewById(android.R.id.content));

        btnReset.setOnClickListener(v -> {
            mapLetterToNumber.clear();
            usedNumbers.clear();
            prepareMappingForPhrase(phrase);
            buildCellsForPhrase(phrase);

            adapter = new CryptogramAdapter(this, cells);
            adapter.setOnCellClickListener(position -> adapter.setSelectedIndex(position));
            rvCrypt.setAdapter(adapter);

            for (Keyboard.Key k : keyList) k.setStatus(LetterStatus.UNDEFINED);
            if (keyboard.getKeyboardAdapter() != null) {
                keyboard.getKeyboardAdapter().notifyDataSetChanged();
            }

            Toast.makeText(this, "Сброшено", Toast.LENGTH_SHORT).show();
        });
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

            if (kKey != null) {
                kKey.setStatus(LetterStatus.GREEN);
                keyboard.notifyKeyChanged(kKey);
            }
        } else {
            // неверная попытка — показываем введённую букву в клетке как WRONG
            adapter.markCellWrongWithAttempt(sel, attempt);

            if (kKey != null) {
                if (presentAnywhere) kKey.setStatus(LetterStatus.YELLOW);
                else kKey.setStatus(LetterStatus.GRAY);
                keyboard.notifyKeyChanged(kKey);
            }
        }
    }
}
