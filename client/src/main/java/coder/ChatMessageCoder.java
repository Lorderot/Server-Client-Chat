package coder;

import protocol.AccessType;
import protocol.ChatMessage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;

public class ChatMessageCoder {
    public static ChatMessage decode(String json) {
        ChatMessage chatMessage = new ChatMessage();
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        String receiver = jsonObject.getString("receiver");
        String sender = jsonObject.getString("sender");
        String time = jsonObject.getString("time");
        chatMessage.setMessage(jsonObject.getString("message"));
        chatMessage.setReceiver(receiver);
        chatMessage.setSender(sender);
        chatMessage.setTime(time);
        chatMessage.setType(AccessType.valueOf(jsonObject.getString("type")));
        return chatMessage;
    }

    public static String encode(ChatMessage chatMessage) {
        String message = chatMessage.getMessage();
        String receiver = chatMessage.getReceiver();
        String sender = chatMessage.getSender();
        String time = chatMessage.getTime();
        if (receiver == null) {
            receiver = "";
        }
        if (sender == null) {
            sender = "";
        }
        if (time == null) {
            time = "";
        }
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder().add("message", message)
                .add("type", chatMessage.getType().toString())
                .add("receiver", receiver)
                .add("sender", sender)
                .add("time", time);
        JsonObject object = objectBuilder.build();
        return object.toString();
    }
}
