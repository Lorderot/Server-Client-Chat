package app;

import controller.ChatMainWindowController;
import controller.NicknameDialogController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import server.Server;

import java.io.IOException;

public class MainApp extends Application{
    private Server server = new Server();
    @Override
    public void start(Stage primaryStage) throws Exception {
        String nickname = showNicknameDialog(server, primaryStage);
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
        mainStage.setResizable(false);
        mainStage.setScene(new Scene(root));
        ChatMainWindowController controller = loader.getController();
        controller.setNickname(nickname);
        controller.setMainApp(this);
        try {
            controller.setServer(server);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, e.getMessage());
        }
        controller.setStage(mainStage);
        if (!controller.loadConfigurationData()) {
            return;
        }
        mainStage.showAndWait();
    }

    public String showNicknameDialog(Server server, Stage parentStage) {
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
            controller.setServer(server);
            stage.showAndWait();
            return controller.getNickName();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void showAlert(Alert.AlertType type, String context) {
        Alert alert = new Alert(type, context);
        alert.showAndWait();
    }
}
