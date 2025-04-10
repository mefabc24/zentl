package org.example.demo3.model;

import org.example.demo3.model.enums.RowType;
import java.util.*;

public class GameBoardImpl implements GameBoard {
    private final Map<Player, Map<RowType, List<Card>>> board;

    public GameBoardImpl() {
        this.board = new HashMap<>();
    }

    @Override
    public void addCardToRow(Card card, RowType row, Player player) {
        board.computeIfAbsent(player, p -> new EnumMap<>(RowType.class))
                .computeIfAbsent(row, r -> new ArrayList<>())
                .add(card);
    }

    @Override
    public int calculateRowPower(RowType row, Player player) {
        return board.getOrDefault(player, Collections.emptyMap())
                .getOrDefault(row, Collections.emptyList())
                .stream()
                .mapToInt(Card::getPower)
                .sum();
    }

    @Override
    public int calculateTotalPower(Player player) {
        return Arrays.stream(RowType.values())
                .filter(r -> r != RowType.ANY)
                .mapToInt(r -> calculateRowPower(r, player))
                .sum();
    }

    @Override
    public void clearBoard() {
        board.clear();
    }

    @Override
    public Map<RowType, List<Card>> getPlayerRows(Player player) {
        return Collections.unmodifiableMap(
                board.getOrDefault(player, Collections.emptyMap())
        );
    }
}