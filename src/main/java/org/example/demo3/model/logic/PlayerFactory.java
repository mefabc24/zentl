package org.example.demo3.model.logic;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.BotDifficulty;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.GameMode;
import org.example.demo3.model.player.AdvancedBot;
import org.example.demo3.model.player.TacticalBot;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.player.PlayerImpl;

import java.util.List;

public class PlayerFactory {
    
    public static Player createPlayer(String name, BotDifficulty difficulty, Faction faction, List<Card> deck) {
        switch (difficulty) {
            case EASY:
                return new TacticalBot(name, faction, deck);
            case ADVANCED:
                return new AdvancedBot(name, faction, deck);
            default: // A safe fallback in case of NONE or other values
                System.err.println("Warning: Invalid bot difficulty provided. Defaulting to Easy Bot.");
                return new TacticalBot(name + " (Default)", faction, deck);
        }
    }
    
    public static Player createPlayer2(GameMode mode, BotDifficulty difficulty, Faction faction, List<Card> deck) {
        if (mode == GameMode.PLAYER_VS_PLAYER) {
            return new PlayerImpl("Player 2", faction, deck);
        } else { // PLAYER_VS_BOT mode
            // We can now delegate to our new, more general method.
            return createPlayer("Bot", difficulty, faction, deck);
        }
    }
}