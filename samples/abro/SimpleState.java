package samples.abro;

import samples.abro.interfaces.State;

public class SimpleState implements State {
    private final State parentState;
    private final boolean _isFinal;

    public SimpleState(State parentState, boolean isFinal) {
        this.parentState = parentState;
        this._isFinal = isFinal;
    }

    @Override
    public Object getVariable(String name) {
        return parentState.getVariable(name);
    }

    @Override
    public void setVariable(String name, Object value) {
        parentState.setVariable(name, value);
    }

    @Override
    public void enter() {
        // No entry actions
    }

    @Override
    public void leave() {
        // No exit actions
    }

    @Override
    public void tick() {
        // No internal behavior
    }

    @Override
    public void reset() {
        // No reset actions
    }

    @Override
    public boolean isFinal() {
        return _isFinal;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

}
