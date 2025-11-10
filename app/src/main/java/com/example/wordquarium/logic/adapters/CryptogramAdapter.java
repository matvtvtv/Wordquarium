package com.example.wordquarium.logic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.wordquarium.R;
import com.example.wordquarium.data.model.CharCell;
import java.util.List;

/**
 * Адаптер для отображения клеток криптограммы.
 * Повторяю: визуальная выделенность выборки реализована через selectedIndex,
 * но при этом сохраняется статус в CharCell (CORRECT / HIDDEN / WRONG).
 */
public class CryptogramAdapter extends RecyclerView.Adapter<CryptogramAdapter.VH> {

    private final List<CharCell> items;
    private final LayoutInflater inflater;
    private int selectedIndex = -1;

    public interface OnCellClickListener {
        void onCellClick(int position);
    }

    private OnCellClickListener onCellClickListener;

    public void setOnCellClickListener(OnCellClickListener l) {
        this.onCellClickListener = l;
    }

    public CryptogramAdapter(Context context, List<CharCell> items) {
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_cipher_cell, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CharCell cell = items.get(position);

        // номер под клеткой
        holder.number.setText(cell.isLetter() ? String.valueOf(cell.getNumber()) : "");

        // если пунктуация/пробел — просто показываем символ
        if (!cell.isLetter()) {
            holder.letter.setText(String.valueOf(cell.getCh()));
            holder.letter.setBackgroundResource(android.R.color.transparent);
            return;
        }

        // если это буква, смотрим статус + выбранность
        boolean isSelected = (position == selectedIndex);

        switch (cell.getStatus()) {
            case CORRECT:
                holder.letter.setText(String.valueOf(Character.toUpperCase(cell.getCh())));
                holder.letter.setBackgroundResource(R.drawable.cell_background_green);
                break;

            case WRONG:
                // показываем букву-attempt (если есть), иначе пусто
                if (cell.getAttempt() != null) {
                    holder.letter.setText(String.valueOf(Character.toUpperCase(cell.getAttempt())));
                } else {
                    holder.letter.setText("");
                }
                // если выбранная — показываем "selected" фон (темнее), иначе yellow фон
                holder.letter.setBackgroundResource(isSelected ? R.drawable.cell_background_selected : R.drawable.cell_background_yellow);
                break;

            case HIDDEN:
            default:
                // скрытое состояние — если выбрано — показываем selected фон, иначе undefined
                holder.letter.setText("");
                holder.letter.setBackgroundResource(isSelected ? R.drawable.cell_background_selected : R.drawable.cell_background_undefined);
                break;
        }

        // клик по элементу — переключаем selectedIndex и уведомляем listener
        holder.itemView.setOnClickListener(v -> {
            if (!cell.isLetter()) return;
            int prev = selectedIndex;
            if (selectedIndex == position) {
                selectedIndex = -1;
            } else {
                selectedIndex = position;
            }
            // уведомляем изменения для прошлой и текущей позиций (если в пределах)
            if (prev >= 0) notifyItemChanged(prev);
            notifyItemChanged(position);

            if (onCellClickListener != null) onCellClickListener.onCellClick(selectedIndex);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView letter;
        TextView number;
        public VH(@NonNull View itemView) {
            super(itemView);
            letter = itemView.findViewById(R.id.tvLetter);
            number = itemView.findViewById(R.id.tvNumber);
        }
    }

    // --- Управление selection из Activity ---
    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int idx) {
        if (selectedIndex == idx) return;
        int prev = selectedIndex;
        selectedIndex = idx;
        if (prev != -1) notifyItemChanged(prev);
        if (selectedIndex != -1) notifyItemChanged(selectedIndex);
    }

    // --- Публичные операции, вызываемые из Activity ---

    /**
     * Открывает (раскрывает) только указанную позицию.
     */
    public void revealAtPosition(int position) {
        if (position < 0 || position >= items.size()) return;
        CharCell c = items.get(position);
        if (!c.isLetter()) return;
        c.setStatus(CharCell.CellStatus.CORRECT);
        c.setAttempt(null);
        notifyItemChanged(position);
        // снять выделение после успешного угадывания
        if (selectedIndex == position) {
            int prev = selectedIndex;
            selectedIndex = -1;
            notifyItemChanged(prev);
        }
    }

    /**
     * Пометить выбранную клетку как WRONG и записать попытку пользователя (отобразится в клетке).
     */
    public void markCellWrongWithAttempt(int position, char attempt) {
        if (position < 0 || position >= items.size()) return;
        CharCell c = items.get(position);
        if (!c.isLetter()) return;
        c.setStatus(CharCell.CellStatus.WRONG);
        c.setAttempt(Character.toUpperCase(attempt));
        notifyItemChanged(position);
        // не снимаем выбранность — пользователь может попробовать ещё раз (при желании)
    }
}
