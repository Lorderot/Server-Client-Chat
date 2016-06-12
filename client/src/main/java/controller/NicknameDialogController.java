package controller;

import app.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import server.ChatHandler;
import server.ConnectionHandler;

public class NicknameDialogController {
    private ConnectionHandler connectionHandler;
    private ChatHandler chatHandler;
    private String nickName;
    private Stage stage;

    @FXML
    private TextField nicknameField;
    @FXML
    private TextField ipField;

    @FXML
    public void initialize() {
        nicknameField.setEditable(true);
        ipField.setText(ConnectionHandler.defaultAddress);
    }

    public void handleEnter() {
        String name = nicknameField.getText();
        String ip = ipField.getText();
        if (ip != null) {
            connectionHandler = new ConnectionHandler(ip);
        } else {
            connectionHandler = new ConnectionHandler();
        }
        chatHandler = new ChatHandler(connectionHandler);
        try {
            chatHandler.loadConfigurations(name);
        } catch (Exception e) {
            MainApp.showAlert(Alert.AlertType.ERROR, e.getMessage());
            connectionHandler.closeConnection();
            return;
        }
        nickName = nicknameField.getText();
        stage.close();
    }

    public void handleExit() {
        stage.close();
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

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public String getNickName() {
        return nickName;
    }

    public ChatHandler getChatHandler() {
        return chatHandler;
    }
}
