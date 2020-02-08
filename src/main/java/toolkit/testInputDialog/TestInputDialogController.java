package main.java.toolkit.testInputDialog;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.util.Duration;
import main.java.model.FiniteStateMachine;
import main.java.model.State;
import main.java.model.Transition;
import main.java.shared.AlertCreator;
import main.java.shared.Unicode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author Mert Acar
 * <p>
 * Controller to handle actions on the test input word resultsDialog.
 * </p>
 */
public class TestInputDialogController {
    @FXML
    private Label lblResult;
    @FXML
    private Label lblOfPaths;
    @FXML
    private ListView<String> lvPaths;
    @FXML
    private Tooltip pathsTooltip;

    private FiniteStateMachine finiteStateMachine;
    private Dialog<String> resultsDialog;
    private Pane workspacePane;

    private ArrayList<ArrayList<State>> acceptedPaths;
    private ArrayList<ArrayList<State>> rejectedPaths;
    private boolean isAccepted;
    private boolean isPathsInfinite;

    /**
     * Initialises the controller by storing references to the specified finite automaton, workspace pane, toolkit
     * toolbar, outer toolkit buttons, and the simulation resultsDialog.
     * Sets up the required window and slider listeners as well as the context menu for entries in the list.
     *
     * @param fsm           the finite automaton used by the toolkit
     * @param requestDialog the resultsDialog to request the testing of an input word
     * @param resultDialog  the resultsDialog which will show the results of the tested input word
     * @param pane          representing the workspace
     */
    public void initTestInputWordDialogController(FiniteStateMachine fsm, Dialog<String> requestDialog, Dialog<String> resultDialog, Pane pane) {
        finiteStateMachine = fsm;
        resultsDialog = resultDialog;
        workspacePane = pane;

        resultsDialog.initModality(Modality.NONE);
        configureTooltipTimers();
        requestDialog.showAndWait().ifPresent(result -> testInputWord(result + ""));
        lblResult.prefWidthProperty().bind(resultsDialog.getDialogPane().widthProperty().subtract(20));
    }

    private void configureTooltipTimers() {
        try {
            Field behaviour = pathsTooltip.getClass().getDeclaredField("BEHAVIOR");
            behaviour.setAccessible(true);
            Object behaviourObject = behaviour.get(pathsTooltip);

            Field activationTimer = behaviourObject.getClass().getDeclaredField("activationTimer");
            Field hideTimer = behaviourObject.getClass().getDeclaredField("hideTimer");
            activationTimer.setAccessible(true);
            hideTimer.setAccessible(true);
            // Adjust the default timers used by the tooltip component
            ((Timeline) activationTimer.get(behaviourObject)).getKeyFrames().setAll(new KeyFrame(new Duration(200)));
            ((Timeline) hideTimer.get(behaviourObject)).getKeyFrames().setAll(new KeyFrame(new Duration(10000)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ArrayList<State>> initialiseSearchSpace(String word) {
        acceptedPaths = new ArrayList<>();
        rejectedPaths = new ArrayList<>();
        ArrayList<ArrayList<State>> workingPaths = new ArrayList<>();

        // Initialise the first path in the search space
        ArrayList<State> startPath = new ArrayList<>();
        startPath.add(finiteStateMachine.getInitialState());
        startPath.add(new State((word.length() == 0) ? Unicode.EPSILON : word, null));
        workingPaths.add(startPath);
        return workingPaths;
    }

    /**
     * Tests the specified input word on the machine and states whether it was accepted via the results resultsDialog.
     *
     * @param word to be processed on the finite automaton
     */
    private void testInputWord(String word) {
        // Check if an initial state has been defined
        if (!finiteStateMachine.hasInitialState()) {
            Alert errorAlert = AlertCreator.createInitialisedAlert("Result", "First define an initial state!", Alert.AlertType.INFORMATION, workspacePane.getScene().getWindow(), Modality.WINDOW_MODAL);
            Platform.runLater(errorAlert::showAndWait);
            return;
        }

        processWord(initialiseSearchSpace(word));

        if (word.length() == 0) {
            word = Unicode.EPSILON;
        }

        if (isAccepted) {
            lblResult.setText("Result: \'" + word + "\' has been Accepted!");
            lblOfPaths.setText("Accepted Paths*");
            // Remove possible duplicate paths
            acceptedPaths = new ArrayList<>(new LinkedHashSet<>(acceptedPaths));
            addFormattedPathsToListView(acceptedPaths);
        } else {
            lblResult.setText("Result: \'" + word + "\' has been Rejected!");
            lblOfPaths.setText("Rejected Paths*");
            // Remove possible duplicate paths
            rejectedPaths = new ArrayList<>(new LinkedHashSet<>(rejectedPaths));
            addFormattedPathsToListView(rejectedPaths);
        }

        if (!isPathsInfinite) {
            // Remove the asterisk if there is no ε-cycle
            lblOfPaths.setText(lblOfPaths.getText().substring(0, lblOfPaths.getText().length() - 1));
            // Remove the tooltip if there is no ε-cycle
            lblOfPaths.setTooltip(null);
            // Remove label styling (dotted underline and navy blue text) if there is no ε-cycle
            lblOfPaths.setStyle("-fx-border-style: hidden; -fx-text-fill: #000000");
        }
        Platform.runLater(() -> resultsDialog.showAndWait());
    }

    private void processWord(ArrayList<ArrayList<State>> workingPaths) {
        boolean isPathDeadEnd;
        int numOfPaths;

        while (!workingPaths.isEmpty()) {
            numOfPaths = workingPaths.size();
            for (int j = 0; j < numOfPaths; j++) {
                isPathDeadEnd = true;
                ArrayList<State> toCheck = workingPaths.remove(0);
                String remainingWordToBeProcessed = toCheck.get(toCheck.size() - 1).getLabel();
                State currentStateOfMachine = toCheck.get(toCheck.size() - 2);

                // We check the state at index size - 2 because the format of each path is [state, label, state, label, ...]
                List<Transition> availableTransitions = finiteStateMachine.getOutgoingTransitions(currentStateOfMachine);
                for (Transition t : availableTransitions) {
                    if (t.getSymbol().equals(remainingWordToBeProcessed.charAt(0) + "") || t.getSymbol().equals(Unicode.EPSILON)) {
                        ArrayList<State> newPath = new ArrayList<>(toCheck);
                        newPath.add(t.getToState());
                        isPathDeadEnd = false;
                        // If an empty word transition was used, the input symbol has not been consumed
                        if (t.getSymbol().equals(Unicode.EPSILON)) {
                            newPath.add(new State(remainingWordToBeProcessed, null));
                        } else {
                            // Check if the remaining input word length is equal to 1, if it is then consuming it results in the empty word
                            if (remainingWordToBeProcessed.length() == 1) {
                                newPath.add(new State(Unicode.EPSILON, null));
                            } else {
                                newPath.add(new State(remainingWordToBeProcessed.substring(1), null));
                            }
                        }
                        // Add this new path to our search space if it is not (infinitely) looping
                        if (!isPathLooping(newPath)) {
                            workingPaths.add(newPath);
                        }
                        // If our old path was processing the empty word, we consider the case of not using the transition
                        if (remainingWordToBeProcessed.equals(Unicode.EPSILON)) {
                            // Check the result of this path if we had stopped here
                            if (currentStateOfMachine.isAccepting()) {
                                isAccepted = true;
                                acceptedPaths.add(toCheck);
                            } else {
                                rejectedPaths.add(toCheck);
                            }
                        }
                    }
                }
                // If the path has ended (i.e. no where to go), check if it was accepting or not
                if (isPathDeadEnd) {
                    if (remainingWordToBeProcessed.equals(Unicode.EPSILON) && currentStateOfMachine.isAccepting()) {
                        isAccepted = true;
                        acceptedPaths.add(toCheck);
                    } else {
                        rejectedPaths.add(toCheck);
                    }
                }
            }
        }
    }

    private boolean isPathLooping(ArrayList<State> path) {
        if (path.size() <= 2) {
            return false;
        }
        // Checking if the latest <state, word> pair has already been visited, if we have then there exists a loop
        State wordToFind = path.get(path.size() - 1);
        State stateToFind = path.get(path.size() - 2);
        // We check from right to left on the computation
        for (int k = path.size() - 3; k > 0; k -= 2) {
            if (!path.get(k).equals(wordToFind)) {
                // If the word is not equal to the word we are looking for, we know that the rest of the pairs on the left won't be equal either
                return false;
            } else if (path.get(k - 1).equals(stateToFind)) {
                isPathsInfinite = true;
                return true;
            }
        }
        return false;
    }

    private void addFormattedPathsToListView(ArrayList<ArrayList<State>> paths) {
        List<String> toAddToListView = new ArrayList<>(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            ArrayList<State> pathToAdd = paths.get(i);
            StringBuilder formattedPath = new StringBuilder();
            for (int k = 0; k < pathToAdd.size(); k += 2) {
                formattedPath.append("(").append(pathToAdd.get(k).getLabel()).append(",").append(pathToAdd.get(k + 1).getLabel()).append("), ");
            }
            toAddToListView.add(formattedPath.substring(0, formattedPath.length() - 2));
        }
        Platform.runLater(() -> lvPaths.getItems().setAll(toAddToListView));
    }

    @FXML
    private void onOkButtonClick() {
        resultsDialog.close();
    }
}
