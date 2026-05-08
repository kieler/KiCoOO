package samples.abro;

import samples.abro.interfaces.State;

public class WaitAB implements State {
    private final State parentState;

    private State activeSubStateA;
    private State activeSubStateB;

    private final State WaitA, WaitB;
    private final State DoneA, DoneB;

    public WaitAB(State parentState) {
        this.parentState = parentState;

        this.WaitA = new SimpleState(this, false);
        this.WaitB = new SimpleState(this, false);
        this.DoneA = new SimpleState(this, true);
        this.DoneB = new SimpleState(this, true);

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
        // No entry actions
    }

    @Override
    public void leave() {
        // No exit actions
    }

    @Override
    public void tick() {
        // Region A
        activeSubStateA.tick();
        if (activeSubStateA.equals(WaitA)) {
            if ((boolean) getVariable("A")) {
                WaitA.leave();
                activeSubStateA = DoneA;
                DoneA.reset();
                DoneA.enter();
            }
        }

        // Region B
        activeSubStateB.tick();
        if (activeSubStateB.equals(WaitB)) {
            if ((boolean) getVariable("B")) {
                WaitB.leave();
                activeSubStateB = DoneB;
                DoneB.reset();
                DoneB.enter();
            }
        }
    }

    @Override
    public void reset() {
        // reset Region A
        activeSubStateA = WaitA;
        WaitA.reset();
        // reset Region B
        activeSubStateB = WaitB;
        WaitB.reset();
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return (activeSubStateA.isFinal()
                && activeSubStateB.isFinal());
    }

}
