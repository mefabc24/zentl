package org.example.demo3.model.player;

import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Faction;
import java.util.List;

public class EasyBot extends PlayerImpl {
    public EasyBot(String name, Faction faction, List<Card> deck) {
        super(name, faction, deck);
    }
    // Brainz
    public Card chooseCardToPlay() {
        if (getHand().isEmpty()) {
            return null;
        }
        
        return getHand().getFirst();
    }
}