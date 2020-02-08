package test.java.toolkit.testInputDialog;

import javafx.scene.control.ListView;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import test.java.toolkit.ToolkitTestSetup;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

/**
 * @author Mert Acar
 */
public class TestInputDialogControllerTest extends ToolkitTestSetup {

    @Test
    public void shouldAcceptKnownAcceptedWord() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchTestInputDialog();
        writeToTextField("01010101010");
        clickOK();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(lookup("#lblResult").queryLabeled().getText(), containsString("Accepted"));
    }

    @Test
    public void shouldRejectKnownRejectedWord() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchTestInputDialog();
        writeToTextField("011101110");
        clickOK();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(lookup("#lblResult").queryLabeled().getText(), containsString("Rejected"));
    }

    @Test
    public void shouldAvoidInfinitePathsWhenTestingOnAutomataWithEmptyWordLoops() {
        loadTestFiniteAutomaton("twoStatesWithEmptyWordLoop.fsm");
        launchTestInputDialog();
        clickOK();
        WaitForAsyncUtils.waitForFxEvents();
        ListView pathsListView = lookup("#lvPaths").queryListView();
        assertEquals(1, pathsListView.getItems().size());
    }

    @Test
    public void testingWithoutInitialStateShouldRaiseAlert() {
        launchTestInputDialog();
        clickOK();
        // Sometimes the alert is delayed
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertTrue(lookup(".alert").tryQuery().isPresent());
        clickOK();
    }

    @Test
    public void shouldRejectKnownStuckWord() {
        loadTestFiniteAutomaton("automatonWithEmptyWordTransition.fsm");
        launchTestInputDialog();
        writeToTextField("abbabC");
        clickOK();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(lookup("#lblResult").queryLabeled().getText(), containsString("Rejected"));
        clickOK();
    }

    @Test
    public void shouldAcceptEmptyWordOnKnownAcceptingAutomaton() {
        loadTestFiniteAutomaton("twoStatesWithEmptyWordLoop.fsm");
        launchTestInputDialog();
        clickOK();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(lookup("#lblResult").queryLabeled().getText(), containsString("Accepted"));
    }

    private void launchTestInputDialog() {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnTestInput");
    }

    private void writeToTextField(String input) {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(".dialog-pane .text-field");
        write(input);
    }

    private void clickOK() {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(".dialog-pane .button:default");
    }

}
