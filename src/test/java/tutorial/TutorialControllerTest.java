package test.java.tutorial;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author Mert Acar
 */
public class TutorialControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main/java/tutorial/tutorial.fxml"));
        stage.setScene(new Scene(root));
        stage.setAlwaysOnTop(true);
        stage.show();
    }

    @Test
    public void clickingStatesTutorialShouldLaunchLink() {
        testTutorialButtonWithID("#statesTutorialThumbnail");
    }

    @Test
    public void clickingTransitionsTutorialShouldLaunchLink() {
        testTutorialButtonWithID("#transitionsTutorialThumbnail");
    }

    @Test
    public void clickingMovingComponentsTutorialShouldLaunchLink() {
        testTutorialButtonWithID("#movingComponentsTutorialThumbnail");
    }

    @Test
    public void clickingAlphabetTutorialShouldLaunchLink() {
        testTutorialButtonWithID("#alphabetTutorialThumbnail");
    }

    @Test
    public void clickingSavingLoadingExportingTutorialShouldLaunchLink() {
        testTutorialButtonWithID("#savingLoadingExportingTutorialThumbnail");
    }

    @Test
    public void clickingTestingInputTutorialShouldLaunchLink() {
        testTutorialButtonWithID("#testingInputTutorialThumbnail");
    }

    @Test
    public void clickingSimulatingInputTutorialShouldLaunchLink() {
        testTutorialButtonWithID("#simulationTutorialThumbnail");
    }

    @Test
    public void clickingBackShouldReturnToMainMenu() {
        WaitForAsyncUtils.sleep(1500, TimeUnit.MILLISECONDS);
        assertTrue(lookup("#title").queryText().getText().toLowerCase().contains("tutorial"));
        clickOn("#btnBack");
        WaitForAsyncUtils.sleep(1500, TimeUnit.MILLISECONDS);
        assertFalse(lookup("#title").queryText().getText().toLowerCase().contains("tutorial"));
    }

    private void testTutorialButtonWithID(String id) {
        WaitForAsyncUtils.sleep(1500, TimeUnit.MILLISECONDS);
        clickOn(id);
        WaitForAsyncUtils.sleep(1500, TimeUnit.MILLISECONDS);
        // Can't actually access the browser so just check that the error alert didn't appear
        assertFalse(lookup(".information").tryQuery().isPresent());
    }

}
