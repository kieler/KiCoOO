package kieler_gen.base_classes;

import java.util.List;

public class Region {

    protected State activeState = null;

    protected List<State> states = List.of();
    protected State initialState = null;

    public void enter() {
        if (activeState != null) {
            activeState.enter();
        }
    }

    public void leave() {
        if (activeState != null) {
            activeState.leave();
        }
    }

    public boolean isTerminated() {
        if (activeState == null) {
            return false;
        }
        return activeState.isFinal();
    }

    protected void transitionTo(State newState, Runnable... transitionActions) {
        if (activeState != null) {
            activeState.leave();
        }
        for (Runnable action : transitionActions) {
            action.run();
        }
        activeState = newState;
        if (activeState != null) {
            activeState.reset();
            activeState.enter();
            activeState.delayedEnabled = false;
        }
    }

    // TODO: this could also be part of the State interface, but then we would need
    // to return some kind of "transition result" object that indicates whether a
    // transition was taken, and if so, which state we transitioned to.
    // Also the state would then need to have a reference to the states it can
    // transition to.
    /**
     * Check for strong aborts and perform the first one that is enabled. Return
     * true if a transition was taken, false otherwise.
     * 
     * @return true if a transition was taken, false otherwise
     */
    public boolean handlePreemptiveTransitions() {
        // default implementation does nothing
        return false;
    }

    /**
     * Check for weak aborts and perform the first one that is enabled. Return true
     * if a transition was taken, false otherwise.
     * 
     * @return true if a transition was taken, false otherwise
     */
    public boolean handleNonPreemptiveTransitions() {
        // default implementation does nothing
        return false;
    }

    public void tick() {
        if (activeState == null) {
            transitionTo(initialState);
        }

        boolean transitioned = handlePreemptiveTransitions();
        if (!transitioned) {
            activeState.tick();
            transitioned = handleNonPreemptiveTransitions();
        }
        if (transitioned && activeState != null) {
            // tick the new active state if a transition was taken, to allow for immediate
            // transitions and to ensure that
            // delayed transitions are allowed in the next tick.
            activeState.tick();
        }
        if (activeState != null) {
            activeState.delayedEnabled = true;
        }
    }

    public void reset() {
        activeState = null;
        for (State state : states) {
            state.reset();
        }
    }

}
