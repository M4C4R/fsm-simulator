package main.java;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import main.java.shared.FXMLManager;

/**
 * @author Mert Acar
 * <p>
 * Main driver to lauch the software.
 * </p>
 */
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(new Group(), 700, 550);
        stage.setTitle("Finite Automata Visual Toolkit");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/main/resources/stateIcon.png")));
        stage.setScene(scene);
        Font.loadFont(getClass().getResource("/main/resources/fonts/OpenSans-Regular.ttf").toExternalForm(), 10);
        Font.loadFont(getClass().getResource("/main/resources/fonts/OpenSans-Bold.ttf").toExternalForm(), 10);
        FXMLManager.loadFXMLFileToStage(stage, "/main/java/menu/menu.fxml");
        Platform.runLater(() -> {
            stage.setMinHeight(stage.getHeight());
            stage.setHeight(stage.getMinHeight());
            stage.setMinWidth(scene.getWidth() + 15);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}