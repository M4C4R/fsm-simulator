package main.java.shared;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;

/**
 * @author Mert Acar
 * <p>
 *     Provides simplified interface to access {@link Clipboard}.
 * </p>
 */
public class ClipboardExt {
    /**
     * Adds a specified string to the user's clipboard.
     *
     * @param content to add to the user's clipboard
     */
    public static void addStringToUserClipboard(String content) {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    /**
     * Returns a string representation of the content within the user's clipboard.
     *
     * @return content of the clipboard
     */
    public static String getContentOfUserClipboard() {
        return String.valueOf(Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT));
    }
}
