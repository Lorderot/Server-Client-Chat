package coder;

import protocol.ClientStatus;
import protocol.ServerMessage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;

public class ServerMessageCoder {
    public static String encode(ServerMessage serverMessage) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        String nickname = serverMessage.getNickname();
        String time = serverMessage.getTime();
        ClientStatus status = serverMessage.getStatus();
        if (nickname != null) {
            jsonBuilder.add("nickname", serverMessage.getNickname());
        } else {
            jsonBuilder.addNull("nickname");
        }
        if (time != null) {
            jsonBuilder.add("time", time);
        } else {
            jsonBuilder.addNull("time");
        }
        if (status != null) {
            jsonBuilder.add("status", status.toString());
        } else {
            jsonBuilder.addNull("status");
        }
        JsonObject json = jsonBuilder.build();
        return json.toString();
    }

    public static ServerMessage decode(String json) {
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        ServerMessage serverMessage = new ServerMessage();
        serverMessage.setNickname(jsonObject.getString("nickname"));
        serverMessage.setStatus(ClientStatus
                .valueOf(jsonObject.getString("status")));
        serverMessage.setTime(jsonObject.getString("time"));
        return serverMessage;
    }
}
