package samples.abro;

import samples.abro.interfaces.State;

public class ABO implements State {

    private final State parentState;

    private State activeSubState;

    private final State WaitAB, Done;

    public ABO(State parentState) {
        this.parentState = parentState;
        this.WaitAB = new WaitAB(this);
        this.Done = new SimpleState(this, false);
        reset();
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
        setVariable("O", false);
        activeSubState.enter();
    }

    public void leave() {
        activeSubState.leave();
    }

    @Override
    public void tick() {
        // No strong abort

        activeSubState.tick();

        // Do weak abort check
        if (activeSubState.equals(WaitAB)) {
            if (WaitAB.isTerminated()) {
                WaitAB.leave();
                activeSubState = Done;
                Done.reset();
                Done.enter();
            }
        }
    }

    @Override
    public void reset() {
        activeSubState = WaitAB;
        WaitAB.reset();
    }

    public boolean isFinal() {
        return false;
    }

    public boolean isTerminated() {
        return activeSubState.isFinal();
    }
}
