package org.example.demo3.model.cards;

import java.util.UUID;

public class CardInstance {
    private final Card cardDefinition;
    private final UUID instanceId;

    public CardInstance(Card cardDefinition) {
        if (cardDefinition == null) {
            throw new IllegalArgumentException("CardDefinition cannot be null");
        }
        this.cardDefinition = cardDefinition;
        this.instanceId = UUID.randomUUID();
    }

    // Getter
    public Card getCardDefinition() { return cardDefinition; }
    public UUID getInstanceId() { return instanceId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardInstance that = (CardInstance) o;
        return instanceId.equals(that.instanceId);
    }

    @Override
    public int hashCode() {
        return instanceId.hashCode();
    }

    @Override
    public String toString() {
        return "CardInstance{" +
                "cardDefinition=" + cardDefinition.getName() +
                ", instanceId=" + instanceId +
                '}';
    }
}
