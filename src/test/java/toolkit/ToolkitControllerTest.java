package test.java.toolkit;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import main.java.model.FiniteStateMachine;
import main.java.model.State;
import main.java.toolkit.simulationDialog.SimulationDialogController;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * @author Mert Acar
 */
public class ToolkitControllerTest extends ToolkitTestSetup {

    private FiniteStateMachine finiteStateMachine;

    @Before
    public void getFiniteStateMachineReference() {
        finiteStateMachine = toolkitController.getFiniteStateMachine();
    }

    @Test
    public void pressingBackShouldLaunchMainMenu() {
        // Check that we are currently on the toolkit and not on the main menu
        assertEquals("Workspace", lookup("#title").queryText().getText());
        clickOnMatchedStringQuery("#btnBack", MouseButton.PRIMARY);
        // Check that the main menu has been launched
        assertEquals("Finite Automata Visual Toolkit", lookup("#title").queryText().getText());
    }

    @Test
    public void pressingLoadShouldLaunchFileExplorer() {
        testForLossOfFocusOnClick("#btnLoad");
        closeFileChooser();
    }

    @Test
    public void pressingSaveShouldLaunchFileExplorer() {
        testForLossOfFocusOnClick("#btnSave");
        closeFileChooser();
    }

    @Test
    public void pressingExportShouldLaunchFileExplorer() {
        testForLossOfFocusOnClick("#btnExport");
        closeFileChooser();
    }

    @Test
    public void addingInitialStateToWorkspaceShouldUpdateFiniteStateMachine() {
        clickOnMatchedStringQuery("#cbTools", MouseButton.PRIMARY);
        clickOnMatchedMatcherQuery(hasText(matchesPattern("Initial State")), MouseButton.PRIMARY);
        // Check that no initial state has been defined.
        assertFalse(finiteStateMachine.hasInitialState());
        clickOnMatchedStringQuery("#workspacePane", MouseButton.PRIMARY);
        // Check that an initial state now exists.
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertTrue(finiteStateMachine.hasInitialState());
    }

    @Test
    public void addingAcceptingStateToWorkspaceShouldUpdateFiniteStateMachine() {
        clickOnMatchedStringQuery("#cbTools", MouseButton.PRIMARY);
        clickOnMatchedMatcherQuery(hasText(matchesPattern("Accepting State")), MouseButton.PRIMARY);
        // Check that no accepting states have been defined.
        assertTrue(finiteStateMachine.getAcceptingStates().isEmpty());
        clickOnMatchedStringQuery("#workspacePane", MouseButton.PRIMARY);
        // Check that an accepting state now exists.
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertFalse(finiteStateMachine.getAcceptingStates().isEmpty());
    }

    @Test
    public void addingStateToWorkspaceShouldUpdateFiniteStateMachine() {
        clickOnMatchedStringQuery("#cbTools", MouseButton.PRIMARY);
        clickOnMatchedMatcherQuery(hasText(matchesPattern("State")), MouseButton.PRIMARY);
        // Check that no states have been defined.
        assertTrue(finiteStateMachine.getStates().isEmpty());
        clickOnMatchedStringQuery("#workspacePane", MouseButton.PRIMARY);
        // Check that a state now exists.
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertFalse(finiteStateMachine.getStates().isEmpty());
    }

    @Test
    public void addingMultipleStatesToWorkspaceShouldUpdateFiniteStateMachine() {
        clickOnMatchedStringQuery("#cbTools", MouseButton.PRIMARY);
        clickOnMatchedMatcherQuery(hasText(matchesPattern("State")), MouseButton.PRIMARY);
        // Check that no states have been defined.
        assertTrue(finiteStateMachine.getStates().isEmpty());
        clickOnMatchedStringQuery("#workspacePane", MouseButton.PRIMARY);
        moveBy(100, 0).clickOn(MouseButton.PRIMARY);
        moveBy(0, -100).clickOn(MouseButton.PRIMARY);
        moveBy(-100, 0).clickOn(MouseButton.PRIMARY);
        // Check that four states now exists.
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertEquals(4, finiteStateMachine.getStates().size());
    }

    @Test
    public void pressingBackWithUnsavedWorkShouldLaunchPrompt() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        clickOnMatchedStringQuery("#btnBack", MouseButton.PRIMARY);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertTrue(lookup(".alert").tryQuery().isPresent());
        clickOnMatchedStringQuery("Cancel", MouseButton.PRIMARY);
    }

    @Test
    public void pressingBackWithSimulationOpenShouldCloseSimulationDialog() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        clickOnMatchedStringQuery("#btnSimulateInput", MouseButton.PRIMARY);
        // Check that the simulation is showing
        assertTrue(SimulationDialogController.isDialogShowing);
        clickOnMatchedStringQuery("#btnBack", MouseButton.PRIMARY);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        clickOnMatchedStringQuery("Continue", MouseButton.PRIMARY);
        // Check that the simulation is now closed
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertFalse(SimulationDialogController.isDialogShowing);
    }

    @Test
    public void pressingClearShouldEmptyFiniteAutomaton() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        // Check that the finite state machine is currently not empty
        assertFalse(finiteStateMachine.getStates().isEmpty());
        clickOnMatchedStringQuery("#btnClear", MouseButton.PRIMARY);
        clickOnMatchedStringQuery("Continue", MouseButton.PRIMARY);
        // Check that the finite state machine is now empty
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertTrue(finiteStateMachine.getStates().isEmpty());
    }

    @Test
    public void addingMoreThanOneInitialStateToWorkspaceShouldRaiseAlert() {
        clickOnMatchedStringQuery("#cbTools", MouseButton.PRIMARY);
        clickOnMatchedMatcherQuery(hasText(matchesPattern("Initial State")), MouseButton.PRIMARY);
        clickOnMatchedStringQuery("#workspacePane", MouseButton.PRIMARY);
        moveBy(100, 0, Motion.DEFAULT);
        clickOn(MouseButton.PRIMARY);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertTrue(lookup(".alert").tryQuery().isPresent());
    }

    @Test
    public void draggingStateShouldMoveIt() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        Button btnState = lookup("#btnState").queryAs(Button.class);
        double currentX = btnState.getLayoutX();
        drag(btnState, MouseButton.PRIMARY).moveBy(100, 0).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertEquals((int) currentX + 100, (int) btnState.getLayoutX());
    }

    @Test
    public void togglingInitialStateViaContextMenuShouldUpdateFiniteStateMachine() {
        clickOnMatchedStringQuery("#cbTools", MouseButton.PRIMARY);
        clickOnMatchedMatcherQuery(hasText(matchesPattern("Initial State")), MouseButton.PRIMARY);
        clickOnMatchedStringQuery("#workspacePane", MouseButton.PRIMARY);
        assertTrue(finiteStateMachine.hasInitialState());
        clickOnMatchedStringQuery("#btnState", MouseButton.SECONDARY);
        clickOnMatchedStringQuery("Toggle Initial State", MouseButton.PRIMARY);
        assertFalse(finiteStateMachine.hasInitialState());
    }

    @Test
    public void togglingAcceptingStateViaContextMenuShouldUpdateFiniteStateMachine() {
        clickOnMatchedStringQuery("#cbTools", MouseButton.PRIMARY);
        clickOnMatchedMatcherQuery(hasText(matchesPattern("Accepting State")), MouseButton.PRIMARY);
        clickOnMatchedStringQuery("#workspacePane", MouseButton.PRIMARY);
        assertEquals(1, finiteStateMachine.getAcceptingStates().size());
        clickOnMatchedStringQuery("#btnAcceptingState", MouseButton.SECONDARY);
        clickOnMatchedStringQuery("Toggle Accepting", MouseButton.PRIMARY);
        assertEquals(0, finiteStateMachine.getAcceptingStates().size());
    }

    @Test
    public void togglingDeleteViaContextMenuShouldUpdateFiniteStateMachine() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        int numberOfStates = finiteStateMachine.size();
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnState", MouseButton.SECONDARY).clickOn("Delete");
        assertEquals(--numberOfStates, finiteStateMachine.size());
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnState", MouseButton.SECONDARY).clickOn("Delete");
        assertEquals(--numberOfStates, finiteStateMachine.size());
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnState", MouseButton.SECONDARY).clickOn("Delete");
        assertEquals(--numberOfStates, finiteStateMachine.size());
    }

    @Test
    public void togglingInitialStateWhileAnotherInitialStateExistsViaContextMenuShouldUpdateFiniteStateMachine() {
        clickOnMatchedStringQuery("#cbTools", MouseButton.PRIMARY);
        clickOnMatchedMatcherQuery(hasText(matchesPattern("Accepting State")), MouseButton.PRIMARY);
        clickOnMatchedStringQuery("#workspacePane", MouseButton.PRIMARY);

        clickOnMatchedStringQuery("#cbTools", MouseButton.PRIMARY);
        clickOnMatchedMatcherQuery(hasText(matchesPattern("State")), MouseButton.PRIMARY);
        moveTo("#workspacePane").moveBy(100, 0).clickOn(MouseButton.PRIMARY);
        // Now an accepting state and a standard state should exist on the workspace

        clickOnMatchedStringQuery("#btnState", MouseButton.SECONDARY);
        clickOnMatchedStringQuery("Toggle Initial State", MouseButton.PRIMARY);
        // Now the standard state is also the initial state.
        State initialState = finiteStateMachine.getInitialState();

        clickOnMatchedStringQuery("#btnAcceptingState", MouseButton.SECONDARY);
        clickOnMatchedStringQuery("Toggle Initial State", MouseButton.PRIMARY);
        // Now the accepting state is the initial state.
        assertNotEquals(initialState, finiteStateMachine.getInitialState());
    }

    @Test
    public void draggingReflexiveArrowShouldMoveIt() {
        loadTestFiniteAutomaton("singleStateWithReflexiveArrow.fsm");
        Label arrowLabel = lookup("#lblSymbol").queryAs(Label.class);
        double currentX = arrowLabel.getLayoutX();
        drag(lookup("#lblSymbol").queryAs(Label.class)).moveBy(0, 150).release(MouseButton.PRIMARY);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertNotEquals(arrowLabel.getLayoutX(), currentX);
    }

    @Test
    public void togglingDeleteOnReflexiveArrowViaContextMenuShouldDeleteIt() {
        loadTestFiniteAutomaton("singleStateWithReflexiveArrow.fsm");
        clickOn("#lblSymbol", MouseButton.SECONDARY);
        assertEquals(1, finiteStateMachine.getAllTransitions().size());
        clickOnMatchedStringQuery("Delete", MouseButton.PRIMARY);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertEquals(0, finiteStateMachine.getAllTransitions().size());
    }

    @Test
    public void togglingDeleteOnArrowViaContextMenuShouldDeleteIt() {
        loadTestFiniteAutomaton("twoStatesWithEmptyWordLoop.fsm");
        clickOn("#lblSymbol", MouseButton.SECONDARY);
        assertEquals(2, finiteStateMachine.getAllTransitions().size());
        clickOnMatchedStringQuery("Delete", MouseButton.PRIMARY);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertEquals(1, finiteStateMachine.getAllTransitions().size());
    }

    private void testForLossOfFocusOnClick(String query) {
        // Check that the workspace currently has the focus
        clickOnMatchedStringQuery("#workspacePane", MouseButton.PRIMARY);
        assertTrue(stage.isFocused());
        // Click on the specified element
        clickOnMatchedStringQuery(query, MouseButton.PRIMARY);
        // The workspace should have lost focus due to the click
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertFalse(stage.isFocused());
    }

    private void closeFileChooser() {
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        press(KeyCode.ESCAPE);
    }

    private void clickOnMatchedStringQuery(String query, MouseButton mouseButton) {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(query, mouseButton);
    }

    private void clickOnMatchedMatcherQuery(Matcher matcher, MouseButton mouseButton) {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(matcher, mouseButton);
    }
}
