package org.example.demo3.model.player;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.WeatherCard;
import org.example.demo3.model.enums.*;

import java.util.*;
import java.util.stream.Stream;

// an advanced bot using the minimax algorithm with alpha-beta pruning
// => determines the optimal move by simulating thousands of future game states and scoring them with a heuristic function (evaluateState)
public class AdvancedBot extends PlayerImpl {
    // MAX_DEPTH should be uneven
    private static final int MAX_DEPTH = 11; // controlls foresight, but time complexity: O(b^d) in worst case | d = depth, b = number of possible moves (how many cards can be played) => has huge impact on performance

    // simulation counter (logging)
    private long statesEvaluated;
    private long branchesPruned;

    public AdvancedBot(String name, Faction faction, List<Card> deck) {
        super(name, faction, deck);
    }

    // chooses the best card to play
    public Card chooseCardToPlay(Player opponent, Board board, int round) {
        if (getHand().isEmpty()) return null;
        GameState initialState = new GameState(this, opponent, board, round);

        this.statesEvaluated = 0;
        this.branchesPruned = 0;

        // get possible moves at the top level
        List<Card> possibleMoves = getPossibleMoves(initialState, true);
        Move bestMove = new Move(null, Integer.MIN_VALUE);
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        System.out.println("--- advanced bot is thinking... ---");
        for (Card move : possibleMoves) {
            GameState newState = simulateMove(initialState, move, true);
            Move result = minimax(newState, MAX_DEPTH - 1, alpha, beta, false); // start recursion

            String moveName = (move == null) ? "PASS" : move.getName();
            System.out.println("evaluated move: " + moveName + " | final score: " + result.score);

            if (result.score > bestMove.score) {
                bestMove = new Move(move, result.score);
            }
            alpha = Math.max(alpha, result.score);
        }
        System.out.println("---  advanced bot best move: " + (bestMove.card == null ? "PASS" : bestMove.card.getName()) + " with score " + bestMove.score + " ---");
        System.out.println("---  Total states evaluated: " + this.statesEvaluated);
        System.out.println("---  Branches pruned: " + this.branchesPruned);

        return bestMove.card;
    }

    // minimax with alpha-beta pruning
    private Move minimax(GameState state, int depth, int alpha, int beta, boolean isBotTurn) {
        this.statesEvaluated++;

        // (BASECASE) as the max depth is reached or both players passed/have empty hands, we return from our search evaluating the state the simualation ended in
        if (depth == 0 || state.isTerminal()) {
            int evalScore = evaluateState(state);
            return new Move(null, evalScore);
        }

        // get all possible moves => all cards the current player can play + pass (passing is always an option)
        List<Card> possibleMoves = getPossibleMoves(state, isBotTurn);

        // save the best possible move we found so far
        Move bestMove = isBotTurn ? new Move(null, Integer.MIN_VALUE) : new Move(null, Integer.MAX_VALUE);

        // play every card the current player has in his hand
        for (Card move : possibleMoves) {

            GameState newState = simulateMove(state, move, isBotTurn);

            // we call minimax while alternating who is playing and reducing the depth by one
            Move result = minimax(newState, depth - 1, alpha, beta, !isBotTurn);

            // evaluating if we found a better move for the bot (maximiser)
            if (isBotTurn) {
                // if this branch has a better outcome, we update our best choice at this depth
                // => we pair the move (card played) from THIS depth with the score that bubbled up from the leaf node
                if (result.score > bestMove.score) {
                    bestMove = new Move(move, result.score);
                }
                alpha = Math.max(alpha, result.score); // pruning: alpha tracks the highest score the bot can reach, we found so far
            }
            // evaluating if we found a better move for the player (minimiser)
            else {
                if (result.score < bestMove.score) {
                    bestMove = new Move(move, result.score);
                }
                beta = Math.min(beta, result.score); // pruning: beta tracks the lowest score the player can reach, we found so far
            }

            // the actual pruning:
            // if the maximizer's guaranteed score (alpha) is better than
            // the minimizer's guaranteed score (beta), the minimizer will never let this path happen
            // so we can stop exploring this branch, for better understanding: https://www.geeksforgeeks.org/minimax-algorithm-in-game-theory-set-4-alpha-beta-pruning/
            if (beta <= alpha) {
                this.branchesPruned++;
                break;
            }
        }

        return bestMove;
    }

    // evaluation function: controlls the actual behaviour of the bot => the basecases are evaluated here
    private int evaluateState(GameState state) {
        // 1. calculate base values
        int botScore = state.board.calculateTotalPower(state.bot);
        int opponentScore = state.board.calculateTotalPower(state.opponent);
        int scoreDiff = botScore - opponentScore; // is the bot leading?

        int botHandSize = state.bot.getHand().size();
        int opponentHandSize = state.opponent.getHand().size();
        int handDiff = botHandSize - opponentHandSize; // tells us if we have an advantage in cards over our opponent

        // 2. analyze situation
        int botWins = state.bot.getWins();
        int oppWins = state.opponent.getWins();

        // if we are behind in round 2 or if its round 3 we must win the round
        boolean isMustWinRound = (state.round == 2 && botWins == 0 && oppWins == 1) || (state.round == 3);

        // 3. terminal state check
        // crucial if a round end is reached within the search depth
        if (state.isTerminal()) {
            if (scoreDiff <= 0) { // bot loses or draws the round
                if (isMustWinRound) {
                    // a draw in a must win scenario is better than losing, but still not good (2:2 draw is slightly better the lose )
                    return (scoreDiff == 0) ? -9000 : -10000 + scoreDiff;
                } else {
                    // a strategic pass is okay, but not a big win
                    // the value must be lower than a normal win
                    return 150 + (handDiff * 40) - scoreDiff;
                }
            } else { // bot is winning
                // points above the min win are overkill
                int overkill = scoreDiff - 1;
                int marginPenalty = overkill * 75;

                if (isMustWinRound) {
                    // game won => good, but dont overshoot
                    return 10000 + (handDiff * 100) - marginPenalty;
                }
                // normal roundwin
                return 1000 + (handDiff * 100) - marginPenalty;
            }
        }

        // 4. dynamic heuristic evaluation for non-terminal states
        int scoreMultiplier;
        int handAdvantageMultiplier;

        if (isMustWinRound) {
            // in a must win round, the score difference is most important
            scoreMultiplier = 60; // extremely high focus on points
            handAdvantageMultiplier = 15; // card advantage less relevant
        } else {
            // if it's round 1 or the bot is up 1-0, a strategic loss is ok
            scoreMultiplier = 20; // points matter less
            handAdvantageMultiplier = 25; // hand advantage provides a strategic edge
        }


        // get the total power of the remaining cards in the hand
        int botHandPower = state.bot.getHand().stream().mapToInt(Card::getPower).sum();
        int oppHandPower = state.opponent.getHand().stream().mapToInt(Card::getPower).sum();
        int handPowerDiff = botHandPower - oppHandPower;
        int handPowerMultiplier = 3;

        // other bonuses/penalties
        int cardValuePenalty = calculateCardValuePenalty(state);

        if (isMustWinRound) {
            cardValuePenalty /= 2;
        }

        // evaluation of the non terminal states
        int score = scoreDiff * scoreMultiplier;
        int hand = handDiff * handAdvantageMultiplier;

        return score + hand + handPowerDiff * handPowerMultiplier + cardValuePenalty;
    }

    // penalizes playing high value cards in early rounds => resource management and avoiding overcommitment, the bot should win rounds efficiently
    private int calculateCardValuePenalty(GameState state) {
        int penalty = 0;

        // count valuable cards played by the bot
        for (List<Card> row : state.board.getPlayerRows(state.bot).values()) {
            for (Card card : row) {
                if (card.getRarity() == Rarity.MYTHIC) {
                    penalty -= 200;
                } else if (card.getRarity() == Rarity.LEGENDARY) {
                    penalty -= 100;
                } else if (card.getPower() >= 10) {
                    penalty -= 50;
                }
                if (card.getEffectType() == EffectType.MEDIC) {
                    // heavy penalty for using a valuable revive effect early (in later rounds the handdif the medic gives, negates this penalty)
                    penalty -= 140;
                }
            }
        }
        return penalty;
    }

    // returns all possible moves for the current player
    private List<Card> getPossibleMoves(GameState state, boolean isBotTurn) {
        Player player = isBotTurn ? state.bot : state.opponent;

        // if a player has passed, their only move is to pass again.
        if (player.hasPassed() || player.getHand().isEmpty()) {
            List<Card> moves = new ArrayList<>();
            moves.add(null); // null = pass
            return moves;
        }

        // the possible moves are all cards that are left
        List<Card> moves = new ArrayList<>(player.getHand());
        moves.add(null); // pass is always an option
        return moves;
    }

    // simulates playing a card (or passing) for the current player
    private GameState simulateMove(GameState state, Card card, boolean isBotMove) {
        // for every gamestate we need a new deepcopy (new objects with the same values)
        GameState newState = state.copy();
        Player activePlayer = isBotMove ? newState.bot : newState.opponent;

        // null = pass
        if (card == null) {
            activePlayer.pass();
            return newState;
        }

        activePlayer.getHandInternal().remove(card);

        playCardOnBoard(newState, card, activePlayer);

        return newState;
    }

    // plays a card onto board
    private void playCardOnBoard(GameState state, Card card, Player activePlayer) {
        Board board = state.board;
        if (card.getCardType() == CardType.UNIT) {
            board.addCardToRow(card, activePlayer);
        } else if (card.getCardType() == CardType.WEATHER) {
            board.addWeatherCard(card);
            applyWeatherEffect(state, (WeatherCard) card);
        } else if (card.getCardType() == CardType.SPECIAL) {
            applySpecialEffect(state, card, activePlayer);
        }
    }

    // these are pretty much the same as im AbstractGameService only isolated for better performance
    private void applyWeatherEffect(GameState state, WeatherCard card) {
        WeatherType type = card.getWeatherType();
        RowType targetRow;
        switch (type) {
            case FROST:
                targetRow = RowType.MELEE;
                break;
            case FOG:
                targetRow = RowType.RANGED;
                break;
            case RAIN:
                targetRow = RowType.SIEGE;
                break;
            default:
                return;
        }

        state.board.setWeatherEffect(targetRow, type);
    }

    private void applySpecialEffect(GameState state, Card card, Player activePlayer) {
        switch (card.getEffectType()) {
            case CLEAR_WEATHER:
            case RALLY:
                state.board.clearWeatherEffects();
                state.board.clearWeatherCards();
                break;
            case COMMANDERS_HORN:
                applyHornEffect(state, activePlayer);
                break;
            case SCORCH:
                applyScorchEffect(state);
                break;
            case MEDIC:
                applyMedicEffect(activePlayer);
                break;
            case DIMERITIUM_BOMB:
                state.board.clearAllHornEffects();
                state.board.clearWeatherEffects();
                state.board.clearWeatherCards();
                break;
        }
    }

    private void applyHornEffect(GameState state, Player player) {
        Optional<RowType> targetRow = Stream
                .of(RowType.MELEE, RowType.RANGED, RowType.SIEGE)
                .max(Comparator.comparingInt(r ->
                        state.board
                                .getPlayerRows(player)
                                .getOrDefault(r, List.of())
                                .size()))
                .filter(r ->
                        !state.board
                                .getPlayerRows(player)
                                .getOrDefault(r, List.of())
                                .isEmpty());
        targetRow.ifPresent(row -> state.board.applyHornEffect(player, row));
    }

    private void applyScorchEffect(GameState state) {
        List<Card> allUnits = new ArrayList<>();
        allUnits.addAll(state.board
                .getPlayerRows(state.bot)
                .values()
                .stream()
                .flatMap(List::stream)
                .toList());
        allUnits.addAll(state.board
                .getPlayerRows(state.opponent)
                .values()
                .stream()
                .flatMap(List::stream)
                .toList());

        int maxPower = allUnits
                .stream()
                .filter(c ->
                        c.getRarity() != Rarity.LEGENDARY && c.getRarity() != Rarity.MYTHIC)
                .mapToInt(Card::getPower)
                .max()
                .orElse(0);

        if (maxPower > 0) {
            List<Card> toScorch = allUnits.stream().filter(c ->
                            c.getPower() == maxPower &&
                                    c.getRarity() != Rarity.LEGENDARY &&
                                    c.getRarity() != Rarity.MYTHIC)
                    .toList();

            for (Card scorched : toScorch) {
                if (state.board.getPlayerRows(state.bot)
                        .values()
                        .stream()
                        .anyMatch(l -> l.contains(scorched))) {
                    state.board.removeCard(scorched, state.bot);
                } else {
                    state.board.removeCard(scorched, state.opponent);
                }
            }
        }
    }

    private void applyMedicEffect(Player player) {
        Optional<Card> toRevive = player
                .getDiscardPile()
                .stream()
                .filter(c ->
                        c.getCardType() == CardType.UNIT &&
                                c.getRarity() != Rarity.LEGENDARY &&
                                c.getRarity() != Rarity.MYTHIC)
                .max(Comparator.comparingInt(Card::getPower));

        if (toRevive.isPresent()) {
            Card revived = toRevive.get();
            player.removeFromDiscardPile(revived);
            player.addToHand(revived);
        }
    }

    // holds a deepcopied snapshot of the game for simulation
    private static class GameState {
        Player bot;
        Player opponent;
        Board board;
        int round;

        // entry point => creates the initial simulation state from the real game objects
        public GameState(Player bot, Player opponent, Board board, int round) {
            this.bot = bot.copy();
            this.opponent = opponent != null ? opponent.copy() : null;
            this.board = board.copy();
            this.round = round;
        }

        // internal helper constructor for copy function
        private GameState(
                Player bot,
                Player opponent,
                Board board,
                int round,
                boolean noCopy // flag to choose the right constructor
        ) {
            this.bot = bot;
            this.opponent = opponent;
            this.board = board;
            this.round = round;
        }

        // deepcopys all objects in a gamestate
        public GameState copy() {
            Player newBot = this.bot.copy();
            Player newOpponent = this.opponent.copy();
            Board newBoard = this.board.copy();
            return new GameState(newBot, newOpponent, newBoard, this.round, true);
        }

        // checks for simulation ending conditions => the round is over if both players have passed or have no cards left.
        public boolean isTerminal() {
            return ((bot.hasPassed() && opponent.hasPassed()) || (bot.getHand().isEmpty() && opponent.getHand().isEmpty()));
        }
    }

    // a move contains the card played an the given score
    private static class Move {
        Card card;
        int score;

        public Move(Card card, int score) {
            this.card = card;
            this.score = score;
        }
    }
}