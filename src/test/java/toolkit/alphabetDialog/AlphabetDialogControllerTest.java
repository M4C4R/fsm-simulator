package test.java.toolkit.alphabetDialog;

import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import test.java.toolkit.ToolkitTestSetup;

import java.util.Set;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.testfx.api.FxAssert.verifyThat;

/**
 * @author Mert Acar
 */
public class AlphabetDialogControllerTest extends ToolkitTestSetup {

    @Test
    public void clickingAddShouldAddInputToAlphabet() {
        launchAlphabetDialog();
        String symbol = "1";
        writeToTextField(symbol);
        clickAddButton();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(lookup("#lblAlphabet").queryLabeled().getText(), containsString(symbol));
    }

    @Test
    public void clickingRemoveShouldRemoveInputFromAlphabet() {
        launchAlphabetDialog();
        String symbol = "A";
        writeToTextField(symbol);
        clickAddButton();
        writeToTextField(symbol);
        clickRemoveButton();
        WaitForAsyncUtils.waitForFxEvents();
        verifyThat(lookup("#lblAlphabet").queryLabeled().getText(), not(containsString(symbol)));
    }

    @Test
    public void textFieldShouldRestrictInputToSingleAlphanumericalCharacter() {
        launchAlphabetDialog();
        String input = "1234567890";
        writeToTextField(input);
        // Check that the textfield only kept the first number of the input
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(lookup("#tfInput").queryTextInputControl().getText(), containsString(String.valueOf(input.charAt(0))));
        press(KeyCode.BACK_SPACE);

        writeToTextField("!\"£$%^&*()-_+[]{}'#@~<>,.?|\\¬`");
        // Check that the none of the special characters have been kept
        assertEquals(0, lookup("#tfInput").queryTextInputControl().getText().length());

        input = "abcdefghijklmnopqrstuvwxyz";
        writeToTextField(input);
        // Check that the textfield only kept the first letter of the input
        WaitForAsyncUtils.waitForFxEvents();
        assertThat(lookup("#tfInput").queryTextInputControl().getText(), containsString(String.valueOf(input.charAt(0))));
    }

    @Test
    public void removingSymbolShouldDeleteCorrespondingTransitions() {
        loadTestFiniteAutomaton("DFA_010_Suffix.fsm");
        launchAlphabetDialog();
        String symbol = "0";
        writeToTextField(symbol);
        clickRemoveButton();
        // Check that the transitions using the removed symbol have been deleted
        assertFalse(isSymbolPartOfAnExistingTransition(symbol));
    }

    private boolean isSymbolPartOfAnExistingTransition(String symbol) {
        Set<Label> labels = (Set<Label>) (Set) lookup("#lblSymbol").queryAll();
        return labels.removeIf(label -> label.getText().equals(symbol));
    }

    private void launchAlphabetDialog() {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnAlphabet");
    }

    private void writeToTextField(String input) {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#tfInput");
        write(input);
    }

    private void clickAddButton() {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnAdd");
    }

    private void clickRemoveButton() {
        WaitForAsyncUtils.waitForFxEvents();
        clickOn("#btnRemove");
    }
}
