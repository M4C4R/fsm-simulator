package main.java.shared;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @author Mert Acar
 * <p>
 * Returns an initialised {@code Alert} to the user when supplied with a title, text, alert type, window, and modality.
 * </p>
 */
public class AlertCreator {
    /**
     * Initialises a generic alert with the specified attributes.
     * Removes the default header text and graphic of the alert.
     * Also applies some css for generic prompts and a standard warning icon to the window.
     *
     * @param title     of the alert window
     * @param text      to be displayed on the {@code Alert}
     * @param alertType for the initialised {@code Alert}
     * @param window    which is the parent of this {@code Alert}
     * @param modality  that the {@code Alert} should use
     * @return an initialised and configured {@code Alert}
     */
    public static Alert createInitialisedAlert(String title, String text, Alert.AlertType alertType, Window window, Modality modality) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        Label label = new Label(text);
        label.setTextAlignment(TextAlignment.CENTER);
        HBox hbox = new HBox(label);
        HBox.setMargin(label, new Insets(5, 0, 0, 0));
        alert.getDialogPane().setContent(hbox);
        alert.getDialogPane().getStylesheets().addAll(
                AlertCreator.class.getResource("/main/java/shared/genericPrompt.css").toExternalForm(),
                AlertCreator.class.getResource("/main/java/shared/scrollbarStyling.css").toExternalForm(),
                AlertCreator.class.getResource("/main/java/shared/buttonStyling.css").toExternalForm());
        alert.initOwner(window);
        alert.initModality(modality);
        alert.getDialogPane().setStyle("-fx-padding: 0 0 0 -10");
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().setAll(new Image(AlertCreator.class.getResourceAsStream("/main/resources/" + "warningIcon.png")));
        return alert;
    }
}
