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

    abstract public void tick();

    abstract public void reset();

}
