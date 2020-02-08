package main.java.menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import main.java.shared.FXMLManager;
import main.java.shared.URLLauncher;

import java.io.IOException;

/**
 * @author Mert Acar
 * <p>
 * Controller for the main menu of the software; interprets the navigation buttons on the main menu and launches the
 * appropriate FXML file.
 * </p>
 */
public class MenuController {
    @FXML
    private BorderPane bPaneContainer;
    @FXML
    private ImageView automatonImage;

    @FXML
    private void initialize() {
        // Bind the image to the size of its container
        automatonImage.fitHeightProperty().bind(bPaneContainer.heightProperty().divide(2));
    }

    @FXML
    private void launchToolkit(ActionEvent e) throws IOException {
        FXMLManager.loadFXMLFileToStage(e, "/main/java/toolkit/toolkit.fxml");
    }

    @FXML
    private void launchTutorial(ActionEvent e) throws IOException {
        FXMLManager.loadFXMLFileToStage(e, "/main/java/tutorial/tutorial.fxml");
    }

    @FXML
    private void launchBackgroundInformation(ActionEvent e) {
        URLLauncher.launchURL("https://en.wikipedia.org/wiki/Deterministic_finite_automaton", ((Node) e.getSource()).getScene().getWindow());
    }
}
