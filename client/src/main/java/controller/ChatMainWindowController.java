package controller;

import JSONcoder.DataTransmissionProtocolCoder;
import app.MainApp;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import protocols.AccessType;
import protocols.DataTransmissionProtocol;
import server.ChatHandler;
import server.ConnectionHandler;
import server.ProtocolHandler;
import util.DateUtil;
import view.ChatConfigurations;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ChatMainWindowController {
    private MainApp mainApp;
    private ChatConfigurations chatConfigurations;
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
    private TextFlow chatTextFlow;
    @FXML
    private ScrollPane scrollPane;
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
        messageTextArea.setEditable(true);
        messageTextArea.setWrapText(true);
        chatTextFlow.heightProperty().addListener(
                (observable, oldValue, newValue) -> {
            scrollPane.setVvalue(newValue.doubleValue());
        });
        scrollPane.setBackground(Background.EMPTY);
    }

    public void completeInitializing() {
        BackgroundFill backgroundFill = new BackgroundFill(
                chatConfigurations.getChatBackgroundColor(),
                CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        chatTextFlow.setBackground(background);
        addMessageListener();
    }

    public void handleSend() {
        String message = processMessageWithSmiles(messageTextArea.getText());
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
        String chatMessage = "";
        chatMessage += time + "\n";
        chatMessage += sender;
        Color color;
        switch (type) {
            case PRIVATE: {
                chatMessage += " whispers you: ";
                color = chatConfigurations.getPrivateMessageColor();
                break;
            }
            case PUBLIC: default: {
                chatMessage += ": ";
                color = chatConfigurations.getPublicMessageColor();
            }
        }
        chatMessage += message + "\n";
        displayMessageWithSmiles(chatMessage,
                chatConfigurations.getMessageFont(), color);
    }

    public void displayServerMessage(String message, String time) {
        String chatMessage = "";
        chatMessage += time + "\n";
        chatMessage += "Server: " + message + "\n";
        displayMessageWithSmiles(chatMessage, chatConfigurations.getMessageFont(),
                chatConfigurations.getServerMessageColor());
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

    public void setChatConfigurations(ChatConfigurations chatConfigurations) {
        this.chatConfigurations = chatConfigurations;
        completeInitializing();
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
        String chatMessage = "";
        chatMessage += time + "\n";
        Color color;
        if (receiver != null) {
            chatMessage += "You whisper to " + receiver + ": ";
            color = chatConfigurations.getPrivateMessageColor();
        } else {
            chatMessage += "You: ";
            color = chatConfigurations.getPublicMessageColor();
        }
        chatMessage += message + "\n";
        displayMessageWithSmiles(chatMessage,
                chatConfigurations.getMessageFont(), color);
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

    private void addMessageListener() {
        int limit = chatConfigurations.getMessageLimit();
        messageTextArea.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.length() > limit) {
                        messageTextArea.setText(newValue.substring(0, limit));
                    }
                    if (newValue.split("\\n").length > 5) {
                        messageTextArea.setText(oldValue);
                    }
                });
    }

    private void displayMessageWithSmiles(String message,
                                          Font font, Color color) {
        String delimiter = String.valueOf(chatConfigurations.getMetaSymbolDelimiter());
        String[] splitMessage = message.split("\\" + delimiter
                + "\\" + delimiter);
        HashMap<String, String> smiles = chatConfigurations.getSmileys();
        ObservableList<Node> listOfNodes = chatTextFlow.getChildren();
        for (String partOfMessage : splitMessage) {
            String imageUrl = smiles.get(partOfMessage);
            if (imageUrl != null) {
                Image image = new Image(imageUrl);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(chatConfigurations.getSmileWidth());
                imageView.setFitHeight(chatConfigurations.getSmileHeight());
                listOfNodes.add(imageView);
            } else {
                Text text = new Text();
                text.setText(partOfMessage.replaceAll("\\\\\\" + delimiter
                        + "\\\\", delimiter));
                text.setFont(font);
                text.setFill(color);
                listOfNodes.add(text);
            }
        }
    }

    private String processMessageWithSmiles(String message) {
        String delimiter = String.valueOf(chatConfigurations.getMetaSymbolDelimiter());
        String messageWithDelimitedSmiles = message
                .replaceAll("\\" + delimiter, "\\\\" + delimiter + "\\\\");
        List<Character> metaSymbols = chatConfigurations.getMetaSymbolsUsedInSmiles();
        for (String smile : chatConfigurations.getSmileys().keySet()) {
            String regExpression = smile;
            for (Character symbol : metaSymbols) {
                if (regExpression.contains(String.valueOf(symbol))) {
                    regExpression = regExpression
                            .replaceAll("\\" + symbol, "\\\\" + symbol);
                }
            }
            String replacement = delimiter + delimiter + regExpression
                    + delimiter + delimiter;
            messageWithDelimitedSmiles =
                    messageWithDelimitedSmiles.replaceAll(regExpression, replacement);
        }
        return messageWithDelimitedSmiles;
    }
}
