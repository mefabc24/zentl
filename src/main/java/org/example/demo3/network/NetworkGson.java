package org.example.demo3.network;

import com.google.gson.*;
import org.example.demo3.model.board.Board;
import org.example.demo3.model.board.GameBoard;
import org.example.demo3.model.cards.*;
import org.example.demo3.model.player.AdvancedBot;
import org.example.demo3.model.player.Player;
import org.example.demo3.model.player.PlayerImpl;
import org.example.demo3.model.player.TacticalBot;
import org.example.demo3.model.typeadapter.RuntimeTypeAdapterFactory;

import java.lang.reflect.Type;

public class NetworkGson {

    private static final Gson INSTANCE = createInstance();

    public static Gson getInstance() {
        return INSTANCE;
    }

    private static Gson createInstance() {
        RuntimeTypeAdapterFactory<Card> cardAdapterFactory = RuntimeTypeAdapterFactory
                .of(Card.class, "cardType")
                .registerSubtype(UnitCard.class, "UNIT")
                .registerSubtype(SpecialCard.class, "SPECIAL")
                .registerSubtype(WeatherCard.class, "WEATHER");

        RuntimeTypeAdapterFactory<Player> playerAdapterFactory = RuntimeTypeAdapterFactory
                .of(Player.class, "playerType")
                .registerSubtype(PlayerImpl.class, "PLAYER_IMPL")
                .registerSubtype(TacticalBot.class, "EASY_BOT")
                .registerSubtype(AdvancedBot.class, "ADVANCED_BOT");

        ExclusionStrategy strategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getName().equals("cardType") && Card.class.isAssignableFrom(f.getDeclaringClass());
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };

        return new GsonBuilder()
                .registerTypeAdapterFactory(cardAdapterFactory)
                .registerTypeAdapterFactory(playerAdapterFactory)
                .registerTypeAdapter(Board.class, new BoardDeserializer())
                .addSerializationExclusionStrategy(strategy)
                .create();
    }

    private static class BoardDeserializer implements JsonDeserializer<Board> {
        @Override
        public Board deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return context.deserialize(json, GameBoard.class);
        }
    }
}