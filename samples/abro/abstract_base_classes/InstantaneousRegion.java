package samples.abro.abstract_base_classes;

public abstract class InstantaneousRegion extends Region {

    @Override
    public void tick() {
        // This region has instantaneous transitions, so we might do multiple transitions in a single tick. We will keep ticking until we reach a stable state (i.e., no more transitions).
        while (true) {
            boolean transitioned = doStrongAborts();
            if (transitioned) {
                continue;
            }
            activeState.tick();
            transitioned = doWeakAborts();
            if (transitioned) {
                continue;
            }
            break;
        }

        activeState.setDelayEnabled(true);
    }
}
