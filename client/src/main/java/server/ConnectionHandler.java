package server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class ConnectionHandler {
    public static final String defaultAddress = "77.47.204.59";
    private final int portNumber = 10000;
    private String serverAddress = defaultAddress;
    private Socket socket;
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private String secureWord;

    public ConnectionHandler() {
    }

    public ConnectionHandler(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void connect() throws IOException {
        socket = new Socket(serverAddress, portNumber);
        reader = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), Charset.forName("UTF-8")));
        writer = new OutputStreamWriter(socket.getOutputStream(),
                Charset.forName("UTF-8"));
    }

    public boolean isConnected() {
        return !socket.isClosed();
    }

    public BufferedReader getReader() {
        return reader;
    }

    public OutputStreamWriter getWriter() {
        return writer;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void closeConnection() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public String getSecureWord() {
        return secureWord;
    }

    public void setSecureWord(String secureWord) {
        this.secureWord = secureWord;
    }
}
