package samples.abro.abstract_base_classes;

public abstract class InstantaneousRegion extends Region {

    @Override
    public void tick() {
        // This region has instantaneous transitions, so we might do multiple
        // transitions in a single tick. We will keep ticking until we reach a stable
        // state (i.e., no more transitions).
        boolean transitioned;
        do {
            transitioned = didStrongAborts();
            if (!transitioned) {
                activeState.tick();
                transitioned = didWeakAborts();
            }
        } while (transitioned);

        this.delayedEnabled = true;
    }
}
