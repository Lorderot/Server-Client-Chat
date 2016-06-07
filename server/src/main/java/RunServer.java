import coder.ChatMessageCoder;
import coder.MessageCoder;
import coder.ServerMessageCoder;
import protocol.*;
import util.DateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class RunServer {
    private static final int portNumber = 2020;
    private static final int maxClients = 5;
    private static final int minLength = 3;
    private static final int maxLength = 20;
    private static Hashtable<String, Client> clients = new Hashtable<>();
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

    public static MessagePacket informClientOffline(String nickname) {
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessageType.SERVER);
        ServerMessage serverMessage = new ServerMessage();
        serverMessage.setStatus(ClientStatus.OFFLINE);
        serverMessage.setTime(DateUtil.toString(new Date()));
        serverMessage.setNickname(nickname);
        messagePacket.setMessage(ServerMessageCoder.encode(serverMessage));
        return messagePacket;
    }

    public static MessagePacket informClientOnline(String nickname) {
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessageType.SERVER);
        ServerMessage serverMessage = new ServerMessage();
        serverMessage.setStatus(ClientStatus.ONLINE);
        serverMessage.setTime(DateUtil.toString(new Date()));
        serverMessage.setNickname(nickname);
        messagePacket.setMessage(ServerMessageCoder.encode(serverMessage));
        return messagePacket;
    }

    private static void processClient(Socket socket) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            if (clients.size() > maxClients) {
                out.println("Server is too busy. Please, try later");
                out.close();
                socket.close();
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            String nickname = reader.readLine();
            if (clients.containsKey(nickname)) {
                out.println("Such nickname exists");
                out.close();
                reader.close();
                socket.close();
                return;
            }
            if (nickname.length() < minLength) {
                out.println("Nickname is too short!");
                out.close();
                reader.close();
                socket.close();
                return;
            }
            if (nickname.length() > maxLength) {
                out.println("Nickname is too long!");
                out.close();
                reader.close();
                socket.close();
                return;
            }
            out.println("ok");
            out.flush();
            String listOfParticipants = "";
            for (Map.Entry<String, Client> entry :  clients.entrySet()) {
                listOfParticipants += entry.getKey() + ",";
            }
            if (!listOfParticipants.equals("")) {
                listOfParticipants = listOfParticipants.substring(0,
                        listOfParticipants.length() - 1);
            }
            out.println(listOfParticipants);
            out.flush();
            MessagePacket serverMessage = informClientOnline(nickname);
            new Thread(() -> {
                clients.entrySet().forEach(entry -> {
                    if (!entry.getKey().equals(nickname)) {
                        entry.getValue().send(MessageCoder.encode(serverMessage));
                    }
                });
            }).start();
            Client client = new Client(nickname, socket, out, reader, clients);
            clients.put(nickname, client);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Client extends Thread {
    private Socket socket;
    private String nickname;
    private Hashtable<String, Client> clients;
    private PrintWriter writer;
    private BufferedReader reader;

    public Client(String nickname, Socket socket, PrintWriter writer,
                  BufferedReader reader, Hashtable<String, Client> clients) {
        this.socket = socket;
        this.clients = clients;
        this.nickname = nickname;
        this.writer = writer;
        this.reader = reader;
    }

    @Override
    public void run() {
        try {
            String jsonMessage;
            while ((jsonMessage = reader.readLine()) != null) {
                MessagePacket messagePacket = MessageCoder.decode(jsonMessage);
                switch (messagePacket.getType()) {
                    case RECEIVE_OBJECT: {
                        break;
                    }
                    case CLIENT: {
                        ChatMessage chatMessage = ChatMessageCoder
                                .decode(messagePacket.getMessage());
                        switch (chatMessage.getType()) {
                            case PUBLIC: {
                                for (Map.Entry<String, Client> entry : clients.entrySet()) {
                                    Client participant = entry.getValue();
                                    if (!participant.equals(this)) {
                                        participant.send(jsonMessage);
                                    }
                                }
                                break;
                            }
                            case PRIVATE: {
                                clients.get(chatMessage.getReceiver()).send(jsonMessage);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clients.remove(nickname);
            for (Map.Entry<String, Client> entry : clients.entrySet()) {
                Client participant = entry.getValue();
                if (!participant.equals(this)) {
                    participant.send(MessageCoder
                            .encode(RunServer.informClientOffline(nickname)));
                }
            }
            writer.close();
            try {
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

    public synchronized void send(String message) {
        writer.println(message);
        writer.flush();
    }
}
