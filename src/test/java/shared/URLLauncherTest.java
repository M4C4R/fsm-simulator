package test.java.shared;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.java.shared.URLLauncher;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * @author Mert Acar
 */
public class URLLauncherTest extends ApplicationTest {

    private Stage stage;
    private Alert alert;

    @Override
    public void start(Stage stage) {
        alert = new Alert(Alert.AlertType.CONFIRMATION, "Testing URL Launcher");
        this.stage = (Stage) alert.getDialogPane().getScene().getWindow();
        this.stage.setAlwaysOnTop(true);
        alert.show();
    }

    @Test
    public void shouldSuccessfullyLaunchBrowserOnValidURI() {
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> URLLauncher.launchURL("http://www.example.com/", stage));
        WaitForAsyncUtils.waitForFxEvents();
        // Can't actually access the browser so just check that the error alert didn't appear
        assertFalse(lookup(".information").tryQuery().isPresent());
    }

    @Test
    public void shouldFailToLaunchBrowserOnInvalidURI() {
        String invalidURI = "abcdef";
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> URLLauncher.launchURL(invalidURI, stage));
        WaitForAsyncUtils.waitForFxEvents();
        // Sometimes the alert is delayed
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        // Check that the error alert is shown and it contains the failed URL
        lookup(".information").queryParent().getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof HBox) {
                ((HBox) node).getChildren().forEach(child -> {
                    if (child instanceof Label) {
                        assertThat(((Label) child).getText(), containsString(invalidURI));
                    }
                });
            }
        });
    }
}
