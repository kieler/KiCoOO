package samples.abro;

import samples.abro.abstract_base_classes.State;
import samples.abro.abstract_base_classes.Region;

public class ABRO extends State {
    private boolean A, B, R, O;

    public ABRO() {
        super();
        setRegions(new Root());
        reset();
    }

    public Object getVariable(String name) {
        return switch (name) {
            case "A" -> A;
            case "B" -> B;
            case "R" -> R;
            case "O" -> O;
            default -> throw new IllegalArgumentException("Unknown variable: " + name);
        };
    }

    public void setVariable(String name, Object value) {
        switch (name) {
            case "A":
                A = (boolean) value;
                break;
            case "B":
                B = (boolean) value;
                break;
            case "R":
                R = (boolean) value;
                break;
            case "O":
                O = (boolean) value;
                break;
            default:
                throw new IllegalArgumentException("Unknown variable: " + name);
        }
    }

    @Override
    public void reset() {
        A = B = R = O = false;
        super.reset();
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    class Root extends Region {

        private final State ABO;

        public Root() {
            this.ABO = new ABO();
            this.reset();
        }

        @Override
        public boolean doStrongAborts() {
            if (activeState.equals(ABO)) {
                if (activeState.isDelayEnabled() && R) {        // if R abort to ABO
                    transitionTo(ABO);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void reset() {
            activeState = ABO;
            ABO.reset();
        }

        class ABO extends State {

            public ABO() {
                super();
                setRegions(new Wait());
            }

            @Override
            public boolean isFinal() {
                return false;
            }

            @Override
            public void onEntry() {
                O = false;          // entry do O = false
            }

            class Wait extends Region {

                private final State WaitAB, Done;

                public Wait() {
                    this.WaitAB = new WaitAB();
                    this.Done = new SimpleState(false);
                    reset();
                }

                @Override
                public boolean doWeakAborts() {
                    if (activeState.equals(WaitAB)) {
                        if (activeState.isTerminated()) {           // do O = true join to Done
                            transitionTo(Done, () -> {O = true;});
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void reset() {
                    activeState = WaitAB;
                    WaitAB.reset();
                    Done.reset();
                }

                class WaitAB extends State {

                    public WaitAB() {
                        super();
                        setRegions(new HandleA(), new HandleB());
                    }

                    @Override
                    public boolean isFinal() {
                        return false;
                    }

                    class HandleA extends Region {

                        private final State WaitA, DoneA;

                        public HandleA() {
                            this.WaitA = new SimpleState(false);
                            this.DoneA = new SimpleState(true);
                            reset();
                        }

                        @Override
                        public boolean doWeakAborts() {
                            if (activeState.equals(WaitA)) {
                                if (activeState.isDelayEnabled() && A) {    // if A go to DoneA
                                    transitionTo(DoneA);
                                    return true;
                                }
                            }
                            return false;
                        }

                        @Override
                        public void reset() {
                            activeState = WaitA;
                            WaitA.reset();
                            DoneA.reset();
                        }
                    }

                    class HandleB extends Region {

                        private final State WaitB, DoneB;

                        public HandleB() {
                            this.WaitB = new SimpleState(false);
                            this.DoneB = new SimpleState(true);
                            reset();
                        }

                        @Override
                        public boolean doWeakAborts() {
                            if (activeState.equals(WaitB)) {
                                if (activeState.isDelayEnabled() && B) {   // if B go to DoneB
                                    transitionTo(DoneB);
                                    return true;
                                }
                            }
                            return false;
                        }

                        @Override
                        public void reset() {
                            activeState = WaitB;
                            WaitB.reset();
                            DoneB.reset();
                        }
                    }
                }
            }

        }

    }
}
