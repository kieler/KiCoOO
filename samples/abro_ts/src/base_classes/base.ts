// class State {
//     private readonly _isFinal: boolean;
//     regions: Region[];

//     constructor(isFinal: boolean) {
//         this._isFinal = isFinal;
//         this.regions = [];
//     }

//     onEntry() {
//         // default implementation does nothing
//     }

//     onTick() {
//         // default implementation does nothing
//     }

//     onExit() {
//         // default implementation does nothing
//     }

//     enter() {
//         this.onEntry();
//         for (let region of this.regions) {
//             region.enter();
//         }
//     }

//     leave() {
//         for (let region of this.regions) {
//             region.leave();
//         }
//         this.onExit();
//     }

//     tick() {
//         this.onTick();
//         for (let region of this.regions) {
//             region.tick();
//         }

//     }

//     localReset() {
//         // default implementation does nothing
//     }

//     reset() {
//         this.localReset();
//         for (let region of this.regions) {
//             region.reset();
//         }
//     }

//     isTerminated() {
//         return this.regions.every(region => region.isTerminated());
//     }

//     isFinal() {
//         return this._isFinal;
//     }
// }

// class Region {
//     activeState: State | null = null;
//     states: State[] = [];
//     initialState: State | null = null;

//     constructor() {
//     }

//     enter() {
//         if (this.activeState !== null) {
//             this.activeState.enter();
//         }
//     }

//     leave() {
//         if (this.activeState !== null) {
//             this.activeState.leave();
//         }
//     }

//     isTerminated() {
//         if (this.activeState === null) {
//             return false;
//         }
//         return this.activeState.isFinal();
//     }

//     reset() {
//         this.activeState = null;
//         for (let state of this.states) {
//             state.reset();
//         }
//     }

//     transitionTo(state: State, action?: () => void) {
//         if (this.activeState !== null) {
//             this.activeState.leave();
//         }
//         if (action) {
//             action();
//         }
//         this.activeState = state;
//         this.activeState.reset();
//         this.activeState.enter();
//     }

//     /**
//      * Check for strong aborts and perform the first one that is enabled. Return
//      * true if a transition was taken, false otherwise.
//      *
//      * @return true if a transition was taken, false otherwise
//      */
//     handlePreemptiveTransitions(): boolean {
//         // default implementation does nothing
//         return false;
//     }

//     /**
//      * Check for weak aborts and perform the first one that is enabled. Return true
//      * if a transition was taken, false otherwise.
//      *
//      * @return true if a transition was taken, false otherwise
//      */
//     handleNonPreemptiveTransitions(): boolean {
//         // default implementation does nothing
//         return false;
//     }

//     tick() {
//         if (this.activeState == null) {
//             this.transitionTo(this.initialState!);
//             this.activeState!.tick();
//         } else {
//             let transitioned = this.handlePreemptiveTransitions();
//             if (!transitioned) {
//                 this.activeState.tick();
//                 transitioned = this.handleNonPreemptiveTransitions();
//             }
//             if (transitioned) {
//                 // tick the new active state if a transition was taken, to allow for immediate transitions and to ensure that
//                 // delayed transitions are allowed in the next tick.
//                 // TODO: check if this is actually does the right thing in this variant of the semantics.
//                 this.activeState.tick();
//             }
//         }
//     }
// }

// abstract class ReferencedState<T extends State> extends State {
//     readonly reference: T;

//     constructor(reference: T, isFinal: boolean) {
//         super(isFinal);
//         this.reference = reference;
//     }

//     abstract copyVariablesToReference(): void;

//     abstract copyVariablesFromReference(): void;

//     override enter() {
//         this.copyVariablesToReference();
//         super.enter();
//         this.copyVariablesFromReference();
//     }

//     override leave() {
//         this.copyVariablesToReference();
//         super.leave();
//         this.copyVariablesFromReference();
//     }

//     override tick() {
//         this.copyVariablesToReference();
//         super.tick();
//         this.copyVariablesFromReference();
//     }

//     // TODO: this is probably not needed. Reset shouldn't change variables.
//     override reset() {
//         this.copyVariablesToReference();
//         super.reset();
//         this.copyVariablesFromReference();
//     }
//}

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

        // this.handlePreemptiveTransitions = handlePreemptiveTransitions?.bind(this) ?? (() => false);
        // this.handleNonPreemptiveTransitions = handleNonPreemptiveTransitions?.bind(this) ?? (() => false);

        this.handlePreemptiveTransitions =
            handlePreemptiveTransitions ??
            function (this: RegionImpl): boolean {
                return false;
            }.bind(this);
        this.handleNonPreemptiveTransitions =
            handleNonPreemptiveTransitions ??
            function (this: RegionImpl): boolean {
                return false;
            }.bind(this);
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

function createSimpleState(_isFinal: boolean): State {
    return {
        regions: [],

        enter: () => { },
        leave: () => { },
        tick: () => { },
        reset: () => { },
        isTerminated: () => false,
        isFinal: () => _isFinal,
    };
}

export { State, Region, createSimpleState, RegionImpl, StateImpl };
