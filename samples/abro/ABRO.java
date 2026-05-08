package samples.abro;

import samples.abro.interfaces.State;

public class ABRO implements State {
    private boolean A, B, R, O;

    private final State ABO;

    public ABRO() {
        this.ABO = new ABO(this);
    }

    @Override
    public Object getVariable(String name) {
        switch (name) {
            case "A":
                return A;
            case "B":
                return B;
            case "R":
                return R;
            case "O":
                return O;
        }
        throw new IllegalArgumentException("Unknown variable: " + name);
    }

    @Override
    public void setVariable(String name, Object value) {
        switch (name) {
            case "A":
                A = (boolean) value;
                break;
            case "B":
                B = (boolean) value;
                break;
            case "R":
                R = (boolean) value;
                break;
            case "O":
                O = (boolean) value;
                break;
            default:
                throw new IllegalArgumentException("Unknown variable: " + name);
        }
    }

    @Override
    public void enter() {
        ABO.enter();
    }

    @Override
    public void leave() {
        ABO.leave();
    }

    @Override
    public void tick() {
        if (R) {
            ABO.leave();
            ABO.reset();
            ABO.enter();
            return;
        }
        ABO.tick();
    }

    @Override
    public void reset() {
        ABO.reset();
    }

    public boolean isFinal() {
        return false;
    }

    public boolean isTerminated() {
        return ABO.isFinal();
    }
}
