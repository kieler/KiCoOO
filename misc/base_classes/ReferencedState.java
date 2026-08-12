package kieler_gen.base_classes;

public abstract class ReferencedState<T extends State> extends State {

    public final T reference;

    public ReferencedState(T reference, boolean isFinal) {
        super(isFinal);
        this.reference = reference;
    }

    // public T getReference() {
    //     return reference;
    // }

    // TODO: update name
    abstract public void copyVariablesIn();

    // TODO: update name
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

    // TODO: this is probably not needed. Reset shouldn't change variables.
    @Override
    public void reset() {
        copyVariablesIn();
        reference.reset();
        copyVariablesOut();
    }

    @Override
    public boolean isTerminated() {
        return reference.isTerminated();
    }
}
