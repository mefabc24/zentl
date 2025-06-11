package org.example.demo3.model.service;

import javafx.application.Platform;
import org.example.demo3.event.PlayCardRequest;
import org.example.demo3.event.PlayerPassed;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.BotDifficulty;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.GameMode;
import org.example.demo3.model.logic.PlayerFactory;
import org.example.demo3.model.player.AdvancedBot;
import org.example.demo3.model.player.EasyBot;
import org.example.demo3.model.player.Player;

import java.util.ArrayList;
import java.util.List;

public class GameService extends AbstractGameService {

    public GameService() {
        super();
    }

    // This is an overloaded method for convenience, it calls the main one
    public void newGame(GameMode mode, BotDifficulty difficulty, Faction p1Faction, Faction p2Faction) {
        this.newGame(mode, difficulty, BotDifficulty.NONE, p1Faction, p2Faction);
    }

    // This is the main method that handles all game modes
    public void newGame(GameMode mode, BotDifficulty bot1Difficulty, BotDifficulty bot2Difficulty, Faction p1Faction, Faction p2Faction) {
        this.isGameActive = true;
        this.gameMode = mode;
        this.botDifficulty = bot1Difficulty; // botDifficulty now refers to Bot 1

        this.board = new GameBoard();

        // Create Player 1 based on the game mode
        if (mode == GameMode.BOT_VS_BOT) {
            List<Card> p1Deck = cardRepository.getRandomDeck(p1Faction);
            this.p1 = PlayerFactory.createPlayer("Bot 1", bot1Difficulty, p1Faction, p1Deck);
        } else {
            // For PvP and PvB, Player 1 is always human
            this.p1 = createPlayer("Player 1", p1Faction);
        }

        // Create Player 2 based on the game mode
        if (mode == GameMode.BOT_VS_BOT) {
            List<Card> p2Deck = cardRepository.getRandomDeck(p2Faction);
            this.p2 = PlayerFactory.createPlayer("Bot 2", bot2Difficulty, p2Faction, p2Deck);
        } else {
            List<Card> p2Deck = cardRepository.getSavedDeck(p2Faction);
            if (p2Deck.isEmpty()) {
                p2Deck = createCustomBotDeck(p2Faction);
            }
            this.p2 = PlayerFactory.createPlayer2(mode, bot1Difficulty, p2Faction, p2Deck);
        }

        if (p1.getDeck().isEmpty() || p2.getDeck().isEmpty()) {
            endGame();
            return;
        }

        this.round = 1;
        this.currentPlayer = p1; // Player 1 is correctly set to start
        dealInitialHands();
        postGameState();

        // ======================= FIX IS HERE =======================
        // The unconditional call to nextTurn() is removed.
        // Instead, we check if the VERY FIRST player is a bot. If so, we trigger its turn.
        // Otherwise, the game waits for human input.
        if (currentPlayer instanceof EasyBot || currentPlayer instanceof AdvancedBot) {
            System.out.println("First player is a bot. Kicking off its turn.");
            handleBotTurn();
        }
        // ===================== END OF FIX ======================
    }

    @Override
    protected void nextTurn() {
        if (!isGameActive) return;

        if (p1.hasPassed() && p2.hasPassed()) {
            finishRound();
            return;
        }

        // ======== FIX IS HERE: Simplified turn switching logic ========
        // The logic is the same for all modes. Switch the player, then check if the new player has already passed.
        currentPlayer = (currentPlayer == p1) ? p2 : p1;

        // If the new current player has already passed, switch back. This handles cases where one player passes early.
        if (currentPlayer.hasPassed()) {
            currentPlayer = (currentPlayer == p1) ? p2 : p1;
        }
        // ===================== END OF FIX ======================

        postGameState();

        // Handle bot turn logic for any bot, regardless of being P1 or P2
        if (currentPlayer instanceof EasyBot || currentPlayer instanceof AdvancedBot) {
            handleBotTurn();
        }
    }

    private void handleBotTurn() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (!isGameActive) return;

                Card cardToPlay = null;
                if (currentPlayer instanceof AdvancedBot bot) {
                    Player opponent = (currentPlayer == p1) ? p2 : p1;
                    cardToPlay = bot.chooseCardToPlay(opponent, board, round);
                } else if (currentPlayer instanceof EasyBot bot) {
                    cardToPlay = bot.chooseCardToPlay();
                }

                final Card finalCardToPlay = cardToPlay;
                Platform.runLater(() -> {
                    if (!isGameActive) return;
                    if (finalCardToPlay != null) {
                        eventBus.post(new PlayCardRequest(currentPlayer, finalCardToPlay));
                    } else {
                        eventBus.post(new PlayerPassed(currentPlayer));
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }


    private List<Card> createCustomBotDeck(Faction faction) {
        System.out.println("INFO: Creating custom deck for bot with faction: " + faction.name());
        return cardRepository.getRandomDeck(faction);
    }

    private void addCardByName(List<Card> deck, String name, int count) {
        cardRepository.getAllCards().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(card -> {
                    for (int i = 0; i < count; i++) {
                        deck.add(card);
                    }
                });
    }
}