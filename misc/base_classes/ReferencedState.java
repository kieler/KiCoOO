package kieler_gen.base_classes;

public abstract class ReferencedState<T extends State> extends State {

    private final T reference;

    public ReferencedState(T reference, boolean isFinal) {
        super(isFinal);
        this.reference = reference;
    }

    public T getReference() {
        return reference;
    }

    abstract public void copyVariablesIn();

    abstract public void copyVariablesOut();

    @Override
    public void enter() {
        copyVariablesIn();
        reference.enter();
        copyVariablesOut();
    }

    @Override
    public void leave() {
        copyVariablesIn();
        reference.leave();
        copyVariablesOut();
    }

    @Override
    public void tick() {
        copyVariablesIn();
        reference.tick();
        copyVariablesOut();
    }

    @Override
    public void reset() {
        copyVariablesIn();
        reference.reset();
        copyVariablesOut();
    }
}
