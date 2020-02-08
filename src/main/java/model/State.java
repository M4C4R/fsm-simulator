package main.java.model;

import javafx.scene.Group;

/**
 * @author Mert Acar
 * <p>
 * Models a State consisting of a label and GUI component which represents the state, as well as
 * attributes specifying whether the State is isAccepting and/or isInitial.
 * </p>
 */
public class State implements Comparable<State>{
    private boolean isInitial;
    private boolean isAccepting;
	private String label;
	private Group onScreenVisual;

    /**
     * The standard radius in px for states
     */
    public static final int  RADIUS_OF_STATE = 30;

    /**
     * Initialises this state by setting its label and group to represent it visually.
     *
     * @param label for this state
     * @param group to visually represent this state
     */
    public State(String label, Group group) {
        this.label = label;
		onScreenVisual = group;
    }

	/**
	 * Initialises this state by setting its label, group to represent it visually, and whether or not this state is
	 * accepting and/or initial.
	 *
	 * @param label       for this state
	 * @param group       to visually represent this state
	 * @param isAccepting whether this state is accepting
	 * @param isInitial   whether this state is the initial state
	 */
	public State(String label, Group group, boolean isAccepting, boolean isInitial) {
		this.label = label;
		onScreenVisual = group;
		this.isAccepting = isAccepting;
		this.isInitial = isInitial;
	}

    /**
     * Checks whether this state is an initial state.
     *
     * @return <tt>true</tt> if the state is an initial state
	 */
	public boolean isInitial() {
		return isInitial;
    }

    /**
     * Checks whether this state is an accepting state.
     *
     * @return <tt>true</tt> if the state is an accepting state
	 */
	public boolean isAccepting() {
        return isAccepting;
    }

    /**
     * Sets this state to be an initial state if the specified value is <tt>true</tt>, otherwise the state is set
     * to not be an initial state.
     *
     * @param value to specify whether this state is an initial state
	 */
	public void setInitial(boolean value) {
		isInitial = value;
    }

    /**
     * Sets this state to be an accepting state if the specified value is <tt>true</tt>, otherwise the state is set
     * to not be an accepting state.
     *
     * @param value to specify whether this state is an accepting state
	 */
	public void setAccepting(boolean value) {
        isAccepting = value;
    }

    /**
     * Get this state's label.
     *
     * @return the label of this state
	 */
	public String getLabel() {
		return label;
    }

    /**
     * Get this state's GUI component.
     *
     * @return the {@code Group} which visually represents the state
	 */
	public Group getOnScreenVisual() {
		return onScreenVisual;
	}

	@Override
	public String toString() {
		return getLabel() + ((isInitial()) ? " (initial)": "");
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;

		if (!(o instanceof State)) {
			return false;
		}

		return ((State) o).getLabel().equals(getLabel());
	}

	@Override
	public int hashCode() {
        // States are uniquely identified by their label, so the hash code of their labels can be used
        return label.hashCode();
	}

	@Override
	public int compareTo(State s) {
		try {
			return Integer.compare(Integer.parseInt(getLabel().substring(1)), Integer.parseInt(s.getLabel().substring(1)));
		} catch (NumberFormatException e) {
			return getLabel().compareTo(s.getLabel());
		}
	}
}
