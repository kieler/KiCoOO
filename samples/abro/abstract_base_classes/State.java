package samples.abro.abstract_base_classes;

import java.util.ArrayList;
import java.util.List;

public abstract class State {
    private boolean delayEnabled = false;

    private final ArrayList<Region> regions;
    private final ArrayList<LocalAction> entryActions, duringActions, exitActions;

    public State() {
        this.regions = new ArrayList<>();
        this.entryActions = new ArrayList<>();
        this.duringActions = new ArrayList<>();
        this.exitActions = new ArrayList<>();
    }

    protected void addRegions(Region... regions) {
        this.regions.addAll(List.of(regions));
    }

    protected void addEntryActions(LocalAction... actions) {
        this.entryActions.addAll(List.of(actions));
    }

    protected void addDuringActions(LocalAction... actions) {
        this.duringActions.addAll(List.of(actions));
    }

    protected void addExitActions(LocalAction... actions) {
        this.exitActions.addAll(List.of(actions));
    }

    public boolean isDelayEnabled() {
        return delayEnabled;
    }

    public void setDelayEnabled(boolean delayEnabled) {
        this.delayEnabled = delayEnabled;
    }

    public void enter(){
        for (LocalAction action : entryActions) {
            action.executeIfGuardTrue();
        }
        for (Region region : regions) {
            region.enter();
        }
    }

    public void leave() {
        for (Region region : regions) {
            region.leave();
        }
        for (LocalAction action : exitActions) {
            action.executeIfGuardTrue();
        }
    }

    public void tick() {
        for (LocalAction action : duringActions) {
            action.executeIfGuardTrue();
        }
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
