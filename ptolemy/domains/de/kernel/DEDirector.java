/* An abstract base class for DE domain director.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating red (lmuliadi@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.graph.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEDirector
//
/** Abstract base class for DE domain director. 
 *
 *  @author Lukito Muliadi
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 */
// FIXME:
// The topological depth of the receivers are static and computed once
// in the initialization() method. This means that mutations are not
// currently supported.
public abstract class DEDirector extends Director {

    /** Construct a director with empty string as name in the
     *  default workspace.
     */
    public DEDirector() {
	super();
    }
  
    /** Construct a director with the specified name in the default
     *  workspace. If the name argument is null, then the name is set to the
     *  empty string. This director is added to the directory of the workspace,
     *  and the version of the workspace is incremented.
     *  @param name The name of this director.
     */
    public DEDirector(String name) {
	super(name);
    }
  
    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     *  @param name The name of this director.
     */
    public DEDirector(Workspace workspace, String name) {
	super(workspace, name);
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Put a "pure event" into the event queue with the specified delay and
     *  depth. The time stamp of the event is the current time plus the
     *  delay.  The depth is used to prioritize events that have equal
     *  time stamps.  A smaller depth corresponds to a higher priority.
     *  A "pure event" is one where no token is transfered.  The event
     *  is associated with a destination actor.  That actor will be fired
     *  when the time stamp of the event is the oldest in the system.
     *  Note that the actor may have no new data at its input ports
     *  when it is fired.
     *
     *  @param actor The destination actor.
     *  @param delay The delay, relative to current time.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    public abstract void enqueueEvent(Actor actor, double delay, long depth)
	 throws IllegalActionException;
       
    /** Put a token into the event queue with the specified destination
     *  receiver, delay and depth. The time stamp of the token is the
     *  current time plus the delay.  The depth is used to prioritize
     *  events that have equal time stamps.  A smaller depth corresponds
     *  to a higher priority.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param delay The delay, relative to current time.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    public abstract void enqueueEvent(DEReceiver receiver, 
	    Token token,
	    double delay, 
	    long depth) 
	 throws IllegalActionException;

    /** Fire the one actor identified by the prefire() method as ready to fire.
     *  If there are multiple simultaneous events destined to this actor,
     *  then they have all been dequeued from the global queue and put into
     *  the corresponding receivers.
     *  <p>
     *  NOTE: Currently, this means that there may be multiple simultaneous
     *  events in a given receiver.  Since many actors may be written in
     *  such a way that they do not expect this, this may change in the future.
     *  I.e., these events may be made visible over multiple firings rather
     *  than all at once.
     *
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void fire() throws IllegalActionException {
      super.fire();
    }

    /** Return the current time of the simulation. Firing actors that need to
     *  know the current time (e.g. for calculating the time stamp of the
     *  delayed outputs) call this method.
     */
    public double getCurrentTime() {
	return _currentTime;
    }
    
    /** Return the time of the earliest event seen in the simulation.
     *  Before the simulation begins, this is java.lang.Double.MAX_VALUE.
     *  @return The start time of the simulation.
     */
    public double getStartTime() {
        return _startTime;
    }
    
    /** Return the stop time of the simulation, as set by setStopTime().
     *  @return The stop time of the simulation.
     */
    public double getStopTime() {
	return _stopTime;
    }

    /** Return true if this director is embedded inside an opaque composite
     *  actor contained by another composite actor.
     */
    public boolean isEmbedded() {
        if (getContainer().getContainer() == null) {
            return false;
        } else {
            return true;
        }
        
    }


    /** Return a new receiver of a type DEReceiver.
     *  @return A new DEReceiver.
     */
    public Receiver newReceiver() {
	return new DEReceiver();
    }
    
    /** FIXME: Describe me!
     */
    public boolean postfire() throws IllegalActionException {
        if (_shouldPostfireReturnFalse) {
            return false;            
        } else {
            return true;
        }

    }

    /** Set the stop time of the simulation.
     *  @param stopTime The new stop time.
     */
    public void setStopTime(double stopTime) {
	this._stopTime = stopTime;
    }



    /** Transfer data from an input port of the container to the
     *  ports it is connected to on the inside.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    public void transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque input port.");
        }
        Receiver[][] insiderecs = port.deepGetReceivers();
        for (int i=0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                try {
                    Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        for (int j=0; j < insiderecs[i].length; j++) {
                            DEReceiver deRec = 
                                (DEReceiver)insiderecs[i][j];

                            Actor container = (Actor)getContainer();
                            double outsideCurrTime = container.getExecutiveDirector().getCurrentTime();

                            deRec.put(t, outsideCurrTime - _currentTime);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "Director.transferInputs: Internal error: " +
                            ex.getMessage());
                }
            }
        }
    }

    /** Transfer data from an output port of the container to the
     *  ports it is connected to on the outside.  The port argument must
     *  be an opaque output port.  If any channel of the output port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     */
    public void transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not an opaque output port.");
        }
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i=0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
                    for (int j=0; j < insiderecs[i].length; j++) {
                        if (insiderecs[i][j].hasToken()) {
                            try {
                                DEReceiver deRec
                                    = (DEReceiver)insiderecs[i][j];
                                Token t = deRec.get();
                                double d = deRec._getTokenDelay();
                                // FIXME: I don't think it's necessary
                                // that the following cast will always valid.
                                ((DEIOPort)port).send(i,t,d);
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: " +
                                        "Internal error: " +
                                        ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }




    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Override the default Director implementation, because in DE
     *  domain, we don't need write access inside an iteration.
     *  @return false.
     */ 
    protected boolean _writeAccessPreference() { 
        // Return false to let the workspace be write-protected.
        // Return true to debug the PtolemyThread.
        return false;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    
    // Defines the stopping condition
    protected double _stopTime = 0.0;
    
    // The time of the earliest event seen in the current simulation.
    protected double _startTime = Double.MAX_VALUE;
    
    // The current time of the simulation.
    // Firing actors may get the current time by calling getCurrentTime()
    protected double _currentTime = 0.0;
    
    // Set to true when it's time to end the simulation.
    // e.g. The earliest time in the global event queue is greater than
    // the stop time.
    // FIXME: This is a hack :(
    protected boolean _shouldPostfireReturnFalse = false;

}
