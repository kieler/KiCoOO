import {
    State,
    Region,
    createSimpleState,
    RegionImpl,
    StateImpl,
} from "./base_classes/base";

const ABRO: (isFinal: boolean) => {
    model: State;
    context: {
        getA: () => boolean;
        getB: () => boolean;
        getR: () => boolean;
        getO: () => boolean;
        setA: (value: boolean) => void;
        setB: (value: boolean) => void;
        setR: (value: boolean) => void;
        setO: (value: boolean) => void;
    };
} = (_isFinal: boolean) => {
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

    const getA = () => A;
    const getB = () => B;
    const getR = () => R;
    const getO = () => O;

    const setA = (value: boolean) => {
        A = value;
    };
    const setB = (value: boolean) => {
        B = value;
    };
    const setR = (value: boolean) => {
        R = value;
    };
    const setO = (value: boolean) => {
        O = value;
    };

    const Root: Region = ((boolean) => {
        let ABO: State = ((_isFinal: boolean) => {
            const Wait: Region = (() => {
                const WaitAB: State = ((_isFinal: boolean) => {
                    const HandleA: Region = (() => {
                        const WaitA: State = createSimpleState(false);
                        const DoneA: State = createSimpleState(true);

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
                        const WaitB: State = createSimpleState(false);
                        const DoneB: State = createSimpleState(true);

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

                const Done: State = createSimpleState(true);

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

            const onEntry = () => {
                O = false;
            };

            return new StateImpl(_isFinal, regions, { onEntry });
        })(false);

        const states: State[] = [ABO];
        const initialState: State | null = ABO;

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

    return {
        model: new StateImpl(_isFinal, regions, { localReset }),
        context: { getA, getB, getR, getO, setA, setB, setR, setO },
    };
};

export { ABRO };
