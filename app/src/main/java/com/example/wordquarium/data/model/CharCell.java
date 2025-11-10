package com.example.wordquarium.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель ячейки криптограммы.
 * Используем Lombok (@Data, @AllArgsConstructor) как ты просил.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharCell {

    public enum CellStatus {
        HIDDEN,    // буква скрыта (по умолчанию)
        CORRECT,   // буква угадана / раскрыта
        WRONG      // пользователь ввёл неверную букву в эту клетку (показывается attempt)
    }

    private char ch;           // оригинальный символ (буква или пунктуация)
    private boolean isLetter;  // это буква?
    private int number;        // число под клеткой (1..32) — 0 для не-букв
    private CellStatus status; // текущее состояние
    private Character attempt; // буква, которую ввёл пользователь для этой клетки (nullable)

    /**
     * Упрощённый конструктор: revealedInitially = true -> CORRECT, иначе HIDDEN (для букв)
     */
    public CharCell(char ch, boolean isLetter, int number, boolean revealedInitially) {
        this.ch = ch;
        this.isLetter = isLetter;
        this.number = number;
        this.attempt = null;
        if (!isLetter) {
            this.status = CellStatus.CORRECT; // пунктуация/пробел — видимы всегда
        } else {
            this.status = revealedInitially ? CellStatus.CORRECT : CellStatus.HIDDEN;
        }
    }
}
