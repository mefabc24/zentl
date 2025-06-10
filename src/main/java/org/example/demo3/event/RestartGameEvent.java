package org.example.demo3.event;

import org.example.demo3.model.enums.BotDifficulty;
import org.example.demo3.model.enums.Faction;
import org.example.demo3.model.enums.GameMode;

public class RestartGameEvent extends Event {
    private final GameMode gameMode;
    private final BotDifficulty botDifficulty;
    private final Faction p1Faction;
    private final Faction p2Faction;

    public RestartGameEvent(GameMode gameMode, BotDifficulty botDifficulty, Faction p1Faction, Faction p2Faction) {
        this.name = "RestartGameEvent";
        this.gameMode = gameMode;
        this.botDifficulty = botDifficulty;
        this.p1Faction = p1Faction;
        this.p2Faction = p2Faction;
    }

    public GameMode getGameMode() { return gameMode; }
    public BotDifficulty getBotDifficulty() { return botDifficulty; }
    public Faction getP1Faction() { return p1Faction; }
    public Faction getP2Faction() { return p2Faction; }
}