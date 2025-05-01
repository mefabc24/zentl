package org.example.demo3.model.cards;

import org.example.demo3.model.enums.CardType;
import org.example.demo3.model.enums.RowType;

public interface Card {
    String getName();

    int getPower();

    RowType getRow();
}