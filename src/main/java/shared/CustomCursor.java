package main.java.shared;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import main.java.toolkit.ToolkitController;

/**
 * @author Mert Acar
 * <p>
 * Contains {@code ImageCursor}s which can be used to change the way the user's cursor looks.
 * </p>
 */
public class CustomCursor {
    private static final Image moveCursorImage = new Image(ToolkitController.class.getResourceAsStream("/main/resources/moveCursor.png"));

    /**
     * A 64x64 move {@code ImageCursor} type.
     */
    public static final Cursor MOVE =
            new ImageCursor(moveCursorImage, moveCursorImage.getWidth() / 2, moveCursorImage.getHeight() / 2);
}
