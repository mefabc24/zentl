package org.example.demo3.model;

import org.example.demo3.model.enums.RowType;
import java.util.List;
import java.util.Map;

public interface GameBoard {
    void addCardToRow(Card card, RowType row, Player player);
    int calculateRowPower(RowType row, Player player);
    int calculateTotalPower(Player player);
    void clearBoard();
    Map<RowType, List<Card>> getPlayerRows(Player player);
}