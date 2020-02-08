package main.java.toolkit;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import main.java.model.FiniteStateMachine;
import main.java.model.State;
import main.java.model.Transition;
import main.java.shared.CustomCursor;

import java.awt.geom.Point2D;
import java.util.HashMap;

/**
 * @author Mert Acar
 * <p>
 * Extends the {@link Arc} shape to draw an Arrow equipped with a label.
 * When given a {@code Transition} and {@code Group}, an arrow is drawn between the two states of the transition and added to the group.
 * Arrow inspired by www.madebyevan.com/fsm/
 * </p>
 */
public class Arrow extends Arc {
    private FiniteStateMachine finiteStateMachine;
    private Pane workspacePane;
    private Button btnFrom;
    private Button btnTo;
    private Label lblSymbol;

    // The percentage the anchor point is away from btnFrom towards btnTo
    private double percentageBetweenStates;
    // The number of pixels the anchor point is away from the centre point of btnFrom and btnTo
    private double perpendicularDistanceToAnchor;
    private double anchorAngle;

    private Polygon arrowHead;
    private Rotate arrowHeadRotation;
    private boolean areStatesReversed;
    private boolean isReflexiveArrow;
    private boolean isAnchorPointSetByUser;

    /**
     * Draws an arrow between the states specified by the transition using the custom values and adds this to the
     * supplied {@code Group}.
     *
     * @param fsm                           used by the toolkit
     * @param transition                    which this arrow will represent
     * @param pane                          representing the workspace
     * @param group                         to store the arrow in
     * @param percentageBetweenStates
     * @param perpendicularDistanceToAnchor
     * @param anchorAngle
     * @param isAnchorPointSetByUser
     */
    public Arrow(FiniteStateMachine fsm, Transition transition, Pane pane, Group group, double percentageBetweenStates, double perpendicularDistanceToAnchor, double anchorAngle, boolean isAnchorPointSetByUser) {
        super();
        this.finiteStateMachine = fsm;
        this.workspacePane = pane;
        this.percentageBetweenStates = percentageBetweenStates;
        this.perpendicularDistanceToAnchor = perpendicularDistanceToAnchor;
        this.anchorAngle = anchorAngle;
        this.isAnchorPointSetByUser = isAnchorPointSetByUser;
        initialiseArrow(transition, group, false);
    }

    /**
     * Draws an arrow between the states specified by the transition and adds this to the supplied {@code Group}.
     * Calculates and uses a default anchor point.
     *
     * @param fsm used by the toolkit
     * @param transition which this arrow will represent
     * @param pane representing the workspace
     * @param group to store the arrow in
     */
    public Arrow(FiniteStateMachine fsm, Transition transition, Pane pane, Group group) {
        super();
        this.finiteStateMachine = fsm;
        this.workspacePane = pane;
        initialiseArrow(transition, group, true);
    }

    /**
     * Get the percentage the anchor point is away from the starting state of the transition to the target state.
     *
     * @return the percentage
     */
    public double getPercentageBetweenStates() {
        return percentageBetweenStates;
    }

    /**
     * Get the number of pixels the anchor point is away from the centre point of the two states of the transition.
     *
     * @return the number of pixels
     */
    public double getPerpendicularDistanceToAnchor() {
        return perpendicularDistanceToAnchor;
    }

    /**
     * Get the angle between the anchor point and target state within the transition.
     *
     * @return the angle in radians
     */
    public double getAnchorAngle() {
        return anchorAngle;
    }

    /**
     * Check whether the anchor point has been set by the user.
     *
     * @return isAnchorPointSetByUser
     */
    public boolean isAnchorPointSetByUser() {
        return isAnchorPointSetByUser;
    }

    private void initialiseArrow(Transition transition, Group group, boolean isNewArrow) {
        // Identify and store the buttons which represent the states that are a part of the specified transition
        extractButtonFromState(transition.getFromState(), false);
        extractButtonFromState(transition.getToState(), true);

        // Identify whether the transition is reflexive
        isReflexiveArrow = btnTo.getText().equals(btnFrom.getText());

        // Create a label for the transition's symbol
        lblSymbol = new Label(transition.getSymbol());
        lblSymbol.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold; -fx-text-fill: #000000;");
        lblSymbol.setId("lblSymbol");

        // Create an arrowhead and bind its location to the target state of the transition
        arrowHead = new Polygon(0, 0, 0, 28, 6, 38, -6, 38, 0, 28);
        arrowHead.layoutXProperty().bind(btnTo.layoutXProperty().add(State.RADIUS_OF_STATE));
        arrowHead.layoutYProperty().bind(btnTo.layoutYProperty().add(State.RADIUS_OF_STATE));
        arrowHead.setStyle("-fx-fill: #000000;");

        // Initialise and add a rotation to the arrowhead
        arrowHeadRotation = new Rotate();
        arrowHead.getTransforms().add(arrowHeadRotation);

        if (isNewArrow) {
            restoreDefaultAnchorPointAndAngle();
        }

        // Configure the Arc's attributes
        setStrokeWidth(2);
        setStyle("-fx-stroke: #000000;");
        setFill(null);
        setType(ArcType.OPEN);

        //Add the arrow to the group and apply the styling
        group.getChildren().addAll(this, lblSymbol, arrowHead);
        group.applyCss();
        group.layout();

        // Calculate and update the arrow's properties
        updateArrowPositioning(false);

        // If any of the state positions move, update the arrow
        btnTo.layoutXProperty().addListener(observable -> updateArrowPositioning(true));
        btnTo.layoutYProperty().addListener(observable -> updateArrowPositioning(true));
        btnFrom.layoutXProperty().addListener(observable -> updateArrowPositioning(true));
        btnFrom.layoutYProperty().addListener(observable -> updateArrowPositioning(true));

        setUpArrowListeners(transition, group);
    }

    private void setUpArrowListeners(Transition transition, Group group) {
        // Set the colour of the arrow to green when the user is hovering over it and it is safe to update the arrow
        group.setOnMouseEntered(mouseEvent -> {
            Color color = (!isSafeToUpdateArrow()) ? Color.RED : Color.LAWNGREEN;
            colourArrow(color);
            group.getParent().setCursor((color.equals(Color.RED)) ? Cursor.DEFAULT : CustomCursor.MOVE);
        });
        // Set the colour of the arrow to black when the user is no longer hovering over it
        group.setOnMouseExited(mouseEvent -> {
            colourArrow(Color.BLACK);
            if (!mouseEvent.isPrimaryButtonDown()) {
                group.getParent().setCursor(Cursor.DEFAULT);
            }
        });
        // Set the anchor point of the arrow to the user's cursor if they have the "Move" tool selected, dragged the arrow, and it is safe to update the arrow
        group.setOnMouseDragged(mouseEvent -> {
            if (isSafeToUpdateArrow()) {
                setAnchorPoint(mouseEvent.getX(), mouseEvent.getY());
                setAnchorAngle(mouseEvent.getX(), mouseEvent.getY());
                isAnchorPointSetByUser = true;
                updateArrowPositioning(false);
                colourArrow(Color.LAWNGREEN);
            }
        });
        // Set the colour of the arrow to black when the user stops dragging the arrow
        group.setOnMouseReleased(group.getOnMouseExited());
        // Set up the arrow's context menu
        ContextMenu contextMenu = setUpContextMenu(transition, group);
        group.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.isSecondaryButtonDown()) {
                contextMenu.show((Node) mouseEvent.getSource(), mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
        });
    }

    private ContextMenu setUpContextMenu(Transition transition, Group group) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(actionEvent -> {
            finiteStateMachine.removeTransition(transition.getFromState(), transition);
            workspacePane.getChildren().remove(group);
        });
        contextMenu.getItems().add(deleteItem);
        return contextMenu;
    }

    private void colourArrow(Color color) {
        setStroke(color);
        arrowHead.setFill(color);
        lblSymbol.setTextFill(color);
    }

    private double btnFromX() {
        return btnFrom.getLayoutX() + State.RADIUS_OF_STATE;
    }

    private double btnFromY() {
        return btnFrom.getLayoutY() + State.RADIUS_OF_STATE;
    }

    private double btnToX() {
        return btnTo.getLayoutX() + State.RADIUS_OF_STATE;
    }

    private double btnToY() {
        return btnTo.getLayoutY() + State.RADIUS_OF_STATE;
    }

    private void restoreDefaultAnchorPointAndAngle() {
        // Identify the centre point of the transition
        double centreOfStatesX = (btnFromX() + btnToX()) / 2;
        double centreOfStatesY = (btnFromY() + btnToY()) / 2;

        if (!isSafeToUpdateArrow()) {
            // Since the states are too close to one another, we offset our centre points
            centreOfStatesX = centreOfStatesY -= 30;
        }

        if (isReflexiveArrow) {
            // Generate a random coordinate where the range is between 1 and 10 (inclusive)
            double randomX = Math.random() * 10 + 1;
            double randomY = Math.random() * 10 + 1;
            // Generate a random operator where 0 = + and 1 = -
            double randomOperatorX = Math.floor(Math.random() * 2);
            double randomOperatorY = Math.floor(Math.random() * 2);
            // Set the default anchor angle of the reflexive arrow to be random to reduce likelihood of overlapping
            double anchorX = centreOfStatesX + ((randomOperatorX == 0) ? randomX : -randomX);
            double anchorY = centreOfStatesY + ((randomOperatorY == 0) ? randomY : -randomY);

            setAnchorPoint(anchorX, anchorY);
            setAnchorAngle(anchorX, anchorY);
        } else {
            // Set the anchor point of the arrow to be an offset of the centre point
            setAnchorPoint(centreOfStatesX + 10, centreOfStatesY + 10);
            setAnchorAngle(centreOfStatesX + 10, centreOfStatesY + 10);
        }
        isAnchorPointSetByUser = false;
    }

    private boolean isSafeToUpdateArrow() {
        // It is safe to update the arrow (visually) if it is a reflexive arrow or the distance between the two states is greater than the radius of a single state
        return isReflexiveArrow || getEuclideanDistance(btnFromX(), btnFromY(), btnToX(), btnToY()) > State.RADIUS_OF_STATE;
    }

    private void extractButtonFromState(State state, Boolean isBtnTo) {
        // Find the button within the state and store it in the corresponding field
        for (Node node : state.getOnScreenVisual().getChildren()) {
            if (node instanceof Button) {
                if (isBtnTo) btnTo = (Button) node;
                else btnFrom = (Button) node;
                break;
            }
        }
    }

    private double getEuclideanDistance(double x, double y, double x2, double y2) {
        return Math.sqrt(Math.pow((x - x2), 2) + Math.pow((y - y2), 2));
    }

    private Point2D.Double getDeltaOfStates() {
        // Returns the X and Y deltas of the two states
        double deltaX = btnToX() - btnFromX();
        double deltaY = btnToY() - btnFromY();
        return new Point2D.Double(deltaX, deltaY);
    }

    private Point2D.Double getAnchorPoint() {
        // Calculates and returns the anchor point
        double distance = getEuclideanDistance(btnFromX(), btnFromY(), btnToX(), btnToY());
        Point2D.Double delta = getDeltaOfStates();
        double anchorX = btnFromX() + (delta.x * percentageBetweenStates) - (delta.y * perpendicularDistanceToAnchor / distance);
        double anchorY = btnFromY() + (delta.y * percentageBetweenStates) + (delta.x * perpendicularDistanceToAnchor / distance);
        return new Point2D.Double(anchorX, anchorY);
    }

    private void setAnchorPoint(double x, double y) {
        double distance = getEuclideanDistance(btnFromX(), btnFromY(), btnToX(), btnToY());
        // To prevent division by zero, typically distance = 0 when dealing with a reflexive arrow
        if (distance == 0) {
            distance = 1;
        }
        Point2D.Double delta = getDeltaOfStates();
        percentageBetweenStates = (delta.x * (x - btnFromX()) + delta.y * (y - btnFromY())) / (distance * distance);
        perpendicularDistanceToAnchor = (delta.x * (y - btnFromY()) - delta.y * (x - btnFromX())) / distance;
    }

    private void setAnchorAngle(double x, double y) {
        // Calculate and store the angle between the anchor point and transition target state
        anchorAngle = Math.atan2(y - btnToY(), x - btnToX());
    }

    private HashMap<String, Double> getPropertiesOfCircle() throws Exception {
        HashMap<String, Double> toReturn = new HashMap<>();

        HashMap<String, Double> circle = new HashMap<>();
        // Initially assume the arrow is reflexive and so put an offset onto the circle's (arrow) position
        circle.put("x", btnToX() + 1.4 * State.RADIUS_OF_STATE * Math.cos(anchorAngle));
        circle.put("y", btnToY() + 1.4 * State.RADIUS_OF_STATE * Math.sin(anchorAngle));
        circle.put("radius", 0.6 * State.RADIUS_OF_STATE);

        double startAngle = 0;
        double endAngle = Math.toDegrees(anchorAngle);

        if (!isReflexiveArrow) {
            Point2D.Double anchor = getAnchorPoint();
            circle = generateCircleFromThreePoints(new Point2D.Double(btnFromX(), btnFromY()), new Point2D.Double(btnToX(), btnToY()), new Point2D.Double(anchor.x, anchor.y));

            startAngle = -Math.toDegrees(Math.atan2(btnFromY() - circle.get("y"), btnFromX() - circle.get("x")));
            // Ensure it is a positive angle
            if (startAngle < 0) startAngle += 360;
            endAngle = Math.toDegrees(Math.atan2(btnToY() - circle.get("y"), btnToX() - circle.get("x")));

            toReturn.put("reverseScale", (perpendicularDistanceToAnchor > 0) ? 1.0 : -1.0);
        }

        Point2D.Double startPoint = translatePointUsingDistanceAndAngle(circle.get("x"), circle.get("y"), circle.get("radius"), (isReflexiveArrow) ? startAngle : Math.toRadians(startAngle));
        Point2D.Double endPoint = translatePointUsingDistanceAndAngle(circle.get("x"), circle.get("y"), circle.get("radius"), (isReflexiveArrow) ? endAngle : Math.toRadians(endAngle));

        toReturn.put("circleX", circle.get("x"));
        toReturn.put("circleY", circle.get("y"));
        toReturn.put("circleRadius", circle.get("radius"));
        toReturn.put("startX", startPoint.x);
        toReturn.put("startY", startPoint.y);
        toReturn.put("endX", endPoint.x);
        toReturn.put("endY", endPoint.y);
        toReturn.put("startAngle", startAngle);
        toReturn.put("endAngle", endAngle);
        return toReturn;
    }

    private Point2D.Double translatePointUsingDistanceAndAngle(double x, double y, double distance, double angle) {
        return new Point2D.Double(x + (distance * Math.cos(angle)), y + (distance * Math.sin(angle)));
    }

    private void checkIfStatesAreReversed() {
        // Check if the states of the transition are reversed on screen
        if (btnFrom.getLayoutX() > btnTo.getLayoutX()) {
            Button t = btnTo;
            btnTo = btnFrom;
            btnFrom = t;
            areStatesReversed = !areStatesReversed;
        }
    }

    private void updateArrowPositioning(boolean isCalledByButtonListener) {
        Point2D.Double computedArrowHead;
        Point2D.Double computedLabel;
        double newRotation;

        checkIfStatesAreReversed();
        HashMap<String, Double> circleProperties;

        if (!isAnchorPointSetByUser && !isReflexiveArrow) {
            restoreDefaultAnchorPointAndAngle();
        }

        try {
            circleProperties = getPropertiesOfCircle();
        } catch (Exception e) {
            return;
        }

        if (isReflexiveArrow) {
            setLength(360);
            computedLabel = computeLabelPosition(circleProperties, 1, 1, 0);
            computedArrowHead = computeArrowHeadPosition(circleProperties, circleProperties.get("endAngle"), 1);
            newRotation = Math.toDegrees(Math.atan2(computedArrowHead.y - btnFromY(), computedArrowHead.x - btnFromX()));
        } else {
            double length = ((circleProperties.get("reverseScale") < 1) ? 0 : 360) - (circleProperties.get("startAngle") + circleProperties.get("endAngle"));
            // Prevents rendering issue when the arc is 360 degrees and not reflexive
            if (Math.abs(length) > 359) {
                if (isCalledByButtonListener) restoreDefaultAnchorPointAndAngle();
                else return;
            }

            setLength(length);
            computedLabel = computeLabelPosition(circleProperties, 2.5, 1, getLength() / 2);
            if (areStatesReversed) {
                computedArrowHead = computeArrowHeadPosition(circleProperties, -circleProperties.get("startAngle"), -circleProperties.get("reverseScale"));
                newRotation = Math.toDegrees(Math.atan2(computedArrowHead.y - btnFromY(), computedArrowHead.x - btnFromX()));
            } else {
                computedArrowHead = computeArrowHeadPosition(circleProperties, circleProperties.get("endAngle"), circleProperties.get("reverseScale"));
                newRotation = Math.toDegrees(Math.atan2(computedArrowHead.y - circleProperties.get("endY"), computedArrowHead.x - circleProperties.get("endX")));
            }
        }

        // Draw the arrow (length of the arrow is specified above)
        setCenterX(circleProperties.get("circleX"));
        setCenterY(circleProperties.get("circleY"));
        setRadiusX(circleProperties.get("circleRadius"));
        setRadiusY(circleProperties.get("circleRadius"));
        setStartAngle(circleProperties.get("startAngle"));

        arrowHeadRotation.setAngle(newRotation - 90);

        lblSymbol.translateXProperty().bind(lblSymbol.widthProperty().divide(2).negate());
        lblSymbol.translateYProperty().bind(lblSymbol.heightProperty().divide(2).negate());
        lblSymbol.setLayoutX(computedLabel.x);
        lblSymbol.setLayoutY(computedLabel.y);
    }

    private Point2D.Double computeLabelPosition(HashMap<String, Double> circleProperties, double xOffset, double yOffset, double angleOffset) {
        Point2D.Double computedLabel = new Point2D.Double();
        computedLabel.x = circleProperties.get("circleX") + (circleProperties.get("circleRadius") + (lblSymbol.getWidth() / 2) + xOffset) * Math.cos(Math.toRadians(angleOffset + circleProperties.get("endAngle")));
        computedLabel.y = circleProperties.get("circleY") + (circleProperties.get("circleRadius") + (lblSymbol.getHeight() / 2) + yOffset) * Math.sin(Math.toRadians(angleOffset + circleProperties.get("endAngle")));
        return computedLabel;
    }

    private Point2D.Double computeArrowHeadPosition(HashMap<String, Double> circleProperties, double angle, double reverseScale) {
        Point2D.Double computedArrowHead = new Point2D.Double();
        computedArrowHead.x = circleProperties.get("circleX") + circleProperties.get("circleRadius") * Math.cos(Math.toRadians(angle + reverseScale * Math.toDegrees(State.RADIUS_OF_STATE / circleProperties.get("circleRadius"))));
        computedArrowHead.y = circleProperties.get("circleY") + circleProperties.get("circleRadius") * Math.sin(Math.toRadians(angle + reverseScale * Math.toDegrees(State.RADIUS_OF_STATE / circleProperties.get("circleRadius"))));
        return computedArrowHead;
    }

    /**
     * Generates and returns the properties of a circle generated from the three specified points.
     *
     * @param A first point
     * @param B second point
     * @param C anchor point
     * @return the properties of the generated circle as a {@code HashMap} with keys:
     * <ul>
     * <li>"x" = centre x coordinate of circle</li>
     * <li>"y" = centre y coordinate of circle</li>
     * <li>"radius" = radius of the circle</li>
     * </ul>
     * @throws Exception
     */
    private HashMap<String, Double> generateCircleFromThreePoints(Point2D.Double A, Point2D.Double B, Point2D.Double C) throws Exception {
        // Calculates the delta points of <A,B> and <B,C>
        double deltaABX = (B.x - A.x < 0.3 && B.x - A.x >= 0) ? 0.3 : (B.x - A.x <= 0 && B.x - A.x > -0.3) ? -0.3 : B.x - A.x;
        double deltaABY = (B.y - A.y < 0.3 && B.y - A.y >= 0) ? 0.3 : (B.y - A.y <= 0 && B.y - A.y > -0.3) ? -0.3 : B.y - A.y;
        double deltaBCX = (C.x - B.x < 0.3 && C.x - B.x >= 0) ? 0.3 : (C.x - B.x <= 0 && C.x - B.x > -0.3) ? -0.3 : C.x - B.x;
        double deltaBCY = (C.y - B.y < 0.3 && C.y - B.y >= 0) ? 0.3 : (C.y - B.y <= 0 && C.y - B.y > -0.3) ? -0.3 : C.y - B.y;

        // Calculates the slopes of points <A,B> and <B,C>
        double slopeAB = deltaABY / deltaABX;
        double slopeBC = deltaBCY / deltaBCX;

        if (Math.abs(slopeAB - slopeBC) < 0.000005) {
            throw new Exception("Division by minuscule value");
        }

        double centreX = (slopeAB * slopeBC * (A.y - C.y) + slopeBC * (A.x + B.x) - slopeAB * (B.x + C.x)) / (2 * (slopeBC - slopeAB));
        double centreY = -1 * (centreX - (A.x + B.x) / 2) / slopeAB + (A.y + B.y) / 2;

        HashMap<String, Double> toReturn = new HashMap<>();
        toReturn.put("x", centreX);
        toReturn.put("y", centreY);
        toReturn.put("radius", getEuclideanDistance(centreX, centreY, C.x, C.y));
        return toReturn;
    }
}