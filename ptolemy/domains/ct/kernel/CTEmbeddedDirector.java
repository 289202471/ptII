/* An embedded director for CT inside CT/FSM.

Copyright (c) 1999-2004 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY
*/

package ptolemy.domains.ct.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.domains.ct.kernel.util.TotallyOrderedSet;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CTEmbeddedDirector
/**
   An embedded director for CT inside CT/FSM. Conceptually, this director
   interacts with a continuous outer domain. As a consequence, this
   director exposes its step size control information. If the container
   of this director is a CTCompositeActor, then this information is
   further exposed to the outer domain.
   <P>
   Unlike the CTMixedSignalDirector, this director does not run ahead
   of the global time and rollback, simply because the step size control
   information is accessible from outer domain which has a continuous
   time and understands the meaning of step size.

   @author  Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (liuj)
   @Pt.AcceptedRating Yellow (chf)
   @see CTMultiSolverDirector
   @see CTTransparentDirector
*/
public class CTEmbeddedDirector extends CTMultiSolverDirector
    implements CTTransparentDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     */
    public CTEmbeddedDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     */
    public CTEmbeddedDirector(Workspace workspace)  {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param container The container.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CTEmbeddedDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        //addDebugListener(new StreamListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Always return true indicating that this director can be
     *  an inside director.
     *  @return True always.
     */
    public boolean canBeInsideDirector() {
        return true;
    }

    /** Always return false indicating that this director cannot
     *  be a top level director.
     *  @return False always.
     */
    public boolean canBeTopLevelDirector() {
        return false;
    }

    /** Execute the subsystem for one iteration. An iteration includes
     *  an event phase, a state resolving phase, and a output phase.
     *  If the state cannot be accurately resolved, then subsystem
     *  may produce a meaningless output. It is the outer domain's
     *  responsibility to check if this subsystem is accurate in
     *  this iteration, by calling the isThisStepAccurate() method
     *  of the CTCompositeActor that contains this director.
     *  @exception IllegalActionException If one of the actors throw
     *  it during one iteration.
     */
    public void fire() throws IllegalActionException {
        // FIXME: A rough design.
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
        if (isDiscretePhase()) {
            setSuggestedNextStepSize(getMinStepSize());
            if (isPureDiscretePhase()) {
                super._iterateDiscreteActors(schedule);
            }
            if (isWaveformGeneratingPhase()) {
                // NOTE: the time a discrete phase execution (waveform phase) 
                // starts is the the time the iteration time starts.
                // NOTE: A ct composite actor is also a waveform generator.
                CompositeActor container = (CompositeActor)getContainer();
                Director exe = container.getExecutiveDirector();
                Time time = exe.getCurrentTime();
                setCurrentTime(exe.getCurrentTime());
                _setIterationBeginTime(exe.getCurrentTime());
                
                super._iterateWaveformGenerators(schedule);
            }
            if (isCreatingIterationStartingStatesPhase()) 
                super._createIterationStartingStates();
            if (isEventGeneratingPhase()) {
                super._iterateEventGenerators(schedule);
            }
        } else {
            // set the current time to the iteration begin time because the upper
            // level hierarchy may request refiring with a smaller step size.
            
            // FIXME: this director may not even has its own time.
            // setCurrentTime(getIterationBeginTime());
            
            // The following statement is decomposed into a set of actions with
            // conditions.
            // super._continuousPhaseExecution();
            
            // Change ODE solver
            _setCurrentODESolver(getODESolver());

            if (isPrefiringDynamicActorsPhase()) {
                super._prefireDynamicActors();
            }
            if (isSolvingStatesPhase()) {
                // instead of resolve states, fire one round.
                if (isFiringDynamicActorsPhase()) {
                    getODESolver().fireDynamicActors();
                }
                if (isFiringStateTransitionActorsPhase()) {
                    getODESolver().fireStateTransitionActors();
                    super.produceOutput();
                }
            }
            // No seperate phase for producing output, because
            // a CT subsystem need to produce output if it works
            // as one of the state transition actors. 
//            if (isProducingOutputsPhase()) {
//                super.produceOutput();
//            }
            if (isUpdatingContinuousStatesPhase()) {
                super.updateContinuousStates();
            }
            if (isFiringEventGeneratorsPhase()) {
                super.fireEventGenerators();
            }
        }
        
         
    }


    /** Return the current integration step size. This method is final
     *  for performance reason.
     *  @return The current step size.
     */
    public double getCurrentStepSize() {
        CompositeActor container = (CompositeActor)getContainer();
        CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
        return exe.getCurrentStepSize();  
    }
    
      /** Return true if this is the discrete phase execution.
       *  @return True if this is the discrete phase execution.
       */
      public boolean isDiscretePhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isDiscretePhase();
      }

      /** Return true if this is the discrete phase execution.
       *  @return True if this is the discrete phase execution.
       */
      public boolean isPureDiscretePhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isPureDiscretePhase();
      }

      /** Return true if this is the discrete phase execution.
       *  @return True if this is the discrete phase execution.
       */
      public boolean isWaveformGeneratingPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isWaveformGeneratingPhase();
      }

      /** Return true if this is the discrete phase execution.
       *  @return True if this is the discrete phase execution.
       */
      public boolean isEventGeneratingPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isEventGeneratingPhase();
      }

      public boolean isCreatingIterationStartingStatesPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isCreatingIterationStartingStatesPhase();
      }

      /* (non-Javadoc)
       * @see ptolemy.domains.ct.kernel.CTGeneralDirector#isSolvingStatesPhase()
       */
      public boolean isSolvingStatesPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isSolvingStatesPhase();
      }

      /* (non-Javadoc)
       * @see ptolemy.domains.ct.kernel.CTGeneralDirector#isProducingOutputsPhase()
       */
      public boolean isProducingOutputsPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isProducingOutputsPhase();
      }

      /* (non-Javadoc)
       * @see ptolemy.domains.ct.kernel.CTGeneralDirector#isUpdatingContinuousStatesPhase()
       */
      public boolean isUpdatingContinuousStatesPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isUpdatingContinuousStatesPhase();
      }

      /* (non-Javadoc)
       * @see ptolemy.domains.ct.kernel.CTGeneralDirector#isPrefiringDynamicActorsPhase()
       */
      public boolean isPrefiringDynamicActorsPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isPrefiringDynamicActorsPhase();
      }

      /* (non-Javadoc)
       * @see ptolemy.domains.ct.kernel.CTGeneralDirector#isFiringEventGeneratorsPhase()
       */
      public boolean isFiringEventGeneratorsPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isFiringEventGeneratorsPhase();
      }

      public boolean isFiringDynamicActorsPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isFiringDynamicActorsPhase();
      }

      /* (non-Javadoc)
       * @see ptolemy.domains.ct.kernel.CTGeneralDirector#isFiringStateTransitionActorsPhase()
       */
      public boolean isFiringStateTransitionActorsPhase() {
          CompositeActor container = (CompositeActor)getContainer();
          CTGeneralDirector exe = (CTGeneralDirector) container.getExecutiveDirector();
          return exe.isFiringStateTransitionActorsPhase();
      }

    /** Return true if the current integration step
     *  is accurate. This is determined by asking all the
     *  step size control actors in the state transition schedule and
     *  output schedule.
     *  @return True if the current step is accurate.
     */
    public boolean isThisStepAccurate() {
        _debug(getName() + ": Checking local actors for success.");
        if (!_isStateAccurate()) {
            //if (_debugging) _debug(getFullName() +
            //        " current step not successful because of STATE.");
            _stateAcceptable = false;
            return false;
        } else if (!_isOutputAccurate()) {
            //if (_debugging) _debug(getFullName() +
            //        " current step not successful because of OUTPUT.");
            _stateAcceptable = true;
            _outputAcceptable = false;
            return false;
        } else {
            _stateAcceptable = true;
            _outputAcceptable = true;
            return true;
        }
    }

    /** Update the states of actors directed by this director.
     *  Discrete events at current time will be consumed and produced.
     *  @return True if this is not a top-level director, or the simulation
     *     is not finished and stop() has not been called.
     *  @exception IllegalActionException Not thrown in this base class.
     */
//    public boolean postfire() throws IllegalActionException {
//        if (_debugging) _debug(getFullName(), " postfire.");
//        // FIXME: this postfire method produces outputs
//        //_discretePhaseExecution();
//        updateContinuousStates();
//        // The current time will be the begin time of the next iteration.
//        _setIterationBeginTime(getCurrentTime());
//        return !_stopRequested;
//    }

    /** Return the predicted next step size, which is the minimum
     *  of the prediction from step size control actors.
     *  @return The predicted step size from this subsystem.
     */
    public double predictedStepSize() {
        try {
            if (_debugging) _debug(getName(), "at " + getCurrentTime(),
                    " predict next step size" + _predictNextStepSize());
            return _predictNextStepSize();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException (
                    " Fail predict the next step size." + ex.getMessage());
        }
    }

    /** Return true always. Recompute the schedules if there
     *  was a mutation. Synchronize time with the outer domain,
     *  and adjust the contents of the breakpoint table with
     *  respect to the current time.
     *  @return True always.
     */
    public boolean prefire() throws IllegalActionException {
        // FIXME: the following code can be simplified into
        // getScheduler().getSchedule();
        // or 
        // return true, 
        // because the initialize
        // method is responsible to get a valid schedule.
        // FIXME: will this be affected by mobile models?
        // I guess not, if the mobile model requests an initialization
        // whenever a model change happens.
        
        getScheduler().getSchedule();
        
        // FIXME: the following method may be masked by HSDirector.
        super._prefireDynamicActors();

        return super.prefire();
    }

    /** Check whether the container implements the CTStepSizeControlActor
     *  interface. If not, then throw an exception.
     *  @exception IllegalActionException If the container of this
     *  director does not implement CTStepSizeControlActor, or one of
     *  the actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getContainer() instanceof CTStepSizeControlActor)) {
            throw new IllegalActionException(this, "can only be contained by "
                    + "a composite actor that implements "
                    + "the CTStepSizeControlActor "
                    + "interface, for example, the continuous "
                    + "time composite actor or the modal model.");
        }
        super.preinitialize();
    }

    /** Return the refined step size if the current fire is not accurate.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        try {
            if (!_stateAcceptable) {
                return _refinedStepWRTState();
            } else if (!_outputAcceptable) {
                return _refinedStepWRTOutput();
            } else {
                return Double.MAX_VALUE;
            }
        } catch ( IllegalActionException ex) {
            throw new InternalErrorException (
                    "Fail to refine step size. " + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Step size of the outer domain.
    private double _outsideStepSize;

    // Indicates whether actors in the output schedule are satisfied.
    private boolean _outputAcceptable;

    // Indicates whether actors in the dynamic actor schedule and the
    // state transition schedule are satisfied.
    private boolean _stateAcceptable;

    // The current time of the outer domain.
    private Time _outsideTime;

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#markState()
     */
    public void markState() {
        try {
            Iterator statefulActors = getScheduler().getSchedule().get(
                    CTSchedule.STATEFUL_ACTORS).actorIterator();
            while (statefulActors.hasNext() && !_stopRequested) {
                CTStatefulActor statefulActor = 
                    (CTStatefulActor) statefulActors.next();
                statefulActor.markState();
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException (e);
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#emitTentativeOutputs()
     */
    public void emitTentativeOutputs() {
        try {
            Iterator dynamicActors = getScheduler().getSchedule().get(
                    CTSchedule.DYNAMIC_ACTORS).actorIterator();
            while (dynamicActors.hasNext() && !_stopRequested) {
                CTDynamicActor dynamicActor = 
                    (CTDynamicActor) dynamicActors.next();
                dynamicActor.emitTentativeOutputs();
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException (e);
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#goToMarkedState()
     */
    public void goToMarkedState() {
        try {
            Iterator statefulActors = getScheduler().getSchedule().get(
                    CTSchedule.STATEFUL_ACTORS).actorIterator();
            while (statefulActors.hasNext() && !_stopRequested) {
                CTStatefulActor statefulActor = 
                    (CTStatefulActor) statefulActors.next();
                statefulActor.goToMarkedState();
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException (e);
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#isStateAccurate()
     */
    public boolean isStateAccurate() {
        return _isStateAccurate();
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#isOutputAccurate()
     */
    public boolean isOutputAccurate() {
        return _isOutputAccurate();
    }
}

