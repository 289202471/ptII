/* Ptides port.

@Copyright (c) 2008-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.lib;

import java.util.HashMap;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.lib.hoc.MirrorPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonConfigurableAttribute; 

/** A specialized port for Ptides platform I/O implementing 
 *  functionality for sensors, actuators and network ports.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class PtidesPort extends MirrorPort {
    
    /** Create a new PtidesPort with a given container and a name.
     * @param container The container of the port. 
     * @param name The name of the port.
     * @throws IllegalActionException If parameters cannot be set.
     * @throws NameDuplicationException If name already exists.
     */
    public PtidesPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        deviceDelay = new Parameter(this, "deviceDelay");
        deviceDelay.setToken(new DoubleToken(0.0));
        deviceDelay.setTypeEquals(BaseType.DOUBLE);
        
        deviceDelayBound = new Parameter(this, "deviceDelayBound");
        deviceDelayBound.setExpression("0.0"); 
        deviceDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        isNetworkPort = new Parameter(this, "isNetworkPort");
        isNetworkPort.setTypeEquals(BaseType.BOOLEAN);
        isNetworkPort.setExpression("false");
        
        actuateAtEventTimestamp = new Parameter(this, "actuateAtEventTimestamp"); 
        actuateAtEventTimestamp.setTypeEquals(BaseType.BOOLEAN);
        actuateAtEventTimestamp.setExpression("true");
        _actuateAtEventTimestamp = true;
        
        platformDelayBound = new Parameter(this, "platformDelayBound");
        platformDelayBound.setExpression("0.0");
        platformDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        sourcePlatformDelayBound = new Parameter(this, "sourcePlatformDelayBound");
        sourcePlatformDelayBound.setExpression("0.0");
        sourcePlatformDelayBound.setTypeEquals(BaseType.DOUBLE); 
        
        networkDelayBound = new Parameter(this, "networkDelayBound");
        networkDelayBound.setExpression("0.0");
        networkDelayBound.setTypeEquals(BaseType.DOUBLE);  
        
        _iconDescription = new SingletonConfigurableAttribute(this, "_iconDescription");
        _iconDescription.setPersistent(false); 
        _setIconAndParameterVisibility();        
    }
    
    /** Actuate at event timestamp parameter that defaults to the boolean value TRUE. 
     *  If this parameter is set to FALSE, an actuator can produce outputs as soon 
     *  as they are available.
     */
    public Parameter actuateAtEventTimestamp; 
    
    /** Device delay parameter that defaults to the double value 0.0. */
    public Parameter deviceDelay;
    
    
    
    /** Device delay bound parameter that defaults to the double value 0.0. */
    public Parameter deviceDelayBound;
    
    public Parameter isNetworkPort;
    
    /** Network delay bound parameter that defaults to the double value 0.0. */
    public Parameter networkDelayBound; 
    
    /** Platform delay bound parameter that defaults to the double value 0.0. */
    public Parameter platformDelayBound;

    /** Source platform delay bound parameter that defaults to the double value 0.0. */
    public Parameter sourcePlatformDelayBound;
    
    public boolean actuateAtEventTimestamp() {
        return _actuateAtEventTimestamp;
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException { 
        if (attribute == isNetworkPort) {
            _isNetworkPort = ((BooleanToken)isNetworkPort.getToken()).booleanValue();
            _setIconAndParameterVisibility();
        } if (attribute == actuateAtEventTimestamp) {
            _actuateAtEventTimestamp = ((BooleanToken) actuateAtEventTimestamp.getToken())
            .booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Return the timestamp for a specific token.
     *  @param t The token.
     *  @return The timestamp.
     */ 
    public Time getTimeStampForToken(Token t) {
        Time time = _transmittedTokens.get(t);
        _transmittedTokenCnt.put(t, _transmittedTokenCnt.get(t).intValue() - 1);
        if (_transmittedTokenCnt.get(t).intValue() == 0) {
            _transmittedTokens.remove(t);
            _transmittedTokenCnt.remove(t);
        }
        return time;
    }

    public boolean isActuatorPort() {
        return isOutput() && !_isNetworkPort;
    }

    public boolean isSensorPort() {
        return isInput() && !_isNetworkPort;
    }
    
    public boolean isNetworkReceiverPort() {
        return isInput() && _isNetworkPort;
    }
    
    public boolean isNetworkTransmitterPort() {
        return isOutput() && _isNetworkPort;
    }
    
    @Override
    public void setInput(boolean isInput) throws IllegalActionException { 
        super.setInput(isInput);
        
        _setIconAndParameterVisibility();
    }

    @Override
    public void setOutput(boolean isInput) throws IllegalActionException { 
        super.setOutput(isInput);
        
        _setIconAndParameterVisibility();
    
    }

    /** Save token and remember timestamp of the token. Then call send of
     *  super class.
     *  @param channelIndex The index of the channel, from 0 to width-1.
     *  @param token The token to send, or null to send no token.
     *  @exception IllegalActionException If the token to be sent cannot
     *   be converted to the type of this port, or if the token is null.
     *  @exception NoRoomException If there is no room in the receiver.
     */
    @Override
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        Time timestamp = ((CompositeActor)getContainer()).getDirector().getModelTime();
        if (_transmittedTokens == null) {
            _transmittedTokens = new HashMap();
            _transmittedTokenCnt = new HashMap();
        }
        if (_transmittedTokens.get(token) == null) {
            _transmittedTokenCnt.put(token, 0);
        }
        _transmittedTokens.put(token, timestamp);
        _transmittedTokenCnt.put(token, _transmittedTokenCnt.get(token).intValue() + 1);
        super.send(channelIndex, token);
    } 
    
    /** Change visibility of parameters depending on the type of
     *  port. FIXME: change icon! 
     *  @throws IllegalActionException Thrown if icon cannot be changed.
     */
    private void _setIconAndParameterVisibility() throws IllegalActionException {
        try {
            if (isSensorPort()) {
                actuateAtEventTimestamp.setVisibility(Settable.NONE);
                networkDelayBound.setVisibility(Settable.NONE);
                platformDelayBound.setVisibility(Settable.NONE);
                sourcePlatformDelayBound.setVisibility(Settable.NONE);
                _iconDescription.configure(null, null, "<svg>\n"
                        + "<polygon points=\"0, 0, 0, 16, 16, 16, 16, 12, 20, 8, 16, 4, 16, 0\" "
                        + "style=\"fill:black\"/>\n" + "</svg>\n");
            } else if (isActuatorPort()) {
                actuateAtEventTimestamp.setVisibility(Settable.FULL);
                networkDelayBound.setVisibility(Settable.NONE);
                platformDelayBound.setVisibility(Settable.NONE);
                sourcePlatformDelayBound.setVisibility(Settable.NONE);
                _iconDescription.configure(null, null, "<svg>\n"
                        + "<polygon points=\"0, 0, 0, 4, -4, 8, 0, 12, 0, 16, 16, 16, 16, 0\" "
                        + "style=\"fill:black\"/>\n" + "</svg>\n");
            } else if (isNetworkReceiverPort()) {
                actuateAtEventTimestamp.setVisibility(Settable.NONE);
                networkDelayBound.setVisibility(Settable.FULL);
                platformDelayBound.setVisibility(Settable.NONE);
                sourcePlatformDelayBound.setVisibility(Settable.FULL);
                _iconDescription.configure(null, null, "<svg>\n"
                        + "<polygon points=\"0, 0, 0, 16, 16, 16, 16, 12, 20, 12, 20, 4, 16, 4, 16, 0\" "
                        + "style=\"fill:black\"/>\n" + "</svg>\n");
            } else if (isNetworkTransmitterPort()) {
                actuateAtEventTimestamp.setVisibility(Settable.NONE);
                networkDelayBound.setVisibility(Settable.NONE);
                platformDelayBound.setVisibility(Settable.FULL);
                sourcePlatformDelayBound.setVisibility(Settable.NONE);
                _iconDescription.configure(null, null, "<svg>\n"
                        + "<polygon points=\"0, 0, 0, 4, -4, 4, -4, 12, 0, 12, 0, 16, 16, 16, 16, 0\" "
                        + "style=\"fill:black\"/>\n" + "</svg>\n");
            } 
        } catch (Exception e) { 
            throw new IllegalActionException(this, e.getMessage());
        } 
    }

    private boolean _actuateAtEventTimestamp;

    private SingletonConfigurableAttribute _iconDescription;

    private boolean _isNetworkPort;

    private HashMap<Token, Time> _transmittedTokens;
    private HashMap<Token, Integer> _transmittedTokenCnt;
    
}
