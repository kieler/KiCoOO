package samples.abro.interfaces;

public interface State {
    public Object getVariable(String name);

    public void setVariable(String name, Object value);

    public void enter();

    public void leave();

    public void tick();

    public void reset();

    public boolean isFinal();

    public boolean isTerminated();
}
