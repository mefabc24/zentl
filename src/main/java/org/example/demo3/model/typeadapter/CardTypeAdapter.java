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
        // 1. Lass Gson das Objekt als generisches JsonObject serialisieren,
        //    ohne unseren speziellen Adapter erneut auszulösen.
        //    Dazu verwenden wir den Standard-Adapter für die konkrete Klasse.
        JsonObject jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();

        // 2. Füge das Typfeld EXPLIZIT hinzu. Dies ist der Kern der Lösung.
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

        // Füge das Feld hinzu, falls es durch die Standard-Serialisierung (wegen transient) fehlt.
        jsonObject.addProperty(TYPE_FIELD, typeName);

        return jsonObject;
    }

    @Override
    public Card deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement typeElement = jsonObject.get(TYPE_FIELD);
        if (typeElement == null || typeElement.isJsonNull()) {
            throw new JsonParseException("Das JSON-Objekt für 'Card' enthält kein '" + TYPE_FIELD + "'-Feld.");
        }
        String typeName = typeElement.getAsString();

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

        return context.deserialize(jsonObject, concreteType);
    }
}