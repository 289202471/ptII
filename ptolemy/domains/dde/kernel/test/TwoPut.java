/* TwoPut is a test class with two output ports used to test token
production AND consumption.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel.test;

import ptolemy.domains.dde.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;


//////////////////////////////////////////////////////////////////////////
//// TwoPut
/**
TwoPut is a test class with two output ports used to test token
production AND consumption. It has a single typed, input multiport
and two output multiports. The fire() method of this class simply
passes through "real" tokens on the _first_ output port ("output1").
Use this class to test feedback loops.


@author John S. Davis II
@version $Id$

*/

public class TwoPut extends TypedAtomicActor {

    /**
     */
    public TwoPut(TypedCompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
         super(cont, name);

         out1 = new TypedIOPort(this, "output1", false, true);
	 out1.setMultiport(true);
	 out1.setTypeEquals(Token.class);
         out2 = new TypedIOPort(this, "output2", false, true);
	 out2.setMultiport(true);
	 out2.setTypeEquals(Token.class);
         input = new TypedIOPort(this, "input", true, false);
	 input.setMultiport(true);
	 input.setTypeEquals(Token.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void fire() throws IllegalActionException {
	Token token = null;
	Receiver[][] inRcvrs = input.getReceivers();
	if( inRcvrs.length == 0 ) {
	    _continueIterations = false;
	}
	for( int i = 0; i < inRcvrs.length; i++ ) {
	    for( int j = 0; j < inRcvrs[i].length; j++ ) {
		DDEReceiver inRcvr = (DDEReceiver)inRcvrs[i][j];
		System.out.println("\n");
		System.out.println("\n");
		System.out.println("#####");
		System.out.println("TwoPut receiver["+i+"]["+j+"];");
		if( inRcvr.hasToken() ) {
		    token = inRcvr.get();
		    out1.broadcast(token);
		    /*
		    Receiver[][] outRcvrs = output.getRemoteReceivers();
		    for( int k = 0; k < outRcvrs.length; k++ ) {
			for( int l = 0; l < outRcvrs[k].length; l++ ) {
			    DDEReceiver outRcvr = (DDEReceiver)outRcvrs[k][l];
			    Thread thr = Thread.currentThread();
			    if( thr instanceof DDEThread ) {
				TimeKeeper kpr = ((DDEThread)thr).getTimeKeeper();
			        outRcvr.put(token, kpr.getCurrentTime());
			    }
			}
		    }
		    */
		}
	    }
	}
    }

    /**
     */
    public boolean postfire() throws IllegalActionException {
	return _continueIterations;
    }

    /**
     */
    public void setOutChan(int ch) throws IllegalActionException {
	_outChannel = ch;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    public TypedIOPort out1;
    public TypedIOPort out2;
    public TypedIOPort input;
    private int _outChannel = -1;
    private boolean _continueIterations = true;

}
