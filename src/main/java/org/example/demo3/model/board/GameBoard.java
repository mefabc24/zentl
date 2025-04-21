package org.example.demo3.model.board;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.player.Player;

import java.util.*;

public class GameBoard implements Board {
    private final Map<Player, Map<RowType, List<Card>>> board;

    public GameBoard() {
        this.board = new HashMap<>();
    }

    public void addCardToRow(Card card, RowType row, Player player) {
        board.computeIfAbsent(player, p -> new EnumMap<>(RowType.class))
                .computeIfAbsent(row, r -> new ArrayList<>())
                .add(card);
    }

    public int calculateRowPower(RowType row, Player player) {
        return board.getOrDefault(player, Collections.emptyMap())
                .getOrDefault(row, Collections.emptyList())
                .stream()
                .mapToInt(Card::getPower)
                .sum();
    }

    public int calculateTotalPower(Player player) {
        return Arrays.stream(RowType.values())
                .filter(r -> r != RowType.ANY)
                .mapToInt(r -> calculateRowPower(r, player))
                .sum();
    }

    public void clearBoard() {
        board.clear();
    }

    public Map<RowType, List<Card>> getPlayerRows(Player player) {
        return Collections.unmodifiableMap(board.getOrDefault(player, Collections.emptyMap()));
    }
}