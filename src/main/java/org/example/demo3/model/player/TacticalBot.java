package org.example.demo3.model.player;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.EffectType;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.enums.RowType;

import java.util.Comparator;
import java.util.List;

/*
this bot plays his weakest cards when ahead and his strongest cards when behind
 */
public class TacticalBot extends PlayerImpl {

    public TacticalBot(String name, Faction faction, List<Card> deck) {
        super(name, faction, deck);
    }

    public Card chooseCardToPlay(Player opponent, Board board, int round) {

        // no cards left => pass
        if (getHand().isEmpty()) {
            return null;
        }

        // check if passing is a good move
        if (shouldPass(opponent, board, round)) {
            return null;
        }

        // find best card to play
        Card chosenCard = findBestCardToPlay(opponent, board, round);

        if (chosenCard != null) {
            return chosenCard;
        } else {
            return getHand().getFirst();
        }
    }

    // evaluate if passing is a good move
    private boolean shouldPass(Player opponent, Board board, int round) {
        int botScore = board.calculateTotalPower(this);
        int opponentScore = board.calculateTotalPower(opponent);

        //  secure win
        if (opponent.hasPassed() && botScore > opponentScore) {
            return true;
        }

        // if we are not in round 1 or lead in round 2 we cant lose the round, or we lose the game
        if(!(round == 1 || (round == 2 && getWins() > opponent.getWins()))) return false;

        // we pass if we are far behind => opponent played his best cards
        if (opponentScore > botScore + 30) {
            return true;
        }

        // also if we have a big advantage then we can pass => player would have to sacrifice a lot of cards for the roundwin
        if (botScore >= opponentScore + 20) {
            return true;
        }

        // when taking the win is to expensive => pass
        return opponent.hasPassed() && (opponentScore - botScore) > 20;
    }

    private Card findBestCardToPlay(Player opponent, Board board, int round) {

        // when ahead play small power unit to save resources
        if (this.getScore() >= opponent.getScore()) {
            Card lowPowerUnit = getHand()
                    .stream()
                    .filter(c -> c.getPower() > 0)
                    .min(Comparator.comparingInt(Card::getPower))
                    .orElse(null);

            if (lowPowerUnit != null) {
                return lowPowerUnit;
            }
        }

        // when behind look for the strongest play

        // 1. look for a good card to play
        Card weatherMove = findOptimalWeatherCard(opponent, board);
        if (weatherMove != null) {
            return weatherMove;
        }

        // 2. find possible high value scorch
        Card scorchMove = findHighValueScorch(opponent, board);
        if (scorchMove != null) {
            return scorchMove;
        }

        // 3. find possible horn
        Card hornMove = findOptimalHorn(board);
        if (hornMove != null) {
            return hornMove;
        }

        // 4. play strongest unit to catch up, when behind
        return getHand()
                .stream()
                .filter(c -> c.getPower() > 0)
                .max(Comparator.comparingInt(Card::getPower))
                .orElse(null);
    }

    private Card findOptimalWeatherCard(Player opponent, Board board) {
        Card bestWeatherCardToPlay = null;
        int max = 0;
        RowType[] rows = new RowType[]{RowType.MELEE, RowType.RANGED, RowType.SIEGE};

        for (RowType rowType : rows) {
            EffectType requiredEffect;
            switch (rowType) {
                case MELEE:  requiredEffect = EffectType.WEATHER_FROST; break;
                case RANGED: requiredEffect = EffectType.WEATHER_FOG;   break;
                case SIEGE:  requiredEffect = EffectType.WEATHER_RAIN;  break;
                default: return null;
            }

            // check if we have the weather card in our hand
            Card weatherCard = getHand().stream()
                    .filter(c -> c.getEffectType() == requiredEffect)
                    .findFirst()
                    .orElse(null);

            if (weatherCard != null) {
                // evaluate impact
                int opponentLoss = calculateWeatherImpact(opponent, rowType, board);
                int myLoss = calculateWeatherImpact(this, rowType, board);

                int advantage = opponentLoss - myLoss;

                // save best play
                if (advantage > max) {
                    max = advantage;
                    bestWeatherCardToPlay = weatherCard;
                }
            }
        }

        // if we achieve a gain bigger then +8 we play the card
        if (bestWeatherCardToPlay != null && max >= 8) {
            return bestWeatherCardToPlay;
        }

        return null;
    }

    private int calculateWeatherImpact(Player player, RowType rowType, Board board) {
        int currentPower = 0;
        int unitsAffected = 0;

        // get all non hero units in the row
        List<Card> nonHeroUnits = board.getPlayerRows(player)
                .getOrDefault(rowType, List.of())
                .stream()
                .filter(c -> c.getRarity() != Rarity.LEGENDARY && c.getRarity() != Rarity.MYTHIC)
                .toList();

        for (Card unit : nonHeroUnits) {
            currentPower += unit.getPower();
            unitsAffected++;
        }

        // The loss is the difference between their current total power and their final total power.
        if (unitsAffected == 0) {
            return 0;
        }
        // return the loss => currentPower - unitsAffected gives the difference in score for the player (loss)
        return currentPower - unitsAffected;
    }

    private Card findHighValueScorch(Player opponent, Board board) {
        // get the best cards for both players
        int maxPowerBot = getMaxNonHeroUnitPower(this, board);
        int maxPowerOpp = getMaxNonHeroUnitPower(opponent, board);

        // only play scorch if opponent has the strongest card and its a valuable target
        if (maxPowerBot < maxPowerOpp && maxPowerOpp >= 10) {
            return getHand().stream().filter(c -> c.getEffectType() == EffectType.SCORCH).findFirst().orElse(null);
        }

        return null;
    }

    private int getMaxNonHeroUnitPower(Player player, Board board) {
        return board
                .getPlayerRows(player)
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(c ->
                        c.getRarity() != Rarity.LEGENDARY &&
                                c.getRarity() != Rarity.MYTHIC
                )
                .mapToInt(Card::getPower)
                .max()
                .orElse(0);
    }

    private Card findOptimalHorn(Board board) {
        // check if we even have a horn card to play
        Card commandersHornCard = getHand()
                .stream()
                .filter(c -> c.getEffectType() == EffectType.COMMANDERS_HORN)
                .findFirst()
                .orElse(null);

        if (commandersHornCard == null) {
            return null;
        }

        // find the row with the most cards => thats the row the horn would be applied to
        RowType targetRowType = null;
        int maxCardCount = 0;
        RowType[] rows = new RowType[]{RowType.MELEE, RowType.RANGED, RowType.SIEGE};

        for (RowType currentRowType : rows) {
            int currentCardCount = board.getPlayerRows(this)
                    .getOrDefault(currentRowType, List.of())
                    .size();

            // save the row with the most cards
            if (currentCardCount > maxCardCount) {
                maxCardCount = currentCardCount;
                targetRowType = currentRowType;
            }
        }

        // if no row has a card return
        if (maxCardCount == 0) {
            return null;
        }

        // calculate the total power of the row
        int rowPower = board.calculateRowPower(targetRowType, this);

        // if the row has more then 15 in total power => its worth to play the horn card
        if (rowPower >= 15) {
            return commandersHornCard;
        } else {
            return null;
        }
    }
}