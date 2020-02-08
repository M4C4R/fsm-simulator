package test.java.toolkit.transitionsDialog;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import main.java.model.State;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import test.java.toolkit.ToolkitTestSetup;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

/**
 * @author Mert Acar
 */
public class TransitionsDialogControllerTest extends ToolkitTestSetup {

    @Test
    public void addingTransitionShouldAppearOnWorkspace() {
        loadTestFiniteAutomaton("threeStatesWithNoTransitions.fsm");
        launchTransitionsDialog();
        // Select the first state in the drop-down list as the from state for the transition
        clickFromStateComboBox();
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        String selectedFromState = ((State) lookup("#cbFromState").queryComboBox().getSelectionModel().getSelectedItem()).getLabel();
        // Select the first symbol in the drop-down list as the symbol for the transition
        clickSymbolComboBox();
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        String selectedSymbol = lookup("#cbSymbol").queryComboBox().getSelectionModel().getSelectedItem().toString();
        // Select the second state in the drop-down list as the to state for the transition
        clickToStateComboBox();
        type(KeyCode.DOWN);
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        String selectedToState = ((State) lookup("#cbToState").queryComboBox().getSelectionModel().getSelectedItem()).getLabel();

        clickAddButton();
        // Check that a transition with the specified symbol and states has been defined
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> assertTrue(isPatternOfPathInListView(selectedFromState + ".+" + selectedSymbol + ".+" + selectedToState)));
    }

    @Test
    public void addingReflexiveTransitionShouldAppearOnWorkspace() {
        loadTestFiniteAutomaton("threeStatesWithNoTransitions.fsm");
        launchTransitionsDialog();
        // Select the first state in the drop-down list as the from state for the transition
        clickFromStateComboBox();
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        String selectedFromState = ((State) lookup("#cbFromState").queryComboBox().getSelectionModel().getSelectedItem()).getLabel();
        // Select the first symbol in the drop-down list as the symbol for the transition
        clickSymbolComboBox();
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        String selectedSymbol = lookup("#cbSymbol").queryComboBox().getSelectionModel().getSelectedItem().toString();
        // Select the first state in the drop-down list as the to state for the transition
        clickToStateComboBox();
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        String selectedToState = ((State) lookup("#cbToState").queryComboBox().getSelectionModel().getSelectedItem()).getLabel();

        clickAddButton();
        // Check that a transition with the specified symbol and states has been defined
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> assertTrue(isPatternOfPathInListView(selectedFromState + ".+" + selectedSymbol + ".+" + selectedToState)));
    }

    @Test
    public void removingTransitionViaButtonShouldRemoveTransitionFromWorkspace() {
        String patternForTransitionToDelete = "Q3.+0.+Q1";
        launchTransitionsDialogThenClickOnMatchedPattern(patternForTransitionToDelete);
        clickRemoveButton();
        // Check that the transition with the specified pattern has been deleted
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> assertFalse(isPatternOfPathInListView(patternForTransitionToDelete)));
    }

    @Test
    public void removingTransitionViaBackspaceKeyShouldRemoveTransitionFromWorkspace() {
        String patternForTransitionToDelete = "Q0.+0.+Q1";
        launchTransitionsDialogThenClickOnMatchedPattern(patternForTransitionToDelete);
        type(KeyCode.BACK_SPACE);
        // Check that the transition with the specified pattern has been deleted
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> assertFalse(isPatternOfPathInListView(patternForTransitionToDelete)));
    }

    @Test
    public void removingTransitionViaDeleteKeyShouldRemoveTransitionFromWorkspace() {
        String patternForTransitionToDelete = "Q3.+0.+Q1";
        launchTransitionsDialogThenClickOnMatchedPattern(patternForTransitionToDelete);
        type(KeyCode.DELETE);
        // Check that the transition with the specified pattern has been deleted
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> assertFalse(isPatternOfPathInListView(patternForTransitionToDelete)));
    }

    @Test
    public void removingTransitionViaContextMenuShouldRemoveTransitionFromWorkspace() {
        String patternForTransitionToDelete = "Q0.+0.+Q1";
        launchTransitionsDialogThenClickOnMatchedPattern(patternForTransitionToDelete, MouseButton.SECONDARY);
        clickOnMatchedStringQuery("Delete", MouseButton.PRIMARY);
        // Check that the transition with the specified pattern has been deleted
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> assertFalse(isPatternOfPathInListView(patternForTransitionToDelete)));
    }

    @Test
    public void addingTransitionBeforeSelectingStatesAndSymbolShouldRaiseAlert() {
        launchTransitionsDialog();
        clickAddButton();
        WaitForAsyncUtils.waitForFxEvents();
        // Sometimes the alert is delayed
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertTrue(lookup(".alert").tryQuery().isPresent());
    }

    @Test
    public void removingTransitionBeforeSelectingTransitionShouldRaiseAlert() {
        launchTransitionsDialog();
        clickRemoveButton();
        WaitForAsyncUtils.waitForFxEvents();
        // Sometimes the alert is delayed
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
        assertTrue(lookup(".alert").tryQuery().isPresent());
    }

    @Test
    public void contextMenuShouldBeHiddenWhenNotRequestedOnListItem() {
        launchTransitionsDialog();
        clickOnMatchedStringQuery("#lvTransitions", MouseButton.SECONDARY);
        WaitForAsyncUtils.waitForFxEvents();
        ContextMenu contextMenuOfListView = lookup("#lvTransitions").queryListView().getContextMenu();
        assertFalse(contextMenuOfListView.isShowing());
    }

    @Test
    public void shouldPreventZoomFactorGreaterThanOne() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchTransitionsDialog();
        clickOnMatchedStringQuery("#tableViewTab", MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        SpreadsheetView spreadsheetView = lookup("#svTransitions").queryAs(SpreadsheetView.class);
        spreadsheetView.setZoomFactor(2.0);
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        assertFalse(spreadsheetView.getZoomFactor() > 1);
    }

    private boolean isPatternOfPathInListView(String pattern) {
        return lookup("#lvTransitions").queryListView().getItems().removeIf(transition -> transition.toString().matches(pattern));
    }

    private void launchTransitionsDialogThenClickOnMatchedPattern(String pattern) {
        launchTransitionsDialogThenClickOnMatchedPattern(pattern, MouseButton.PRIMARY);
    }

    private void launchTransitionsDialogThenClickOnMatchedPattern(String pattern, MouseButton mouseButton) {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchTransitionsDialog();
        clickOnMatchedPattern(pattern, mouseButton);
    }

    private void launchTransitionsDialog() {
        clickOnMatchedStringQuery("#btnTransitions", MouseButton.PRIMARY);
    }

    private void clickFromStateComboBox() {
        clickOnMatchedStringQuery("#cbFromState", MouseButton.PRIMARY);
    }

    private void clickSymbolComboBox() {
        clickOnMatchedStringQuery("#cbSymbol", MouseButton.PRIMARY);
    }

    private void clickToStateComboBox() {
        clickOnMatchedStringQuery("#cbToState", MouseButton.PRIMARY);
    }

    private void clickAddButton() {
        clickOnMatchedStringQuery("#btnAdd", MouseButton.PRIMARY);
    }

    private void clickRemoveButton() {
        clickOnMatchedStringQuery("#btnRemove", MouseButton.PRIMARY);
    }

    private void clickOnMatchedStringQuery(String query, MouseButton mouseButton) {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(query, mouseButton);
    }

    private void clickOnMatchedPattern(String pattern, MouseButton mouseButton) {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn(hasText(matchesPattern(pattern)), mouseButton);
    }
}
