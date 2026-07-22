package kieler_gen.base_classes;

public abstract class InstantaneousRegion extends Region {

    @Override
    public void tick() {
        // This region has instantaneous transitions, so we might do multiple
        // transitions in a single tick. We will keep ticking until we reach a stable
        // state (i.e., no more transitions).
        boolean transitioned;
        do {
            transitioned = handlePreemptiveTransitions();
            if (!transitioned) {
                activeState.tick();
                transitioned = handleNonPreemptiveTransitions();
            }
        } while (transitioned);

        activeState.delayedEnabled = true;
    }
}
