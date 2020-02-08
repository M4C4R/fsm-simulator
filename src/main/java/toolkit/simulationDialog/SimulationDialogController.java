package main.java.toolkit.simulationDialog;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Duration;
import main.java.model.FiniteStateMachine;
import main.java.model.State;
import main.java.model.Transition;
import main.java.shared.AlertCreator;
import main.java.shared.Unicode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author Mert Acar
 * <p>
 * Controller which handles actions that take place on the simulation dialog.
 * </p>
 */
public class SimulationDialogController {
    /**
     * Specifies whether the dialog that this controller controls is showing or not.
     */
    public static boolean isDialogShowing;
    // Colours used to highlight significant paths within the list-view
    public static Color acceptedPathHighlight = Color.rgb(0, 204, 0);
    public static Color rejectedPathHighlight = Color.rgb(255, 51, 51);
    public static Color stuckPathHighlight = Color.rgb(255, 140, 0);

    private static final int DEFAULT_SPEED = 1000; // in milliseconds
    private static final int MAX_NUMBER_OF_PATHS = 10000;
    private static final int DEFAULT_STEPS_CAP = 300; // Default limit
    private static final int STEPS_CAP_FOR_INFINITE_PATHS = 100;
    private static int currentStepsCap;

    @FXML
    private ListView<String> lvPaths;
    @FXML
    private Label lblPathCount;
    @FXML
    private Label lblStepNumber;
    @FXML
    private Label lblSpeed;
    @FXML
    private Slider speedSlider;
    @FXML
    private TextField tfInputWord;
    @FXML
    private ToggleButton btnSimulate;
    @FXML
    private Button btnPlay;
    @FXML
    private Button btnPause;
    @FXML
    private Button btnRewind;
    @FXML
    private Button btnStepForward;
    @FXML
    private Button btnStepBackward;
    @FXML
    private ProgressIndicator timerIndicator;

    private ToolBar toolkitToolBar;
    private FiniteStateMachine finiteStateMachine;
    private Pane workspacePane;
    private Dialog dialog;
    private Button[] outerWorkspaceButtons;

    private ArrayList<ArrayList<State>> paths;
    private HashMap<Integer, ArrayList<ArrayList<State>>> endedPathsHistory;
    private HashMap<Integer, ArrayList<Transition>> transitionsUsedHistory;
    private List<State> highlightedStates;
    private List<Transition> highlightedTransitions;

    private BooleanProperty isPlaying;
    private BooleanProperty isInputWordSet;
    private BooleanProperty isPathsEmpty;
    private IntegerProperty stepNumber;
    private boolean isPathsInfinite;
    private boolean isSpeedChanged;

    private Thread backgroundThread;
    private Timeline timeline;

    /**
     * Initialises the controller by storing references to the specified finite automaton, workspace pane, toolkit
     * toolbar, outer toolkit buttons, and the simulation dialog.
     * Sets up the required window and slider listeners as well as the context menu for entries in the list.
     *
     * @param fsm    the finite automaton used by the toolkit
     * @param pane   representing the workspace
     * @param toolbar     the {@code ToolBar} used by the toolkit
     * @param dialog the simulation dialog
     */
    public void initSimulationDialogController(FiniteStateMachine fsm, Pane pane, ToolBar toolbar, Button[] buttons, Dialog dialog) {
        finiteStateMachine = fsm;
        workspacePane = pane;
        toolkitToolBar = toolbar;
        this.dialog = dialog;
        outerWorkspaceButtons = buttons;
        initialiseFields();
        setUpWindowListener(dialog.getDialogPane().getScene().getWindow());
        restrictTextFieldToAlphanumerical();
        setUpDisablePropertyForSimulationControls();
        tfInputWord.requestFocus();
        setUpSliderListener();
        setUpContextMenu();
    }

    private void initialiseFields() {
        paths = new ArrayList<>();
        endedPathsHistory = new HashMap<>();
        transitionsUsedHistory = new HashMap<>();
        highlightedStates = new ArrayList<>();
        highlightedTransitions = new ArrayList<>();
        isPlaying = new SimpleBooleanProperty(false);
        isInputWordSet = new SimpleBooleanProperty(false);
        isPathsEmpty = new SimpleBooleanProperty(false);
        stepNumber = new SimpleIntegerProperty(0);
        timeline = new Timeline();
        timeline.setCycleCount(100);
        currentStepsCap = DEFAULT_STEPS_CAP;
    }

    private void restrictTextFieldToAlphanumerical() {
        tfInputWord.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z0-9]*")) {
                tfInputWord.setText(oldValue);
            }
        });
    }

    private void setUpWindowListener(Window window) {
        window.setOnCloseRequest(windowEvent -> {
            isDialogShowing = false;
            endSimulation();
        });
        window.setOnHiding(window.getOnCloseRequest());
    }

    private void setUpSliderListener() {
        speedSlider.valueProperty().addListener((observable) -> {
            if (backgroundThread != null && backgroundThread.isAlive()) {
                isSpeedChanged = true;
                backgroundThread.interrupt();
            }
        });
    }

    private void setUpContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction((actionEvent) -> removeSelectedPathFromListView());
        contextMenu.getItems().add(deleteItem);
        lvPaths.setContextMenu(contextMenu);
        // If the user requests the context menu without having selected an entry, we hide the context menu
        lvPaths.setOnContextMenuRequested((contextMenuEvent) -> {
            if (lvPaths.getSelectionModel().isEmpty()) {
                Platform.runLater(contextMenu::hide);
                contextMenuEvent.consume();
            }
        });
    }

    private void setUpDisablePropertyForSimulationControls() {
        BooleanBinding isPlayingOrWaitingOnInputWord = isPlaying.or(isInputWordSet.not());
        BooleanBinding isAtBeginning = stepNumber.lessThanOrEqualTo(0);

        btnPlay.disableProperty().bind(isPlayingOrWaitingOnInputWord.or(isPathsEmpty));
        btnStepForward.disableProperty().bind(isPlayingOrWaitingOnInputWord.or(isPathsEmpty));
        btnRewind.disableProperty().bind(isPlayingOrWaitingOnInputWord.or(isAtBeginning));
        btnStepBackward.disableProperty().bind(isPlayingOrWaitingOnInputWord.or(isAtBeginning));
        // We only want to press pause when a simulation is playing
        btnPause.disableProperty().bind(isPlaying.not());
        // We should be able to change the speed whenever we want as long as an input word has been set
        speedSlider.disableProperty().bind(isInputWordSet.not());
        lblSpeed.disableProperty().bind(isInputWordSet.not());
    }

    private void removeSelectedPathFromListView() {
        ArrayList<State> removedPath = paths.remove(lvPaths.getSelectionModel().getSelectedIndex());
        // Checking if the sub-path of the removed path still exists in our current search space (paths)
        removedPath.remove(removedPath.size() - 1);
        removedPath.remove(removedPath.size() - 1); // Remove twice in order to remove the word and state

        boolean isSubpathLost = true;
        for (int i = 0; i < paths.size(); i++) {
            if (removedPath.equals(paths.get(i).subList(0, stepNumber.getValue() * 2))) {
                isSubpathLost = false;
                break;
            }
        }
        // If the subpath has been lost, we need to record a note of that path to allow for backtracking
        if (isSubpathLost) {
            endedPathsHistory.get(stepNumber.getValue()).add(removedPath);
        }
        // Remove the transition used by this deleted path to ensure it doesn't get highlighted
        if (!transitionsUsedHistory.get(stepNumber.getValue()).isEmpty()) {
            transitionsUsedHistory.get(stepNumber.getValue()).remove(lvPaths.getSelectionModel().getSelectedIndex());
        }

        Platform.runLater(() -> lvPaths.getItems().remove(lvPaths.getSelectionModel().getSelectedIndex()));
        // Reset the highlighting
        if (highlightedTransitions.isEmpty()) {
            resetHighlightedStates();
            highlightEndedPaths();
            highlightCurrentStates();
        } else {
            resetHighlightedTransitions();
            highlightCurrentTransitions(stepNumber.getValue());
        }

        updatePathCountLabel();
        if (paths.isEmpty()) {
            onPauseClick();
            isPathsEmpty.setValue(true);
        }
    }

    private void endSimulation() {
        isPlaying.set(false);
        if (backgroundThread != null) {
            backgroundThread.interrupt();
        }
        lockInputTextField(false);
        setDisableEditingOnWorkspace(false);
        removeHighlights();
        clearCollections();
        stepNumber.setValue(0);
        isPathsInfinite = false;
        currentStepsCap = DEFAULT_STEPS_CAP;
        updateStatsLabels();
    }

    private synchronized void clearCollections() {
        paths.clear();
        endedPathsHistory.clear();
        transitionsUsedHistory.clear();
        Platform.runLater(() -> lvPaths.getItems().clear());
        // Call the Garbage Collector to clear our collections
        System.gc();
    }

    @FXML
    private void handleKeyPressOnListView(KeyEvent event) {
        if ((event.getCode().equals(KeyCode.BACK_SPACE) || event.getCode().equals(KeyCode.DELETE)) && !lvPaths.getSelectionModel().isEmpty()) {
            removeSelectedPathFromListView();
        }
    }

    @FXML
    private void onSimulateButtonClick() {
        if (btnSimulate.isSelected()) {
            setDisableEditingOnWorkspace(true);
            lockInputTextField(true);
            initialisePath(tfInputWord.getText());
        } else {
            endSimulation();
        }
    }

    @FXML
    private void onStepForwardClick() {
        handleSingleForwardStep();
    }

    @FXML
    private void onStepBackwardClick() {
        handleSingleBackwardStep();
    }

    @FXML
    private void onPlayClick() {
        startAutomaticSimulation("Playing Simulation", false);
    }

    @FXML
    private void onPauseClick() {
        isPlaying.set(false);
        stopProgressIndicator();
        if (backgroundThread != null && backgroundThread.isAlive()) {
            backgroundThread.interrupt();
        }
    }

    @FXML
    private void onRewindClick() {
        startAutomaticSimulation("Rewinding Simulation", true);
    }

    private void startAutomaticSimulation(String threadName, boolean isRewinding) {
        isPlaying.set(true);
        backgroundThread = new Thread(() -> {
            while (isPlaying.getValue() && isDialogShowing && ((!isRewinding && !paths.isEmpty() && stepNumber.getValue() < currentStepsCap && paths.size() < MAX_NUMBER_OF_PATHS) || (isRewinding && stepNumber.getValue() > 0))) {
                do {
                    try {
                        startProgressIndicator();
                        Thread.sleep(getDelayTimeFromSpeedSlider());
                        if (isRewinding) {
                            handleSingleBackwardStep();
                        } else {
                            handleSingleForwardStep();
                        }
                        isSpeedChanged = false;
                    } catch (InterruptedException e) {
                        // Pause was clicked or dialog was closed
                    }
                } while (isSpeedChanged);
            }
            isPlaying.set(false);
            stopProgressIndicator();
            checkIfStepLimitReached();
            checkIfPathsLimitReached();
        });
        backgroundThread.setDaemon(true);
        backgroundThread.setName(threadName);
        backgroundThread.start();
    }

    private void lockInputTextField(boolean value) {
        Platform.runLater(() -> tfInputWord.setDisable(value));
        isInputWordSet.set(value);
    }

    private void handleSingleForwardStep() {
        if (checkIfStepLimitReached() || checkIfPathsLimitReached() || paths.isEmpty()) {
            return;
        }

        // If we are highlighting states, then we need to process the next step
        if (highlightedTransitions.isEmpty()) {
            stepNumber.setValue(stepNumber.getValue() + 1);
            processSingleStep();
            if (!paths.isEmpty()) {
                resetHighlightedStates();
                highlightCurrentTransitions(stepNumber.getValue());
                resetHighlightedEndedPaths();
                addFormattedPathsToListView();
                updateStatsLabels();
            } else {
                // If we have explored all paths, we prevent any further steps and return to the last state
                if (isPlaying.getValue()) {
                    onPauseClick();
                }
                handleSingleBackwardStep();
                isPathsEmpty.setValue(true);
                Platform.runLater(() -> AlertCreator.createInitialisedAlert("All Paths Explored", "All paths being simulated have ended!",
                        AlertType.INFORMATION, dialog.getDialogPane().getScene().getWindow(), Modality.WINDOW_MODAL).showAndWait());
            }
        } else {
            // If we are highlighting transitions, then we need to highlight the states next
            resetHighlightedTransitions();
            highlightEndedPaths();
            highlightCurrentStates();
        }
    }

    private void handleSingleBackwardStep() {
        // If we are highlighting states, then we need to highlight the previous transitions next
        if (highlightedTransitions.isEmpty() && !paths.isEmpty()) {
            resetHighlightedStates();
            highlightCurrentTransitions(stepNumber.getValue());
            resetHighlightedEndedPaths();
        } else {
            // If we are highlighting transitions, then we need to backtrack and highlight the states
            for (int i = 0; i < paths.size(); i++) {
                ArrayList<State> pathToBacktrack = paths.get(i);
                pathToBacktrack.remove(pathToBacktrack.size() - 1); // Remove the word label
                pathToBacktrack.remove(pathToBacktrack.size() - 1); // Remove the corresponding state
            }
            // Remove possible duplicate paths
            paths = new ArrayList<>(new LinkedHashSet<>(paths));

            // Recover all paths that have ended at this step
            if (!endedPathsHistory.get(stepNumber.getValue()).isEmpty()) {
                paths.addAll(endedPathsHistory.get(stepNumber.getValue()));
            }
            // Sort the paths in order to keep it in sync with the sorted transitions used history
            paths.sort(Comparator.comparing(o -> (o.get(o.size() - 4).getLabel() + o.get(o.size() - 2).getLabel())));

            stepNumber.setValue(stepNumber.getValue() - 1);
            resetHighlightedTransitions();
            highlightEndedPaths();
            highlightCurrentStates();
        }
        addFormattedPathsToListView();
        updateStatsLabels();
    }

    private void setDisableEditingOnWorkspace(boolean value) {
        Platform.runLater(() -> {
            toolkitToolBar.setDisable(value);
            workspacePane.setDisable(value);
            for (Button button : outerWorkspaceButtons) {
                button.setDisable(value);
            }
        });
    }

    /**
     * Initialises the first path in the search space using the specified input word.
     *
     * @param word to simulate
     */
    private void initialisePath(String word) {
        if (!finiteStateMachine.hasInitialState()) {
            Platform.runLater(() -> {
                btnSimulate.setSelected(false);
                AlertCreator.createInitialisedAlert("Prerequisite", "First define an initial state!",
                        AlertType.INFORMATION, dialog.getDialogPane().getScene().getWindow(), Modality.WINDOW_MODAL).showAndWait();
                onSimulateButtonClick();
            });
            return;
        }
        ArrayList<State> initialPath = new ArrayList<>();
        initialPath.add(finiteStateMachine.getInitialState());
        // If the input word is empty it means the user wishes to simulate the empty word
        initialPath.add(new State((word.length() == 0) ? Unicode.EPSILON : word, null));
        paths.add(initialPath);
        addFormattedPathsToListView();
        highlightEndedPaths();
        highlightCurrentStates();
        endedPathsHistory.put(stepNumber.getValue(), new ArrayList<>());
        transitionsUsedHistory.put(stepNumber.getValue(), new ArrayList<>());
        updateStatsLabels();
    }

    /**
     * Processes a single step on the remaining input word.
     */
    private void processSingleStep() {
        int numOfPaths = paths.size();
        transitionsUsedHistory.put(stepNumber.getValue(), new ArrayList<>());
        endedPathsHistory.put(stepNumber.getValue(), new ArrayList<>());

        for (int j = 0; j < numOfPaths; j++) {
            boolean isPathDeadEnd = true;
            ArrayList<State> toCheck = paths.remove(0);
            // We check the state at index size - 2 because the format of each path is [state, label, state, label, ...]
            List<Transition> availableTransitions = finiteStateMachine.getOutgoingTransitions(toCheck.get(toCheck.size() - 2));
            for (Transition t : availableTransitions) {
                // Check to see if the transition symbol matches the input symbol being observed, or if the transition symbol is the empty word
                if (t.getSymbol().equals(toCheck.get(toCheck.size() - 1).getLabel().charAt(0) + "") || t.getSymbol().equals(Unicode.EPSILON)) {
                    ArrayList<State> newPath = new ArrayList<>(toCheck);
                    newPath.add(t.getToState());
                    isPathDeadEnd = false;
                    transitionsUsedHistory.get(stepNumber.getValue()).add(t);

                    if (t.getSymbol().equals(Unicode.EPSILON)) {
                        // If an empty word transition was used, the input symbol has not been consumed
                        newPath.add(new State(toCheck.get(toCheck.size() - 1).getLabel(), null));

                        // Check if the path has looped
                        if (!isPathsInfinite) {
                            // Checking if this <state, word> pair has already been visited, if we have then there exists a loop
                            State wordToFind = newPath.get(newPath.size() - 1);
                            State stateToFind = newPath.get(newPath.size() - 2);

                            for (int i = toCheck.size() - 1; i > 0; i -= 2) {
                                if (!toCheck.get(i).equals(wordToFind)) {
                                    // If the word is not equal to the word we are looking for, we know that the rest of the pairs on the left won't be equal either
                                    break;
                                } else if (toCheck.get(i - 1).equals(stateToFind)) {
                                    isPathsInfinite = true;
                                    displayLoopDetectedDialog(newPath);
                                    currentStepsCap = stepNumber.getValue() + STEPS_CAP_FOR_INFINITE_PATHS;
                                    onPauseClick();
                                    break;
                                }
                            }
                        }
                    } else {
                        // Check if the remaining input word length is equal to 1, if it is we use the empty word
                        if (toCheck.get(toCheck.size() - 1).getLabel().length() == 1) {
                            newPath.add(new State(Unicode.EPSILON, null));
                        } else {
                            newPath.add(new State(toCheck.get(toCheck.size() - 1).getLabel().substring(1), null));
                        }
                    }
                    paths.add(newPath);
                }
            }
            if (isPathDeadEnd) {
                endedPathsHistory.get(stepNumber.getValue()).add(toCheck);
            }
        }
        // Sort both containers to keep them in sync (important for when users delete paths)
        paths.sort(Comparator.comparing(o -> (o.get(o.size() - 4).getLabel() + o.get(o.size() - 2).getLabel())));
        transitionsUsedHistory.get(stepNumber.getValue()).sort(Transition::compareTo);
    }

    private synchronized void addFormattedPathsToListView() {
        List<String> toAddToListView = new ArrayList<>(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            toAddToListView.add(pathToString(paths.get(i)));
        }
        Platform.runLater(() -> {
            if (isInputWordSet.getValue()) {
                lvPaths.getItems().setAll(toAddToListView);
            }
        });

        if (paths.isEmpty()) {
            isPathsEmpty.setValue(true);
        } else {
            isPathsEmpty.setValue(false);
        }

        if (lvPaths.getContextMenu().isShowing()) {
            Platform.runLater(() -> lvPaths.getContextMenu().hide());
        }
    }

    private String pathToString(ArrayList<State> path) {
        StringBuilder formattedPath = new StringBuilder();
        for (int k = 0; k < path.size(); k += 2) {
            formattedPath.append("(").append(path.get(k).getLabel()).append(",").append(path.get(k + 1).getLabel()).append("), ");
        }
        // Remove the extra comma and whitespace at the end
        return formattedPath.substring(0, formattedPath.length() - 2);
    }

    private boolean checkIfPathHasEnded(ArrayList<State> path) {
        List<Transition> availableTransitions = finiteStateMachine.getOutgoingTransitions(path.get(path.size() - 2));
        for (Transition t : availableTransitions) {
            // A path has not ended if the remaining input word is not the empty word and there is an applicable transition to take
            if (!path.get(path.size() - 1).getLabel().equals(Unicode.EPSILON) && (t.getSymbol().equals(Unicode.EPSILON) || t.getSymbol().equals(path.get(path.size() - 1).getLabel().charAt(0) + ""))) {
                return false;
            }
        }
        return true;
    }

    private synchronized void highlightEndedPaths() {
        // Stores the indices of ended paths that should be highlighted and whether or not they were accepted (true = accepted)
        HashMap<Integer, Boolean> indicesToHighlight = new HashMap<>();
        for (int i = 0; i < paths.size(); i++) {
            ArrayList<State> pathToCheck = paths.get(i);
            if (checkIfPathHasEnded(pathToCheck)) {
                if (pathToCheck.get(pathToCheck.size() - 1).getLabel().equals(Unicode.EPSILON) && pathToCheck.get(pathToCheck.size() - 2).isAccepting()) {
                    indicesToHighlight.put(i, true);
                } else {
                    indicesToHighlight.put(i, false);
                }
            }
        }
        Platform.runLater(() -> lvPaths.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(final ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(final String item, final boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setStyle(null);
                        } else {
                            setText(item);
                            if (indicesToHighlight.containsKey(getIndex())) {
                                if (indicesToHighlight.get(getIndex())) {
                                    // Highlight accepted paths green
                                    setBackground(new Background(new BackgroundFill(acceptedPathHighlight, new CornerRadii(5), new Insets(2))));
                                } else {
                                    if (item.contains(Unicode.EPSILON)) {
                                        // Highlight rejected paths red
                                        setBackground(new Background(new BackgroundFill(rejectedPathHighlight, new CornerRadii(5), new Insets(2))));
                                    } else {
                                        // Highlight stuck paths orange
                                        setBackground(new Background(new BackgroundFill(stuckPathHighlight, new CornerRadii(5), new Insets(2))));
                                    }
                                }
                            }
                        }
                    }
                };
            }
        }));
    }

    private synchronized void resetHighlightedEndedPaths() {
        Platform.runLater(() -> lvPaths.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(final ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(final String item, final boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle(null);
                        } else {
                            setText(item);
                        }
                    }
                };
            }
        }));
    }

    private synchronized void highlightCurrentStates() {
        for (int i = 0; i < paths.size(); i++) {
            ArrayList<State> pathToHighlight = paths.get(i);
            State current = pathToHighlight.get(pathToHighlight.size() - 2);
            Platform.runLater(() -> current.getOnScreenVisual().getChildren().get(0).setStyle("-fx-border-color: #CC9900;"));
            highlightedStates.add(current);
        }
    }

    private synchronized void resetHighlightedStates() {
        for (int i = 0; i < highlightedStates.size(); i++) {
            State stateToReset = highlightedStates.get(i);
            Platform.runLater(() -> stateToReset.getOnScreenVisual().getChildren().get(0).setStyle("-fx-border-color: #000000;"));
        }
        highlightedStates.clear();
    }

    private synchronized void highlightCurrentTransitions(int index) {
        if (transitionsUsedHistory.containsKey(index)) {
            ArrayList<Transition> transitionsToHighlight = transitionsUsedHistory.get(index);
            for (int i = 0; i < transitionsToHighlight.size(); i++) {
                ObservableList<Node> visualComponents = transitionsToHighlight.get(i).getOnScreenVisual().getChildren();
                Platform.runLater(() -> {
                    // Highlight the 0) arrow, 1) label, 2) arrowhead
                    visualComponents.get(0).setStyle(visualComponents.get(0).getStyle().replaceAll("-fx-stroke: #000000", "-fx-stroke: #CC9900"));
                    visualComponents.get(1).setStyle(visualComponents.get(1).getStyle().replaceAll("-fx-text-fill: #000000", "-fx-text-fill: #CC9900"));
                    visualComponents.get(2).setStyle(visualComponents.get(2).getStyle().replaceAll("-fx-fill: #000000", "-fx-fill: #CC9900"));
                });
                highlightedTransitions.add(transitionsToHighlight.get(i));
            }
        }
    }

    private synchronized void resetHighlightedTransitions() {
        for (int i = 0; i < highlightedTransitions.size(); i++) {
            ObservableList<Node> visualComponents = highlightedTransitions.get(i).getOnScreenVisual().getChildren();
            Platform.runLater(() -> {
                // Highlight the 0) arrow, 1) label, 2) arrowhead
                visualComponents.get(0).setStyle(visualComponents.get(0).getStyle().replaceAll("-fx-stroke: #CC9900", "-fx-stroke: #000000"));
                visualComponents.get(1).setStyle(visualComponents.get(1).getStyle().replaceAll("-fx-text-fill: #CC9900", "-fx-text-fill: #000000"));
                visualComponents.get(2).setStyle(visualComponents.get(2).getStyle().replaceAll("-fx-fill: #CC9900", "-fx-fill: #000000"));
            });
        }
        highlightedTransitions.clear();
    }

    private void removeHighlights() {
        resetHighlightedStates();
        resetHighlightedTransitions();
    }

    private long getDelayTimeFromSpeedSlider() {
        long selectedSpeed = Math.round(speedSlider.getValue());
        if (selectedSpeed < 0) {
            // When the value is less than 0, we want a delay in seconds e.g. value -2 implies 2000 milliseconds delay
            return Math.abs(selectedSpeed) * 1000;
        } else if (selectedSpeed > 0) {
            // When the value is greater than 0, we want a delay in milliseconds e.g. value 2 implies 800 milliseconds delay
            return (10 - selectedSpeed) * 100;
        }
        return DEFAULT_SPEED;
    }

    private boolean checkIfStepLimitReached() {
        if (stepNumber.getValue().equals(currentStepsCap) && !paths.isEmpty()) {
            Platform.runLater(() -> AlertCreator.createInitialisedAlert("Limit Reached!", "You have reached the cap of " + currentStepsCap + " steps for this input word!",
                    AlertType.INFORMATION, dialog.getDialogPane().getScene().getWindow(), Modality.WINDOW_MODAL).showAndWait());
            return true;
        }
        return false;
    }

    private boolean checkIfPathsLimitReached() {
        if (paths.size() >= MAX_NUMBER_OF_PATHS) {
            Platform.runLater(() -> AlertCreator.createInitialisedAlert("Limit Reached!", "You have reached the cap of " + MAX_NUMBER_OF_PATHS + " active paths!",
                    AlertType.INFORMATION, dialog.getDialogPane().getScene().getWindow(), Modality.WINDOW_MODAL).showAndWait());
            return true;
        }
        return false;
    }

    private void displayLoopDetectedDialog(ArrayList<State> loopingPath) {
        Platform.runLater(() -> {
            Alert loopDetectedAlert = AlertCreator.createInitialisedAlert("Loop Entered!", "", AlertType.INFORMATION, dialog.getDialogPane().getScene().getWindow(), Modality.WINDOW_MODAL);

            TextArea textArea = new TextArea(pathToString(loopingPath));
            textArea.setEditable(false);
            textArea.setPrefHeight(20);
            textArea.setMaxWidth(360);

            Label lblMessage = new Label("The following path has been identified to loop, this means that there are infinitely many paths. Hence, the number of steps onwards has been capped to " + STEPS_CAP_FOR_INFINITE_PATHS + ".");
            lblMessage.setWrapText(true);
            lblMessage.setMaxWidth(textArea.getMaxWidth());
            lblMessage.setTextAlignment(TextAlignment.CENTER);

            VBox vbox = new VBox(lblMessage, textArea);
            VBox.setMargin(textArea, new Insets(10, 0, -10, 0));

            loopDetectedAlert.getDialogPane().applyCss();
            loopDetectedAlert.getDialogPane().setContent(vbox);
            loopDetectedAlert.showAndWait();
        });
    }

    private void updatePathCountLabel() {
        Platform.runLater(() -> lblPathCount.setText("Active path count: " + paths.size()));
    }

    private void updateStepNumberLabel() {
        Platform.runLater(() -> lblStepNumber.setText("Step: " + stepNumber.getValue()));
    }

    private void updateStatsLabels() {
        updatePathCountLabel();
        updateStepNumberLabel();
    }

    private void startProgressIndicator() {
        stopProgressIndicator();
        // To keep the animation smooth, the progress indicator is incremented by 0.01 every delay/100 milliseconds and this is done 100 times
        timeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(getDelayTimeFromSpeedSlider() / 100), onFinished -> Platform.runLater(() -> timerIndicator.setProgress(timerIndicator.getProgress() + 0.01))));
        timeline.play();
    }

    private void stopProgressIndicator() {
        timeline.stop();
        Platform.runLater(() -> timerIndicator.setProgress(0.0));
    }
}
