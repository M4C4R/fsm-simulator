package test.java.model;

import javafx.scene.Group;
import javafx.scene.layout.HBox;
import main.java.model.State;
import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Mert Acar
 */
public class StateTest {

    @Test
    public void acceptingStateShouldReturnTrueForIsAccepting() {
        State acceptingState = new State("Q0", null);
        acceptingState.setAccepting(true);
        assertTrue(acceptingState.isAccepting());
    }

    @Test
    public void standardStateShouldReturnFalseForIsAcceptingAndIsInitial() {
        State state = new State("Q1", null);
        assertFalse(state.isAccepting());
        assertFalse(state.isInitial());
    }

    @Test
    public void initialStateShouldReturnTrueForIsInitial() {
        State initialState = new State("Q2", null);
        initialState.setInitial(true);
        assertTrue(initialState.isInitial());
    }

    @Test
    public void requestingLabelShouldReturnSpecifiedLabel() {
        String label = "Q3";
        State state = new State(label, null);
        assertEquals(label, state.getLabel());
    }

    @Test
    public void requestingVisualComponentShouldReturnSpecifiedGroup() {
        Group group = new Group(new HBox(10));
        State state = new State("Q4", group);
        assertEquals(group, state.getOnScreenVisual());
    }

    @Test
    public void twoStatesWithSameQPrefixLabelShouldBeEqual() {
        State state1 = new State("Q4", null, false, true);
        State state2 = new State("Q4", null, true, false);
        assertEquals(state1, state2);
        assertEquals(0, state1.compareTo(state2));
        assertEquals(state1.hashCode(), state2.hashCode());
    }

    @Test
    public void twoStatesWithSameLabelShouldBeEqual() {
        State state1 = new State("abbab", null, false, true);
        State state2 = new State("abbab", null, true, false);
        assertEquals(state1, state2);
        assertEquals(0, state1.compareTo(state2));
        assertEquals(state1.hashCode(), state2.hashCode());
    }

    @Test
    public void toStringShouldStateThatItIsInitialOrNot() {
        State state = new State("Q4", null, false, false);
        State initialState = new State("Q5", null, false, true);
        assertThat(state.toString(), not(containsString("initial")));
        assertThat(initialState.toString(), containsString("initial"));
    }
}
