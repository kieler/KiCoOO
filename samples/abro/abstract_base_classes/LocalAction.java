package samples.abro.abstract_base_classes;

import java.util.function.BooleanSupplier;

public class LocalAction {
    
    private final BooleanSupplier guard;
    private final Runnable action;

    public LocalAction(BooleanSupplier guard, Runnable action) {
        this.guard = guard;
        this.action = action;
    }

    public void executeIfGuardTrue() {
        if (guard.getAsBoolean()) {
            action.run();
        }
    }
}
