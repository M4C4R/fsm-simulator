package main.java.shared;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import main.java.model.FiniteStateMachine;
import main.java.model.State;
import main.java.model.Transition;
import main.java.toolkit.Arrow;
import main.java.toolkit.ToolkitController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mert Acar
 * <p>
 * Provides functionality to load a finite automaton from file.
 * </p>
 */
public class LoadFSM {
    private static final Pattern loadBracesPattern = Pattern.compile("(?<=\\{)[^\\{]+(?=\\})");
    private static final Pattern loadArrayPattern = Pattern.compile("(?<=\\[)[^\\[]+(?=\\])");

    /**
     * Launches the systems file explorer allowing users to select a file to load, then loads the specified file
     * into the supplied finite automaton instance and pane.
     *
     * @param finiteStateMachine to load the file into
     * @param windowOwner        the parent window of the file chooser/error alert
     * @param workspacePane      to load the finite automaton into
     * @param toolkitController  of the active Toolkit
     */
    public static void loadFiniteStateMachineFromFile(FiniteStateMachine finiteStateMachine, Window windowOwner, Pane workspacePane, ToolkitController toolkitController) {
        File file = FileChooserExt.getLoadFileUsingFileExplorer("Load FSM", windowOwner,
                new FileChooser.ExtensionFilter("Finite State Machine file (*.fsm)", "*.fsm"));
        if (file != null) {
            loadFiniteStateMachineFromFile(finiteStateMachine, windowOwner, workspacePane, toolkitController, file);
        }
    }

    /**
     * Loads the specified file into the supplied finite automaton instance and pane.
     *
     * @param finiteStateMachine to load the file into
     * @param windowOwner        the parent window of the possible error alert
     * @param workspacePane      to load the finite automaton into
     * @param toolkitController  of the active Toolkit
     * @param file               that should be loaded
     */
    public static void loadFiniteStateMachineFromFile(FiniteStateMachine finiteStateMachine, Window windowOwner, Pane workspacePane, ToolkitController toolkitController, File file) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String lineFromFile;
            toolkitController.resetWorkspace();

            String initialStateLine = "";
            String acceptingStatesLine = "";

            // Read each line from the file to load the state of the finite automaton
            while ((lineFromFile = bufferedReader.readLine()) != null) {
                if (lineFromFile.contains("states")) loadStatesFromString(toolkitController, lineFromFile);
                else if (lineFromFile.contains("alphabet")) loadAlphabetFromString(finiteStateMachine, lineFromFile);
                else if (lineFromFile.contains("initialState")) initialStateLine = lineFromFile;
                else if (lineFromFile.contains("acceptingStates")) acceptingStatesLine = lineFromFile;
                else if (lineFromFile.contains("transitions"))
                    loadTransitionsFromString(finiteStateMachine, workspacePane, lineFromFile);
            }
            // Load both (initial and accepting states) simultaneously in one loop to be more efficient
            loadStateAttributesFromString(toolkitController, finiteStateMachine, initialStateLine, acceptingStatesLine);
        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert errorAlert = AlertCreator.createInitialisedAlert("Corrupt File", "Unable to load the file, seems to be corrupt!", Alert.AlertType.INFORMATION, windowOwner, Modality.WINDOW_MODAL);
                errorAlert.showAndWait();
            });
        }
    }

    private static void loadStatesFromString(ToolkitController toolkitController, String lineFromFile) {
        Matcher matcher = loadBracesPattern.matcher(lineFromFile);
        while (matcher.find()) {
            String[] content = matcher.group().split(",");
            toolkitController.createStateOnMouse(content[0], Double.parseDouble(content[1]) + State.RADIUS_OF_STATE, Double.parseDouble(content[2]) + State.RADIUS_OF_STATE, true);
        }
    }

    private static void loadAlphabetFromString(FiniteStateMachine finiteStateMachine, String lineFromFile) {
        Matcher matcher = loadArrayPattern.matcher(lineFromFile);
        if (matcher.find()) {
            String[] content = matcher.group().split(",");
            for (String symbol : content) {
                finiteStateMachine.addSymbolToAlphabet(symbol.trim().charAt(0));
            }
        }
    }

    private static void loadStateAttributesFromString(ToolkitController toolkitController, FiniteStateMachine finiteStateMachine, String initialStateLine, String acceptingStatesLine) {
        String initialState = ((initialStateLine.split("=").length == 1) ? "" : initialStateLine.split("=")[1]);
        String[] acceptingStates = new String[0];

        Matcher matcher = loadArrayPattern.matcher(acceptingStatesLine);
        if (matcher.find()) {
            acceptingStates = matcher.group().split(",");
        }

        // Locate the accepting states and initial state and update their attributes
        for (State state : finiteStateMachine.getStates()) {
            if (Arrays.asList(acceptingStates).contains(state.getLabel())) {
                Button btnState = (Button) state.getOnScreenVisual().getChildren().get(0);
                btnState.setId("btnAcceptingState");
                state.setAccepting(true);
            }
            if (state.getLabel().equals(initialState)) {
                finiteStateMachine.setInitialState(state);
                state.setInitial(true);
                ObservableList<Node> stateGroupChildren = state.getOnScreenVisual().getChildren();
                stateGroupChildren.add(toolkitController.generateInitialStateArrow((Button) stateGroupChildren.get(0)));
            }
        }
    }

    private static void loadTransitionsFromString(FiniteStateMachine finiteStateMachine, Pane workspacePane, String lineFromFile) {
        Matcher matcher = loadBracesPattern.matcher(lineFromFile);
        while (matcher.find()) {
            String[] content = matcher.group().split(",");
            State fromState = null;
            State toState = null;
            // Locate the from and to states for this transition
            for (State state : finiteStateMachine.getStates()) {
                if (state.getLabel().equals(content[0])) fromState = state;
                if (state.getLabel().equals(content[2])) toState = state;
            }

            if (fromState != null && toState != null) {
                // content[1] is the input symbol used by this transition
                Transition newTransition = new Transition(fromState, content[1], toState);
                if (finiteStateMachine.addTransition(fromState, newTransition)) {
                    Group visual = new Group();
                    workspacePane.getChildren().add(visual);
                    new Arrow(finiteStateMachine, newTransition, workspacePane, visual, Double.parseDouble(content[3]), Double.parseDouble(content[4]), Double.parseDouble(content[5]), Boolean.parseBoolean(content[6]));
                    newTransition.setOnScreenVisual(visual);
                    // Send the arrow behind the states
                    visual.toBack();
                }
            }
        }
    }
}
