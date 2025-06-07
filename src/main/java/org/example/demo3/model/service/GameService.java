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
import org.example.demo3.model.player.EasyBot;
import org.example.demo3.model.player.Player;

import java.util.List;

public class GameService extends AbstractGameService {

    public GameService() {
        super();
    }

    @Override
    public void newGame(GameMode mode, BotDifficulty difficulty, Faction p1Faction, Faction p2Faction) {
        this.isGameActive = true;
        this.gameMode = mode;
        this.botDifficulty = difficulty;

        this.board = new GameBoard();
        this.p1 = createPlayer("Player 1", p1Faction);

        List<Card> p2Deck = cardRepository.getSavedDeck(p2Faction);
        if (p2Deck.isEmpty()) {
            p2Deck = cardRepository.getRandomDeck(p2Faction);
        }
        this.p2 = PlayerFactory.createPlayer2(mode, difficulty, p2Faction, p2Deck);

        if (p1.getDeck().isEmpty() || p2.getDeck().isEmpty()) {
            endGame();
            return;
        }

        this.round = 1;
        this.currentPlayer = p1;
        dealInitialHands();
        postGameState();
    }

    @Override
    protected void nextTurn() {
        if (!isGameActive) return;

        if (p1.hasPassed() && p2.hasPassed()) {
            finishRound();
            return;
        }

        currentPlayer = (currentPlayer == p1) ? p2 : p1;
        if (currentPlayer.hasPassed()) {
            currentPlayer = (currentPlayer == p1) ? p2 : p1;
        }

        postGameState();

        if (isGameActive && currentPlayer instanceof EasyBot) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (!isGameActive) return;

                    Card cardToPlay = ((EasyBot) currentPlayer).chooseCardToPlay();
                    Platform.runLater(() -> {
                        if (!isGameActive) return;
                        if (cardToPlay != null) {
                            eventBus.post(new PlayCardRequest(currentPlayer, cardToPlay));
                        } else {
                            eventBus.post(new PlayerPassed(currentPlayer));
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}