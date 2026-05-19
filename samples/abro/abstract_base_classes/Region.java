package samples.abro.abstract_base_classes;

import java.util.List;

public class Region {

    protected boolean delayedEnabled = false;
    protected State activeState = null;

    protected List<State> states = List.of();
    protected State initialState = null;

    public void enter() {
        activeState.enter();
    }

    public void leave() {
        activeState.leave();
    }

    public boolean isTerminated() {
        return activeState.isFinal();
    }

    protected void transitionTo(State newState, Runnable... transitionActions) {
        activeState.leave();
        for (Runnable action : transitionActions) {
            action.run();
        }
        activeState = newState;
        activeState.reset();
        activeState.enter();
        this.delayedEnabled = false;
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
    public boolean didStrongAborts() {
        // default implementation does nothing
        return false;
    }

    /**
     * Check for weak aborts and perform the first one that is enabled. Return true
     * if a transition was taken, false otherwise.
     * 
     * @return true if a transition was taken, false otherwise
     */
    public boolean didWeakAborts() {
        // default implementation does nothing
        return false;
    }

    public void tick() {
        boolean transitioned = didStrongAborts();
        if (!transitioned) {
            activeState.tick();
            transitioned = didWeakAborts();
        }
        this.delayedEnabled = true;
    }

    public void reset() {
        activeState = initialState;
        for (State state : states) {
            state.reset();
        }
    }

}
