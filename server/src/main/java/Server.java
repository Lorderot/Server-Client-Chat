import JSONcoder.DataTransmissionProtocolCoder;
import JSONcoder.MessageTransmissionProtocolCoder;
import JSONcoder.NicknameTransmissionProtocolCoder;
import protocols.DataTransmissionProtocol;
import protocols.MessageTransmissionProtocol;
import protocols.NicknameTransmissionProtocol;
import protocols.ProtocolType;
import util.DateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

public class Server {
    private static final int portNumber = 10000;
    private static final int maxClients = 5;
    private static final int minNicknameLength = 3;
    private static final int maxNicknameLength = 20;
    private static final int minSecureWordLength = 5;
    private static final int maxSecureWordLength = 20;
    private static final Hashtable<String, Client> clients = new Hashtable<>();
    private static Random generator = new Random();
    public static void main(String[] args) {
        ServerSocket server;
        try {
            server = new ServerSocket(portNumber);
            System.err.println("Server has been started!");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        while (true) {
            try {
                Socket socket = server.accept();
                new Thread(() -> processClient(socket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String generateSecureWord() {
        String secureWord = "";
        int length = generator.nextInt(maxSecureWordLength - minSecureWordLength)
                + minSecureWordLength;
        for (int i = 0; i < length; i++) {
            secureWord += generator.nextInt(10);
        }
        return secureWord;
    }

    public static String checkNickname(String nickname) {
        String restrictionMessage = null;
        if (clients.containsKey(nickname)) {
            restrictionMessage = "Such nickname exists";
        }
        if (nickname.length() < minNicknameLength) {
            restrictionMessage = "Nickname is too short!";
        }
        if (nickname.length() > maxNicknameLength) {
            restrictionMessage = "Nickname is too long!";
        }
        if (!nickname.matches("\\w+")) {
            restrictionMessage = "Please, use only alphabet symbols and digits!";
        }
        if (nickname.matches("\\d+")) {
            restrictionMessage = "Please, don't use only digits!";
        }
        return restrictionMessage;
    }

    public static void sendParticipantList() {
        String participantProtocol = ProtocolCreator
                .createParticipantsProtocol(clients);
        clients.entrySet().forEach(entry -> {
            Client client = entry.getValue();
            String wrapped = ProtocolCreator.wrapProtocol(participantProtocol,
                    ProtocolType.PARTICIPANTS_TRANSMISSION, client.getSecureWord());
            client.send(wrapped);
        });
    }

    public static void sendServerMessageToAll(String message, String time) {
        String messageProtocol = ProtocolCreator
                .createServerMessage(message, time);
        clients.entrySet().forEach(entry -> {
            Client client = entry.getValue();
            String wrapped = ProtocolCreator.wrapProtocol(messageProtocol,
                    ProtocolType.SERVER_MESSAGE_TRANSMISSION, client.getSecureWord());
            client.send(wrapped);
        });
    }

    public static void sendClientMessageToAll(
            MessageTransmissionProtocol messageProtocol) {
        String message = MessageTransmissionProtocolCoder
                .encode(messageProtocol);
        clients.entrySet().forEach(entry -> {
            if (!entry.getKey().equals(messageProtocol.getSender())) {
                Client client = entry.getValue();
                String wrapped = ProtocolCreator.wrapProtocol(message, ProtocolType
                        .CLIENT_MESSAGE_TRANSMISSION, client.getSecureWord());
                client.send(wrapped);
            }
        });
    }

    public static void removeClient(String nickname) {
        clients.remove(nickname);
    }

    public static Client getClient(String nickname) {
        return clients.get(nickname);
    }

    private static String handleNickNameRequest(
            OutputStreamWriter out, BufferedReader reader, String secureWord)
            throws IOException {
        while (true) {
            try {
                String nicknameRequest = reader.readLine();
                DataTransmissionProtocol protocol =
                        DataTransmissionProtocolCoder.decode(nicknameRequest);
                if (!protocol.getSecureWord().equals(secureWord)) {
                    continue;
                }
                NicknameTransmissionProtocol nicknameProtocol =
                        NicknameTransmissionProtocolCoder
                                .decode(protocol.getProtocol());
                String nickname = nicknameProtocol.getNickNameRequest();
                String restrictionMessage =
                        checkNickname(nickname);
                boolean response = true;
                if (restrictionMessage == null) {
                    response = false;
                }
                String nicknameResponse = ProtocolCreator.createNicknameProtocol(
                        restrictionMessage, response);
                String wrappedProtocol = ProtocolCreator
                        .wrapProtocol(nicknameResponse,
                                ProtocolType.NICKNAME_TRANSMISSION, secureWord);
                out.write(wrappedProtocol + "\n");
                out.flush();
                if (response) {
                    return nickname;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static void processClient(Socket socket) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(
                    socket.getOutputStream(), Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), Charset.forName("UTF-8")));
            String secureWord = generateSecureWord();
            String secureWordProtocol = ProtocolCreator
                    .createSecureWordProtocol(secureWord);
            String wrappedProtocol = ProtocolCreator
                    .wrapProtocol(secureWordProtocol,
                            ProtocolType.SECURE_WORD_TRANSMISSION, null);
            out.write(wrappedProtocol + "\n");
            out.flush();
            String nickname = handleNickNameRequest(out, reader, secureWord);
            Client newClient = new Client(nickname, secureWord, socket, out, reader);
            clients.put(nickname, newClient);
            sendParticipantList();
            sendServerMessageToAll("Welcome, " + nickname + ", to chat!",
                    DateUtil.toString(new Date()));
            newClient.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Client extends Thread {
    private Socket socket;
    private String nickname;
    private String secureWord;
    private OutputStreamWriter writer;
    private BufferedReader reader;

    public Client(String nickname, String secureWord, Socket socket,
                  OutputStreamWriter writer, BufferedReader reader) {
        this.socket = socket;
        this.nickname = nickname;
        this.writer = writer;
        this.reader = reader;
        this.secureWord = secureWord;
    }

    @Override
    public void run() {
        String jsonMessage;
        try {
            while (true) {
                jsonMessage = reader.readLine();
                if (jsonMessage == null) {
                    continue;
                }
                try {
                    DataTransmissionProtocol protocol = DataTransmissionProtocolCoder
                            .decode(jsonMessage);
                    if (!secureWord.equals(protocol.getSecureWord())) {
                        continue;
                    }
                    switch (protocol.getType()) {
                        case CLIENT_MESSAGE_TRANSMISSION: {
                            MessageTransmissionProtocol messageProtocol =
                                    MessageTransmissionProtocolCoder
                                    .decode(protocol.getProtocol());
                            switch (messageProtocol.getType()) {
                                case PRIVATE: {
                                    Client receiver = Server.getClient(
                                            messageProtocol.getReceiver());
                                    receiver.send(jsonMessage);

                                }
                                case PUBLIC: {
                                    Server.sendClientMessageToAll(messageProtocol);
                                }
                            }
                            break;
                        }
                        case FILE_TRANSMISSION: {
                            break;
                        }
                        case NICKNAME_TRANSMISSION: {
                            break;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Server.removeClient(nickname);
            Server.sendParticipantList();
            Server.sendServerMessageToAll("Goodbye, " + nickname + "!",
                    DateUtil.toString(new Date()));
            try {
                writer.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void send(String dataTransmissionProtocol) {
        try {
            if (!socket.isOutputShutdown()) {
                writer.write(dataTransmissionProtocol + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSecureWord() {
        return secureWord;
    }
}
