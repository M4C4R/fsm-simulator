package test.java.shared;

import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import main.java.shared.ClipboardExt;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Mert Acar
 */
public class ClipboardExtTest extends ApplicationTest {

    @Test
    public void shouldCopyStringToClipboard() {
        String content = "test123";
        Platform.runLater(() -> {
            ClipboardExt.addStringToUserClipboard(content);
            assertEquals(content, ClipboardExt.getContentOfUserClipboard());
        });
    }

    @Test
    public void shouldFailToReturnStringFromEmptyClipboard() {
        Platform.runLater(() -> {
            Clipboard.getSystemClipboard().clear();
            assertEquals("null", ClipboardExt.getContentOfUserClipboard());
        });
    }
}
