package JSONcoder;

import protocols.AccessType;
import protocols.MessageTransmissionProtocol;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;
import java.text.ParseException;

public class MessageTransmissionProtocolCoder {
    public static MessageTransmissionProtocol decode(String json)
            throws ParseException {
        MessageTransmissionProtocol protocol =
                new MessageTransmissionProtocol();
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        if (!jsonObject.isNull("receiver")) {
            protocol.setReceiver(jsonObject.getString("receiver"));
        }
        if (!jsonObject.isNull("sender")) {
            protocol.setSender(jsonObject.getString("sender"));
        }
        if (!jsonObject.isNull("time")) {
            protocol.setTime(jsonObject.getString("time"));
        }
        if (!jsonObject.isNull("type")) {
            protocol.setType(AccessType.valueOf(jsonObject.getString("type")));
        }
        protocol.setMessage(jsonObject.getString("message"));
        return protocol;
    }

    public static String encode(MessageTransmissionProtocol protocol) {
        String message = protocol.getMessage();
        String receiver = protocol.getReceiver();
        String sender = protocol.getSender();
        String time = protocol.getTime();
        AccessType type = protocol.getType();
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
                .add("message", message);
        if (time == null) {
            objectBuilder.addNull("time");
        } else {
            objectBuilder.add("time", time);
        }
        if (receiver == null) {
            objectBuilder.addNull("receiver");
        } else {
            objectBuilder.add("receiver", receiver);
        }
        if (sender == null) {
            objectBuilder.addNull("sender");
        } else {
            objectBuilder.add("sender", sender);
        }
        if (type == null) {
            objectBuilder.addNull("type");
        } else {
            objectBuilder.add("type", type.toString());
        }
        objectBuilder.add("message", message);
        JsonObject object = objectBuilder.build();
        return object.toString();
    }
}
