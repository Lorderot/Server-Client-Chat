package JSONcoder;

import protocols.SecureWordTransmissionProtocol;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;

public class SecureWordTransmissionProtocolCoder {
    public static SecureWordTransmissionProtocol decode(String json) {
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        SecureWordTransmissionProtocol protocol =
                new SecureWordTransmissionProtocol();
        protocol.setSecureWord(jsonObject.getString("secureWord"));
        return protocol;
    }

    public static String encode(SecureWordTransmissionProtocol protocol) {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        objectBuilder.add("secureWord", protocol.getSecureWord());
        JsonObject jsonObject = objectBuilder.build();
        return jsonObject.toString();
    }
}
