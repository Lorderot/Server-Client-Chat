package JSONcoder;

import protocols.ParticipantsTransmissionProtocol;

import javax.json.*;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ParticipantsTransmissionProtocolCoder {
    public static ParticipantsTransmissionProtocol decode(String json)
            throws ParseException {
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        ParticipantsTransmissionProtocol protocol =
                new ParticipantsTransmissionProtocol();
        JsonArray array = jsonObject.getJsonArray("participants");
        List<String> participants = new ArrayList<>();
        array.forEach(element -> participants.add(element.toString()));
        protocol.setParticipants(participants);
        return protocol;
    }

    public static String encode(ParticipantsTransmissionProtocol protocol) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        protocol.getParticipants().forEach(arrayBuilder::add);
        jsonObjectBuilder.add("participants", arrayBuilder.build());
        return jsonObjectBuilder.build().toString();
    }
}
