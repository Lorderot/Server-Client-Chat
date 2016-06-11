package controller;

import JSONcoder.DataTransmissionProtocolCoder;
import JSONcoder.MessageTransmissionProtocolCoder;
import JSONcoder.ParticipantsTransmissionProtocolCoder;
import app.MainApp;
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
import protocols.*;
import server.Server;
import util.DateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatMainWindowController {
    private MainApp mainApp;
    private BufferedReader serverReader;
    private OutputStreamWriter serverWriter;
    private Server server;
    private String nickname;
    private Stage stage;
    private List<String> participants;
    @FXML
    private TableView<String> participantTableView;
    @FXML
    private TableColumn<String, String> nameColumn;
    @FXML
    private TextArea chatTextArea;
    @FXML
    private TextArea messageTextArea;

    public ChatMainWindowController() {
        server = new Server();
        participants = new ArrayList<>();
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue()));
        chatTextArea.setWrapText(true);
        chatTextArea.setEditable(false);
        messageTextArea.setEditable(true);
        messageTextArea.setWrapText(true);
        participantTableView.setItems(
                FXCollections.observableList(participants));
    }

    public void handleSend() {
        String message = messageTextArea.getText();
        if (message.length() == 0) {
            return;
        }
        DataTransmissionProtocol dataTransmissionProtocol = new DataTransmissionProtocol();
        dataTransmissionProtocol.setType(ProtocolType.CLIENT_MESSAGE_TRANSMISSION);
        MessageTransmissionProtocol messageTransmissionProtocol = new MessageTransmissionProtocol();
        messageTransmissionProtocol.setSender(nickname);
        messageTransmissionProtocol.setTime(DateUtil.toString(new Date()));
        messageTransmissionProtocol.setMessage(message);
        String receiver = participantTableView.getSelectionModel()
                .getSelectedItem();
        if (receiver == null) {
            messageTransmissionProtocol.setType(AccessType.PUBLIC);
        } else {
            messageTransmissionProtocol.setType(AccessType.PRIVATE);
            messageTransmissionProtocol.setReceiver(receiver);
        }
        dataTransmissionProtocol.setProtocol(MessageTransmissionProtocolCoder.encode(messageTransmissionProtocol));
        if (server.isConnected()) {
            try {
                serverWriter.write(DataTransmissionProtocolCoder.encode(dataTransmissionProtocol) + "\n");
                serverWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            messageTextArea.clear();
            receiveMessageFromParticipant(messageTransmissionProtocol);
        } else {
            MainApp.showAlert(Alert.AlertType.ERROR, "Server is disconnected!");
        }
    }

    public void reConnect() {
        server.close();
        nickname = mainApp.showNicknameDialog(stage);
        if (nickname == null) {
            return;
        }
        try {
            setServer(mainApp.getServer());
        } catch (IOException e) {
            e.printStackTrace();
            MainApp.showAlert(Alert.AlertType.ERROR,
                    "Error occurs while connecting!");
            server.close();
            return;
        }
        participants = new ArrayList<>();
        refreshTable();
        loadConfigurationData();
    }

    public boolean loadConfigurationData() {
        try {
            while (true) {
                String json = serverReader.readLine();
                DataTransmissionProtocol dataTransmissionProtocol =
                        DataTransmissionProtocolCoder.decode(json);
                if (!dataTransmissionProtocol.getSecureWord()
                        .equals(server.getSecureWord())) {
                    continue;
                }
                ParticipantsTransmissionProtocol participantsTransmissionProtocol =
                        ParticipantsTransmissionProtocolCoder
                                .decode(dataTransmissionProtocol.getProtocol());
                participants = participantsTransmissionProtocol.getParticipants();
                participantTableView.setItems(FXCollections
                        .observableArrayList(participants));
                break;
            }
        } catch (IOException e) {
            MainApp.showAlert(Alert.AlertType.ERROR, e.getMessage());
            return false;
        }
        ProtocolHandler protocolHandler =
                new ProtocolHandler(this, server, serverReader);
        protocolHandler.run();
        return true;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setServer(Server server) throws IOException {
        this.server = server;
        serverReader = server.getReader();
        serverWriter = server.getWriter();
    }

    public void handleExit() {
        server.close();
        stage.close();
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

    synchronized void writeInChatFromServer(
            MessageTransmissionProtocol message) {
        chatTextArea.appendText("\n"
                + message.getTime() + "\n");
        chatTextArea.appendText(message.getSender());
        switch (message.getType()) {
            case PRIVATE: {
                chatTextArea.appendText(" whispers you: ");
                break;
            }
            case PUBLIC: {
                chatTextArea.appendText(": ");
            }
        }
        chatTextArea.appendText(message.getMessage());
    }

    synchronized void receiveMessageFromServer(
            String message, String time) {
        chatTextArea.appendText("\n"
                + time + "\n");
        chatTextArea.appendText("Server: " + message);
    }

    private synchronized void receiveMessageFromParticipant(
            MessageTransmissionProtocol message) {
        chatTextArea.appendText("\n"
                + message.getTime() + "\n");
        switch (message.getType()) {
            case PRIVATE: {
                chatTextArea.appendText("whisper to "
                        + message.getReceiver() + ": ");
                break;
            }
            case PUBLIC: {
                chatTextArea.appendText(
                        message.getSender() + ": ");
            }
        }
        chatTextArea.appendText(message.getMessage());
    }

    private void refreshTable() {
        participantTableView.setItems(null);
        participantTableView.setItems(FXCollections
                .observableArrayList(participants));

    }
}

class ProtocolHandler implements Runnable {
    private ChatMainWindowController controller;
    private Server server;
    private BufferedReader serverReader;

    public ProtocolHandler(ChatMainWindowController controller,
                           Server server, BufferedReader serverReader) {
        this.controller = controller;
        this.server = server;
        this.serverReader = serverReader;
    }

    @Override
    public void run() {
        String input;
        try {
            while (server.isConnected()
                    && (input = serverReader.readLine()) != null) {
                DataTransmissionProtocol dataTransmissionProtocol
                        = DataTransmissionProtocolCoder.decode(input);
                switch (dataTransmissionProtocol.getType()) {
                    case FILE_TRANSMISSION: {
                        break;
                    }
                    case SERVER_MESSAGE_TRANSMISSION: {
                        MessageTransmissionProtocol message =
                                MessageTransmissionProtocolCoder
                                        .decode(dataTransmissionProtocol.getProtocol());
                        controller.receiveMessageFromServer(
                                message.getMessage(), message.getTime());
                    }
                    case CLIENT_MESSAGE_TRANSMISSION: {
                        MessageTransmissionProtocol message =
                                MessageTransmissionProtocolCoder
                                        .decode(dataTransmissionProtocol.getProtocol());
                        controller.writeInChatFromServer(message);
                    }
                }

            }
        } catch (IOException e) {
            System.err.println("Thread has been fallen: " + e.getMessage());
        } finally {
            System.err.println("Thread has finished work!");
            server.close();
        }
    }
}