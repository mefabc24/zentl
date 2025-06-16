package org.example.demo3.model.player;

import org.example.demo3.model.board.Board;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.*;

import java.util.Comparator;
import java.util.List;

/*
this bot plays his weakest cards when ahead and his strongest cards when behind, it also evaluates the impact of playing effect/weather cards
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

        return findBestCardToPlay(opponent, board, round);
    }

    // evaluate if passing is a good move
    private boolean shouldPass(Player opponent, Board board, int round) {
        int botScore = board.calculateTotalPower(this);
        int opponentScore = board.calculateTotalPower(opponent);

        //  secure win
        if (opponent.hasPassed() && botScore > opponentScore) {
            return true;
        }

        // if we are not in (round 1 or lead in round 2) we cant lose the round, or we lose the game
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
                    // ignore effect cards
                    .filter(c ->  c.getEffectType() == EffectType.NONE)
                    .min(Comparator.comparingInt(Card::getPower))
                    .orElse(null);

            if (lowPowerUnit != null) {
                return lowPowerUnit;
            }
        }

        // when behind look for the strongest play

        // 1. look for a good weather card to play
        Card weatherMove = findOptimalWeatherCard(opponent, board);
        if (weatherMove != null) {
            return weatherMove;
        }

        // 2. find possible high value scorch
        Card scorchMove = findHighValueScorch(opponent, board);
        if (scorchMove != null) {
            return scorchMove;
        }

        // 3. find possible good dimeritium bomb
        Card dimeritiumBombMove = findDimeritiumBombMove(opponent, board);
        if (dimeritiumBombMove != null) {
            return dimeritiumBombMove;
        }

        // 4. find possible clear weather
        Card clearWeatherMove = findClearWeatherMove(opponent, board);
        if (clearWeatherMove != null) {
            return clearWeatherMove;
        }

        // 5. find possible horn
        Card hornMove = findOptimalHorn(board);
        if (hornMove != null) {
            return hornMove;
        }

        // 6. find good medic play
        Card medicMove = findMedicMove();
        if (medicMove != null) {
            return medicMove;
        }

        // 7. we found no good effect card to play => find the best no effect card to play

        // get list of no effect cards
        List<Card> simpleUnits = getHand()
                .stream()
                .filter(c -> c.getEffectType() == EffectType.NONE)
                .toList();

        if (simpleUnits.isEmpty()) {
            return null; // no simple units left => pass
        }

        // find the best move on row not affected by weather
        Card bestSafeMove = simpleUnits.stream()
                .filter(c -> !board.getActiveWeather().containsKey(c.getRowType()))
                .max(Comparator.comparingInt(Card::getPower))
                .orElse(null);

        if (bestSafeMove != null) {
            return bestSafeMove;
        }

        // if no safe move is possible we must play into a nerfed row
        // => play the weakest available simple unit to get at least 1 point
        return simpleUnits.stream()
                .min(Comparator.comparingInt(Card::getPower))
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
                int opponentLoss = calculatePotentialWeatherImpact(opponent, rowType, board);
                int myLoss = calculatePotentialWeatherImpact(this, rowType, board);

                int advantage = opponentLoss - myLoss;

                // save best play
                if (advantage > max) {
                    max = advantage;
                    bestWeatherCardToPlay = weatherCard;
                }
            }
        }

        // if we get high gain => play card
        if (bestWeatherCardToPlay != null && max >= 8) {
            return bestWeatherCardToPlay;
        }

        return null;
    }

    private int calculatePotentialWeatherImpact(Player player, RowType rowType, Board board) {
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

        // The loss is the difference between their current total power and their final total power (final power == cards affected, bc weather sets power to 1)
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

    private Card findDimeritiumBombMove(Player opponent, Board board) {
        // check if we even have a bomb card to play
        Card bombCard = getHand().stream()
                .filter(c -> c.getEffectType() == EffectType.DIMERITIUM_BOMB)
                .findFirst()
                .orElse(null);

        if (bombCard == null) {
            return null;
        }

        int totalAdvantage = 0;

        // 1. calculate advantage from clearing horn effects
        int opponentHornBuff = calculateHornBuff(opponent, board);
        int myHornBuff = calculateHornBuff(this, board);
        totalAdvantage += (opponentHornBuff - myHornBuff);

        // 2. calculate advantage from clearing weather effects
        int myWeatherGain = calculateWeatherGain(this, board);
        int opponentWeatherGain = calculateWeatherGain(opponent, board);
        totalAdvantage += (myWeatherGain - opponentWeatherGain);

        // if the total advantage is high => play the bomb
        if (totalAdvantage >= 12) {
            return bombCard;
        }

        return null;
    }

    private int calculateHornBuff(Player player, Board board) {
        int powerFromHorn = 0;
        for (RowType row : new RowType[]{RowType.MELEE, RowType.RANGED, RowType.SIEGE}) {
            // check if horn is actually active on this row for the player
            if (board.isHornActive(player, row)) {
                // the buff is equal to the rows base power
                // Horn doubled the value so / 2 to get the buff impact
                powerFromHorn += board.calculateRowPower(row, player) / 2;
            }
        }
        return powerFromHorn;
    }

    private Card findClearWeatherMove(Player opponent, Board board) {
        // check if we even have a clear weather card to play
        Card clearWeatherCard = getHand().stream()
                .filter(c -> c.getEffectType() == EffectType.CLEAR_WEATHER || c.getEffectType() == EffectType.RALLY)
                .findFirst()
                .orElse(null);

        if (clearWeatherCard == null || board.getActiveWeather().isEmpty()) {
            return null;
        }

        // calculate how much both players would gain from clearing weather
        int myGain = calculateWeatherGain(this, board);
        int opponentGain = calculateWeatherGain(opponent, board);

        // calculate net advantage from clearing weather
        int advantage = myGain - opponentGain;

        // high advantage => play card
        if (advantage >= 8) {
            return clearWeatherCard;
        }

        return null;
    }

    private int calculateWeatherGain(Player player, Board board) {
        int totalGain = 0;
        // only look at active weather effects
        for (RowType row : board.getActiveWeather().keySet()) {
            int powerLoss = 0;
            List<Card> nonHeroUnits = board.getPlayerRows(player)
                    .getOrDefault(row, List.of())
                    .stream()
                    .filter(c -> c.getRarity() != Rarity.LEGENDARY && c.getRarity() != Rarity.MYTHIC)
                    .toList();

            for (Card unit : nonHeroUnits) {
                // gain is the difference between base power and 1
                powerLoss += (unit.getPower() - 1);
            }
            totalGain += powerLoss;
        }
        return totalGain;
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
            // horn cannot be applied to a row that already has a horn
            if (board.isHornActive(this, currentRowType)) {
                continue;
            }

            int currentCardCount = board.getPlayerRows(this)
                    .getOrDefault(currentRowType, List.of())
                    .size();

            // save the row with the most cards
            if (currentCardCount > maxCardCount) {
                maxCardCount = currentCardCount;
                targetRowType = currentRowType;
            }
        }

        // if no valid row has a card return
        if (targetRowType == null || maxCardCount == 0) {
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

    private Card findMedicMove() {
        // check if we even have a medic card to play
        Card medicCard = getHand().stream()
                .filter(c -> c.getEffectType() == EffectType.MEDIC)
                .findFirst()
                .orElse(null);

        if (medicCard == null) {
            return null;
        }

        // find the strongest non hero unit in the graveyard
        Card cardToRevive = getDiscardPile().stream()
                .filter(c -> c.getCardType() == CardType.UNIT && c.getRarity() != Rarity.LEGENDARY && c.getRarity() != Rarity.MYTHIC)
                .max(Comparator.comparingInt(Card::getPower))
                .orElse(null);

        // if there is a valuable target to revive => play the medic
        if (cardToRevive != null && cardToRevive.getPower() >= 8) {
            return medicCard;
        }

        return null;
    }
}