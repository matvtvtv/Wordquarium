package com.example.wordquarium.logic.adapters;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class Keyboard {

    private final RecyclerView keyboard;
    private final List<Key> keys;
    @Getter private KeyboardAdapter keyboardAdapter;
    @Setter private View.OnClickListener onKeyClickListener;

    public Keyboard(RecyclerView keyboard, List<Key> keys) {
        this.keys = keys;
        this.keyboard = keyboard;
    }

    public void create(Context context, View view) {
        keyboardAdapter = new KeyboardAdapter(context, keys);
        if (onKeyClickListener != null) {
            keyboardAdapter.setOnClickListener(onKeyClickListener);
        }

        keyboard.setAdapter(keyboardAdapter);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(com.google.android.flexbox.JustifyContent.CENTER);
        keyboard.setLayoutManager(layoutManager);
    }

    public void notifyKeyChanged(int position) {
        if (keyboardAdapter != null) {
            keyboardAdapter.notifyItemChanged(position);
        }
    }

    public void notifyKeyChanged(Key key) {
        int pos = getKeyPosition(key);
        if (pos >= 0 && keyboardAdapter != null) {
            keyboardAdapter.notifyItemChanged(pos);
        }
    }

    public Key findByKeyText(String text) {
        if (text == null) return null;
        for (Key key : keys) {
            if (text.equals(key.getKeyText())) return key;
        }
        return null;
    }

    public int getKeyPosition(Key key) {
        if (key == null) return -1;
        for (int i = 0; i < keys.size(); i++) {
            Key k = keys.get(i);
            if (k != null && k.getKeyText() != null && k.getKeyText().equals(key.getKeyText())) {
                return i;
            }
        }
        return -1;
    }

    @Data
    public static class Key {
        String keyText;
        LetterStatus status;

        public Key(String keyText) {
            this.keyText = keyText;
            this.status = LetterStatus.UNDEFINED;
        }

        public Key(String keyText, LetterStatus status) {
            this.keyText = keyText;
            this.status = status;
        }
    }

}
