/* Reads an audio file and divides the audio data into blocks.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

package ptolemy.domains.pn.demo.RunLength;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import java.io.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNImageSink
/**
Stores an image file (int the ASCII PBM format) and creates a matrix token

@author Mudit Goel
@version $Id$
*/

public class MatrixUnpacker extends AtomicActor {

    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public MatrixUnpacker(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        _output = new IOPort(this, "output", false, true);
        _dimen = new IOPort(this, "dimensions", false, true);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads one block of data from file and writes it to output port.
     *  Assuming data in 16 bit, higher byte first format.
     */
    public void fire() throws IllegalActionException {
        IntMatrixToken imagetok = (IntMatrixToken)_input.get(0);
        int[][] image = imagetok.intMatrix();
        //send the dimensions. First the rows, then the columns
        _dimen.broadcast(new IntToken(image.length));
        _dimen.broadcast(new IntToken(image[0].length));

        //Now sequentialize the Matrix
        //int[] result = new int[image.length*image[0].length];
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[0].length; j++) {
                //result[i*image.length +j] = image[i][j];
                _output.broadcast(new IntToken(image[i][j]));
            }
        }
    }

    //public boolean postfire() {
    //return false;
    //}

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private IOPort _dimen;
    private IOPort _output;
    private IOPort _input;
}
