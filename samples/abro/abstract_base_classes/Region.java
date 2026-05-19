package samples.abro.abstract_base_classes;

public abstract class Region {

    protected State activeState;

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
    }

    // TODO: this could also be part of the State interface, but then we would need to return some kind of "transition result" object that indicates whether a transition was taken, and if so, which state we transitioned to.
    // Also the state would then need to have a reference to the states it can transition to.
    /**
     * Check for strong aborts and perform the first one that is enabled. Return true if a transition was taken, false otherwise.
     * @return true if a transition was taken, false otherwise
     */
    public boolean doStrongAborts() {
        // default implementation does nothing
        return false;
    }

    /**
     * Check for weak aborts and perform the first one that is enabled. Return true if a transition was taken, false otherwise.
     * @return true if a transition was taken, false otherwise
     */
    public boolean doWeakAborts() {
        // default implementation does nothing
        return false;
    }

    public void tick() {
        boolean transitioned = doStrongAborts();
        if (transitioned) {
            return;     // TODO: is this correct? If we do a strong abort, should we enable delay for the new active state immediately, or should we wait until the next tick?
        }
        activeState.tick();
        transitioned = doWeakAborts();
        if (transitioned) {
            return;
        }
        activeState.setDelayEnabled(true);
    }

    abstract public void reset();

}
