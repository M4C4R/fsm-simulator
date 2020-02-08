package test.java.model;

import main.java.model.FiniteStateMachine;
import main.java.model.State;
import main.java.model.Transition;
import main.java.shared.Unicode;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Mert Acar
 */
public class FiniteStateMachineTest {
    private static FiniteStateMachine finiteStateMachine;

    @BeforeClass
    public static void instantiateFiniteStateMachineInstance() {
        finiteStateMachine = new FiniteStateMachine();
    }

    @After
    public void resetFiniteStateMachine() {
        finiteStateMachine.clear();
    }

    @Test
    public void addingDuplicateCharactersToAlphabetShouldBePrevented() {
        // Add two distinct characters to the alphabet, these should return true as they are new/distinct
        assertTrue(finiteStateMachine.addSymbolToAlphabet('A'));
        assertTrue(finiteStateMachine.addSymbolToAlphabet('B'));

        // Add the same two characters to the alphabet, these should return false as they are duplicates
        assertFalse(finiteStateMachine.addSymbolToAlphabet('A'));
        assertFalse(finiteStateMachine.addSymbolToAlphabet('B'));

        // Check that the duplicate characters are not kept
        assertEquals(2, finiteStateMachine.getAlphabet().size());

        // Check that the two distinct symbols are in the alphabet
        assertTrue(finiteStateMachine.getAlphabet().contains('A'));
        assertTrue(finiteStateMachine.getAlphabet().contains('B'));
    }

    @Test
    public void addingDuplicateStatesShouldBePrevented() {
        State state = getNewStandardState("Q0");
        finiteStateMachine.addState(state);
        finiteStateMachine.addState(state);
        assertEquals(1, finiteStateMachine.getStates().size());
    }

    @Test
    public void addingInitialStateShouldUpdateInitialStateReference() {
        State initialState1 = getNewInitialState("Q0");
        State initialState2 = getNewInitialState("Q1");

        finiteStateMachine.addState(initialState1);
        assertEquals(initialState1, finiteStateMachine.getInitialState());

        finiteStateMachine.addState(initialState2);
        assertEquals(initialState2, finiteStateMachine.getInitialState());
    }

    @Test
    public void removingSymbolsFromAlphabetShouldUpdateAlphabet() {
        finiteStateMachine.addSymbolToAlphabet('A');
        finiteStateMachine.addSymbolToAlphabet('B');
        finiteStateMachine.addSymbolToAlphabet('C');
        List<Character> alphabet = new ArrayList<>(finiteStateMachine.getAlphabet());

        finiteStateMachine.removeSymbolFromAlphabet('B');
        finiteStateMachine.removeSymbolFromAlphabet('C');
        assertNotEquals(alphabet, finiteStateMachine.getAlphabet());
    }

    @Test
    public void removingInitialStateShouldUpdateInitialStateReference() {
        State initialState = getNewInitialState("Q0");
        finiteStateMachine.addState(initialState);
        assertEquals(initialState, finiteStateMachine.getInitialState());
        assertTrue(finiteStateMachine.hasInitialState());

        finiteStateMachine.removeState(initialState);
        assertNotEquals(initialState, finiteStateMachine.getInitialState());
        assertFalse(finiteStateMachine.hasInitialState());
    }

    @Test
    public void requestingAcceptingStatesShouldReturnAllAcceptingStates() {
        State acceptingState1 = getNewAcceptingState("Q0");
        State acceptingState2 = getNewAcceptingState("Q1");
        finiteStateMachine.addState(acceptingState1);
        finiteStateMachine.addState(acceptingState2);
        assertTrue(finiteStateMachine.getAcceptingStates().contains(acceptingState1));
        assertTrue(finiteStateMachine.getAcceptingStates().contains(acceptingState2));
        // Check that the finite automaton maintains the set of accepting states even after removals
        finiteStateMachine.removeState(acceptingState1);
        assertFalse(finiteStateMachine.getAcceptingStates().contains(acceptingState1));
    }

    @Test
    public void addingStatesAndRemovingStatesShouldUpdateSize() {
        State state1 = getNewStandardState("Q0");
        State state2 = getNewStandardState("Q1");
        finiteStateMachine.addState(state1);
        assertEquals(1, finiteStateMachine.size());
        finiteStateMachine.addState(state2);
        assertEquals(2, finiteStateMachine.size());
        finiteStateMachine.removeState(state1);
        assertEquals(1, finiteStateMachine.size());
        finiteStateMachine.removeState(state2);
        assertEquals(0, finiteStateMachine.size());
    }

    @Test
    public void addedTransitionShouldBeRetrievableByOriginatingState() {
        State state = getNewStandardState("Q0");
        finiteStateMachine.addState(state);
        Transition transition = new Transition(state, "A", state);
        finiteStateMachine.addTransition(state, transition);
        assertTrue(finiteStateMachine.getOutgoingTransitions(state).contains(transition));
    }

    @Test
    public void addingDuplicateTransitionsShouldBePrevented() {
        State state = getNewStandardState("Q0");
        finiteStateMachine.addState(state);
        Transition transition = new Transition(state, "B", state);
        assertTrue(finiteStateMachine.addTransition(state, transition));
        assertFalse(finiteStateMachine.addTransition(state, transition));
        assertEquals(1, finiteStateMachine.getAllTransitions().size());
    }

    @Test
    public void removingTransitionsShouldUpdateAutomaton() {
        State state = getNewStandardState("Q1");
        finiteStateMachine.addState(state);
        Transition transition = new Transition(state, "C", state);

        finiteStateMachine.addTransition(state, transition);
        assertEquals(1, finiteStateMachine.getAllTransitions().size());
        finiteStateMachine.removeTransition(state, transition);
        assertEquals(0, finiteStateMachine.getAllTransitions().size());
    }

    @Test
    public void shouldTrackEmptyWordTransitionsOnAdditionAndRemoval() {
        State state = getNewStandardState("Q2");
        finiteStateMachine.addState(state);
        Transition transition = new Transition(state, Unicode.EPSILON, state);

        finiteStateMachine.addTransition(state, transition);
        assertTrue(finiteStateMachine.doesEmptyTransitionExist());
        finiteStateMachine.removeTransition(state, transition);
        assertFalse(finiteStateMachine.doesEmptyTransitionExist());
    }

    @Test
    public void removingNonExistentTransitionShouldReturnFalse() {
        State state = getNewStandardState("Q3");
        finiteStateMachine.addState(state);
        assertFalse(finiteStateMachine.removeTransition(state, new Transition(state, Unicode.EPSILON, state)));
    }

    @Test
    public void toStringShouldReturnFiniteAutomatonAttributes() {
        State initialState = getNewInitialState("Q0");
        State acceptingState = getNewAcceptingState("Q1");
        Transition transition = new Transition(initialState, "E", acceptingState);
        finiteStateMachine.addState(initialState);
        finiteStateMachine.addState(acceptingState);
        finiteStateMachine.addTransition(initialState, transition);
        String toString = finiteStateMachine.toString().toLowerCase();
        String[] terms = {"size", "initial state", "alphabet", "states", "transitions",
                initialState.getLabel().toLowerCase(), acceptingState.getLabel().toLowerCase(), transition.getSymbol().toLowerCase()};
        for (String term : terms) {
            assertThat(toString, containsString(term));
        }
    }

    private State getNewStandardState(String label) {
        return new State(label, null, false, false);
    }

    private State getNewInitialState(String label) {
        return new State(label, null, false, true);
    }

    private State getNewAcceptingState(String label) {
        return new State(label, null, true, false);
    }
}