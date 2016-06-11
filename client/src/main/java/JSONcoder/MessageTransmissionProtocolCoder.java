package JSONcoder;

import protocols.AccessType;
import protocols.MessageTransmissionProtocol;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;

public class MessageTransmissionProtocolCoder {
    public static MessageTransmissionProtocol decode(String json) {
        MessageTransmissionProtocol protocol =
                new MessageTransmissionProtocol();
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        String receiver = jsonObject.getString("receiver");
        String sender = jsonObject.getString("sender");
        String time = jsonObject.getString("time");
        protocol.setMessage(jsonObject.getString("message"));
        protocol.setReceiver(receiver);
        protocol.setSender(sender);
        protocol.setTime(time);
        protocol.setType(AccessType.valueOf(jsonObject.getString("type")));
        return protocol;
    }

    public static String encode(MessageTransmissionProtocol protocol) {
        String message = protocol.getMessage();
        String receiver = protocol.getReceiver();
        String sender = protocol.getSender();
        String time = protocol.getTime();
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
                .add("message", message)
                .add("type", protocol.getType().toString())
                .add("receiver", receiver)
                .add("sender", sender);
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
        objectBuilder.add("message", message);
        JsonObject object = objectBuilder.build();
        return object.toString();
    }
}
