package org.example.demo3.model.player;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Faction;
import java.util.List;

public class AdvancedBot extends PlayerImpl {
    public AdvancedBot(String name, Faction faction, List<Card> deck) {
        super(name, faction, deck);
    }

    // Brainz
    public Card chooseCardToPlay() {
        if (getHand().isEmpty()) {
            return null;
        }
        // Big brain move
        return getHand().stream()
                .max((c1, c2) -> Integer.compare(c1.getPower(), c2.getPower()))
                .orElse(null);
    }
}