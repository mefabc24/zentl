package org.example.demo3.model;

import org.example.demo3.model.enums.CardType;

public interface Card {
    String getName();
    int getPower();
    String getDescription();
    CardType getType();
}