package main.java.toolkit.alphabetDialog;

import javafx.fxml.FXML;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import main.java.model.FiniteStateMachine;
import main.java.model.Transition;

import java.util.List;

/**
 * @author Mert Acar
 * <p>
 * Controller to handle actions on the alphabet dialog.
 * </p>
 */
public class AlphabetDialogController {
    @FXML
    private TextField tfInput;
    @FXML
    private Label lblAlphabet;

    private FiniteStateMachine finiteStateMachine;
    private Dialog<String> dialog;
    private Pane workspacePane;

    /**
     * Initialises the controller by storing a reference to the workspace pane, finite automaton, and dialog.
     * Then updates the label displaying the alphabet by querying the finite automaton.
     *
     * @param fsm    the finite automaton used by the toolkit
     * @param dialog that this controller controls
     * @param pane   representing the workspace
     */
    public void initAlphabetDialogController(FiniteStateMachine fsm, Dialog<String> dialog, Pane pane) {
        finiteStateMachine = fsm;
        this.dialog = dialog;
        workspacePane = pane;
        updateAlphabet();
        restrictTextFieldToAlphanumericCharacters();
        forceFocusOnTextField();
    }

    private void restrictTextFieldToAlphanumericCharacters() {
        tfInput.textProperty().addListener((observable, oldValue, newValue) -> {
            // Only allow the text field to ever contain at most a single alphanumeric character
            if (!newValue.matches("[a-zA-Z0-9]?")) tfInput.setText(oldValue);
        });
    }

    private void forceFocusOnTextField() {
        tfInput.focusedProperty().addListener((observable, oldValue, newValue) -> {
            // Request focus on the text field whenever it loses focus
            if (!newValue) tfInput.requestFocus();
        });
    }

    /**
     * Extracts the text from the text field and adds it to the finite automaton's alphabet, then updates the
     * label displaying the alphabet if it was added successfully.
     */
    @FXML
    private void onAddButtonClick() {
        String symbol = tfInput.getText().trim();
        if (!symbol.isEmpty() && finiteStateMachine.addSymbolToAlphabet(symbol.charAt(0))) {
            updateAlphabet();
        }
    }

    /**
     * Extracts the text from the text field and removes it from the finite automaton's alphabet, then updates the
     * label displaying the alphabet if it was removed successfully.
     */
    @FXML
    private void onRemoveButtonClick() {
        String symbol = tfInput.getText().trim();
        if (!symbol.isEmpty() && finiteStateMachine.removeSymbolFromAlphabet(symbol.charAt(0))) {
            removeTransitionsUsingSymbol(tfInput.getText());
            updateAlphabet();
        }
    }

    private void removeTransitionsUsingSymbol(String symbol) {
        List<Transition> toCheck = finiteStateMachine.getAllTransitions();
        for (Transition t : toCheck) {
            if (t.getSymbol().equals(symbol)) {
                finiteStateMachine.removeTransition(t.getFromState(), t);
                workspacePane.getChildren().remove(t.getOnScreenVisual());
            }
        }
    }

    private void updateAlphabet() {
        tfInput.clear();
        String alphabet = finiteStateMachine.getAlphabet().toString();
        lblAlphabet.setText(alphabet.substring(1, alphabet.length() - 1));
        // Size the window to fit the content as the alphabet label may grow bigger than the default window size
        dialog.getDialogPane().getScene().getWindow().sizeToScene();
    }
}
