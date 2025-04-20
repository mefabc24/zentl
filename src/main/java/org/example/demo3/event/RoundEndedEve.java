package org.example.demo3.event;

import org.example.demo3.model.player.Player;

public class RoundEndedEve extends Event {
    private final int round;
    private final Player winner;
    private final Player p1;
    private final Player p2;

    public RoundEndedEve(int round, Player winner, Player p1, Player p2) {
        this.round = round;
        this.winner = winner;
        this.p1 = p1;
        this.p2 = p2;
        this.name = "RoundEndedEve";
    }

    public Player getWinner() {
        return winner;
    }

    public int getRound() {
        return round;
    }

    public Player getP1() {
        return p1;
    }

    public Player getP2() {
        return p2;
    }
}