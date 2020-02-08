package main.java.shared;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import main.java.model.FiniteStateMachine;
import main.java.model.State;
import main.java.model.Transition;
import main.java.toolkit.Arrow;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * @author Mert Acar
 * <p>
 * Provides functionality to save a specified finite automaton to file.
 * </p>
 */
public class SaveFSM {
    /**
     * Launches the systems file explorer allowing users to select a save location, then saves the specified finite
     * automaton to file.
     *
     * @param finiteStateMachine which is to be saved
     * @param windowOwner        the parent window of the file chooser/error alert
     */
    public static void saveFiniteStateMachineToFile(FiniteStateMachine finiteStateMachine, Window windowOwner) {
        File file = FileChooserExt.getSaveFileUsingFileExplorer("Save FSM", "My_FiniteStateMachine.fsm", windowOwner,
                new FileChooser.ExtensionFilter("Finite State Machine file (*.fsm)", "*.fsm"));
        // Specify the precision of doubles
        DecimalFormat precisionOfFourFormat = new DecimalFormat("#.0000");

        // Check that the location to save to has been selected
        if (file != null) {
            try {
                PrintWriter writer = new PrintWriter(file, "UTF-8");
                saveFSMStates(finiteStateMachine, writer, precisionOfFourFormat);
                saveFSMAlphabet(finiteStateMachine, writer);
                saveFSMInitialState(finiteStateMachine, writer);
                saveFSMAcceptingStates(finiteStateMachine, writer);
                saveFSMTransitions(finiteStateMachine, writer, precisionOfFourFormat);
                writer.close();
            } catch (IOException e) {
                Alert errorAlert = AlertCreator.createInitialisedAlert("Unexpected Error", "Unable to save the finite automaton!", Alert.AlertType.INFORMATION, windowOwner, Modality.WINDOW_MODAL);
                Platform.runLater(errorAlert::showAndWait);
            }
        }
    }

    private static void saveFSMStates(FiniteStateMachine finiteStateMachine, PrintWriter writer, DecimalFormat formatter) {
        writer.print("states=");
        for (State state : finiteStateMachine.getStates()) {
            Button btnState = (Button) state.getOnScreenVisual().getChildren().get(0);
            writer.print("{" + state.getLabel() + "," + formatter.format(btnState.getLayoutX()) + "," + formatter.format(btnState.getLayoutY()) + "}");
        }
    }

    private static void saveFSMAlphabet(FiniteStateMachine finiteStateMachine, PrintWriter writer) {
        writer.print("\nalphabet=");
        if (!finiteStateMachine.getAlphabet().isEmpty()) {
            writer.print(finiteStateMachine.getAlphabet());
        }
    }

    private static void saveFSMInitialState(FiniteStateMachine finiteStateMachine, PrintWriter writer) {
        writer.print("\ninitialState=");
        if (finiteStateMachine.hasInitialState()) {
            writer.print(finiteStateMachine.getInitialState().getLabel());
        }
    }

    private static void saveFSMAcceptingStates(FiniteStateMachine finiteStateMachine, PrintWriter writer) {
        writer.print("\nacceptingStates=[");
        if (finiteStateMachine.getAcceptingStates().isEmpty()) {
            writer.print("]");
        } else {
            StringBuilder acceptingStates = new StringBuilder();
            for (State state : finiteStateMachine.getAcceptingStates()) {
                acceptingStates.append(state.getLabel()).append(",");
            }
            writer.print(acceptingStates.substring(0, acceptingStates.length() - 1) + "]");
        }
    }

    private static void saveFSMTransitions(FiniteStateMachine finiteStateMachine, PrintWriter writer, DecimalFormat formatter) {
        writer.print("\ntransitions=");
        for (Transition transition : finiteStateMachine.getAllTransitions()) {
            Arrow arrow = (Arrow) transition.getOnScreenVisual().getChildren().get(0);
            writer.print("{" + transition.getFromState().getLabel() + "," + transition.getSymbol() + "," + transition.getToState().getLabel() +
                    "," + formatter.format(arrow.getPercentageBetweenStates()) + "," + formatter.format(arrow.getPerpendicularDistanceToAnchor()) +
                    "," + formatter.format(arrow.getAnchorAngle()) + "," + arrow.isAnchorPointSetByUser() + "}");
        }
    }
}
