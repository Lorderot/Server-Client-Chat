package coder;

import protocol.MessagePacket;
import protocol.MessageType;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;

public class MessageCoder {
    public static String encode(MessagePacket messagePacket) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("type", messagePacket.getType().toString())
                .add("message", messagePacket.getMessage());
        return jsonBuilder.build().toString();
    }

    public static MessagePacket decode(String json) {
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setMessage(jsonObject.getString("message"));
        messagePacket.setType(MessageType
                .valueOf(jsonObject.getString("type")));
        return messagePacket;
    }
}
