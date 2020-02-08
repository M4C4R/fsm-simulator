package test.java.shared;

import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import main.java.shared.CustomCursor;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.Assert.assertEquals;

/**
 * @author Mert Acar
 */
public class CustomCursorTest extends ApplicationTest {

    private Alert alert;

    @Override
    public void start(Stage stage) throws Exception {
        alert = new Alert(Alert.AlertType.CONFIRMATION, "Testing Custom Cursor");
        ((Stage) alert.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
        alert.show();
    }

    @Test
    public void settingToMoveShouldChangeCursor() {
        WaitForAsyncUtils.waitForFxEvents();
        moveTo(".button");
        alert.getDialogPane().setCursor(Cursor.DEFAULT);
        assertEquals(Cursor.DEFAULT, alert.getDialogPane().getCursor());

        WaitForAsyncUtils.waitForFxEvents();
        alert.getDialogPane().setCursor(CustomCursor.MOVE);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(CustomCursor.MOVE, alert.getDialogPane().getCursor());
    }

    @Test
    public void revertingMoveCursorShouldChangeCursorToDefault() {
        WaitForAsyncUtils.waitForFxEvents();
        moveTo(".button");
        alert.getDialogPane().setCursor(CustomCursor.MOVE);
        assertEquals(CustomCursor.MOVE, alert.getDialogPane().getCursor());

        WaitForAsyncUtils.waitForFxEvents();
        alert.getDialogPane().setCursor(Cursor.DEFAULT);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(Cursor.DEFAULT, alert.getDialogPane().getCursor());
    }
}
