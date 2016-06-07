package controller;

import app.MainApp;
import coder.ChatMessageCoder;
import coder.MessageCoder;
import coder.ServerMessageCoder;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import protocol.*;
import server.Server;
import util.DateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ChatMainWindowController {
    private MainApp mainApp;
    private BufferedReader serverReader;
    private PrintWriter serverWriter;
    private Server server;
    private String nickname;
    private Stage stage;
    private ArrayList<String> participants;
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
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessageType.CLIENT);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(nickname);
        chatMessage.setTime(DateUtil.toString(new Date()));
        chatMessage.setMessage(message);
        String receiver = participantTableView.getSelectionModel()
                .getSelectedItem();
        if (receiver == null) {
            chatMessage.setType(AccessType.PUBLIC);
        } else {
            chatMessage.setType(AccessType.PRIVATE);
            chatMessage.setReceiver(receiver);
        }
        messagePacket.setMessage(ChatMessageCoder.encode(chatMessage));
        if (server.isConnected()) {
            serverWriter.println(MessageCoder.encode(messagePacket));
            serverWriter.flush();
            messageTextArea.clear();
            writeInChat(chatMessage);
        } else {
            MainApp.showAlert(Alert.AlertType.ERROR, "Server is disconnected!");
        }
    }

    public void reConnect() {
        if (!server.isConnected()) {
            nickname = mainApp.showNicknameDialog(server, stage);
            if (nickname == null) {
                return;
            }
            try {
                serverReader = server.getReader();
                serverWriter = server.getWriter();
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
    }

    public boolean loadConfigurationData() {
        String listOfParticipants;
        try {
            listOfParticipants = serverReader.readLine();
        } catch (IOException e) {
            MainApp.showAlert(Alert.AlertType.ERROR, e.getMessage());
            return false;
        }
        if (listOfParticipants != null && !listOfParticipants.equals("")) {
            Arrays.asList(listOfParticipants.split(","))
                    .forEach(nickname -> participants.add(nickname));
            participantTableView.setItems(FXCollections
                    .observableArrayList(participants));
        }
        runReaderThread();
        return true;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setServer(Server server) throws Exception {
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

    private synchronized void writeInChat(ChatMessage chatMessage) {
        chatTextArea.appendText("\n"
                + chatMessage.getTime() + "\n");
        switch (chatMessage.getType()) {
            case PRIVATE: {
                chatTextArea.appendText("whisper to "
                        + chatMessage.getReceiver() + ": ");
                break;
            }
            case PUBLIC: {
                chatTextArea.appendText(chatMessage.getSender() + ": ");
            }
        }
        chatTextArea.appendText(chatMessage.getMessage());
    }

    private synchronized void writeInChatFromServer(ChatMessage chatMessage) {
        chatTextArea.appendText("\n"
                + chatMessage.getTime() + "\n");
        chatTextArea.appendText(chatMessage.getSender());
        switch (chatMessage.getType()) {
            case PRIVATE: {
                chatTextArea.appendText(" whispers: ");
                break;
            }
            case PUBLIC: {
                chatTextArea.appendText(": ");
            }
        }
        chatTextArea.appendText(chatMessage.getMessage());
    }

    private void runReaderThread() {
        new Thread(() -> {
            String input;
            try {
                while (server.isConnected()
                        && (input = serverReader.readLine()) != null) {
                    MessagePacket messagePacket = MessageCoder.decode(input);
                    switch (messagePacket.getType()) {
                        case RECEIVE_OBJECT: {
                            break;
                        }
                        case SERVER: {
                            ServerMessage serverMessage =
                                    ServerMessageCoder.decode(messagePacket.getMessage());
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setTime(serverMessage.getTime());
                            chatMessage.setType(AccessType.PUBLIC);
                            chatMessage.setSender("Server");
                            String nickname = serverMessage.getNickname();
                            switch (serverMessage.getStatus()) {
                                case OFFLINE: {
                                    participants.remove(nickname);
                                    refreshTable();
                                    chatMessage.setMessage(nickname
                                            + " has gone offline!");
                                    writeInChatFromServer(chatMessage);
                                    break;
                                }
                                case ONLINE: {
                                    participants.add(nickname);
                                    refreshTable();
                                    chatMessage.setMessage("Welcome, "
                                            +  nickname + "!");
                                    writeInChatFromServer(chatMessage);
                                }
                            }
                            break;
                        }
                        case CLIENT: synchronized (this){
                            ChatMessage chatMessage = ChatMessageCoder
                                    .decode(messagePacket.getMessage());
                            writeInChatFromServer(chatMessage);
                        }
                    }

                }
            } catch (IOException e) {
                System.err.println("Thread has been fallen: " + e.getMessage());
            } finally {
                System.err.println("Thread has finished work!");
                server.close();
            }
        }).start();
    }

    private void refreshTable() {
        participantTableView.setItems(null);
        participantTableView.setItems(FXCollections
                .observableArrayList(participants));

    }
}