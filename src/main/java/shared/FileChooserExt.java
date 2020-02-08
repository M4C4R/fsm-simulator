package main.java.shared;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

/**
 * @author Mert Acar
 * <p>
 * Provides simplified interface to access {@link FileChooser}.
 * </p>
 */
public class FileChooserExt {
    /**
     * Launches a file explorer customised by the specified attributes and then returns the file which corresponds to
     * the save location specified by the user.
     *
     * @param title           of the file explorer
     * @param initialFileName of the file to be saved
     * @param windowOwner     of the file explorer
     * @param filter          extension of the file
     * @return the {@link File} selected by the user
     */
    public static File getSaveFileUsingFileExplorer(String title, String initialFileName, Window windowOwner, FileChooser.ExtensionFilter filter) {
        return initialiseFileChooser(title, initialFileName, filter).showSaveDialog(windowOwner);
    }

    /**
     * Launches a file explorer customised by the specified attributes and then returns the file which corresponds to
     * the file to be loaded specified by the user.
     *
     * @param title       of the file explorer
     * @param windowOwner of the file explorer
     * @param filter      extension of the file
     * @return the {@link File} selected by the user
     */
    public static File getLoadFileUsingFileExplorer(String title, Window windowOwner, FileChooser.ExtensionFilter filter) {
        return initialiseFileChooser(title, "", filter).showOpenDialog(windowOwner);
    }

    /**
     * Customises a file explorer using the specified attributes and then returns this {@link FileChooser}.
     *
     * @param title           of the file explorer
     * @param initialFileName of the file to be saved
     * @param filters         extensions for the file
     * @return the customised {@link FileChooser}
     */
    public static FileChooser getExportFileChooser(String title, String initialFileName, FileChooser.ExtensionFilter... filters) {
        return initialiseFileChooser(title, initialFileName, filters);
    }

    private static FileChooser initialiseFileChooser(String title, String initialFileName, FileChooser.ExtensionFilter... filters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialFileName(initialFileName);
        fileChooser.getExtensionFilters().addAll(filters);
        return fileChooser;
    }
}
