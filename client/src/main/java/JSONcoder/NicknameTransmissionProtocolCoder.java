package JSONcoder;

import protocols.NicknameTransmissionProtocol;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;
import java.text.ParseException;

public class NicknameTransmissionProtocolCoder {
    public static String encode(NicknameTransmissionProtocol protocol) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        String nickname = protocol.getNickNameRequest();
        String restriction = protocol.getRestriction();
        if (nickname == null) {
            jsonBuilder.addNull("nickNameRequest");
        } else {
            jsonBuilder.add("nickNameRequest", protocol.getNickNameRequest());
        }
        if (restriction == null) {
            jsonBuilder.addNull("restriction");
        } else {
            jsonBuilder.add("restriction", restriction);
        }
        jsonBuilder.add("serverResponse", protocol.getServerResponse());
        return jsonBuilder.build().toString();
    }

    public static NicknameTransmissionProtocol decode(String json)
            throws ParseException {
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        NicknameTransmissionProtocol protocol = new NicknameTransmissionProtocol();
        if (!jsonObject.isNull("nickNameRequest")) {
            protocol.setNickNameRequest(jsonObject.getString("nickNameRequest"));
        }
        if (!jsonObject.isNull("serverResponse")) {
            protocol.setServerResponse(jsonObject.getBoolean("serverResponse"));
        }
        if (!jsonObject.isNull("restriction")) {
            protocol.setRestriction(jsonObject.getString("restriction"));
        }
        return protocol;
    }
}
