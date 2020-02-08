package test.java.shared;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import main.java.shared.DialogCreator;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.assertEquals;

/**
 * @author Mert Acar
 */
public class DialogCreatorTest extends ApplicationTest {

    private Alert alert;

    @Override
    public void start(Stage stage) throws Exception {
        alert = new Alert(Alert.AlertType.CONFIRMATION, "Testing Custom Cursor");
        ((Stage) alert.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
        alert.show();
    }

    @Test
    public void shouldCreateDialogWithSpecifiedTitleAndOwner() {
        String title = "TestingIsFun";
        String iconName = "transitionIcon.png";
        Window owningWindow = alert.getDialogPane().getScene().getWindow();
        Platform.runLater(() -> {
            Dialog dialog = DialogCreator.createInitialisedDialog(title, iconName, owningWindow, Modality.APPLICATION_MODAL);
            assertEquals(title, dialog.getTitle());
            assertEquals(owningWindow, dialog.getOwner());
        });
    }
}
