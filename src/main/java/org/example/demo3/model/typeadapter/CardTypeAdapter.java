package org.example.demo3.model.typeadapter;

import com.google.gson.*;
import org.example.demo3.model.cards.Card;
import org.example.demo3.model.cards.SpecialCard;
import org.example.demo3.model.cards.UnitCard;
import org.example.demo3.model.cards.WeatherCard;

import java.lang.reflect.Type;

public class CardTypeAdapter implements JsonSerializer<Card>, JsonDeserializer<Card> {

    private static final String TYPE_FIELD = "cardType";

    @Override
    public JsonElement serialize(Card src, Type typeOfSrc, JsonSerializationContext context) {
        // 1. Serialisiere das Objekt zu einem JsonObject
        JsonObject jsonObject = context.serialize(src).getAsJsonObject();

        // 2. Füge das Typfeld manuell hinzu, basierend auf der Klasse.
        //    Dies funktioniert auch dann, wenn das Feld in der Java-Klasse 'transient' ist!
        String typeName;
        if (src instanceof UnitCard) {
            typeName = "UNIT";
        } else if (src instanceof SpecialCard) {
            typeName = "SPECIAL";
        } else if (src instanceof WeatherCard) {
            typeName = "WEATHER";
        } else {
            throw new IllegalArgumentException("Unbekannter Kartentyp: " + src.getClass().getName());
        }
        jsonObject.addProperty(TYPE_FIELD, typeName);

        return jsonObject;
    }

    @Override
    public Card deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // 1. Hole das Typfeld aus dem JSON
        JsonElement typeElement = jsonObject.get(TYPE_FIELD);
        if (typeElement == null || typeElement.isJsonNull()) {
            throw new JsonParseException("Das JSON-Objekt für 'Card' enthält kein '" + TYPE_FIELD + "'-Feld.");
        }
        String typeName = typeElement.getAsString();

        // 2. Bestimme die konkrete Klasse basierend auf dem Typfeld
        Type concreteType;
        switch (typeName) {
            case "UNIT":
                concreteType = UnitCard.class;
                break;
            case "SPECIAL":
                concreteType = SpecialCard.class;
                break;
            case "WEATHER":
                concreteType = WeatherCard.class;
                break;
            default:
                throw new JsonParseException("Unbekannter Kartentyp im JSON: " + typeName);
        }

        // 3. Deserialisiere das JsonObject in die konkrete Klasse
        return context.deserialize(jsonObject, concreteType);
    }
}