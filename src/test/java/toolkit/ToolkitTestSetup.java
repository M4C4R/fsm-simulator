package test.java.toolkit;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.java.shared.LoadFSM;
import main.java.toolkit.ToolkitController;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Mert Acar
 * <p>
 * Launches the toolkit and stores references to the toolkit controller and stage in use, ready to be tested.
 * This class is meant to be extended by classes which are testing components on the toolkit.
 * </p>
 */
public class ToolkitTestSetup extends ApplicationTest {
    protected Stage stage;
    protected ToolkitController toolkitController;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/java/toolkit/toolkit.fxml"));
        Parent root = fxmlLoader.load();
        toolkitController = fxmlLoader.getController();
        this.stage = stage;
        this.stage.setScene(new Scene(root));
        this.stage.setAlwaysOnTop(true);
        this.stage.show();
    }

    protected void loadTestFiniteAutomaton(String fileName) {
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        // Load a finite automaton to set up the testing area
        File file = new File(getClass().getResource("/test/test_automata/" + fileName).getPath());
        Platform.runLater(() -> {
            LoadFSM.loadFiniteStateMachineFromFile(toolkitController.getFiniteStateMachine(), stage.getScene().getWindow(), toolkitController.getWorkspacePane(), toolkitController, file);
        });
        WaitForAsyncUtils.sleep(1000, TimeUnit.MILLISECONDS);
    }
}
