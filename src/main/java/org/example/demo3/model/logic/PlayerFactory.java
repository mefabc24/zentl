package org.example.demo3.model.logic;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.BotDifficulty;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.GameMode;
import org.example.demo3.model.player.AdvancedBot;
import org.example.demo3.model.player.EasyBot;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.player.PlayerImpl;

import java.util.List;

public class PlayerFactory {

    public static Player createPlayer2(GameMode mode, BotDifficulty difficulty, Faction faction, List<Card> deck) {
        if (mode == GameMode.PLAYER_VS_PLAYER) {
            return new PlayerImpl("Player 2", faction, deck);
        } else { // PLAYER_VS_BOT
            switch (difficulty) {
                case EASY:
                    return new EasyBot("Easy Bot", faction, deck);
                case ADVANCED:
                    return new AdvancedBot("Advanced Bot", faction, deck);
                default: // Fallback
                    return new EasyBot("Easy Bot (Default)", faction, deck);
            }
        }
    }
}