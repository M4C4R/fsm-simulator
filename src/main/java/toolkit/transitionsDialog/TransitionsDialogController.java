package main.java.toolkit.transitionsDialog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import main.java.model.FiniteStateMachine;
import main.java.model.State;
import main.java.model.Transition;
import main.java.shared.AlertCreator;
import main.java.shared.Unicode;
import main.java.toolkit.Arrow;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mert Acar
 * <p>
 * Controller which handles actions that take place on the transitions dialog.
 * </p>
 */
public class TransitionsDialogController {
	@FXML
	private ComboBox<State> cbFromState;
	@FXML
	private ComboBox<String> cbSymbol;
	@FXML
	private ComboBox<State> cbToState;
	@FXML
	private Button btnRemove;
	@FXML
	private TabPane tabPane;
	@FXML
	private ListView<Transition> lvTransitions;
	@FXML
	private SpreadsheetView svTransitions;

	private FiniteStateMachine finiteStateMachine;
	private Pane workspacePane;

	/**
	 * Initialises the controller by storing the finite automaton reference and workspace pane, as well as
	 * updating the combo-boxes, list-view, and transition table.
	 *
	 * @param fsm    the finite automaton used by the toolkit
	 * @param pane   representing the workspace
	 */
	public void initTransitionsDialogController(FiniteStateMachine fsm, Pane pane) {
		finiteStateMachine = fsm;
		workspacePane = pane;
		updateViews();
		populateComboBoxes();
		// Hide the remove button when the user is viewing the transition table tab
		btnRemove.visibleProperty().bind(tabPane.getSelectionModel().selectedIndexProperty().isNotEqualTo(1));
		// Prevent the user zooming in on the transition table greater than the standard zoom factor
		svTransitions.zoomFactorProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.doubleValue() > 1) {
				svTransitions.setZoomFactor(1.0);
			}
		});
        setUpContextMenu();
	}

	@FXML
	private void onAddButtonClick() {
		// Identify the values that the user has selected
		State from = cbFromState.getValue();
		String symbol = cbSymbol.getValue();
		State to = cbToState.getValue();
		// Ensure none of the values are missing
		if(from != null && symbol != null && to != null){
			Transition newTransition = new Transition(from, symbol, to);
			if (finiteStateMachine.addTransition(cbFromState.getValue(), newTransition)) {
				updateViews();
				Group visual = new Group();
				workspacePane.getChildren().add(visual);
				new Arrow(finiteStateMachine, newTransition, workspacePane, visual);
				newTransition.setOnScreenVisual(visual);
				// Send the arrow behind the states
				visual.toBack();
			}
		} else {
			Alert alert = AlertCreator.createInitialisedAlert("Invalid Action", "You must select a value from each list before adding a transition", AlertType.INFORMATION, workspacePane.getScene().getWindow(), Modality.APPLICATION_MODAL);
			Platform.runLater(alert::showAndWait);
		}
	}

	@FXML
	private void onRemoveButtonClick() {
		Transition transition = lvTransitions.getSelectionModel().getSelectedItem();
		if (transition != null) {
			finiteStateMachine.removeTransition(transition.getFromState(), transition);
			updateViews();
			workspacePane.getChildren().remove(transition.getOnScreenVisual());
		} else {
			Alert alert = AlertCreator.createInitialisedAlert("Invalid Action", "You must click an existing transition from the list view first!" +
					"\nTip: The 'Backspace' and 'Delete' keys are shortcuts to removing transitions.", AlertType.INFORMATION, workspacePane.getScene().getWindow(), Modality.APPLICATION_MODAL);
			Platform.runLater(alert::showAndWait);
		}
	}

    @FXML
    private void handleKeyPressOnListView(KeyEvent event) {
        if (event.getCode().equals(KeyCode.BACK_SPACE) || event.getCode().equals(KeyCode.DELETE)) onRemoveButtonClick();
    }

    private void setUpContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.setOnAction((actionEvent) -> onRemoveButtonClick());
        contextMenu.getItems().add(deleteItem);
        lvTransitions.setContextMenu(contextMenu);
        lvTransitions.setOnContextMenuRequested((contextMenuEvent) -> {
			// Ensure that an entry in the list view has been selected, if not then hide the context menu
            if (lvTransitions.getSelectionModel().isEmpty()) {
                contextMenu.hide();
                contextMenuEvent.consume();
            }
        });
    }

	private void updateViews() {
		Platform.runLater(() -> {
			updateListView();
			updateSpreadsheetView();
		});
	}

	private void updateListView() {
		lvTransitions.getItems().clear();
		lvTransitions.getItems().addAll(finiteStateMachine.getAllTransitions());
        lvTransitions.getItems().sort(Transition::compareTo);
	}

	private void updateSpreadsheetView() {
		int numberOfStates = finiteStateMachine.size();
		int numberOfInputSymbols = finiteStateMachine.getAlphabet().size();
		GridBase grid;

		if (finiteStateMachine.doesEmptyTransitionExist()) {
			numberOfInputSymbols++;
			grid = new GridBase(numberOfStates, numberOfInputSymbols);
			// Manually add the empty string as a column header
			grid.getColumnHeaders().add(Unicode.EPSILON);
		} else {
			grid = new GridBase(numberOfStates, numberOfInputSymbols);
		}

		// An empty spreadsheet doesn't look pleasant, so we only enable the row header if we have content to display
		svTransitions.setShowRowHeader(numberOfStates > 0 && numberOfInputSymbols > 0);
		svTransitions.setShowColumnHeader(numberOfStates > 0 && numberOfInputSymbols > 0);
		svTransitions.setEditable(false);
		svTransitions.setContextMenu(null);
		svTransitions.setFixingColumnsAllowed(false);
		svTransitions.setFixingRowsAllowed(false);

		// Add each symbol as a column header
		grid.getColumnHeaders().addAll(finiteStateMachine.getAlphabet().stream().map(String::valueOf).collect(Collectors.toList()));

		// Add each state label as a row header
		grid.getRowHeaders().addAll(finiteStateMachine.getStates().stream().map(State::getLabel).collect(Collectors.toList()));

        svTransitions.getColumns().forEach(column -> column.setPrefWidth(35));
        svTransitions.setRowHeaderWidth(42);
		// Sort the row and column headers
        grid.getRowHeaders().sort(Comparator.comparingInt(o -> Integer.parseInt(o.substring(1))));
		grid.getColumnHeaders().sort(String::compareTo);

		//Initialise a 2DArray to represent the cells, each inner-list stores the values of the cells for each column, for a given row
		ObservableList<ObservableList<SpreadsheetCell>> rowsOfColumns = FXCollections.observableArrayList();

		populateSpreadsheetView(numberOfStates, numberOfInputSymbols, grid, rowsOfColumns);
		grid.setRows(rowsOfColumns);
		svTransitions.setGrid(grid);
	}

	private void populateSpreadsheetView(int numberOfStates, int numberOfInputSymbols, GridBase grid, ObservableList<ObservableList<SpreadsheetCell>> rowsOfColumns) {
		for (int i = 0; i < numberOfStates; i++) {
			ObservableList<SpreadsheetCell> singleRow = FXCollections.observableArrayList();
			// Identify all the outgoing transitions for the row (state)
			State fromState = new State(grid.getRowHeaders().get(i), null);
			List<Transition> outgoingTransitionsForState = finiteStateMachine.getOutgoingTransitions(fromState);

			for (int j = 0; j < numberOfInputSymbols; j++) {
				// Identify all the outgoing transitions from the row (state) which make use of this column (symbol)
                List<Transition> outgoingTransitionsForStateSymbolPair = new ArrayList<>();
				for (Transition transition : outgoingTransitionsForState) {
					if (transition.getSymbol().equals(grid.getColumnHeaders().get(j))) {
						outgoingTransitionsForStateSymbolPair.add(transition);
					}
				}

				// The cell value should include all the states reachable by this <state,symbol> pair
				StringBuilder cellValue = new StringBuilder();
				for (Transition transition : outgoingTransitionsForStateSymbolPair) {
					cellValue.append(transition.getToState().getLabel()).append(", ");
				}

				if (cellValue.length() > 0) {
					singleRow.add(SpreadsheetCellType.STRING.createCell(i, j, 1, 1, cellValue.substring(0, cellValue.length() - 2)));
					// Need to use final version of j in lambdas
					final int finalJ = j;
					Platform.runLater(() -> svTransitions.getColumns().get(finalJ).fitColumn());
				} else {
					singleRow.add(SpreadsheetCellType.STRING.createCell(i, j, 1, 1, ""));
				}
			}
			rowsOfColumns.add(singleRow);
		}
	}

	private void populateComboBoxes() {
		cbFromState.getItems().addAll(finiteStateMachine.getStates());
		cbFromState.getItems().sort(State::compareTo);
		cbSymbol.getItems().addAll(finiteStateMachine.getAlphabet().stream().map(String::valueOf).collect(Collectors.toList()));
		cbSymbol.getItems().add(Unicode.EPSILON);
		cbSymbol.getItems().sort(String::compareTo);
		cbToState.getItems().addAll(cbFromState.getItems());
	}
}

