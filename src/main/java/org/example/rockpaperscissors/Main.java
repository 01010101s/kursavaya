package org.example.rockpaperscissors;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class Main extends Application {
    public static String playerName;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/rockpaperscissors/main.fxml"));
        Parent root = loader.load();
        stage.setTitle("Камень, ножницы, бумага");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();

        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Выход");
            alert.setContentText("Выйти.");

            Optional<ButtonType> result =  alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                MainController.closeGame();
                javafx.application.Platform.exit();
            }
            else {
                event.consume();
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
