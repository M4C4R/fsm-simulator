package test.java.menu;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.shared.ClipboardExt;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * @author Mert Acar
 */
public class MenuControllerTest extends ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main/java/menu/menu.fxml"));
        this.stage = stage;
        this.stage.setScene(new Scene(root));
        this.stage.setAlwaysOnTop(true);
        this.stage.show();
    }

    @Test
    public void menuShouldContainAutomatonImage() {
        assertTrue(lookup("#automatonImage").tryQuery().isPresent());
    }

    @Test
    public void menuShouldContainThreeButtonsWithText() {
        assertTrue(lookup("#btnUseToolkit").tryQuery().isPresent());
        assertTrue(lookup("#btnTutorial").tryQuery().isPresent());
        assertTrue(lookup("#btnBackInfo").tryQuery().isPresent());
        verifyThat("#btnUseToolkit", hasText(containsString("Toolkit")));
        verifyThat("#btnTutorial", hasText(containsString("Tutorial")));
        verifyThat("#btnBackInfo", hasText(containsString("Information")));
    }

    @Test
    public void clickingToolkitButtonShouldLaunchToolkit() {
        WaitForAsyncUtils.waitForFxEvents();
        // Check that we are currently not on the toolkit
        assertNotEquals("Workspace", lookup("#title").queryText().getText());
        clickOn("#btnUseToolkit");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Workspace", lookup("#title").queryText().getText());
    }

    @Test
    public void clickingTutorialButtonShouldLaunchTutorial() {
        WaitForAsyncUtils.waitForFxEvents();
        // Check that we are currently not on the tutorial
        assertNotEquals("Tutorial", lookup("#title").queryText().getText());
        clickOn("#btnTutorial");
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals("Tutorial", lookup("#title").queryText().getText());
    }

    @Test
    public void clickingBackgroundInformationButtonShouldLaunchLink() {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(stage.isFocused());
        clickOn("#btnBackInfo");
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> assertThat(ClipboardExt.getContentOfUserClipboard(), containsString("wiki")));
    }
}
