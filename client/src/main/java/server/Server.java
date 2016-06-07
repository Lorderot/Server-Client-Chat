package server;

import java.io.*;
import java.net.Socket;

public class Server {
    private final int portNumber;
    private final String serverAddress;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public Server() {
        portNumber = 2020;
        serverAddress = "192.168.1.138";
    }

    public void connect() throws IOException {
        socket = new Socket(serverAddress, portNumber);
        reader = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream());
    }

    public boolean isConnected() {
        return !socket.isClosed();
    }

    public BufferedReader getReader() throws IOException {
        return reader;
    }

    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    public void close() {
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
