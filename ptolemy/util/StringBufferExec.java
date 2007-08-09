/* Run a list of commands and save the output in a StringBuffer.

 Copyright (c) 2007 The Regents of the University of California.
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
package ptolemy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

/** Execute commands in a subprocess and accumulate the output in a
 StringBuffer. 

 <p>As an alternative to this class, see 
 {@link ptolemy.gui.JTextAreaExec}, which uses Swing; and
 {@link ptolemy.util.StreamExec}, which writes to stderr and stdout.

 <p>Sample usage:
 <pre> 
 List execCommands = new LinkedList();
 execCommands.add("date");
 execCommands.add("sleep 3");
 execCommands.add("date");
 execCommands.add("notACommand");

 final StringBufferExec exec = new StringBufferExec();
 exec.setCommands(execCommands);

 exec.start();
 </pre>


 <p>Loosely based on Example1.java from
 http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html
 <p>See also
 http://developer.java.sun.com/developer/qow/archive/135/index.jsp
 and
 http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html

 @see ptolemy.gui.JTextAreaExec

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class StringBufferExec extends StreamExec {

    /** Create a StringBufferExec. */
    public StringBufferExec() {
        super();
        buffer = new StringBuffer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the text message to the StringBuffer.
     *  The output automatically gets a trailing newline
     *  appended.
     *  @param text The text to append to standard error.
     */
    public void stderr(final String text) {
        buffer.append(text);
    }

    /** Append the text message to the StringBuffer
     *  The output automatically gets a trailing newline appended.
     *  @param text The text to append to standard out.
     */
    public void stdout(final String text) {
        buffer.append(text);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public StringBuffer buffer;

}
