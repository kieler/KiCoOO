package samples.abro;

import samples.abro.abstract_base_classes.State;
import samples.abro.abstract_base_classes.Region;

public class ABRO extends State {
    private boolean A, B, R, O;

    private final Region Root;

    public ABRO() {
        this.Root = new Root();
    }

    @Override
    public Object getVariable(String name) {
        switch (name) {
            case "A":
                return A;
            case "B":
                return B;
            case "R":
                return R;
            case "O":
                return O;
        }
        throw new IllegalArgumentException("Unknown variable: " + name);
    }

    @Override
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
    public void enter() {
        Root.enter();
    }

    @Override
    public void leave() {
        Root.leave();
    }

    @Override
    public void tick() {
        Root.tick();
    }

    @Override
    public void reset() {
        setDelayEnabled(false);
        Root.reset();
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return Root.isTerminated();
    }


    class Root extends Region {

        private final State ABO;

        public Root() {
            this.ABO = new ABO();
            this.reset();
        }

        @Override
        public void tick() {
            boolean tickAgain = true;
            while (tickAgain) {
                tickAgain = false;
                if (activeState.equals(ABO)) {
                    if (activeState.isDelayEnabled() && R) {
                        activeState.leave();
                        // no effect
                        activeState = ABO;
                        ABO.reset();
                        ABO.enter();
                        tickAgain = true;
                        continue;
                    }
                }
                activeState.tick();
            }

            activeState.setDelayEnabled(true);
        }

        @Override
        public void reset() {
            activeState = ABO;
            ABO.reset();
        }

        public class ABO extends State {

            private final Region Wait;

            public ABO() {
                this.Wait = new Wait();
            }

            @Override
            public void enter() {
                O = false;
                Wait.enter();
            }

            public void leave() {
                Wait.leave();
                // no exit actions
            }

            @Override
            public void tick() {
                Wait.tick();
            }

            @Override
            public void reset() {
                setDelayEnabled(false);
                Wait.reset();
            }

            public boolean isFinal() {
                return false;
            }

            @Override
            public boolean isTerminated() {
                return Wait.isTerminated();
            }

            class Wait extends Region {

                private final State WaitAB, Done;

                public Wait() {
                    this.WaitAB = new WaitAB();
                    this.Done = new SimpleState(false);
                    reset();
                }

                @Override
                public void tick() {
                    boolean tickAgain = true;
                    while (tickAgain) {
                        tickAgain = false;
                        // No strong abort
                        activeState.tick();
                        // weak abort check
                        if (activeState.equals(WaitAB)) {
                            if (activeState.isTerminated()) {
                                activeState.leave();
                                O = true;
                                activeState = Done;
                                Done.reset();
                                Done.enter();
                                tickAgain = true;
                                continue;
                            }
                        }
                    }
                    activeState.setDelayEnabled(true);
                }

                @Override
                public void reset() {
                    activeState = WaitAB;
                    WaitAB.reset();
                    Done.reset();
                }

                class WaitAB extends State {

                    private final Region HandleA, HandleB;

                    public WaitAB() {
                        this.HandleA = new HandleA();
                        this.HandleB = new HandleB();
                    }

                    @Override
                    public boolean isFinal() {
                        return false;
                    }

                    @Override
                    public boolean isTerminated() {
                        return HandleA.isTerminated() && HandleB.isTerminated();
                    }

                    @Override
                    public void enter() {
                        // no entry actions
                        HandleA.enter();
                        HandleB.enter();
                    }

                    @Override
                    public void leave() {
                        HandleA.leave();
                        HandleB.leave();
                        // no exit actions
                    }

                    @Override
                    public void tick() {
                        HandleA.tick();
                        HandleB.tick();
                    }

                    @Override
                    public void reset() {
                        setDelayEnabled(false);
                        HandleA.reset();
                        HandleB.reset();
                    }


                    class HandleA extends Region {

                        private final State WaitA, DoneA;

                        public HandleA() {
                            this.WaitA = new SimpleState(false);
                            this.DoneA = new SimpleState(true);
                            reset();
                        }

                        @Override
                        public boolean isTerminated() {
                            return activeState.isFinal();
                        }

                        @Override
                        public void tick() {
                            boolean tickAgain = true;
                            while (tickAgain) {

                                activeState.tick();

                                if (activeState.equals(WaitA)) {
                                    if (activeState.isDelayEnabled() && A) {
                                        activeState.leave();
                                        activeState = DoneA;
                                        DoneA.reset();
                                        DoneA.enter();
                                        continue;
                                    }
                                }
                                tickAgain = false;
                            }

                            activeState.setDelayEnabled(true);
                        }

                        @Override
                        public void reset() {
                            activeState = WaitA;
                            WaitA.reset();
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
                        public void tick() {
                            boolean tickAgain = true;
                            while (tickAgain) {

                                activeState.tick();

                                if (activeState.equals(WaitB)) {
                                    if (activeState.isDelayEnabled() && B) {
                                        activeState.leave();
                                        activeState = DoneB;
                                        DoneB.reset();
                                        DoneB.enter();
                                        continue;
                                    }
                                }

                                tickAgain = false;
                            }
                            activeState.setDelayEnabled(true);
                        }

                        @Override
                        public void enter() {
                            activeState.enter();
                        }

                        @Override
                        public void leave() {
                            activeState.leave();
                        }

                        @Override
                        public void reset() {
                            activeState = WaitB;
                            WaitB.reset();
                        }
                    }
                }
            }

        }

    }
}
