package controller;

import app.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import server.Server;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class NicknameDialogController {
    private Server server;
    private String nickName;
    private Stage stage;

    @FXML
    private TextField nicknameField;

    @FXML
    public void initialize() {
        nicknameField.setEditable(true);
        nicknameField.setText("Kolia");
    }

    public void handleEnter() {
        String name = nicknameField.getText();
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
        BufferedReader serverReader = server.getReader();
        PrintWriter serverWriter = server.getWriter();
        serverWriter.println(nickName);
        serverWriter.flush();
        String answer = serverReader.readLine();
        if (!answer.equals("ok")) {
            throw new Exception(answer);
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

    public void setServer(Server server) {
        this.server = server;
    }

    public String getNickName() {
        return nickName;
    }
}
