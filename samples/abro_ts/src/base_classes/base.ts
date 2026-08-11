
interface State {
    regions: Region[];

    enter(): void;
    leave(): void;
    tick(): void;
    reset(): void;
    isTerminated(): boolean;
    isFinal(): boolean;
}

interface Region {
    states: State[];
    initialState: State;
    activeState: State | null;

    enter(): void;
    leave(): void;
    isTerminated(): boolean;
    reset(): void;
    tick(): void;
}

class StateImpl implements State {
    regions: Region[];
    private _isFinal: boolean;
    private onEntry?: () => void;
    private onTick?: () => void;
    private onExit?: () => void;
    private localReset?: () => void;

    constructor(
        _isFinal: boolean,
        regions: Region[],
        {
            onEntry,
            onTick,
            onExit,
            localReset,
        }: {
            onEntry?: () => void;
            onTick?: () => void;
            onExit?: () => void;
            localReset?: () => void;
        } = {},
    ) {
        this._isFinal = _isFinal;
        this.regions = regions;
        this.onEntry = onEntry;
        this.onTick = onTick;
        this.onExit = onExit;
        this.localReset = localReset;
    }

    enter() {
        this.onEntry?.();
        for (let region of this.regions) {
            region.enter();
        }
    }

    leave() {
        for (let region of this.regions) {
            region.leave();
        }
        this.onExit?.();
    }

    tick() {
        this.onTick?.();
        for (let region of this.regions) {
            region.tick();
        }
    }

    reset() {
        this.localReset?.();
        for (let region of this.regions) {
            region.reset();
        }
    }

    isTerminated() {
        return this.regions.every((region) => region.isTerminated());
    }

    isFinal() {
        return this._isFinal;
    }
}

class RegionImpl implements Region {
    states: State[];
    initialState: State;
    activeState: State | null = null;

    private handlePreemptiveTransitions: () => boolean;
    private handleNonPreemptiveTransitions: () => boolean;

    constructor(
        states: State[],
        initialState: State,
        {
            handlePreemptiveTransitions,
            handleNonPreemptiveTransitions,
        }: {
            handlePreemptiveTransitions?: (this: RegionImpl) => boolean;
            handleNonPreemptiveTransitions?: (this: RegionImpl) => boolean;
        } = {},
    ) {
        this.states = states;
        this.initialState = initialState;

        this.handlePreemptiveTransitions = handlePreemptiveTransitions ?? (() => false);
        this.handleNonPreemptiveTransitions = handleNonPreemptiveTransitions ?? (() => false);
    }

    enter(): void {
        this.activeState?.enter();
    }

    leave(): void {
        this.activeState?.leave();
    }

    isTerminated(): boolean {
        return this.activeState?.isFinal() ?? false;
    }

    reset(): void {
        this.activeState = null;
        for (const state of this.states) {
            state.reset();
        }
    }

    transitionTo(state: State, action?: () => void): void {
        this.activeState?.leave();
        if (action) {
            action();
        }
        this.activeState = state;
        this.activeState.reset();
        this.activeState.enter();
    }

    tick(): void {
        if (this.activeState == null) {
            this.transitionTo(this.initialState);
            const newActiveState = this.activeState as State | null; // Safe-ish type assertion to satisfy the type checker.
            newActiveState?.tick();
        } else {
            let transitioned = this.handlePreemptiveTransitions();
            if (!transitioned) {
                this.activeState?.tick();
                transitioned = this.handleNonPreemptiveTransitions();
            }
            if (transitioned) {
                // tick the new active state if a transition was taken, to allow for immediate transitions and to ensure that
                // delayed transitions are allowed in the next tick.
                this.activeState?.tick();
            }
        }
    }
}

// This could possibly be two singletons.
class SimpleState implements State {
    regions: Region[];
    private _isFinal: boolean;

    constructor(_isFinal: boolean) {
        this._isFinal = _isFinal;
        this.regions = [];
    }

    enter() { }
    leave() { }
    tick() { }
    reset() { }
    isTerminated() { return false; }
    isFinal() { return this._isFinal; }
}

class ReferenceState implements State {
    regions: Region[] = [];

    private copyVariablesToReference: () => void;
    private copyVariablesFromReference: () => void;

    constructor(
        private _isFinal: boolean,
        private reference: State,
        {
            copyVariablesToReference,
            copyVariablesFromReference,
        }: {
            copyVariablesToReference: () => void;
            copyVariablesFromReference: () => void;
        },
    ) {
        this.copyVariablesToReference = copyVariablesToReference;
        this.copyVariablesFromReference = copyVariablesFromReference;
    }

    enter(): void {
        this.copyVariablesToReference();
        this.reference.enter();
        this.copyVariablesFromReference();
    }
    leave(): void {
        this.copyVariablesToReference();
        this.reference.leave();
        this.copyVariablesFromReference();
    }
    tick(): void {
        this.copyVariablesToReference();
        this.reference.tick();
        this.copyVariablesFromReference();
    }
    reset(): void {
        this.copyVariablesToReference();
        this.reference.reset();
        this.copyVariablesFromReference();
    }
    isTerminated(): boolean {
        return this.reference.isTerminated();
    }
    isFinal(): boolean {
        return this._isFinal;
    }
}


export { State, Region, SimpleState, ReferenceState, RegionImpl, StateImpl };
