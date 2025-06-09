package org.example.demo3.model.service;

import org.example.demo3.event.*;
import org.example.demo3.model.board.Board;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.WeatherCard;
import org.example.demo3.model.enums.*;
import org.example.demo3.model.logic.CardRepository;
import org.example.demo3.model.logic.GameEngine;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.player.PlayerImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractGameService {
    protected static final int INITIAL_HAND_SIZE = 10;
    protected static final int CARDS_TO_DRAW_PER_ROUND = 3;

    protected final EventBus eventBus = EventBus.getInstanz();
    protected final GameEngine gameEngine = new GameEngine();
    protected final CardRepository cardRepository;
    protected final SoundService soundService;

    protected Board board;
    protected Player p1, p2, currentPlayer;
    protected int round;
    protected GameMode gameMode;
    protected BotDifficulty botDifficulty;
    protected volatile boolean isGameActive = true;

    private final EventHandler<RestartGameEvent> restartGameHandler = event -> newGame(event.getGameMode(), event.getBotDifficulty(), event.getP1Faction(), event.getP2Faction());
    private final EventHandler<PlayCardRequest> playCardHandler = event -> cardPlayed(event);
    private final EventHandler<PlayerPassed> playerPassedHandler = event -> playerPassed(event);

    public AbstractGameService() {
        this.cardRepository = CardRepository.getInstance();
        this.soundService = SoundService.getInstance();
        subscribeToEvents();
    }

    public abstract void newGame(GameMode mode, BotDifficulty difficulty, Faction p1Faction, Faction p2Faction);
    protected abstract void nextTurn();

    public void shutdown() {
        System.out.println("GameService is shutting down...");
        this.isGameActive = false;
        unsubscribeFromEvents();
    }

    protected void subscribeToEvents() {
        eventBus.subscribe(RestartGameEvent.class, restartGameHandler);
        eventBus.subscribe(PlayCardRequest.class, playCardHandler);
        eventBus.subscribe(PlayerPassed.class, playerPassedHandler);
    }

    private void unsubscribeFromEvents() {
        System.out.println("GameService unsubscribing from EventBus...");
        eventBus.unsubscribe(RestartGameEvent.class, restartGameHandler);
        eventBus.unsubscribe(PlayCardRequest.class, playCardHandler);
        eventBus.unsubscribe(PlayerPassed.class, playerPassedHandler);
    }

    protected void cardPlayed(PlayCardRequest event) {
        if (!isGameActive) return;
        Player player = event.getPlayer();
        Card card = event.getCard();
        soundService.playSoundForCard(card);

        if (player.removeFromHand(card)) {
            if (card.getCardType() == CardType.UNIT) {
                board.addCardToRow(card, player);
            } else if (card.getCardType() == CardType.WEATHER) {
                board.addWeatherCard(card);
            } else { // SPECIAL-Karte
                player.addToDiscardPile(card);
            }
            handleCardEffect(card, player);
            postGameState();
            nextTurn();
        }
    }

    protected void playerPassed(PlayerPassed event) {
        if (!isGameActive) return;
        event.getPlayer().pass();
        System.out.println("[DEBUG] Player " + event.getPlayer().getName() + " has passed.");
        postGameState();
        nextTurn();
    }

    protected void finishRound() {
        if (!isGameActive) return;
        updateScores();
        Player roundWinner = gameEngine.determineRoundWinner(p1, p2);

        if (roundWinner == p1) p1.setWins(p1.getWins() + 1);
        else if (roundWinner == p2) p2.setWins(p2.getWins() + 1);
        else {
            p1.setWins(p1.getWins() + 1);
            p2.setWins(p2.getWins() + 1);
        }
        eventBus.post(new RoundEndedEve(round, roundWinner, p1, p2));
        if (p1.getWins() >= 2 || p2.getWins() >= 2) endGame();
        else nextRoundSetup();
    }

    protected void nextRoundSetup() {
        if (!isGameActive) return;
        round++;
        Stream.of(p1, p2).forEach(p -> board.getPlayerRows(p).values().stream().flatMap(List::stream).forEach(p::addToDiscardPile));
        board.getActiveWeatherCards().forEach(currentPlayer::addToDiscardPile);
        board.clearWeatherCards();
        board.clearBoard();
        board.clearWeatherEffects();
        board.clearAllHornEffects();
        p1.resetPass();
        p2.resetPass();
        for (int i = 0; i < CARDS_TO_DRAW_PER_ROUND; i++) {
            if (!p1.getDeck().isEmpty()) p1.drawCard();
            if (!p2.getDeck().isEmpty()) p2.drawCard();
        }
        postGameState();
        nextTurn();
    }

    protected void endGame() {
        if (!isGameActive) return;
        this.isGameActive = false;
        Player winner = gameEngine.determineGameWinner(p1, p2);
        eventBus.post(new GameEndedEve(p1, p2, winner, p1.getWins(), p2.getWins(), p1.getName(), p2.getName()));
    }

    protected Player createPlayer(String name, Faction faction) {
        List<Card> deck = cardRepository.getSavedDeck(faction);
        if (deck.isEmpty()) deck = cardRepository.getRandomDeck(faction);
        return new PlayerImpl(name, faction, deck);
    }

    protected void dealInitialHands() {
        for (int i = 0; i < INITIAL_HAND_SIZE; i++) {
            if (!p1.getDeck().isEmpty()) p1.drawCard();
            if (!p2.getDeck().isEmpty()) p2.drawCard();
        }
    }

    protected void postGameState() {
        if (!isGameActive) return;
        updateScores();
        eventBus.post(new GameStateUpdateEve(p1, p2, currentPlayer, round, board, gameMode, botDifficulty));
    }

    protected void updateScores() {
        p1.setScore(board.calculateTotalPower(p1));
        p2.setScore(board.calculateTotalPower(p2));
        // NEUE DEBUG-AUSGABE: Zeigt die finalen Punktestände nach jeder Aktualisierung.
        System.out.println("--------------------------------------------------");
        System.out.println("[DEBUG] SCORES UPDATED | P1: " + p1.getScore() + " | P2: " + p2.getScore());
        System.out.println("--------------------------------------------------");
    }

    protected void handleCardEffect(Card card, Player player) {
        // NEUE DEBUG-AUSGABE: Zeigt an, welcher Effekt getriggert wird.
        System.out.println("\n============== EFFECT TRIGGER ==============");
        System.out.println("[DEBUG] Player:      " + player.getName());
        System.out.println("[DEBUG] Card Played: " + card.getName());
        System.out.println("[DEBUG] EffectType:  " + card.getEffectType());
        System.out.println("============================================");

        if (card.getEffectType() == null) return;
        String effectMessage = "";

        switch (card.getEffectType()) {
            case COMMANDERS_HORN:
                Optional<RowType> targetRow = Stream.of(RowType.MELEE, RowType.RANGED, RowType.SIEGE)
                        .max(Comparator.comparingInt(r -> board.getPlayerRows(player).getOrDefault(r, List.of()).size()))
                        .filter(r -> !board.getPlayerRows(player).getOrDefault(r, List.of()).isEmpty());
                if (targetRow.isPresent()) {
                    board.applyHornEffect(player, targetRow.get());
                    effectMessage = player.getName() + "'s " + targetRow.get().name() + " row is now doubled!";
                } else effectMessage = card.getName() + " had no units to affect.";
                break;
            case MEDIC:
                Optional<Card> cardToRevive = player.getDiscardPile().stream()
                        .filter(c -> c.getCardType() == CardType.UNIT && c.getRarity() != Rarity.LEGENDARY && c.getRarity() != Rarity.MYTHIC)
                        .max(Comparator.comparingInt(Card::getPower));
                if (cardToRevive.isPresent()) {
                    Card revivedCard = cardToRevive.get();
                    player.removeFromDiscardPile(revivedCard);
                    player.addToHand(revivedCard);
                    effectMessage = player.getName() + " revived '" + revivedCard.getName() + "' to their hand!";
                } else {
                    effectMessage = "No unit in discard pile to revive.";
                }
                break;
            case SCORCH:
                List<Card> allUnitsOnBoard = new ArrayList<>();
                board.getPlayerRows(p1).values().forEach(allUnitsOnBoard::addAll);
                board.getPlayerRows(p2).values().forEach(allUnitsOnBoard::addAll);
                int maxPower = allUnitsOnBoard.stream()
                        .filter(c -> c.getRarity() != Rarity.LEGENDARY && c.getRarity() != Rarity.MYTHIC)
                        .mapToInt(Card::getPower).max().orElse(0);
                if (maxPower > 0) {
                    List<Card> cardsToScorch = allUnitsOnBoard.stream()
                            .filter(c -> c.getPower() == maxPower && c.getRarity() != Rarity.LEGENDARY && c.getRarity() != Rarity.MYTHIC)
                            .collect(Collectors.toList());
                    StringBuilder scorchedNames = new StringBuilder();
                    cardsToScorch.forEach(scorchedCard -> {
                        scorchedNames.append(scorchedCard.getName()).append(", ");
                        if (board.getPlayerRows(p1).values().stream().anyMatch(list -> list.contains(scorchedCard))) {
                            board.removeCard(scorchedCard, p1); p1.addToDiscardPile(scorchedCard);
                        } else {
                            board.removeCard(scorchedCard, p2); p2.addToDiscardPile(scorchedCard);
                        }
                    });
                    effectMessage = "Scorch destroyed the strongest unit(s): " + scorchedNames.substring(0, scorchedNames.length() - 2) + "!";
                } else effectMessage = "Scorch found no non-hero units to destroy.";
                break;
            case WEATHER_FROST:
                board.setWeatherEffect(RowType.MELEE, ((WeatherCard) card).getWeatherType());
                effectMessage = "Biting Frost settles on the Melee rows!";
                System.out.println("[DEBUG] Board state updated: MELEE weather is now active for ALL players.");
                break;
            case WEATHER_FOG:
                board.setWeatherEffect(RowType.RANGED, ((WeatherCard) card).getWeatherType());
                effectMessage = "Impenetrable Fog descends on the Ranged rows!";
                System.out.println("[DEBUG] Board state updated: RANGED weather is now active for ALL players.");
                break;
            case WEATHER_RAIN:
                board.setWeatherEffect(RowType.SIEGE, ((WeatherCard) card).getWeatherType());
                effectMessage = "Torrential Rain pours on the Siege rows!";
                System.out.println("[DEBUG] Board state updated: SIEGE weather is now active for ALL players.");
                break;
            case CLEAR_WEATHER:
            case RALLY: // Ist jetzt absichtlich identisch zu CLEAR_WEATHER
                board.clearWeatherEffects();
                board.getActiveWeatherCards().forEach(player::addToDiscardPile);
                board.clearWeatherCards();
                effectMessage = "The skies have cleared!";
                System.out.println("[DEBUG] Board state updated: ALL weather effects cleared.");
                break;
            case DIMERITIUM_BOMB:
                board.clearAllHornEffects();
                board.clearWeatherEffects();
                board.getActiveWeatherCards().forEach(player::addToDiscardPile);
                board.clearWeatherCards();
                effectMessage = "A Dimeritium Bomb nullifies all magic on the battlefield!";
                System.out.println("[DEBUG] Board state updated: ALL weather and horn effects cleared.");
                break;
        }
        if (!effectMessage.isEmpty()) eventBus.post(new EffectLogEvent(effectMessage));
    }
}