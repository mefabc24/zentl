package org.example.demo3.network;

import com.google.gson.*;
import org.example.demo3.model.board.Board;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.*;
import org.example.demo3.model.player.AdvancedBot;
import org.example.demo3.model.player.TacticalBot;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.player.PlayerImpl;
import org.example.demo3.model.typeadapter.RuntimeTypeAdapterFactory;

import java.lang.reflect.Type;

// handles deserialization of interfaces and abstract classes for the LAN multiplayer
public class NetworkGson {

    private static final Gson INSTANCE = createInstance();

    public static Gson getInstance() {
        return INSTANCE;
    }

    private static Gson createInstance() {
        // RuntimeTypeAdapter for the card interface => tells gson which class to use based on the cardType field in the json
        RuntimeTypeAdapterFactory<Card> cardAdapterFactory = RuntimeTypeAdapterFactory
                .of(Card.class, "cardType")
                .registerSubtype(UnitCard.class, "UNIT")
                .registerSubtype(SpecialCard.class, "SPECIAL")
                .registerSubtype(WeatherCard.class, "WEATHER");

        // RuntimeTypeAdapter for the player interface
        RuntimeTypeAdapterFactory<Player> playerAdapterFactory = RuntimeTypeAdapterFactory
                .of(Player.class, "playerType")
                .registerSubtype(PlayerImpl.class, "PLAYER_IMPL")
                .registerSubtype(TacticalBot.class, "EASY_BOT")
                .registerSubtype(AdvancedBot.class, "ADVANCED_BOT");

        return new GsonBuilder()
                .registerTypeAdapterFactory(cardAdapterFactory)
                .registerTypeAdapterFactory(playerAdapterFactory)
                // register custom deserializer for the board
                .registerTypeAdapter(Board.class, new BoardDeserializer())
                .create();
    }

    // deserializer for the board interface => forces gson to deserialize a board interface into a gameboard object
    private static class BoardDeserializer implements JsonDeserializer<Board> {
        @Override
        public Board deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            // tell gson to treat the json object as a gameboard class
            return context.deserialize(json, GameBoard.class);
        }
    }
}