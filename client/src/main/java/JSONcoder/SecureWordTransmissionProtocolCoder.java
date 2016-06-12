package JSONcoder;

import protocols.SecureWordTransmissionProtocol;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;
import java.text.ParseException;

public class SecureWordTransmissionProtocolCoder {
    public static SecureWordTransmissionProtocol decode(String json)
            throws ParseException {
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        SecureWordTransmissionProtocol protocol =
                new SecureWordTransmissionProtocol();
        if (!jsonObject.isNull("secureWord")) {
            protocol.setSecureWord(jsonObject.getString("secureWord"));
        }
        return protocol;
    }

    public static String encode(SecureWordTransmissionProtocol protocol) {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        String secureWord = protocol.getSecureWord();
        if (secureWord == null) {
            objectBuilder.addNull("secureWord");
        } else {
            objectBuilder.add("secureWord", protocol.getSecureWord());
        }
        JsonObject jsonObject = objectBuilder.build();
        return jsonObject.toString();
    }
}
