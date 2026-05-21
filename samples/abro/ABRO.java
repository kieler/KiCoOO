package samples.abro;

import java.util.List;
import samples.abro.abstract_base_classes.State;
import samples.abro.abstract_base_classes.Region;

public class ABRO extends State {
    public boolean A, B, R, O;

    public ABRO() {
        super(false);
        this.regions = List.of(new Root());
        reset();
    }

    @Override
    public void localReset() {
        A = false;
        B = false;
        R = false;
        O = false;
    }

    class Root extends Region {

        private final State ABO;

        public Root() {
            this.ABO = new ABO();
            this.initialState = ABO;
            this.states = List.of(ABO);
        }

        @Override
        public boolean didStrongAborts() {
            if (activeState.equals(ABO)) {
                if (delayedEnabled && R) { // if R abort to ABO
                    transitionTo(ABO);
                    return true;
                }
            }
            return false;
        }

        class ABO extends State {

            public ABO() {
                super(false);
                this.regions = List.of(new Wait());
            }

            @Override
            public void onEntry() {
                O = false; // entry do O = false
            }

            class Wait extends Region {

                private final State WaitAB, Done;

                public Wait() {
                    this.WaitAB = new WaitAB();
                    this.Done = new State(false);

                    this.initialState = WaitAB;
                    this.states = List.of(WaitAB, Done);
                }

                @Override
                public boolean didWeakAborts() {
                    if (activeState.equals(WaitAB)) {
                        if (activeState.isTerminated()) { // do O = true join to Done
                            transitionTo(Done, () -> { O = true; });
                            return true;
                        }
                    }
                    return false;
                }

                class WaitAB extends State {

                    public WaitAB() {
                        super(false);
                        this.regions = List.of(new HandleA(), new HandleB());
                    }

                    class HandleA extends Region {

                        private final State WaitA, DoneA;

                        public HandleA() {
                            this.WaitA = new State(false);
                            this.DoneA = new State(true);

                            this.initialState = WaitA;
                            this.states = List.of(WaitA, DoneA);
                        }

                        @Override
                        public boolean didWeakAborts() {
                            if (activeState.equals(WaitA)) {
                                if (this.delayedEnabled && A) { // if A go to DoneA
                                    transitionTo(DoneA);
                                    return true;
                                }
                            }
                            return false;
                        }
                    }

                    class HandleB extends Region {

                        private final State WaitB, DoneB;

                        public HandleB() {
                            this.WaitB = new State(false);
                            this.DoneB = new State(true);

                            this.initialState = WaitB;
                            this.states = List.of(WaitB, DoneB);
                        }

                        @Override
                        public boolean didWeakAborts() {
                            if (activeState.equals(WaitB)) {
                                if (this.delayedEnabled && B) { // if B go to DoneB
                                    transitionTo(DoneB);
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                }
            }
        }
    }
}
