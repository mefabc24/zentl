package org.example.demo3.model.cards;

import javafx.scene.image.Image;
import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.Rarity;
import org.example.demo3.model.enums.RowType;

public interface Card {

    // Getter
    int getId();
    int getPower();
    int getAmount();
    int getSelectedAmount();
    int getMaxAmount();

    String getName();
    String getDescription();
    String getImagePath();

    Image getImage();
    Faction getFaction();
    Rarity getRarity();
    CardType getCardType();
    RowType getRowType();

    boolean isUnlocked();

    // Setter
    void setUnlocked(boolean unlocked);
    void setSelectedAmount(int selectedAmount);
}