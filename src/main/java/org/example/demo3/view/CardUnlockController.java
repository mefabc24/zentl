package org.example.demo3.view;

import javafx.fxml.FXML;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.logic.CardRepository;
import org.example.demo3.model.service.NavigationService;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CardUnlockController {
    private NavigationService navigationService;
    CardRepository cardRepository = CardRepository.getInstance();
    List<Card> cards = cardRepository.getUnlockableCards();

    public void setNavigationService(NavigationService navigationService) {
        this.navigationService = navigationService;
    }

    @FXML
    private void initialize() {

    }

    private Card getRandomCard() {
        Rarity rarity = Rarity.getRandom();
        List<Card> filteredCards = cards.stream()
                .filter(card -> card.getRarity() == rarity)
                .toList();

        if (filteredCards.isEmpty()) return null;

        return filteredCards.get(ThreadLocalRandom.current().nextInt(filteredCards.size()));
    }


    @FXML
    public void generateCard() {
        Card card = getRandomCard();
        System.out.println(card);

        if (card != null) {
            System.out.println("Karte erhalten: " + card);
            card.setAmount(card.getAmount() + 1);
            System.out.println("Neuer Amount für " + card.getName() + ": " + card.getAmount());

            cardRepository.save();
        } else {
            System.out.println("Keine freischaltbare Karte gefunden.");
        }

    }

}
