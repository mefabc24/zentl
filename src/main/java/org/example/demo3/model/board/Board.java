package org.example.demo3.model.board;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.RowType;
import org.example.demo3.model.enums.WeatherType;
import org.example.demo3.model.player.Player;

import java.util.List;
import java.util.Map;

public interface Board {
    void addCardToRow(Card card, Player player);
    void removeCard(Card card, Player player);
    int calculateRowPower(RowType row, Player player);
    int calculateTotalPower(Player player);
    void clearBoard();
    Map<RowType, List<Card>> getPlayerRows(Player player);

    // Methoden für Wettereffekte
    void setWeatherEffect(RowType rowType, WeatherType weatherType);
    void clearWeatherEffects();
    void addWeatherCard(Card card);
    List<Card> getActiveWeatherCards();
    void clearWeatherCards();


    // Methoden für Horn-Effekte
    void applyHornEffect(Player player, RowType rowType);
    void clearHornEffects(Player player);
    void clearAllHornEffects();

    // bots
    public Board copyWithPlayerMapping(Map<Player, Player> playerMapping);
}