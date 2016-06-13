package server;

import JSONcoder.*;
import protocols.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ChatHandler {
    private String nickname;
    private ConnectionHandler connectionHandler;
    private List<String> participants;
    private OutputStreamWriter serverWriter;
    private BufferedReader serverReader;
    private final long timeToAwait = 5;

    public ChatHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
        this.connectionHandler = connectionHandler;
    }

    public void loadConfigurations(String nickName) throws Exception{
        connectionHandler.connect();
        serverReader = connectionHandler.getReader();
        serverWriter = connectionHandler.getWriter();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> response = executorService.submit(() -> {
            try {
                getSecureWord();
                verifyNickname(nickName);
                loadParticipants();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return  e.getMessage();
            } finally {
                executorService.shutdown();
            }
        });
        executorService.awaitTermination(timeToAwait, TimeUnit.SECONDS);
        if (!response.isDone()) {
            throw new Exception("Waiting time for connectionHandler" +
                    " response has gone! Please, try again!");
        } else {
            String exception = response.get();
            if (exception != null) {
                throw new Exception(exception);
            }
        }
    }

    public void send(String message, String time, String receiver)
            throws Exception {
        DataTransmissionProtocol dataTransmissionProtocol =
                new DataTransmissionProtocol();
        dataTransmissionProtocol.setType(ProtocolType.CLIENT_MESSAGE_TRANSMISSION);
        dataTransmissionProtocol.setSecureWord(connectionHandler.getSecureWord());
        MessageTransmissionProtocol messageTransmissionProtocol
                = new MessageTransmissionProtocol();
        messageTransmissionProtocol.setSender(nickname);
        messageTransmissionProtocol.setTime(time);
        messageTransmissionProtocol.setMessage(message);
        if (receiver == null) {
            messageTransmissionProtocol.setType(AccessType.PUBLIC);
        } else {
            messageTransmissionProtocol.setType(AccessType.PRIVATE);
            messageTransmissionProtocol.setReceiver(receiver);
        }
        String json = MessageTransmissionProtocolCoder
                .encode(messageTransmissionProtocol);
        dataTransmissionProtocol.setProtocol(json);
        if (connectionHandler.isConnected()) {
            serverWriter.write(DataTransmissionProtocolCoder
                    .encode(dataTransmissionProtocol) + "\n");
            serverWriter.flush();
        } else {
            throw new Exception("Server is disconnected!");
        }
    }

    public String getNickname() {
        return nickname;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    private void getSecureWord() throws IOException {
        while (true) {
            try {
                String jsonProtocol = serverReader.readLine();
                if (jsonProtocol == null) {
                    continue;
                }
                DataTransmissionProtocol dataTransmissionProtocol
                        = DataTransmissionProtocolCoder.decode(jsonProtocol);
                SecureWordTransmissionProtocol protocol =
                        SecureWordTransmissionProtocolCoder
                                .decode(dataTransmissionProtocol.getProtocol());
                connectionHandler.setSecureWord(protocol.getSecureWord());
                return;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNickname(String nickName) throws IOException {
        DataTransmissionProtocol dataTransmissionProtocol
                = new DataTransmissionProtocol();
        NicknameTransmissionProtocol nicknameTransmissionProtocol
                = new NicknameTransmissionProtocol();
        nicknameTransmissionProtocol.setNickNameRequest(nickName);
        String json = NicknameTransmissionProtocolCoder
                .encode(nicknameTransmissionProtocol);
        dataTransmissionProtocol.setProtocol(json);
        dataTransmissionProtocol.setType(ProtocolType.NICKNAME_TRANSMISSION);
        dataTransmissionProtocol.setSecureWord(connectionHandler.getSecureWord());
        String jsonProtocol = DataTransmissionProtocolCoder
                .encode(dataTransmissionProtocol);
        serverWriter.write(jsonProtocol + "\n");
        serverWriter.flush();
    }

    private void verifyNickname(String nickName) throws Exception {
        boolean nicknameAccepting = false;
        NicknameTransmissionProtocol protocol;
        while (!nicknameAccepting) {
            sendNickname(nickName);
            protocol = getNickNameResponse();
            nicknameAccepting = protocol.getServerResponse();
            if (!nicknameAccepting) {
                String restriction = protocol.getRestriction();
                if (restriction != null) {
                    throw new Exception(protocol.getRestriction());
                } else {
                    throw new Exception("Server doesn't accept such nickname!");
                }
            }
        }
        this.nickname = nickName;
    }

    private void loadParticipants() throws IOException {
        while (true) {
            try {
                String json = serverReader.readLine();
                DataTransmissionProtocol dataTransmissionProtocol =
                        DataTransmissionProtocolCoder.decode(json);
                if (!dataTransmissionProtocol.getSecureWord()
                        .equals(connectionHandler.getSecureWord())) {
                    continue;
                }
                ParticipantsTransmissionProtocol participantProtocol =
                        ParticipantsTransmissionProtocolCoder
                                .decode(dataTransmissionProtocol.getProtocol());
                participants = participantProtocol.getParticipants();
                participants.remove(nickname);
                break;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private NicknameTransmissionProtocol getNickNameResponse()
            throws IOException {
        while (true) {
            try {
                String jsonProtocol = serverReader.readLine();
                if (jsonProtocol == null) {
                    continue;
                }
                DataTransmissionProtocol dataTransmissionProtocol
                        = DataTransmissionProtocolCoder.decode(jsonProtocol);
                if (!dataTransmissionProtocol.getSecureWord()
                        .equals(connectionHandler.getSecureWord())) {
                    continue;
                }
                return NicknameTransmissionProtocolCoder
                        .decode(dataTransmissionProtocol.getProtocol());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
