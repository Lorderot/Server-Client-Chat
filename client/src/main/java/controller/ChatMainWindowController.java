package controller;

import JSONcoder.DataTransmissionProtocolCoder;
import app.MainApp;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import protocols.AccessType;
import protocols.DataTransmissionProtocol;
import server.ChatHandler;
import server.ConnectionHandler;
import server.ProtocolHandler;
import util.DateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class ChatMainWindowController {
    private MainApp mainApp;
    private ConnectionHandler connectionHandler;
    private ChatHandler chatHandler;
    private ProtocolHandler protocolHandler;
    private String nickname;
    private Stage stage;
    private List<String> participants;
    private volatile boolean quit = false;
    @FXML
    private TableView<String> participantTableView;
    @FXML
    private TableColumn<String, String> nameColumn;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private TextArea messageTextArea;

    public ChatMainWindowController() {
        connectionHandler = new ConnectionHandler();
        protocolHandler = new ProtocolHandler(this);
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue()));
        chatTextArea.setWrapText(true);
        chatTextArea.setEditable(false);
        messageTextArea.setEditable(true);
        messageTextArea.setWrapText(true);
    }

    public void handleSend() {
        String message = messageTextArea.getText();
        if (message.length() == 0) {
            return;
        }
        String receiver = participantTableView.getSelectionModel()
                .getSelectedItem();
        String time = DateUtil.toString(new Date());
        try {
            chatHandler.send(message, time, receiver);
            displayOwnMessage(message, time, receiver);
            messageTextArea.clear();
        } catch (Exception e) {
            e.printStackTrace();
            MainApp.showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    public void reconnect() {
        connectionHandler.closeConnection();
        nickname = mainApp.showNicknameDialog(stage);
        if (nickname == null) {
            return;
        }
        setConnectionHandler(mainApp.getConnectionHandler());
        setChatHandler(mainApp.getChatHandler());
    }

    public void handleExit() {
        connectionHandler.closeConnection();
        quit = true;
        stage.close();
    }

    public void displayClientMessage(String message, String sender,
                                     String time, AccessType type) {
        chatTextArea.appendText("\n" + time + "\n");
        chatTextArea.appendText(sender);
        switch (type) {
            case PRIVATE: {
                chatTextArea.appendText(" whispers you: ");
                break;
            }
            case PUBLIC: {
                chatTextArea.appendText(": ");
            }
        }
        chatTextArea.appendText(message);
    }

    public void displayServerMessage(String message, String time) {
        chatTextArea.appendText("\n" + time + "\n");
        chatTextArea.appendText("Server: " + message);
    }

    public void setChatHandler(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
        participants = chatHandler.getParticipants();
        refreshTable();
    }

    public void setParticipants(List<String> participants) {
        participants.remove(nickname);
        chatHandler.setParticipants(participants);
        this.participants = participants;
        refreshTable();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
        runReadingThread();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(event -> {
            handleExit();
        });
        stage.getScene().setOnKeyPressed(event -> {
            if (new KeyCodeCombination(KeyCode.ENTER,
                    KeyCombination.CONTROL_DOWN).match(event)) {
                handleSend();
            }
            if (event.getCode() == KeyCode.ESCAPE) {
                participantTableView.getSelectionModel().clearSelection();
            }
        });
    }

    private void displayOwnMessage(String message, String time, String receiver) {
        chatTextArea.appendText("\n" + time + "\n");
        if (receiver != null) {
            chatTextArea.appendText("You whisper to " + receiver + ": ");
        } else {
            chatTextArea.appendText("You: ");
        }
        chatTextArea.appendText(message);
    }

    private void refreshTable() {
        participantTableView.setItems(null);
        participantTableView.setItems(FXCollections
                .observableArrayList(participants));
    }

    private void runReadingThread() {
        new Thread(() -> {
            String input;
            BufferedReader serverReader = connectionHandler.getReader();
            try {
                while ((input = serverReader.readLine()) != null) {
                    try {
                        DataTransmissionProtocol dataTransmissionProtocol
                                = DataTransmissionProtocolCoder.decode(input);
                        if (!connectionHandler.getSecureWord().equals(
                                dataTransmissionProtocol.getSecureWord())) {
                            continue;
                        }
                        protocolHandler.handle(
                                dataTransmissionProtocol.getProtocol(),
                                dataTransmissionProtocol.getType());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.err.println("Thread has been fallen: " + e.getMessage());
            } finally {
                System.err.println("Thread has finished work!");
                connectionHandler.closeConnection();
                if (!quit) {
                    Platform.runLater(() -> {
                        MainApp.showAlert(Alert.AlertType.ERROR,
                                "Disconnected from server! Please, try to reconnect!");
                    });
                }
            }
        }).start();
    }
}
