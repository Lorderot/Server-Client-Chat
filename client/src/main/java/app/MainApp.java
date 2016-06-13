package app;

import controller.ChatMainWindowController;
import controller.NicknameDialogController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import server.ChatHandler;
import server.ConnectionHandler;
import view.ChatConfigurations;

import java.io.IOException;

public class MainApp extends Application{
    private ConnectionHandler connectionHandler;
    private ChatHandler chatHandler;
    private ChatConfigurations chatConfigurations
            = new ChatConfigurations();

    @Override
    public void start(Stage primaryStage) throws Exception {
        String nickname = showNicknameDialog(primaryStage);
        if (nickname != null) {
            showChatMainWindow(nickname);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    public void showChatMainWindow(String nickname) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/ChatMainWindow.fxml"));
        BorderPane root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Stage mainStage = new Stage();
        mainStage.setScene(new Scene(root));
        root.setBackground(Background.EMPTY);
        ChatMainWindowController controller = loader.getController();
        controller.setNickname(nickname);
        controller.setMainApp(this);
        controller.setConnectionHandler(connectionHandler);
        controller.setChatHandler(chatHandler);
        controller.setChatConfigurations(chatConfigurations);
        controller.setStage(mainStage);
        mainStage.showAndWait();
    }

    public String showNicknameDialog(Stage parentStage) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/NicknameDialog.fxml"));
        try {
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initOwner(parentStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Authorisation");
            NicknameDialogController controller = loader.getController();
            controller.setStage(stage);
            stage.showAndWait();
            connectionHandler = controller.getConnectionHandler();
            chatHandler = controller.getChatHandler();
            return controller.getNickName();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public ChatHandler getChatHandler() {
        return chatHandler;
    }

    public static void showAlert(Alert.AlertType type, String context) {
        Alert alert = new Alert(type, context);
        alert.showAndWait();
    }
}
