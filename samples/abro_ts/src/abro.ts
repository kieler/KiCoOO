import {
    State,
    Region,
    SimpleState,
    RegionImpl,
    StateImpl,
} from "./base_classes/base";

const ABRO = (_isFinal: boolean) => {
    let A = false;
    let B = false;
    let R = false;
    let O = false;

    const localReset = () => {
        A = false;
        B = false;
        R = false;
        O = false;
    };

    const context = {
        get A() { return A; },
        set A(value: boolean) { A = value; },
        get B() { return B; },
        set B(value: boolean) { B = value; },
        get R() { return R; },
        set R(value: boolean) { R = value; },
        get O() { return O; },
        set O(value: boolean) { O = value; },
    };

    const Root: Region = (() => {
        let ABO: State = ((_isFinal: boolean) => {
            const Wait: Region = (() => {
                const WaitAB: State = ((_isFinal: boolean) => {
                    const HandleA: Region = (() => {
                        const WaitA: State = new SimpleState(false);
                        const DoneA: State = new SimpleState(true);

                        const states: State[] = [WaitA, DoneA];
                        const initialState: State = WaitA;

                        const handleNonPreemptiveTransitions = function (
                            this: RegionImpl,
                        ): boolean {
                            if (this.activeState === WaitA) {
                                if (A) {
                                    this.transitionTo(DoneA);
                                    return true;
                                }
                            }
                            return false;
                        };

                        return new RegionImpl(states, initialState, {
                            handleNonPreemptiveTransitions,
                        });
                    })();

                    const HandleB: Region = (() => {
                        const WaitB: State = new SimpleState(false);
                        const DoneB: State = new SimpleState(true);

                        const states: State[] = [WaitB, DoneB];
                        const initialState: State = WaitB;

                        const handleNonPreemptiveTransitions = function (
                            this: RegionImpl,
                        ): boolean {
                            if (this.activeState === WaitB) {
                                if (B) {
                                    this.transitionTo(DoneB);
                                    return true;
                                }
                            }
                            return false;
                        };

                        return new RegionImpl(states, initialState, {
                            handleNonPreemptiveTransitions,
                        });
                    })();

                    const regions: Region[] = [HandleA, HandleB];

                    return new StateImpl(_isFinal, regions);
                })(false);

                const Done: State = new SimpleState(true);

                const states: State[] = [WaitAB, Done];
                const initialState: State = WaitAB;

                const handleNonPreemptiveTransitions = function (
                    this: RegionImpl,
                ): boolean {
                    if (this.activeState === WaitAB) {
                        if (this.activeState.isTerminated()) {
                            this.transitionTo(Done, () => {
                                O = true;
                            });
                            return true;
                        }
                    }
                    return false;
                };

                return new RegionImpl(states, initialState, {
                    handleNonPreemptiveTransitions,
                });
            })();

            const regions: Region[] = [Wait];

            const onEntry = function onEntry(): void {
                O = false;
            };

            return new StateImpl(_isFinal, regions, { onEntry });
        })(false);

        const states: State[] = [ABO];
        const initialState: State = ABO;

        const handlePreemptiveTransitions = function (this: RegionImpl): boolean {
            if (this.activeState === ABO) {
                if (R) {
                    this.transitionTo(ABO);
                    return true;
                }
            }
            return false;
        };

        return new RegionImpl(states, initialState, {
            handlePreemptiveTransitions,
        });
    })();

    const regions: Region[] = [Root];

    const model: State = new StateImpl(_isFinal, regions, { localReset });

    return {
        model,
        context,
    };
};

export { ABRO };
