package main.java.model;

import main.java.shared.Unicode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Mert Acar
 * <p>
 * Model of a finite automaton, consists of an initial state, states (which individually specify whether they are
 * accepting), transitions, and an alphabet.
 * </p>
 */
public class FiniteStateMachine {
	private State initialState;
	private List<State> states;
	private List<Character> alphabet;
	private Map<State, List<Transition>> transitions;

    private int numberOfEmptyTransitions;

	/**
	 * Initialises the finite automaton's containers storing the states, alphabet, and transitions.
	 */
	public FiniteStateMachine() {
		states = new ArrayList<>();
		alphabet = new ArrayList<>();
		transitions = new HashMap<>();
	}

	/**
	 * Adds the specified symbol to this finite automaton's alphabet.
	 *
	 * @param symbol to add to the alphabet
	 * @return <tt>true</tt> if it was successfully added
	 */
	public boolean addSymbolToAlphabet(Character symbol) {
		if (alphabet.contains(symbol)) {
			return false;
		}
		return alphabet.add(symbol);
	}

	/**
	 * Removes the specified symbol from this finite automaton's alphabet.
	 *
	 * @param symbol to remove from the alphabet
	 * @return <tt>true</tt> if it was successfully removed
	 */
	public boolean removeSymbolFromAlphabet(Character symbol) {
		return alphabet.remove(symbol);
	}

	/**
	 * Adds the specified state to this finite automaton.
	 * If this state is an initial state, the finite automaton updates its initial state reference to match this state.
	 *
	 * @param state to add to the finite automaton
	 */
	public void addState(State state) {
		if (states.contains(state)) {
			return;
		} else if (state.isInitial()) {
			if (hasInitialState()) {
				getInitialState().setInitial(false);
			}
			setInitialState(state);
		}
		states.add(state);
		// Initialise an index for this state's outgoing transitions
		transitions.put(state, new ArrayList<>());
	}

	/**
	 * Removes the specified state from this finite automaton.
	 * If this state is an initial state, the finite automaton removes its initial state reference.
	 *
	 * @param state to remove from the finite automaton
	 */
	public void removeState(State state) {
		if (state.isInitial()) {
			setInitialState(null);
		}
		states.remove(state);
		transitions.remove(state);
	}

	/**
	 * Checks whether a state has already been assigned as the initial state.
	 *
	 * @return <tt>true</tt> if the finite automaton has an initial state
	 */
	public boolean hasInitialState() {
		return initialState != null;
	}

	/**
	 * Sets this finite automaton's initial state, clearing any previously assigned initial state.
	 *
	 * @param state to be assigned as the initial state
	 */
	public void setInitialState(State state) {
		if (hasInitialState()) {
			getInitialState().setInitial(false);
		}
		initialState = state;
	}

	/**
	 * Get this finite automaton's initial state.
	 *
	 * @return the initial {@code State} or {@code null}
	 */
	public State getInitialState() {
		return initialState;
	}

	/**
	 * Get a list of this finite automaton's accepting states.
	 *
	 * @return {@code List} containing the accepting states
     */
	public List<State> getAcceptingStates() {
		return states.stream().filter(State::isAccepting).collect(Collectors.toCollection(ArrayList::new));
    }

	/**
	 * Get a count of the number of states defined for this finite automaton.
	 *
	 * @return the number of states within this finite automaton
	 */
	public int size() {
		return states.size();
	}

	/**
	 * Get this finite automaton's alphabet.
	 *
	 * @return the alphabet used by this finite automaton
	 */
	public List<Character> getAlphabet() {
		return alphabet;
	}

	/**
	 * Get this finite automaton's collection of states.
	 *
	 * @return the collection of states used by this finite automaton
	 */
	public List<State> getStates() {
		return states;
	}

	/**
	 * Add the specified transition to this finite automaton.
	 * Assumes that the specified state is already a part of the finite automaton.
	 *
	 * @param state which the transition starts at
	 * @param transition to be added to this finite automaton
	 * @return <tt>true</tt> if the transition was successfully added
	 */
	public boolean addTransition(State state, Transition transition) {
		List<Transition> currentTransitions = transitions.get(state);
		if (currentTransitions.contains(transition)) {
			return false;
		}
        if (transition.getSymbol().equals(Unicode.EPSILON)) {
            numberOfEmptyTransitions++;
        }
		return currentTransitions.add(transition);
	}

	/**
	 * Remove the specified transition from this finite automaton.
	 *
	 * @param state which the transition starts at
	 * @param transition to be removed from this finite automaton
	 * @return <tt>true</tt> if the transition was successfully removed
	 */
	public boolean removeTransition(State state, Transition transition) {
		List<Transition> currentTransitions = transitions.get(state);
		if (!currentTransitions.contains(transition)) {
			return false;
		}
        if (transition.getSymbol().equals(Unicode.EPSILON)) {
            numberOfEmptyTransitions--;
        }
		return currentTransitions.remove(transition);
	}

	/**
	 * Get all the transitions outgoing from the specified state.
	 *
	 * @param state to get the transitions of
	 * @return {@code List} of transitions outgoing from the specified state, or {@code null} if the state is not a
	 * part of this finite automaton
	 */
    public List<Transition> getOutgoingTransitions(State state) {
		return transitions.get(state);
	}

	/**
	 * Get all the transitions that are a part of this finite automaton.
	 *
	 * @return {@code List} of this finite automaton's transitions
	 */
	public List<Transition> getAllTransitions() {
		List<Transition> allTransitions = new ArrayList<>();
		transitions.values().forEach(allTransitions::addAll);
		return allTransitions;
	}

	/**
	 * Clear this finite automaton's defined attributes to make it empty.
	 */
	public void clear() {
		initialState = null;
		states.clear();
		alphabet.clear();
		transitions.clear();
        numberOfEmptyTransitions = 0;
    }

    /**
     * Check whether the finite automaton contains a transition using the empty word symbol (i.e. epsilon).
     *
     * @return <tt>true</tt> if there exists a transition using the empty symbol
     */
    public boolean doesEmptyTransitionExist() {
        return numberOfEmptyTransitions > 0;
    }

	@Override
	public String toString() {
		return "Size = " + states.size() + "\nInitial state = " + initialState + "\nAlphabet = " + alphabet + "\nStates = " + states + "\nTransitions = " + getAllTransitions() + "\n";
	}
}
