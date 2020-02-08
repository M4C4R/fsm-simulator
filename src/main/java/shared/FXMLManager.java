package main.java.shared;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author Mert Acar
 * <p>
 * Offers functions to load specified FXML files onto stages.
 * </p>
 */
public class FXMLManager {
    /**
     * Identifies the action event's {@code Window} and loads the specified FXML file to this window.
     *
     * @param e    the action event to extract the {@code Window} from
     * @param path to the FXML file to be loaded
     * @throws IOException
     */
    public static void loadFXMLFileToStage(ActionEvent e, String path) throws IOException {
        Node source = (Node) e.getSource();
        loadFXMLFileToStage((Stage) source.getScene().getWindow(), path);
    }

    /**
     * Loads the FXML file onto the stage and calls {@link Stage#show()}.
     *
     * @param stage to load the FXML file onto
     * @param path to the FXML file to be loaded
     * @throws IOException
     */
    public static void loadFXMLFileToStage(Stage stage, String path) throws IOException {
        Parent root = FXMLLoader.load(FXMLManager.class.getResource(path));
        stage.getScene().setRoot(root);
        stage.show();
    }
}
