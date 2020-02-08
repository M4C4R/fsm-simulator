package test.java.toolkit.simulationDialog;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import main.java.shared.Unicode;
import main.java.toolkit.simulationDialog.SimulationDialogController;
import org.junit.Test;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;
import test.java.toolkit.ToolkitTestSetup;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;

/**
 * @author Mert Acar
 */
public class SimulationDialogControllerTest extends ToolkitTestSetup {

    @Test
    public void shouldOnlyDisplayOnePathWhenSimulateIsClicked() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchSimulationDialog();
        writeToTextField("010$");
        clickSimulate();
        WaitForAsyncUtils.waitForFxEvents();
        ListView pathsListView = lookup("#lvPaths").queryListView();
        assertEquals(1, pathsListView.getItems().size());
    }

    @Test
    public void shouldRaiseAlertWhenAllPathsExplored() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchSimulationDialog();
        writeToTextField("010");
        clickSimulate();
        clickPlay();
        // Should take ~6 seconds to explore all paths on the default speed
        WaitForAsyncUtils.sleep(6500, TimeUnit.MILLISECONDS);
        // Sometimes the alert is delayed
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertTrue(lookup(".alert").tryQuery().isPresent());
    }

    @Test
    public void simulatingWithoutInitialStateShouldRaiseAlert() {
        launchSimulationDialog();
        clickSimulate();
        // Sometimes the alert is delayed
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertTrue(lookup(".alert").tryQuery().isPresent());
        clickOK();
    }

    @Test
    public void shouldShowOnePathWhenRewoundToBeginning() {
        shouldRaiseAlertWhenAllPathsExplored();
        clickOK();
        clickRewind();
        // Should take ~6 seconds to rewind to the beginning on the default speed
        WaitForAsyncUtils.sleep(6500, TimeUnit.MILLISECONDS);
        ListView pathsListView = lookup("#lvPaths").queryListView();
        assertEquals(1, pathsListView.getItems().size());
    }

    @Test
    public void shouldShowSameSetOfPathsOnStepForwardThenStepBackward() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchSimulationDialog();
        writeToTextField("010");
        clickSimulate();
        clickStepForward();
        clickStepForward();
        // Record current paths being shown
        ObservableList currentPaths = lookup("#lvPaths").queryListView().getItems();
        // Take a step forward
        clickStepForward();
        // Take a step backward
        clickStepBackward();
        // Check that the same list of paths are being shown
        assertEquals(currentPaths, lookup("#lvPaths").queryListView().getItems());
    }

    @Test
    public void clickingSimulateTwiceShouldClearAllPaths() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchSimulationDialog();
        writeToTextField("010");
        clickSimulate();
        clickStepForward();
        clickSimulate();
        ListView pathsListView = lookup("#lvPaths").queryListView();
        assertEquals(0, pathsListView.getItems().size());
    }

    @Test
    public void shouldRaiseAlertOnLoopingPath() {
        loadTestFiniteAutomaton("twoStatesWithEmptyWordLoop.fsm");
        launchSimulationDialog();
        writeToTextField("010");
        clickSimulate();
        clickPlay();
        // Should take ~3 seconds to identify loop on the default speed and this input
        WaitForAsyncUtils.sleep(3500, TimeUnit.MILLISECONDS);
        // Sometimes the alert is delayed
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertTrue(lookup(".alert").tryQuery().isPresent());
    }

    @Test
    public void increasingSpeedShouldMakeExploringPathsQuicker() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchSimulationDialog();
        writeToTextField("010");
        clickSimulate();
        clickPlay();
        // Increase the speed by dragging the slider
        drag("#speedSlider").moveBy(-100, 0, Motion.DEFAULT).drag("#speedSlider").moveBy(100, 0, Motion.DEFAULT);
        // Should take ~1 seconds to explore all paths on the default speed
        WaitForAsyncUtils.sleep(1500, TimeUnit.MILLISECONDS);
        // Sometimes the alert is delayed
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertTrue(lookup(".alert").tryQuery().isPresent());
    }

    @Test
    public void shouldHighlightStuckPathOrangeAcceptedPathGreenRejectedPathRed() {
        loadTestFiniteAutomaton("input00ToGetStuckAcceptedAndRejected.fsm");
        launchSimulationDialog();
        writeToTextField("00");
        clickSimulate();
        clickStepForward();
        clickStepForward();
        // Click on the path that should be stuck and hence highlighted orange
        clickOnMatchedStringQuery("(Q0,00), (Q1,0)", MouseButton.PRIMARY);
        assertEquals(SimulationDialogController.stuckPathHighlight, getColourOfSelectedListCell());
        clickStepForward();
        clickStepForward();
        // Click on the path that should be rejected and hence highlighted red
        clickOnMatchedStringQuery("(Q0,00), (Q5,0), (Q6," + Unicode.EPSILON + ")", MouseButton.PRIMARY);
        assertEquals(SimulationDialogController.rejectedPathHighlight, getColourOfSelectedListCell());
        // Click on the path that should be accepted and hence highlighted green
        clickOnMatchedStringQuery("(Q0,00), (Q2,0), (Q3," + Unicode.EPSILON + ")", MouseButton.PRIMARY);
        assertEquals(SimulationDialogController.acceptedPathHighlight, getColourOfSelectedListCell());
    }

    @Test
    public void removingPathViaContextMenuShouldDeletePathFromListView() {
        loadTestFiniteAutomaton("input00ToGetStuckAcceptedAndRejected.fsm");
        launchSimulationDialog();
        writeToTextField("00");
        clickSimulate();
        clickStepForward();
        // Click on a path to delete
        String pathToDelete = "(Q0,00), (Q1,0)";
        clickOnMatchedStringQuery(pathToDelete, MouseButton.SECONDARY);
        clickOnMatchedStringQuery("Delete", MouseButton.PRIMARY);
        assertFalse(lookup("#lvPaths").queryListView().getItems().contains(pathToDelete));
    }

    @Test
    public void removingPathViaBackspaceKeyShouldDeletePathFromListView() {
        loadTestFiniteAutomaton("input00ToGetStuckAcceptedAndRejected.fsm");
        launchSimulationDialog();
        writeToTextField("00");
        clickSimulate();
        clickStepForward();
        // Click on a path to delete
        String pathToDelete = "(Q0,00), (Q1,0)";
        clickOnMatchedStringQuery(pathToDelete, MouseButton.PRIMARY);
        press(KeyCode.BACK_SPACE);
        assertFalse(lookup("#lvPaths").queryListView().getItems().contains(pathToDelete));
    }

    @Test
    public void removingAllPathsShouldResultInListDisplayingPlaceholderText() {
        loadTestFiniteAutomaton("input00ToGetStuckAcceptedAndRejected.fsm");
        launchSimulationDialog();
        writeToTextField("00");
        clickSimulate();
        clickStepForward();
        clickOnMatchedStringQuery("(Q0,00), (Q1,0)", MouseButton.SECONDARY);
        clickOnMatchedStringQuery("Delete", MouseButton.PRIMARY);
        clickOnMatchedStringQuery("(Q0,00), (Q5,0)", MouseButton.SECONDARY);
        clickOnMatchedStringQuery("Delete", MouseButton.PRIMARY);
        clickOnMatchedStringQuery("(Q0,00), (Q2,0)", MouseButton.SECONDARY);
        clickOnMatchedStringQuery("Delete", MouseButton.PRIMARY);
        assertEquals(0, lookup("#lvPaths").queryListView().getItems().size());
        verifyThat(lookup("#lblNoPaths").queryLabeled().getText(), containsString("no paths"));
    }

    @Test
    public void contextMenuShouldBeHiddenWhenNotRequestedOnListItem() {
        launchSimulationDialog();
        clickOnMatchedStringQuery("#lvPaths", MouseButton.SECONDARY);
        WaitForAsyncUtils.waitForFxEvents();
        ContextMenu contextMenuOfListView = lookup("#lvPaths").queryListView().getContextMenu();
        assertFalse(contextMenuOfListView.isShowing());
    }

    private Paint getColourOfSelectedListCell() {
        String selectedPath = String.valueOf(lookup("#lvPaths").queryListView().getSelectionModel().getSelectedItem());
        for (ListCell listCell : (Set<ListCell>) (Set) lookup("#lvPaths .list-cell:selected").queryAll()) {
            if (listCell.getText().equals(selectedPath)) {
                return listCell.getBackground().getFills().get(0).getFill();
            }
        }
        return Paint.valueOf(Color.BLACK.toString());
    }

    private void launchSimulationDialog() {
        clickOnMatchedStringQuery("#btnSimulateInput", MouseButton.PRIMARY);
    }

    private void writeToTextField(String input) {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(".dialog-pane .text-field");
        write(input);
    }

    private void clickSimulate() {
        clickOnMatchedStringQuery("#btnSimulate", MouseButton.PRIMARY);
    }

    private void clickPlay() {
        clickOnMatchedStringQuery("#btnPlay", MouseButton.PRIMARY);
    }

    private void clickRewind() {
        clickOnMatchedStringQuery("#btnRewind", MouseButton.PRIMARY);
    }

    private void clickStepForward() {
        clickOnMatchedStringQuery("#btnStepForward", MouseButton.PRIMARY);
    }

    private void clickStepBackward() {
        clickOnMatchedStringQuery("#btnStepBackward", MouseButton.PRIMARY);
    }

    private void clickOK() {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(".alert .button:default");
    }

    private void clickOnMatchedStringQuery(String query, MouseButton mouseButton) {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(query, mouseButton);
    }
}
