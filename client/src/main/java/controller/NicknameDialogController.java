package controller;

import JSONcoder.DataTransmissionProtocolCoder;
import JSONcoder.NicknameTransmissionProtocolCoder;
import JSONcoder.SecureWordTransmissionProtocolCoder;
import app.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import protocols.DataTransmissionProtocol;
import protocols.NicknameTransmissionProtocol;
import protocols.SecureWordTransmissionProtocol;
import server.Server;

import javax.json.JsonException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class NicknameDialogController {
    private Server server;
    private String nickName;
    private Stage stage;
    private BufferedReader serverReader;
    private OutputStreamWriter serverWriter;

    @FXML
    private TextField nicknameField;
    @FXML
    private TextField ipField;

    @FXML
    public void initialize() {
        nicknameField.setEditable(true);
        ipField.setText(Server.defaultAddress);
    }

    public void handleEnter() {
        String name = nicknameField.getText();
        String ip = ipField.getText();
        if (ip != null) {
            server = new Server(ip);
        } else {
            server = new Server();
        }
        try {
            connect(name);
        } catch (Exception e) {
            MainApp.showAlert(Alert.AlertType.ERROR, e.getMessage());
            server.close();
            return;
        }
        nickName = nicknameField.getText();
        stage.close();
    }

    public void handleExit() {
        stage.close();
    }

    public void connect(String nickName) throws Exception{
        server.connect();
        serverReader = server.getReader();
        serverWriter = server.getWriter();
        boolean nicknameAccepting = false;
        getSecureWord();
        NicknameTransmissionProtocol protocol;
        while (!nicknameAccepting) {
            sendNickname(nickName);
            protocol = getNickNameResponse();
            nicknameAccepting = protocol.getServerRespond();
            if (!nicknameAccepting) {
                MainApp.showAlert(Alert.AlertType.ERROR,
                        protocol.getRestriction());
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleEnter();
            }
            if (event.getCode() == KeyCode.ESCAPE) {
                handleExit();
            }
        });
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getNickName() {
        return nickName;
    }

    private void getSecureWord() throws IOException {
        while (true) {
            try {
                String jsonProtocol = serverReader.readLine();
                DataTransmissionProtocol dataTransmissionProtocol
                        = DataTransmissionProtocolCoder.decode(jsonProtocol);
                if (!dataTransmissionProtocol.getSecureWord()
                        .equals(server.getSecureWord())) {
                    continue;
                }
                SecureWordTransmissionProtocol protocol =
                        SecureWordTransmissionProtocolCoder
                                .decode(dataTransmissionProtocol.getProtocol());
                server.setSecureWord(protocol.getSecureWord());
                return;
            } catch (JsonException e) {
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
        String json =
                NicknameTransmissionProtocolCoder.encode(nicknameTransmissionProtocol);
        dataTransmissionProtocol.setProtocol(json);
        dataTransmissionProtocol.setSecureWord(server.getSecureWord());
        String jsonProtocol = DataTransmissionProtocolCoder
                .encode(dataTransmissionProtocol);
        serverWriter.write(jsonProtocol + "\n");
        serverWriter.flush();
    }
    private NicknameTransmissionProtocol getNickNameResponse() throws IOException {
        while (true) {
            try {
                String jsonProtocol = serverReader.readLine();
                DataTransmissionProtocol dataTransmissionProtocol
                        = DataTransmissionProtocolCoder.decode(jsonProtocol);
                if (!dataTransmissionProtocol.getSecureWord()
                        .equals(server.getSecureWord())) {
                    continue;
                }
                NicknameTransmissionProtocol protocol =
                        NicknameTransmissionProtocolCoder
                                .decode(dataTransmissionProtocol.getProtocol());
                return protocol;
            } catch (JsonException e) {
                e.printStackTrace();
            }
        }
    }
}
