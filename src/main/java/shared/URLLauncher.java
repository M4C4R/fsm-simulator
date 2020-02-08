package main.java.shared;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.awt.Desktop;
import java.net.URI;

/**
 * @author Mert Acar
 * <p>
 * Attempts to launch a specified URL in the user's browser and copies the URL to the user's clipboard,
 * shows an alert on failure.
 * </p>
 */
public class URLLauncher {
    /**
     * Attempts to launch the specified url in the user's default web browser.
     *
     * @param url    the URL to launch
     * @param window the window which will be the parent of the alert if the url cannot be launched
     */
    public static void launchURL(String url, Window window) {
        ClipboardExt.addStringToUserClipboard(url);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception exception) {
                    showURLLaunchErrorDialog(window, url);
                }
            }).start();
        } else {
            showURLLaunchErrorDialog(window, url);
        }
    }

    private static void showURLLaunchErrorDialog(Window window, String url) {
        Platform.runLater(() -> AlertCreator.createInitialisedAlert("Error", "Unable to launch web browser, the URL below has been copied to your clipboard!\n\n" + url,
                Alert.AlertType.INFORMATION, window, Modality.WINDOW_MODAL).showAndWait());
    }
}
