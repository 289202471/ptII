/* An adapter class for ptolemy.actor.AtomicActor.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.logicalOrBackward.actor;

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AtomicActor

/**
 An adapter class for ptolemy.actor.AtomicActor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class AtomicActor extends PropertyConstraintHelper {

    /**
     * Construct an adapter for the given AtomicActor. This is the
     * adapter class for any ActomicActor that does not have a
     * specific defined adapter class. Default actor constraints
     * are set for this adapter.
     * @param solver The given solver.
     * @param actor The given ActomicActor.
     * @exception IllegalActionException
     */
    public AtomicActor(PropertyConstraintSolver solver,
            ptolemy.actor.AtomicActor actor) throws IllegalActionException {

        super(solver, actor);
    }

    /**
     * Construct an adapter for the given AtomicActor. This is the
     * adapter class for any ActomicActor that does not have a
     * specific defined adapter class.
     * @param solver The given solver.
     * @param actor The given ActomicActor.
     * @param useDefaultConstraints Indicate whether this adapter uses the
     * default actor constraints.
     * @exception IllegalActionException If the adapter cannot be
     * initialized in the superclass.
     */
    public AtomicActor(PropertyConstraintSolver solver,
            ptolemy.actor.AtomicActor actor, boolean useDefaultConstraints)
            throws IllegalActionException {

        super(solver, actor, useDefaultConstraints);
    }

    /**
     * Return the list of property-able Attributes.
     * A property-able Attribute is a StringAttribute with the name
     * "guardTransition", a StringAttribute in an Expression actor,
     * a StringAttribute with the name "expression" or a Variable
     * with full visibility.  However, Variables with certain names
     * are excluded.
     * @see ptolemy.data.properties.Propertyable
     * @return The list of property-able Attributes.
     */
    protected List<Attribute> _getPropertyableAttributes() {
        // do not set up default constraints for attributes in AtomicActors
        return new LinkedList<Attribute>();
    }
}
