package samples.abro.abstract_base_classes;

import java.util.List;

public abstract class State {
    private boolean delayEnabled = false;

    private List<Region> regions;

    public State() {
        this.regions = List.of();
    }

    public void setRegions(Region ...regions) {
        this.regions = List.of(regions);
    }

    public boolean isDelayEnabled() {
        return delayEnabled;
    }

    public void setDelayEnabled(boolean delayEnabled) {
        this.delayEnabled = delayEnabled;
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

    public void enter(){
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

    public void reset() {
        delayEnabled = false;
        for (Region region : regions) {
            region.reset();
        }
    }

    public boolean isTerminated() {
        return regions.stream().allMatch(Region::isTerminated);
    }
    
    public abstract boolean isFinal();
}
