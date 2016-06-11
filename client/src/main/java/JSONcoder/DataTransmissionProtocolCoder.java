package JSONcoder;

import protocols.DataTransmissionProtocol;
import protocols.ProtocolType;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.StringReader;

public class DataTransmissionProtocolCoder {
    public static String encode(DataTransmissionProtocol protocol) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("type", protocol.getType().toString())
                .add("protocol", protocol.getProtocol())
                .add("secureWord", protocol.getSecureWord());
        return jsonBuilder.build().toString();
    }

    public static DataTransmissionProtocol decode(String json) {
        JsonObject jsonObject = Json.createReader(new StringReader(json))
                .readObject();
        DataTransmissionProtocol protocol = new DataTransmissionProtocol();
        protocol.setProtocol(jsonObject.getString("protocol"));
        protocol.setType(ProtocolType
                .valueOf(jsonObject.getString("type")));
        protocol.setSecureWord(jsonObject.getString("secureWord"));
        return protocol;
    }
}
