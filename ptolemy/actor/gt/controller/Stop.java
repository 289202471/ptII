/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import ptolemy.actor.Manager;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.domains.erg.kernel.ERGController;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// Stop

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Stop extends GTEvent {

    /**
     *  @param container
     *  @param name
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    public Stop(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        isFinalEvent.setToken(BooleanToken.TRUE);
        isFinalEvent.setVisibility(Settable.NOT_EDITABLE);
        isFinalEvent.setPersistent(false);
    }

    public RefiringData fire(ArrayToken arguments)
    throws IllegalActionException {
        ERGController controller = getController();

        Manager manager = controller.getManager();
        if (manager != null) {
            manager.finish();
        } else {
            throw new IllegalActionException(this,
                    "Cannot stop without a Manager.");
        }

        return null;
    }
}
