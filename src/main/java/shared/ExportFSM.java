package main.java.shared;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Mert Acar
 * <p>
 * Provides functionality to export a snapshot of a specified pane to file.
 * </p>
 */
public class ExportFSM {
    /**
     * Launches the systems file explorer allowing users to select an export location, then exports the specified pane
     * to file using the chosen extension.
     *
     * @param pane        which is to be exported
     * @param windowOwner the parent window of the file chooser/error alert
     */
    public static void exportFiniteStateMachineAsImage(Pane pane, Window windowOwner) {
        FileChooser fileChooser = FileChooserExt.getExportFileChooser("Export FSM", "Capture",
                new FileChooser.ExtensionFilter("png file (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("jpg file (*.jpg)", "*.jpg"));

        File file = fileChooser.showSaveDialog(windowOwner);

        // Check that the location to export to has been selected
        if (file != null) {
            try {
                SnapshotParameters sp = new SnapshotParameters();
                sp.setTransform(Transform.scale(2.5, 2.5));

                // Prepare the image file that will be written onto
                WritableImage writableImage = new WritableImage((int) (pane.getWidth() * 2.5), (int) (pane.getHeight() * 2.5));
                // Take a snapshot (screenshot) of the workspace
                pane.snapshot(sp, writableImage);

                // Save the file accordingly based on whether the user wants to save it as a PNG or JPG
                if (fileChooser.getSelectedExtensionFilter().getDescription().contains("png")) {
                    saveImageAsPNG(writableImage, file);
                } else {
                    saveImageAsJPG(writableImage, file);
                }
            } catch (IOException e) {
                Alert errorAlert = AlertCreator.createInitialisedAlert("Unexpected Error", "Unable to export the finite automaton as an image!", Alert.AlertType.INFORMATION, windowOwner, Modality.WINDOW_MODAL);
                Platform.runLater(errorAlert::showAndWait);
            }
        }
    }

    private static void saveImageAsJPG(WritableImage writableImage, File file) throws IOException {
        BufferedImage image = SwingFXUtils.fromFXImage(writableImage, null);
        // Remove alpha-channel from buffered image to prevent the orange highlight bug
        BufferedImage imageRGB = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.OPAQUE);
        Graphics2D graphics = imageRGB.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        ImageIO.write(imageRGB, "jpg", file);
        graphics.dispose();
    }

    private static void saveImageAsPNG(WritableImage writableImage, File file) throws IOException {
        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
        ImageIO.write(renderedImage, "png", file);
    }
}
