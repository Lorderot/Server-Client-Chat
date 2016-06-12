import JSONcoder.*;
import protocols.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ProtocolCreator {
    public static String createServerMessage(String message, String time) {
        MessageTransmissionProtocol messageProtocol =
                new MessageTransmissionProtocol();
        messageProtocol.setMessage(message);
        messageProtocol.setTime(time);
        return MessageTransmissionProtocolCoder
                .encode(messageProtocol);
    }

    public static String createSecureWordProtocol(String newSecureWord) {
        SecureWordTransmissionProtocol secureWordProtocol =
                new SecureWordTransmissionProtocol();
        secureWordProtocol.setSecureWord(newSecureWord);
        return SecureWordTransmissionProtocolCoder
                .encode(secureWordProtocol);
    }

    public static String createNicknameProtocol(
            String restrictionMessage, boolean response) {
        NicknameTransmissionProtocol nicknameTransmissionProtocol
                = new NicknameTransmissionProtocol();
        nicknameTransmissionProtocol.setServerResponse(response);
        nicknameTransmissionProtocol.setRestriction(restrictionMessage);
        return NicknameTransmissionProtocolCoder
                .encode(nicknameTransmissionProtocol);
    }

    public static String createParticipantsProtocol(
            Hashtable<String, Client> clients) {
        ParticipantsTransmissionProtocol participantsTransmissionProtocol =
                new ParticipantsTransmissionProtocol();
        List<String> participants = new ArrayList<>();
        clients.keySet().forEach(participants::add);
        participantsTransmissionProtocol.setParticipants(participants);
        return ParticipantsTransmissionProtocolCoder
                .encode(participantsTransmissionProtocol);
    }

    public static String wrapProtocol(
            String protocol, ProtocolType type, String secureWord) {
        DataTransmissionProtocol transmissionProtocol =
                new DataTransmissionProtocol();
        transmissionProtocol.setProtocol(protocol);
        transmissionProtocol.setType(type);
        transmissionProtocol.setSecureWord(secureWord);
        return DataTransmissionProtocolCoder.encode(transmissionProtocol);
    }
}
