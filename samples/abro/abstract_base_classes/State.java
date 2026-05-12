package samples.abro.abstract_base_classes;

public abstract class State {
    private boolean delayEnabled = false;

    public boolean isDelayEnabled() {
        return delayEnabled;
    }

    public void setDelayEnabled(boolean delayEnabled) {
        this.delayEnabled = delayEnabled;
    }

    public Object getVariable(String name) {
        throw new IllegalArgumentException("Unknown variable: " + name);
    }

    public void setVariable(String name, Object value) {
        throw new IllegalArgumentException("Unknown variable: " + name);
    }

    public abstract void enter();

    public abstract void leave();

    public abstract void tick();

    public void reset() {
        delayEnabled = false;
    }

    public abstract boolean isFinal();

    public abstract boolean isTerminated();
}
