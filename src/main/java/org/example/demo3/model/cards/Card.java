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

    String getName();
    String getDescription();
    String getImagePath();

    Image getImage();
    Faction getFaction();
    Rarity getRarity();
    CardType getCardType();
    RowType getRowType();

    boolean isSelected();
    boolean isUnlocked();

    // Setter
    void setSelected(boolean selected);
    void setUnlocked(boolean unlocked);

    // Toggle
    void toggleSelected();
}