/*
 * Automatically generated Java code by
 * KIELER SCCharts - The Key to Efficient Modeling
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 */

import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ABRO {

  public Iface iface;
  private TickData rootContext;
  
  public static final String ORIGINAL_SCCHART = "/home/dam/kicoOO/samples/abro/abro-vars.sctx";
  

  /**
 * Enumeration for the possible thread states.
 * The chosen scheduling regime (IUR) uses four states to maintain the statuses of threads.
 */
  public enum ThreadStatus {
    TERMINATED("TERMINATED"), RUNNING("RUNNING"), READY("READY"), PAUSING("PAUSING");
    
    private String name;
    
    private ThreadStatus(String name) {
        this.name = name;
    }
  }

/**
 * The interface containing all model variables (inputs, outputs)
 */
public static class Iface {
  boolean A; // Input
  boolean B; // Input
  boolean R; // Input
  boolean O; // Output
}
			
  /**
 * Runtime data for the root level program
 */
  public static class TickData {
    ThreadStatus threadStatus;

ABRO_regionRootContext ABRO_regionRoot = new ABRO_regionRootContext();
    
    public Stream<String> getCurrentState() {
  return Stream.of(
      ABRO_regionRoot.getCurrentState()
    ).flatMap(i -> i);
    }
  }

	  /**
	   * Enumeration for all states of the Root region
	   */
	  public enum ABRO_regionRootStates {
	  ABO("State ABO (-1139924301)"), ABORUNNING("State ABO (-1139924301)"), __EA_INIT("State ABRO (1764331820)");

	private String origin;

	ABRO_regionRootStates(String origin) {
	  this.origin = origin;
	}
	
	public String getOrigin() {
	  return origin;
	}
	}

	/**
	 * The runtime thread data of region Root
	 */
	public static class ABRO_regionRootContext {
	  ThreadStatus threadStatus;
	  ABRO_regionRootStates activeState;
	  ABRO_regionRoot_stateABO_regionWaitContext ABRO_regionRoot_stateABO_regionWait = new ABRO_regionRoot_stateABO_regionWaitContext();
	  
	  public Stream<String> getCurrentState() {
	    switch (activeState) {
	    case ABO:
	    case ABORUNNING:
	      return Stream.of(
	          ABRO_regionRoot_stateABO_regionWait.getCurrentState()
	        ).flatMap(i -> i);
	    default:
	      return Stream.of(activeState.getOrigin());
	    }
	  }
	}

	  /**
	   * Enumeration for all states of the Wait region
	   */
	  public enum ABRO_regionRoot_stateABO_regionWaitStates {
	  WAITAB("State WaitAB (-28937571)"), WAITABRUNNING("State WaitAB (-28937571)"), DONE("State Done (-930190455)"), _AABORTED(""), _AC1(""), __EA_INIT1("State ABO (-1139924301)");

	private String origin;

	ABRO_regionRoot_stateABO_regionWaitStates(String origin) {
	  this.origin = origin;
	}
	
	public String getOrigin() {
	  return origin;
	}
	}

	/**
	 * The runtime thread data of region Wait
	 */
	public static class ABRO_regionRoot_stateABO_regionWaitContext {
	  ThreadStatus threadStatus;
	  ABRO_regionRoot_stateABO_regionWaitStates activeState;
	  boolean delayedEnabled;
	  ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAContext ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA = new ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAContext();
	  ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBContext ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB = new ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBContext();
	  
	  public Stream<String> getCurrentState() {
	    switch (activeState) {
	    case WAITAB:
	    case WAITABRUNNING:
	      return Stream.of(
	          ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA.getCurrentState(),
	          ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB.getCurrentState()
	        ).flatMap(i -> i);
	    default:
	      return Stream.of(activeState.getOrigin());
	    }
	  }
	}

	  /**
	   * Enumeration for all states of the HandleA region
	   */
	  public enum ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAStates {
	  WAITA("State WaitA (1803274743)"), DONEA1("State DoneA (1786149258)");

	private String origin;

	ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAStates(String origin) {
	  this.origin = origin;
	}
	
	public String getOrigin() {
	  return origin;
	}
	}

	/**
	 * The runtime thread data of region HandleA
	 */
	public static class ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAContext {
	  ThreadStatus threadStatus;
	  ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAStates activeState;
	  boolean delayedEnabled;
	  
	  public Stream<String> getCurrentState() {
	    switch (activeState) {
	    default:
	      return Stream.of(activeState.getOrigin());
	    }
	  }
	}

	  /**
	   * Enumeration for all states of the HandleB region
	   */
	  public enum ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBStates {
	  WAITB("State WaitB (1932357463)"), DONEB1("State DoneB (1915231978)");

	private String origin;

	ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBStates(String origin) {
	  this.origin = origin;
	}
	
	public String getOrigin() {
	  return origin;
	}
	}

	/**
	 * The runtime thread data of region HandleB
	 */
	public static class ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBContext {
	  ThreadStatus threadStatus;
	  ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBStates activeState;
	  boolean delayedEnabled;
	  
	  public Stream<String> getCurrentState() {
	    switch (activeState) {
	    default:
	      return Stream.of(activeState.getOrigin());
	    }
	  }
	}

/**
 * State ABRO (1764331820)
 */
private void ABRO_root(TickData context) {
if (context.ABRO_regionRoot.threadStatus != ThreadStatus.TERMINATED) {
  context.ABRO_regionRoot.threadStatus = ThreadStatus.RUNNING;
}
ABRO_regionRoot(context.ABRO_regionRoot);
  context.threadStatus = ThreadStatus.READY;
}
			
private void ABRO_regionRoot(ABRO_regionRootContext context) {
  while (context.threadStatus == ThreadStatus.RUNNING) {
    switch (context.activeState) {
			      	case ABO:
			      	  ABRO_regionRoot_stateABO(context);
      		// Superstate: intended fall-through 

		case ABORUNNING:
		  ABRO_regionRoot_stateABO_running(context);
break;
			
			      	case __EA_INIT:
			      	  ABRO_regionRoot_state__EA_Init(context);
break;
			
    }
  }
}
			
/**
 * State ABO (-1139924301)
 */
private void ABRO_regionRoot_stateABO(ABRO_regionRootContext context) {
context.ABRO_regionRoot_stateABO_regionWait.activeState = ABRO_regionRoot_stateABO_regionWaitStates.__EA_INIT1;
context.ABRO_regionRoot_stateABO_regionWait.delayedEnabled = false;
context.ABRO_regionRoot_stateABO_regionWait.threadStatus = ThreadStatus.READY;

  context.activeState = ABRO_regionRootStates.ABORUNNING;
}

/**
 * State ABO (-1139924301)
 */
private void ABRO_regionRoot_stateABO_running(ABRO_regionRootContext context) {
if (context.ABRO_regionRoot_stateABO_regionWait.threadStatus != ThreadStatus.TERMINATED) {
  context.ABRO_regionRoot_stateABO_regionWait.threadStatus = ThreadStatus.RUNNING;
}
ABRO_regionRoot_stateABO_regionWait(context.ABRO_regionRoot_stateABO_regionWait);
  if (context.ABRO_regionRoot_stateABO_regionWait.threadStatus == ThreadStatus.TERMINATED) {  // Transition ABO (Priority 1) -> ABO (1813591389)
    context.activeState = ABRO_regionRootStates.ABO;
  } else {
    context.ABRO_regionRoot_stateABO_regionWait.delayedEnabled = true;
    context.threadStatus = ThreadStatus.READY;
  }
}
			
private void ABRO_regionRoot_stateABO_regionWait(ABRO_regionRoot_stateABO_regionWaitContext context) {
  while (context.threadStatus == ThreadStatus.RUNNING) {
    switch (context.activeState) {
			      	case WAITAB:
			      	  ABRO_regionRoot_stateABO_regionWait_stateWaitAB(context);
      		// Superstate: intended fall-through 

		case WAITABRUNNING:
		  ABRO_regionRoot_stateABO_regionWait_stateWaitAB_running(context);
break;
			
			      	case DONE:
			      	  ABRO_regionRoot_stateABO_regionWait_stateDone(context);
break;
			
			      	case _AABORTED:
			      	  ABRO_regionRoot_stateABO_regionWait_state_Aaborted(context);
break;
			
			      	case _AC1:
			      	  ABRO_regionRoot_stateABO_regionWait_state_AC1(context);
break;
			
			      	case __EA_INIT1:
			      	  ABRO_regionRoot_stateABO_regionWait_state__EA_Init1(context);
break;
			
    }
  }
}
			
/**
 * State WaitAB (-28937571)
 */
private void ABRO_regionRoot_stateABO_regionWait_stateWaitAB(ABRO_regionRoot_stateABO_regionWaitContext context) {
context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA.activeState = ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAStates.WAITA;
context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA.delayedEnabled = false;
context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA.threadStatus = ThreadStatus.READY;
context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB.activeState = ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBStates.WAITB;
context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB.delayedEnabled = false;
context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB.threadStatus = ThreadStatus.READY;

  context.activeState = ABRO_regionRoot_stateABO_regionWaitStates.WAITABRUNNING;
}

/**
 * State WaitAB (-28937571)
 */
private void ABRO_regionRoot_stateABO_regionWait_stateWaitAB_running(ABRO_regionRoot_stateABO_regionWaitContext context) {
if (context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA.threadStatus != ThreadStatus.TERMINATED) {
  context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA.threadStatus = ThreadStatus.RUNNING;
}
if (context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB.threadStatus != ThreadStatus.TERMINATED) {
  context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB.threadStatus = ThreadStatus.RUNNING;
}
ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA(context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA);
ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB(context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB);
  if (context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA.threadStatus == ThreadStatus.TERMINATED && 
      context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB.threadStatus == ThreadStatus.TERMINATED) {  // Transition WaitAB (Priority 1) -> Done (373066629)
    context.delayedEnabled = false;
    context.activeState = ABRO_regionRoot_stateABO_regionWaitStates._AC1;
  } else {
    context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA.delayedEnabled = true;
    context.ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB.delayedEnabled = true;
    context.threadStatus = ThreadStatus.READY;
  }
}
			
private void ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA(ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAContext context) {
  while (context.threadStatus == ThreadStatus.RUNNING) {
    switch (context.activeState) {
			      	case WAITA:
			      	  ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA_stateWaitA(context);
break;
			
			      	case DONEA1:
			      	  ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA_stateDoneA1(context);
break;
			
    }
  }
}
			
/**
 * State WaitA (1803274743)
 */
private void ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA_stateWaitA(ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAContext context) {
  if (context.delayedEnabled && (iface.R)) {  // Transition WaitAB (Priority 1) -> Done (373066629)
    context.delayedEnabled = false;
    context.activeState = ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAStates.DONEA1;
  }
  else if (context.delayedEnabled && (iface.A)) { // Transition WaitA (Priority 1) -> DoneA (321268216)
    context.delayedEnabled = false;
    context.activeState = ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAStates.DONEA1;
  } else {
    context.threadStatus = ThreadStatus.READY;
  }
}
			
/**
 * State DoneA (1786149258)
 */
private void ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleA_stateDoneA1(ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleAContext context) {
  context.threadStatus = ThreadStatus.TERMINATED;
}
			
private void ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB(ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBContext context) {
  while (context.threadStatus == ThreadStatus.RUNNING) {
    switch (context.activeState) {
			      	case WAITB:
			      	  ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB_stateWaitB(context);
break;
			
			      	case DONEB1:
			      	  ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB_stateDoneB1(context);
break;
			
    }
  }
}
			
/**
 * State WaitB (1932357463)
 */
private void ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB_stateWaitB(ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBContext context) {
  if (context.delayedEnabled && (iface.R)) {  // Transition WaitAB (Priority 1) -> Done (373066629)
    context.delayedEnabled = false;
    context.activeState = ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBStates.DONEB1;
  }
  else if (context.delayedEnabled && (iface.B)) { // Transition WaitB (Priority 1) -> DoneB (-1822573384)
    context.delayedEnabled = false;
    context.activeState = ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBStates.DONEB1;
  } else {
    context.threadStatus = ThreadStatus.READY;
  }
}
			
/**
 * State DoneB (1915231978)
 */
private void ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleB_stateDoneB1(ABRO_regionRoot_stateABO_regionWait_stateWaitAB_regionHandleBContext context) {
  context.threadStatus = ThreadStatus.TERMINATED;
}
			
/**
 * State Done (-930190455)
 */
private void ABRO_regionRoot_stateABO_regionWait_stateDone(ABRO_regionRoot_stateABO_regionWaitContext context) {
  if (context.delayedEnabled && (iface.R)) {  // Transition ABO (Priority 1) -> ABO (1813591389)
    context.delayedEnabled = false;
    context.activeState = ABRO_regionRoot_stateABO_regionWaitStates._AABORTED;
  } else {
    context.threadStatus = ThreadStatus.READY;
  }
}
			
private void ABRO_regionRoot_stateABO_regionWait_state_Aaborted(ABRO_regionRoot_stateABO_regionWaitContext context) {
  context.threadStatus = ThreadStatus.TERMINATED;
}
			
private void ABRO_regionRoot_stateABO_regionWait_state_AC1(ABRO_regionRoot_stateABO_regionWaitContext context) {
  if (iface.R) {  // Transition ABO (Priority 1) -> ABO (1813591389)
    context.delayedEnabled = false;
    context.activeState = ABRO_regionRoot_stateABO_regionWaitStates._AABORTED;
  }
  else { // Transition WaitAB (Priority 1) -> Done (373066629)
    iface.O = true;
    context.delayedEnabled = false;
    context.activeState = ABRO_regionRoot_stateABO_regionWaitStates.DONE;
  }
}
			
/**
 * State ABO (-1139924301)
 */
private void ABRO_regionRoot_stateABO_regionWait_state__EA_Init1(ABRO_regionRoot_stateABO_regionWaitContext context) {
  iface.O = false;
  context.delayedEnabled = false;
  context.activeState = ABRO_regionRoot_stateABO_regionWaitStates.WAITAB;
}
			
/**
 * State ABRO (1764331820)
 */
private void ABRO_regionRoot_state__EA_Init(ABRO_regionRootContext context) {
  iface.O = false;
  context.activeState = ABRO_regionRootStates.ABO;
}
			

  public void init() {
  reset();
  tick();
  }

  public void reset() {
  rootContext.ABRO_regionRoot.activeState = ABRO_regionRootStates.__EA_INIT;
  rootContext.ABRO_regionRoot.threadStatus = ThreadStatus.READY;

    rootContext.threadStatus = ThreadStatus.READY;
  }

  public void tick() {
  if (rootContext.threadStatus == ThreadStatus.TERMINATED) return;

    ABRO_root(rootContext);
  }
  
  public String getCurrentState() {
  return rootContext.getCurrentState().distinct().collect(Collectors.joining(","));
  }

  public ABRO() {
  this.iface = new Iface();
  this.rootContext = new TickData();
  }

}
