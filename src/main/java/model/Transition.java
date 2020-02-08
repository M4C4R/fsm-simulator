package main.java.model;

import javafx.scene.Group;

/**
 * @author Mert Acar
 * Models a Transition consisting of a starting state, ending state, input symbol, and GUI component which represents
 * the transition.
 */
public class Transition implements Comparable<Transition> {
	private State from;
	private String symbol;
	private State to;
	private Group onScreenVisual;

    /**
     * Initialises the Transition by specifying the starting state for this transition, the target state, and
     * input symbol.
     *
     * @param from   the {@code State} which this transition starts from
     * @param symbol which the transition makes use of
     * @param to     the {@code State} which this transition goes to
     */
    public Transition(State from, String symbol, State to) {
        this.from = from;
        this.symbol = symbol;
        this.to = to;
    }

	/**
	 * Sets the GUI component that this transition is represented by.
     *
     * @param group to represent this transition
	 */
	public void setOnScreenVisual(Group group) {
		onScreenVisual = group;
    }

    /**
     * Get this transition's GUI component.
     *
     * @return the {@code Group} which visually represents the transition
	 */
	public Group getOnScreenVisual() {
		return onScreenVisual;
    }

    /**
     * Get the {@code State} which this transition starts from.
     *
     * @return the starting {@code State} of this transition
	 */
	public State getFromState() {
        return from;
    }

    /**
     * Get the {@code State} which this transition goes to.
     *
     * @return the {@code State} which this transition points to
	 */
	public State getToState() {
		return to;
	}

	/**
	 * Get the input symbol which this transition makes use of.
     *
     * @return the input symbol used by this transition
	 */
	public String getSymbol() {
		return symbol;
	}

	@Override
    public String toString() {
        return from.getLabel() + "  \u2501\u3014 " + symbol + " \u3015\u2501\u2B9E  " + to.getLabel();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;

		if (!(o instanceof Transition)) {
			return false;
		}

		Transition t = (Transition) o;

		return t.getFromState().equals(from) && t.getSymbol().equals(symbol) && t.getToState().equals(to);
	}

	@Override
    public int hashCode() {
        // Transitions are uniquely identified by the combination of their from, to, and input symbol.
        // Hence, the sum of these attributes' hash codes can be used.
        return from.hashCode() + symbol.hashCode() + to.hashCode();
	}

	@Override
	public int compareTo(Transition t) {
		return (from.getLabel() + to.getLabel()).compareTo(t.from.getLabel() + t.to.getLabel());
	}
}
