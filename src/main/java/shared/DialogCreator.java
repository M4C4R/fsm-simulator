package main.java.shared;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @author Mert Acar
 * <p>
 * Returns an initialised {@code Dialog} to the user when supplied with a title, icon file name, window, and modality.
 * </p>
 */
public class DialogCreator {
    /**
     * Initialises a generic dialog with the specified attributes.
     * Removes the default buttons from the dialog.
     *
     * @param title    of the dialog window
     * @param iconName within the resources folder to be added to the dialog window
     * @param window   which is the parent of this {@code Dialog}
     * @param modality that the {@code Dialog} should use
     * @return an initialised {@code Dialog}
     */
    public static Dialog<String> createInitialisedDialog(String title, String iconName, Window window, Modality modality) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Button btCancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        btCancel.setManaged(false);
        btCancel.setVisible(false);
        dialog.initOwner(window);
        dialog.initModality(modality);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().setAll(new Image(DialogCreator.class.getResourceAsStream("/main/resources/" + iconName)));
        return dialog;
    }
}
