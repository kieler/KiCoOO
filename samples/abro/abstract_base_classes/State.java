package samples.abro.abstract_base_classes;

import java.util.List;

public class State {
    private final boolean _isFinal;

    protected List<Region> regions;

    public State(boolean isFinal) {
        this._isFinal = isFinal;
        this.regions = List.of();
    }

    public void onEntry() {
        // default implementation does nothing
    }

    public void onTick() {
        // default implementation does nothing
    }

    public void onExit() {
        // default implementation does nothing
    }

    public void enter() {
        this.onEntry();
        for (Region region : regions) {
            region.enter();
        }
    }

    public void leave() {
        for (Region region : regions) {
            region.leave();
        }
        this.onExit();
    }

    public void tick() {
        this.onTick();
        for (Region region : regions) {
            region.tick();
        }

    }

    public void localReset() {
        // default implementation does nothing
    }

    public void reset() {
        localReset();
        for (Region region : regions) {
            region.reset();
        }
    }

    public boolean isTerminated() {
        return regions.stream().allMatch(Region::isTerminated);
    }

    public boolean isFinal() {
        return _isFinal;
    }
}
