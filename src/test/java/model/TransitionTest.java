package test.java.model;

import javafx.scene.Group;
import javafx.scene.layout.HBox;
import main.java.model.State;
import main.java.model.Transition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Mert Acar
 */
public class TransitionTest {

    @Test
    public void constructingWithStatesAndSymbolShouldSetReferences() {
        State state1 = new State("Q1", null);
        State state2 = new State("Q2", null);
        String symbol = "A";
        Transition transition = new Transition(state1, symbol, state2);
        assertEquals(state1, transition.getFromState());
        assertEquals(symbol, transition.getSymbol());
        assertEquals(state2, transition.getToState());
    }

    @Test
    public void requestingVisualComponentShouldReturnSpecifiedGroup() {
        Group group = new Group(new HBox(10));
        Transition transition = new Transition(null, null, null);
        transition.setOnScreenVisual(group);
        assertEquals(group, transition.getOnScreenVisual());
    }

    @Test
    public void twoTransitionsWithSameStatesAndSymbolShouldBeEqual() {
        State state1 = new State("Q3", null);
        State state2 = new State("Q4", null);
        String symbol = "B";
        Transition transition1 = new Transition(state1, symbol, state2);
        Transition transition2 = new Transition(state1, symbol, state2);

        assertEquals(transition1, transition2);
        assertEquals(0, transition1.compareTo(transition2));
        assertEquals(transition1.hashCode(), transition2.hashCode());
    }

    @Test
    public void transitionShouldNotBeEqualWithNonTransitionType() {
        State state1 = new State("Q5", null);
        State state2 = new State("Q6", null);
        String symbol = "C";
        Transition transition = new Transition(state1, symbol, state2);

        assertNotEquals(transition, state1);
        assertNotEquals(transition, symbol);
        assertNotEquals(transition, state2);
    }
}
