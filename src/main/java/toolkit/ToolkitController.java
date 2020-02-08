package main.java.toolkit;

import com.sun.javafx.stage.StageHelper;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.java.model.FiniteStateMachine;
import main.java.model.State;
import main.java.model.Transition;
import main.java.shared.AlertCreator;
import main.java.shared.CustomCursor;
import main.java.shared.DialogCreator;
import main.java.shared.ExportFSM;
import main.java.shared.FXMLManager;
import main.java.shared.LoadFSM;
import main.java.shared.SaveFSM;
import main.java.toolkit.alphabetDialog.AlphabetDialogController;
import main.java.toolkit.simulationDialog.SimulationDialogController;
import main.java.toolkit.testInputDialog.TestInputDialogController;
import main.java.toolkit.transitionsDialog.TransitionsDialogController;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;

/**
 * @author Mert Acar
 * <p>
 * Controller which handles actions on the Toolkit feature of the software.
 * </p>
 */
public class ToolkitController {
	@FXML
	private Pane workspacePane;
	@FXML
	private ScrollPane workspaceScrollPane;
	@FXML
	private ToolBar toolbar;
	@FXML
	private ComboBox<String> cbTools;
	@FXML
	private Button btnBack;
	@FXML
	private Button btnClear;
	@FXML
	private Button btnSave;
	@FXML
	private Button btnLoad;
	@FXML
	private Button btnExport;

	private final String[] TOOLS = {"Initial State", "State", "Accepting State"};
	private FiniteStateMachine finiteStateMachine;

	/**
	 * Instantiates and stores a new finite automaton.
	 */
	public ToolkitController() {
		finiteStateMachine = new FiniteStateMachine();
	}

	@FXML
	private void initialize() {
		cbTools.getItems().addAll(TOOLS);
		cbTools.setValue("Select Type");

		workspacePane.setCursor(Cursor.DEFAULT);

		// Prevent graphics outside the area of the workspace being shown
		Rectangle clipRect = new Rectangle();
		clipRect.heightProperty().bind(workspacePane.heightProperty());
		clipRect.widthProperty().bind(workspacePane.widthProperty());
		workspacePane.setClip(clipRect);

		// Once the scene has loaded, add a window listener to show a confirmation prompt when user tries exiting the app with unsaved work
		workspacePane.sceneProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				workspacePane.getScene().getWindow().setOnCloseRequest(windowEvent -> {
					if ((workspacePane.getChildren().size() != 0 || !finiteStateMachine.getAlphabet().isEmpty()) && !requestUserConfirmation("Exit confirmation", "Any unsaved work will be lost!")) {
						windowEvent.consume();
					}
				});
			}
		});
		workspacePane.minHeightProperty().bind(workspaceScrollPane.heightProperty().subtract(6));
		workspacePane.minWidthProperty().bind(workspaceScrollPane.widthProperty().subtract(6));
	}

	@FXML
	private void launchMainMenu(ActionEvent e) throws IOException {
		// Check that there is no unsaved work or ask the user to confirm whether they want to leave
		if ((workspacePane.getChildren().size() == 0 && finiteStateMachine.getAlphabet().isEmpty()) || requestUserConfirmation("Exit confirmation", "Any unsaved work will be lost!")) {
			resetWorkspace();
			workspacePane.setCursor(Cursor.DEFAULT);
			workspacePane.getScene().setCursor(workspacePane.getCursor());
			ObservableList<Stage> stages = StageHelper.getStages();
			// Check to see if any windows are open e.g. Simulation dialog, if so then close them
			if (stages.size() > 1) {
				// Record the main stage's title to identify which stages should be closed
				String mainStageTitle = ((Stage) workspacePane.getScene().getWindow()).getTitle();
				for (int i = 0; i < stages.size(); i++) {
					if (!stages.get(i).getTitle().equals(mainStageTitle)) {
						stages.get(i).close();
                        i--;
					}
				}
			}
			FXMLManager.loadFXMLFileToStage(e, "/main/java/menu/menu.fxml");
		}
	}

	private Boolean requestUserConfirmation(String title, String question) {
		// Ask the user a question and get their response as true or false
		Alert alert = AlertCreator.createInitialisedAlert(title, question, AlertType.CONFIRMATION, workspacePane.getScene().getWindow(), Modality.WINDOW_MODAL);
        ButtonType buttonTypeContinue = new ButtonType("Continue", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeContinue, buttonTypeCancel);
        return alert.showAndWait().get() == buttonTypeContinue;
    }

	@FXML
	private void onClearWorkspaceClick() {
		if ((workspacePane.getChildren().size() == 0 && finiteStateMachine.getAlphabet().isEmpty()) || requestUserConfirmation("Clear Confirmation", "Any unsaved work will be lost!")) {
			resetWorkspace();
		}
	}

	/**
	 * Clears the workspace by resetting the finite automaton and clearing all visual components on the toolkit.
	 */
	public void resetWorkspace() {
		finiteStateMachine.clear();
		workspacePane.getChildren().clear();
		System.gc();
	}

	@FXML
	private void exportFiniteStateMachineAsImage() {
		// Set background to white to improve image
		Background oldBackground = workspacePane.getBackground();
		workspacePane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		// Export the workspace
		ExportFSM.exportFiniteStateMachineAsImage(workspacePane, workspacePane.getScene().getWindow());
		// Restore original workspace background
		workspacePane.setBackground(oldBackground);
	}

	@FXML
	private void saveFiniteStateMachineToFile() {
		SaveFSM.saveFiniteStateMachineToFile(finiteStateMachine, workspacePane.getScene().getWindow());
	}

	@FXML
	private void loadFiniteStateMachineFromFile() {
		LoadFSM.loadFiniteStateMachineFromFile(finiteStateMachine, workspacePane.getScene().getWindow(), workspacePane, this);
	}

	@FXML
	private void onWorkspaceClick(MouseEvent e) {
		String selectedTool = cbTools.getValue();
		// Check that the user performed a primary-click and has selected a tool and is currently hovering over a state/transition
		if (e.isPrimaryButtonDown() && !selectedTool.equals("Select Type") && !workspacePane.getCursor().equals(CustomCursor.MOVE)) {
			// If the user tries adding another initial state to the workspace, alert them that this is not allowed
			if (selectedTool.equals(TOOLS[0]) && finiteStateMachine.hasInitialState()) {
				Alert errorAlert = AlertCreator.createInitialisedAlert("Invalid Action", "Only 1 initial state allowed per Finite State Machine!", AlertType.INFORMATION, workspacePane.getScene().getWindow(), Modality.WINDOW_MODAL);
				Platform.runLater(errorAlert::showAndWait);
			} else {
				createStateOnMouse(e.getX(), e.getY());
			}
		}
	}

	@FXML
	private void onAlphabetButtonClick() {
		Dialog<String> alphabetDialog = DialogCreator.createInitialisedDialog("Modify Alphabet", "alphabetIcon.png", workspacePane.getScene().getWindow(), Modality.WINDOW_MODAL);
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/java/toolkit/alphabetDialog/alphabet_dialog.fxml"));
			alphabetDialog.getDialogPane().setContent(fxmlLoader.load());
			fxmlLoader.<AlphabetDialogController>getController().initAlphabetDialogController(finiteStateMachine, alphabetDialog, workspacePane);
			Platform.runLater(alphabetDialog::showAndWait);
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	@FXML
	private void onTransitionsButtonClick() {
		Dialog<String> transitionsDialog = DialogCreator.createInitialisedDialog("Modify Transitions", "transitionIcon.png", workspacePane.getScene().getWindow(), Modality.WINDOW_MODAL);
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/java/toolkit/transitionsDialog/transitions_dialog.fxml"));
			transitionsDialog.getDialogPane().setContent(loader.load());
			loader.<TransitionsDialogController>getController().initTransitionsDialogController(finiteStateMachine, workspacePane);
			Platform.runLater(transitionsDialog::showAndWait);
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	@FXML
	private void onTestInputWordButtonClick() {
		TextInputDialog tid = new TextInputDialog();
		tid.setTitle("Test Input Word");
		tid.setHeaderText(null);
		tid.setContentText("Enter an input word to test:");
		tid.getDialogPane().getStylesheets().addAll(
				AlertCreator.class.getResource("/main/java/shared/genericPrompt.css").toExternalForm(),
				AlertCreator.class.getResource("/main/java/shared/scrollbarStyling.css").toExternalForm(),
				AlertCreator.class.getResource("/main/java/shared/buttonStyling.css").toExternalForm());

		tid.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
			// Restrict the TextField to only contain alphanumeric characters
            if (!newValue.matches("[a-zA-Z0-9]*")) tid.getEditor().setText(oldValue);
		});

		tid.initOwner(workspacePane.getScene().getWindow());
		tid.initModality(Modality.WINDOW_MODAL);
		tid.setGraphic(null);

		Dialog<String> testResultDialog = DialogCreator.createInitialisedDialog("Result", "resultIcon.png", workspacePane.getScene().getWindow(), Modality.WINDOW_MODAL);
		testResultDialog.setResizable(true);
		Stage stage = (Stage) testResultDialog.getDialogPane().getScene().getWindow();
		stage.setMinHeight(236);
		stage.setMinWidth(testResultDialog.getDialogPane().getWidth() + 53);

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/java/toolkit/testInputDialog/testInputResults_dialog.fxml"));
			testResultDialog.getDialogPane().setContent(loader.load());
			loader.<TestInputDialogController>getController().initTestInputWordDialogController(finiteStateMachine, tid, testResultDialog, workspacePane);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	@FXML
	private void onSimulateInputWordButtonClick() {
		// Check that the simulation dialog is not already being shown
		if (!SimulationDialogController.isDialogShowing) {
			SimulationDialogController.isDialogShowing = true;
			Dialog<String> simulationDialog = DialogCreator.createInitialisedDialog("Simulate Input Word", "simulationIcon.png", workspacePane.getScene().getWindow(), Modality.NONE);
			simulationDialog.setResizable(true);
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/java/toolkit/simulationDialog/simulation_dialog.fxml"));
				simulationDialog.getDialogPane().setContent(loader.load());
				Stage stage = (Stage) simulationDialog.getDialogPane().getScene().getWindow();
				stage.setMinHeight(274);
				stage.setMinWidth(simulationDialog.getDialogPane().getWidth() + 130);
				Button[] outerWorkspaceButtons = {btnBack, btnClear, btnSave, btnLoad, btnExport};
				loader.<SimulationDialogController>getController().initSimulationDialogController(finiteStateMachine, workspacePane, toolbar, outerWorkspaceButtons, simulationDialog);
				Platform.runLater(simulationDialog::show);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}

	/**
	 * Creates a new {@code State} and positions it using the specified X and Y coordinates.
	 *
	 * @param label                 of the new {@code State}
	 * @param mouseX                X location of the new {@code State}
	 * @param mouseY                Y location of the new {@code State}
	 * @param isBeingLoadedFromFile should be <tt>True</tt> if this method is being called while loading from file
	 */
	public void createStateOnMouse(String label, double mouseX, double mouseY, boolean isBeingLoadedFromFile) {
		double stateDiameter = 2 * State.RADIUS_OF_STATE;
		// Create a button to represent the state visually
		Button btnState = new Button(label);
		btnState.setPrefSize(stateDiameter, stateDiameter);
		btnState.setLayoutX(mouseX - State.RADIUS_OF_STATE);
		btnState.setLayoutY(mouseY - State.RADIUS_OF_STATE);
		btnState.setId("btnState");

		Group stateGroup = new Group(btnState);
		workspacePane.getChildren().add(stateGroup);
		State state = new State(label, stateGroup);

		if (!isBeingLoadedFromFile) {
			correctStateButtonPosition(btnState, stateDiameter);
			// If the state is initial or accepting, update the corresponding attributes
			if (cbTools.getValue().equals(TOOLS[0])) {
				stateGroup.getChildren().addAll(generateInitialStateArrow(btnState));
				state.setInitial(true);
			} else if (cbTools.getValue().equals(TOOLS[2])) {
				btnState.setId("btnAcceptingState");
				state.setAccepting(true);
			}
		}
		setUpStateButtonListeners(btnState, stateDiameter);
		finiteStateMachine.addState(state);
		setUpStateContextMenu(state, btnState);
	}

	private void createStateOnMouse(double mouseX, double mouseY) {
		createStateOnMouse("Q" + generateStateID(), mouseX, mouseY, false);
	}

	private void correctStateButtonPosition(Button btnState, double stateDiameter) {
		// If the button is placed outside the workspace area [x axis], move it to the inner-edge of the workspace area
		if(btnState.getLayoutX() < 0) {
			btnState.setLayoutX(0);
		} else if (btnState.getLayoutX() + (stateDiameter) > workspacePane.getWidth()) {
			btnState.setLayoutX(workspacePane.getWidth() - (stateDiameter));
		}

		// If the button is placed outside the workspace area [y axis], move it to the inner-edge of the workspace area
		if (btnState.getLayoutY() < 0) {
			btnState.setLayoutY(0);
		} else if (btnState.getLayoutY() + (stateDiameter) > workspacePane.getHeight()) {
			btnState.setLayoutY(workspacePane.getHeight() - (stateDiameter));
		}
	}

	private void setUpStateButtonListeners(Button btnState, double stateDiameter) {
		Point2D.Double delta = new Point2D.Double();
		// Identify the delta-point when the user clicks the button [to move it]
		btnState.setOnMousePressed(mouseEvent -> {
			delta.x = btnState.getLayoutX() - mouseEvent.getSceneX();
			delta.y = btnState.getLayoutY() - mouseEvent.getSceneY();
		});
		btnState.setOnMouseReleased(mouseEvent -> btnState.setStyle("-fx-border-color: #000000;"));
		// When the user is dragging the button, change the border colour to green and update its position
		btnState.setOnMouseDragged(mouseEvent -> {
			btnState.setStyle("-fx-border-color: #7CFC00;");
			btnState.setLayoutX(mouseEvent.getSceneX() + delta.x);
			btnState.setLayoutY(mouseEvent.getSceneY() + delta.y);
			correctStateButtonPosition(btnState, stateDiameter);
		});
		btnState.setOnMouseEntered(mouseEvent -> workspacePane.setCursor(CustomCursor.MOVE));
		btnState.setOnMouseExited(mouseEvent -> workspacePane.setCursor(Cursor.DEFAULT));
	}

	private void setUpStateContextMenu(State state, Button btnState) {
		ContextMenu contextMenu = new ContextMenu();
		MenuItem toggleInitial = new MenuItem("Toggle Initial State");
		MenuItem toggleAccepting = new MenuItem("Toggle Accepting");
		MenuItem delete = new MenuItem("Delete");
		contextMenu.getItems().addAll(toggleInitial, toggleAccepting, delete);
		setUpStateContextMenuListeners(toggleInitial, toggleAccepting, delete, state, btnState);
		btnState.setContextMenu(contextMenu);
	}

	private void setUpStateContextMenuListeners(MenuItem toggleInitial, MenuItem toggleAccepting, MenuItem delete, State state, Button btnState) {
		// Update the initial state if the user attempts to set an existing state to be initial
		toggleInitial.setOnAction(event -> {
			state.setInitial(!state.isInitial());
			if (state.isInitial()) {
				if (finiteStateMachine.hasInitialState()) {
					finiteStateMachine.getInitialState().setInitial(false);
					removeInitialStateArrow(finiteStateMachine.getInitialState());
				}
				finiteStateMachine.setInitialState(state);
				state.getOnScreenVisual().getChildren().add(generateInitialStateArrow(btnState));
			} else {
				finiteStateMachine.setInitialState(null);
				removeInitialStateArrow(state);
			}
		});
		// Update the state's appearance and attribute if the user attempts to set an existing state to be accepting
		toggleAccepting.setOnAction(event -> {
			btnState.setId((btnState.getId().equals("btnState")) ? "btnAcceptingState" : "btnState");
			state.setAccepting(!state.isAccepting());
		});
		// Remove all related transitions from the state if the user attempts to delete an existing state
		delete.setOnAction(event -> {
			removeRelatedTransitions(state);
			finiteStateMachine.removeState(state);
			workspacePane.getChildren().remove(state.getOnScreenVisual());
		});
	}

	private void removeInitialStateArrow(State state) {
		for(Node n : state.getOnScreenVisual().getChildren()) {
			if(n instanceof Group) {
				state.getOnScreenVisual().getChildren().remove(n);
				break;
			}
		}
	}

	private void removeRelatedTransitions(State state) {
		List<Transition> toCheck = finiteStateMachine.getAllTransitions();
		for(Transition t: toCheck) {
			if(t.getToState().equals(state) || t.getFromState().equals(state)) {
				finiteStateMachine.removeTransition(t.getFromState(), t);
				workspacePane.getChildren().remove(t.getOnScreenVisual());
			}
		}
	}

	/**
	 * Adds arrows to this button (representing a state) to make it clear that this is representing an initial state.
	 *
	 * @param btnState to bind the arrows to
	 * @return {@code Group} which contains the visual components of this button
	 */
	public Group generateInitialStateArrow(Button btnState) {
		return new Group(generateHalfInitialStateArrow(btnState, -State.RADIUS_OF_STATE / 2, 20), generateHalfInitialStateArrow(btnState, -State.RADIUS_OF_STATE / 2, 40));
	}

	private Line generateHalfInitialStateArrow(Button btnState, double xOffset, double yOffset) {
		Line arrowHalf = new Line();
		arrowHalf.setStrokeWidth(2);
		arrowHalf.startXProperty().bind(btnState.layoutXProperty().add(xOffset));
		arrowHalf.startYProperty().bind(btnState.layoutYProperty().add(yOffset));
		arrowHalf.endXProperty().bind(btnState.layoutXProperty());
		arrowHalf.endYProperty().bind(btnState.layoutYProperty().add(State.RADIUS_OF_STATE));
		return arrowHalf;
	}

	private int generateStateID() {
		boolean[] takenIDsBelowSizeOfFSM = new boolean[finiteStateMachine.size()];
		// Set IDs (smaller than the size of the FSM) which are being used to true in their corresponding index
		finiteStateMachine.getStates().forEach(state -> {
			int ID = Integer.parseInt(state.getLabel().substring(1));
			if (ID < takenIDsBelowSizeOfFSM.length) {
				takenIDsBelowSizeOfFSM[ID] = true;
			}
		});

		// Check if there is a gap in the IDs, i.e. a state has been deleted hence its ID is available
		for (int i = 0; i < takenIDsBelowSizeOfFSM.length; i++) {
			if (!takenIDsBelowSizeOfFSM[i]) {
				return i;
			}
		}

		// If there is no gap in the IDs, then use a new ID
		return takenIDsBelowSizeOfFSM.length;
	}

	/**
	 * Gets the finite automaton instance in use.
	 *
	 * @return the {@code FiniteStateMachine} in use
	 */
	public FiniteStateMachine getFiniteStateMachine() {
		return finiteStateMachine;
	}

	/**
	 * Gets the workspace pane of the toolkit.
	 *
	 * @return the workspace {@code Pane} in use
	 */
	public Pane getWorkspacePane() {
		return workspacePane;
	}
}
