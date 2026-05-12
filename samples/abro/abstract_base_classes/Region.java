package samples.abro.abstract_base_classes;

public abstract class Region {

    protected State activeState;

    public void enter() {
        activeState.enter();
    }

    public void leave() {
        activeState.leave();
    }

    abstract public void tick();

    abstract public void reset();

    public boolean isTerminated() {
        return activeState.isFinal();
    }
}
