/* Check the input streams against a parameter value and outputs
 a boolean true if result is correct.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
review output port.
*/

package ptolemy.actor.lib;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Test
/**

This actor compares the inputs against the value specified by the
<i>correctValues</i> parameter.  That parameter is an ArrayToken,
where each element of the array should have the same type as the
input.  The length of this array is the number of iterations of this
actor that are tested.  Subsequent iterations always succeed, so the
actor can be used as a "power-up" test for a model, checking the first
few iterations against some known results.
<p>
The input is a multiport.  If there is more than one channel connected
to it, then each element of <i>correctValues</i> must itself be an
ArrayToken, with length matching the number of channels.
Suppose for example that the width of the input is one,
and the first three inputs should be 1, 2, and 3.  Then you can
set <i>correctValues</i> to
<pre>
    {1, 2, 3}
</pre>
Suppose instead that the input has width two, and the correct values
in the first iteration are 1 on the first channel and 2 on the second.
Then on the second iteration, the correct values are 3 on the first
channel and 4 on the second.  Then you can set <i>correctValues</i> to
<pre>
    {{1, 2}, {3, 4}}
</pre>
With this setting, no tests are performed after the first two iterations
of this actor.
<p>
The input values are checked in the postfire() method, which checks to
make sure that each input channel has a token.  If an input value is
missing or differs from what it should be, then postfire() throws an
exception. Thus, the test passes if no exception is thrown.
<p>
If the input is a DoubleToken or ComplexToken,
then the comparison passes if the value is close to what it should
be, within the specified <i>tolerance</i> (which defaults to
10<sup>-9</sup>.  The input data type is undeclared, so it can
resolve to anything.
<p>

During ever iteration in which this actor checks an input value
against a value from <i>correctValues</i>, the actor outputs a boolean
with value false.  After reacing the end of the <i>correctValues</i>,
this actor outputs true from its output port on every firing,
regardless of the input data, indicating that the test has passed.
This is useful for implementing tests that have data-dependent
stopping conditions.

@see NonStrictTest
@author Edward A. Lee, Christopher Hylands, Jim Armsrong
@version $Id$
@since Ptolemy II 1.0
*/

public class Test extends Sink {

    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Test(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.BOOLEAN);

        Token[] defaultEntries = new Token[1];
        defaultEntries[0] = new BooleanToken(true);
        ArrayToken defaultArray = new ArrayToken(defaultEntries);
        correctValues = new Parameter(this, "correctValues", defaultArray);
        correctValues.setTypeEquals(new ArrayType(BaseType.UNKNOWN));

        tolerance = new Parameter(this, "tolerance", new DoubleToken(1e-9));
        tolerance.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A matrix specifying what the input should be.
     *  This defaults to a one-by-one array containing a boolean true.
     */
    public Parameter correctValues;

    /** A double specifying how close the input has to be to the value
     *  given by <i>correctValues</i>.  This is a DoubleToken, with default
     *  value 10<sup>-9</sup>.
     */
    public Parameter tolerance;

    /** An output port, which outputs true when the actor has reached
     * the end of the <i>correctValues</i>, indicating that the test
     * has passed.  Otherwise the port outputs false.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>tolerance</i>, then check
     *  that it is increasing and nonnegative.
     *  @exception IllegalActionException If the indexes vector is not
     *  increasing and nonnegative, or the indexes is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == tolerance) {
            _tolerance = ((DoubleToken)(tolerance.getToken())).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to set the iteration counter to zero.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _numberOfInputTokensSeen = 0;
        output.send(0, new BooleanToken(false));
    }

    /** Read one token from each input channel and compare against
     *  the value specified in <i>correctValues</i>.  If the iteration count
     *  is larger than the length of <i>correctValues</i>, then return
     *  immediately, indicating that the inputs correctly matched
     *  the values in <i>correctValues</i> and that the test succeeded.
     *
     *  @exception IllegalActionException If an input is missing,
     *   or if its value does not match the required value.
     */
    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();
        if (_numberOfInputTokensSeen
                >= ((ArrayToken)(correctValues.getToken())).length()) {
            // Consume and discard input values.  We are beyond the end
            // of the correctValues array.
            for (int i = 0; i < width; i++) {
                if (input.hasToken(i)) {
                    input.get(i);
                }
            }
            // Indicate that the test has passed.
            output.send(0, new BooleanToken(true));
            return true;
        } else { 
            output.send(0, new BooleanToken(false));
        }
        Token referenceToken
            = ((ArrayToken)(correctValues.getToken()))
            .getElement(_numberOfInputTokensSeen);
        Token[] reference;
        if (width == 1 && !(referenceToken instanceof ArrayToken)) {
            reference = new Token[1];
            reference[0] = referenceToken;
        } else {
            try {
                reference = ((ArrayToken)referenceToken).arrayValue();
            } catch (ClassCastException ex) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n"
                        + "Width of input is " + width
                        + ", but correctValues parameter"
                        + "is not an array "
                        + "of arrays.");
            }
            if (width != reference.length) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n"
                        + "Width of input is "
                        + width
                        + ", which does not match"
                        + "the  width of the "
                        + _numberOfInputTokensSeen
                        + "-th element of"
                        + " correctValues, "
                        + reference.length);
            }
        }
        for (int i = 0; i < width; i++) {
            if (!input.hasToken(i)) {
                throw new IllegalActionException(this,
                        "Test fails in iteration "
                        + _numberOfInputTokensSeen + ".\n"
                        + "Empty input on channel " + i);
            }
            Token token = input.get(i);
            boolean isClose;
            try {
                isClose =
                    token.isCloseTo(reference[i], _tolerance).booleanValue();
            } catch (IllegalActionException ex) {
                // Chain the exceptions together so we know which test
                // actor failed if there was more than one...
                throw new IllegalActionException(this, ex,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n"
                        + "Value was: " + token
                        + ". Should have been: "+ reference[i]);
            }

            if (!isClose) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n"
                        + "Value was: " + token
                        + ". Should have been: " + reference[i]);
            }
        }
        _numberOfInputTokensSeen++;
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Number of input tokens seen by this actor in the fire method.*/
    protected int _numberOfInputTokensSeen = 0;

    /** A double that is read from the <i>tolerance</i> parameter
     *        specifying how close the input has to be to the value
     *  given by <i>correctValues</i>.  This is a double, with default
     *  value 10<sup>-9</sup>.
     */
    protected double _tolerance;
}
