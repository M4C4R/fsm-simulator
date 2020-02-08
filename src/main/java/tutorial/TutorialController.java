package main.java.tutorial;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import main.java.shared.FXMLManager;
import main.java.shared.URLLauncher;

import java.io.IOException;

/**
 * @author Mert Acar
 * <p>
 * Controller which handles actions on the Tutorial pane of the software.
 * </p>
 */
public class TutorialController {
    @FXML
    private GridPane tutorialsContainer;
    @FXML
    private ImageView statesTutorialThumbnail;
    @FXML
    private ImageView transitionsTutorialThumbnail;
    @FXML
    private ImageView movingComponentsTutorialThumbnail;
    @FXML
    private ImageView alphabetTutorialThumbnail;
    @FXML
    private ImageView savingLoadingExportingTutorialThumbnail;
    @FXML
    private ImageView testingInputTutorialThumbnail;
    @FXML
    private ImageView simulationTutorialThumbnail;

    @FXML
    private void initialize() {
        // Bind the widths of the images to the overall width of the window
        // This makes them look better when the window is enlarged
        Platform.runLater(() -> {
            bindWidthToGridCell(statesTutorialThumbnail);
            bindWidthToGridCell(transitionsTutorialThumbnail);
            bindWidthToGridCell(movingComponentsTutorialThumbnail);
            bindWidthToGridCell(alphabetTutorialThumbnail);
            bindWidthToGridCell(savingLoadingExportingTutorialThumbnail);
            bindWidthToGridCell(testingInputTutorialThumbnail);
            bindWidthToGridCell(simulationTutorialThumbnail);
        });
    }

    private void bindWidthToGridCell(ImageView imageView) {
        imageView.fitWidthProperty().bind(tutorialsContainer.widthProperty().divide(3).subtract(20));
    }

    @FXML
    private void onStatesTutorialClick() {
        launchTutorial("FXkssabLvIA");
    }

    @FXML
    private void onTransitionsTutorialClick() {
        launchTutorial("RwkuFnRF5N4");
    }

    @FXML
    private void onMovingComponentsTutorialClick() {
        launchTutorial("uOS4oA7aIWA");
    }

    @FXML
    private void onAlphabetTutorialClick() {
        launchTutorial("M8rRMM5YLJ8");
    }

    @FXML
    private void onSavingLoadingExportingTutorialClick() {
        launchTutorial("Zz24zmkQWUY");
    }

    @FXML
    private void onTestingInputTutorialClick() {
        launchTutorial("bf3nshKS3Lg");
    }

    @FXML
    private void onSimulationTutorialClick() {
        launchTutorial("CqzEuz-5fHU");
    }

    private void launchTutorial(String videoCode) {
        URLLauncher.launchURL("https://www.youtube.com/watch?v=" + videoCode + "&list=PLshiFZXV2HpimYGVJAxKUhsZCN6DVulbf", tutorialsContainer.getScene().getWindow());
    }

    @FXML
    private void launchMainMenu(ActionEvent e) throws IOException {
        FXMLManager.loadFXMLFileToStage(e, "/main/java/menu/menu.fxml");
    }
}
