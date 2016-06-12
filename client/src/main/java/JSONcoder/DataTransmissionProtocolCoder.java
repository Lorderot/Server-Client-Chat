package JSONcoder;

import protocols.DataTransmissionProtocol;
import protocols.ProtocolType;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;
import java.text.ParseException;

public class DataTransmissionProtocolCoder {
    public static String encode(DataTransmissionProtocol protocol) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("type", protocol.getType().toString())
                .add("protocol", protocol.getProtocol());
        String secureWord = protocol.getSecureWord();
        if (secureWord == null) {
            jsonBuilder.addNull("secureWord");
        } else {
            jsonBuilder.add("secureWord", protocol.getSecureWord());
        }
        return jsonBuilder.build().toString();
    }

    public static DataTransmissionProtocol decode(String json) throws ParseException {
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        DataTransmissionProtocol protocol = new DataTransmissionProtocol();
        protocol.setProtocol(jsonObject.getString("protocol"));
        protocol.setType(ProtocolType
                .valueOf(jsonObject.getString("type")));
        if (jsonObject.isNull("secureWord")) {
            protocol.setSecureWord(null);
        } else {
            protocol.setSecureWord(jsonObject.getString("secureWord"));
        }
        return protocol;
    }
}
