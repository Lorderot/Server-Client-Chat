package server;

import JSONcoder.MessageTransmissionProtocolCoder;
import JSONcoder.ParticipantsTransmissionProtocolCoder;
import controller.ChatMainWindowController;
import protocols.MessageTransmissionProtocol;
import protocols.ParticipantsTransmissionProtocol;
import protocols.ProtocolType;

import java.text.ParseException;

public class ProtocolHandler {
    private ChatMainWindowController controller;

    public ProtocolHandler(ChatMainWindowController controller) {
        this.controller = controller;
    }

    public void handle(String protocol, ProtocolType type)
            throws ParseException, NullPointerException {
        if (protocol == null) {
            throw new NullPointerException();
        }
        switch (type) {
            case FILE_TRANSMISSION: {
                break;
            }
            case SERVER_MESSAGE_TRANSMISSION: {
                MessageTransmissionProtocol message =
                        MessageTransmissionProtocolCoder
                                .decode(protocol);
                displayServerMessage(message.getMessage(), message.getTime());
                break;
            }
            case CLIENT_MESSAGE_TRANSMISSION: {
                MessageTransmissionProtocol message =
                        MessageTransmissionProtocolCoder
                                .decode(protocol);
                displayClientMessage(message);
                break;
            }
            case PARTICIPANTS_TRANSMISSION: {
                ParticipantsTransmissionProtocol participantsTransmission =
                        ParticipantsTransmissionProtocolCoder
                                .decode(protocol);
                controller.setParticipants(
                        participantsTransmission.getParticipants());
            }
        }
    }

    private void displayClientMessage(
            MessageTransmissionProtocol message) {
        controller.displayClientMessage(message.getMessage(),
                message.getSender(), message.getTime(), message.getType());
    }

    private void displayServerMessage(
            String message, String time) {
        controller.displayServerMessage(message, time);
    }
}
