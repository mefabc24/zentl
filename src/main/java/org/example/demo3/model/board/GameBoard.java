package org.example.demo3.model.board;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.enums.WeatherType;
import org.example.demo3.model.player.Player;

import java.util.*;

public class GameBoard implements Board {
    private final Map<String, Map<RowType, List<Card>>> board;
    private final Map<RowType, WeatherType> activeWeather;
    private final Map<String, Set<RowType>> activeHorns;
    private final List<Card> activeWeatherCards;

    public GameBoard() {
        this.board = new HashMap<>();
        this.activeWeather = new EnumMap<>(RowType.class);
        this.activeHorns = new HashMap<>();
        this.activeWeatherCards = new ArrayList<>();
    }

    @Override
    public void addCardToRow(Card card, Player player) {
        board.computeIfAbsent(player.getName(), p -> {
            activeHorns.put(p, new HashSet<>());
            return new EnumMap<>(RowType.class);
        }).computeIfAbsent(card.getRowType(), r -> new ArrayList<>()).add(card);
    }

    @Override
    public void removeCard(Card card, Player player) {
        Map<RowType, List<Card>> playerRows = board.get(player.getName());
        if (playerRows != null) {
            List<Card> row = playerRows.get(card.getRowType());
            if (row != null) {
                row.remove(card);
            }
        }
    }

    @Override
    public int calculateRowPower(RowType row, Player player) {
        List<Card> cardsInRow = board.getOrDefault(player.getName(), Collections.emptyMap())
                .getOrDefault(row, Collections.emptyList());

        int basePower = 0;
        WeatherType weather = activeWeather.get(row);

        for (Card card : cardsInRow) {
            boolean isHero = card.getRarity() == Rarity.LEGENDARY || card.getRarity() == Rarity.MYTHIC;
            if (weather != null && !isHero) {
                basePower += 1;
            } else {
                basePower += card.getPower();
            }
        }

        if (activeHorns.getOrDefault(player.getName(), Collections.emptySet()).contains(row)) {
            return basePower * 2;
        }
        return basePower;
    }

    @Override
    public int calculateTotalPower(Player player) {
        return Arrays.stream(new RowType[]{RowType.MELEE, RowType.RANGED, RowType.SIEGE})
                .mapToInt(row -> calculateRowPower(row, player))
                .sum();
    }

    @Override
    public void clearBoard() {
        board.values().forEach(map -> map.values().forEach(List::clear));
        clearWeatherCards();
    }

    @Override
    public Map<RowType, List<Card>> getPlayerRows(Player player) {
        return board.getOrDefault(player.getName(), Collections.emptyMap());
    }

    @Override
    public void setWeatherEffect(RowType rowType, WeatherType weatherType) {
        activeWeather.put(rowType, weatherType);
    }

    @Override
    public void clearWeatherEffects() {
        activeWeather.clear();
    }

    @Override
    public void addWeatherCard(Card card) {
        if (card.getCardType() == CardType.WEATHER) {
            this.activeWeatherCards.add(card);
        }
    }

    @Override
    public List<Card> getActiveWeatherCards() {
        return Collections.unmodifiableList(activeWeatherCards);
    }

    @Override
    public void clearWeatherCards() {
        this.activeWeatherCards.clear();
    }

    @Override
    public void applyHornEffect(Player player, RowType rowType) {
        activeHorns.computeIfAbsent(player.getName(), p -> new HashSet<>()).add(rowType);
    }

    @Override
    public void clearHornEffects(Player player) {
        activeHorns.getOrDefault(player.getName(), Collections.emptySet()).clear();
    }

    @Override
    public void clearAllHornEffects() {
        activeHorns.values().forEach(Set::clear);
    }

    // needed for minimax, to create deepcopys
    public Board copy() {
        GameBoard newBoard = new GameBoard();

        // iterate through each players rows and creates new lists for each row
        this.board.forEach((playerName, rows) -> {
            Map<RowType, List<Card>> newRows = new EnumMap<>(RowType.class);
            rows.forEach((rowType, cards) -> newRows.put(rowType, new ArrayList<>(cards)));
            newBoard.board.put(playerName, newRows);
        });

        // create a new hashset for each players active horns
        this.activeHorns.forEach((playerName, hornSet) -> {
            newBoard.activeHorns.put(playerName, new HashSet<>(hornSet));
        });

        newBoard.activeWeather.putAll(this.activeWeather);
        newBoard.activeWeatherCards.addAll(this.activeWeatherCards);

        return newBoard;
    }
}