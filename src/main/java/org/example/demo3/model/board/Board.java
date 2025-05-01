package org.example.demo3.model.board;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.player.Player;

import java.util.List;
import java.util.Map;

public interface Board {
    void addCardToRow(Card card, RowType row, Player player);

    int calculateRowPower(RowType row, Player player);

    int calculateTotalPower(Player player);

    void clearBoard();

    Map<RowType, List<Card>> getPlayerRows(Player player);
}