package samples.abro;

import samples.abro.abstract_base_classes.State;

public class SimpleState extends State {

    private final boolean _isFinal;

    public SimpleState( boolean isFinal) {
        this._isFinal = isFinal;
    }

    @Override
    public boolean isFinal() {
        return _isFinal;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void enter() {
        // Do nothing
    }

    @Override
    public void leave() {
        // Do nothing
    }

    @Override
    public void tick() {
        // Do nothing
    }

}
