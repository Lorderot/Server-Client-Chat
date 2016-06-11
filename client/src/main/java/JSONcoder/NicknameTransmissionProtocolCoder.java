package JSONcoder;

import protocols.NicknameTransmissionProtocol;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;

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
        jsonBuilder.add("serverRespond", protocol.getServerRespond());
        return jsonBuilder.build().toString();
    }

    public static NicknameTransmissionProtocol decode(String json) {
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        NicknameTransmissionProtocol protocol = new NicknameTransmissionProtocol();
        protocol.setNickNameRequest(jsonObject.getString("nickNameRequest"));
        protocol.setServerRespond(jsonObject.getBoolean("serverRespond"));
        protocol.setRestriction(jsonObject.getString("restriction"));
        return protocol;
    }
}
