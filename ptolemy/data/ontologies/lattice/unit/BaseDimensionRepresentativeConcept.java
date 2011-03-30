/* A representative concept in the unitSystem ontology for a set of units for
 * a specific physical dimension that is one of the 7 base dimensions.

 Copyright (c) 1998-2011 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.unit;

import java.util.List;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BaseDimensionRepresentativeConcept

/** A representative concept in the unitSystem ontology for a set of units for
 *  a specific physical dimension that is one of the 7 base dimensions.
 *  
 *  There are 7 base dimensions from which all other unit dimensions are
 *  derived. These can be specified in a units system ontology by 7
 *  BaseUnitRepresentativeConcepts: 
 *  <ul>
 *  <li>Mass - SI unit kilograms (kg)
 *  <li>Position - SI unit meters (m)
 *  <li>Time - SI unit seconds (sec)
 *  <li>Current - SI unit amperes (amp)
 *  <li>Temperature - SI unit Kelvin (K)
 *  <li>Amount of substance - SI unit moles (mol)
 *  <li>Intensity of light - SI unit candela (cd)
 *  </ul>
@see BaseUnitConcept
@author Charles Shelton
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
*/
public class BaseDimensionRepresentativeConcept extends DimensionRepresentativeConcept {

    /** Create a new BaseUnitRepresentativeConcept with the specified name and
     *  ontology.
     *  
     *  @param ontology The specified ontology where this concept resides.
     *  @param name The specified name for the concept.
     *  @exception NameDuplicationException If the ontology already contains a
     *   concept with the specified name.
     *  @exception IllegalActionException If the base class throws it.
     */
    public BaseDimensionRepresentativeConcept(CompositeEntity ontology, String name)
            throws NameDuplicationException, IllegalActionException {
        super(ontology, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Return a BaseUnitConcept instance from the given concept string
     *  representation. The string must represent one of the units specified
     *  for this physical dimension.
     *  @param infiniteConceptString The string that represents the unit concept
     *   to be returned.
     *  @return The BaseUnitConcept represented by the given string.
     *  @throws IllegalActionException Thrown if there is a problem creating
     *   the unit concept.
     */
    protected BaseUnitConcept _createInfiniteConceptInstance(
            String infiniteConceptString) throws IllegalActionException {
        
        if (containsThisInfiniteConceptString(infiniteConceptString)) {
            String unitName = infiniteConceptString.substring(getName()
                    .length() + 1);
            
            return BaseUnitConcept.createBaseUnitConcept(getOntology(), this,
                    _findUnitRecordByName(unitName));
        } else {
            throw new IllegalActionException(this, "The given string cannot " +
                        "be used to derive a valid infinite concept contained " +
                        "by this representative.");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Return the unit info record token with the given Name field. First
     *  look in the array of user defined record tokens, and if it is not
     *  found there then look in the list of pre-specified unit
     *  parameters.
     *  @param unitName The value of the Name field of the unit record token to
     *   be found.
     *  @return The unit info RecordToken with the given Name field.
     *  @throws IllegalActionException Thrown if the unit cannot be found, or
     *   if the unit specification parameter is invalid.
     */
    private RecordToken _findUnitRecordByName(String unitName)
            throws IllegalActionException {
        RecordToken userDefinedRecord = _findUserDefinedUnitRecordByName(unitName);
        if (userDefinedRecord == null) {
            
            // Find the given unitName in the list of pre-specified parameters.
            List<Parameter> unitParameterList = attributeList(Parameter.class);
            for (Parameter unitParameter : unitParameterList) {
                if (unitName.equals(unitParameter.getName())) {
                    Token[] unitRecordArray = new Token[3];
                    unitRecordArray[0] = new StringToken(unitName);

                    Token unitConversionInfo = unitParameter.getToken();
                    if (unitConversionInfo instanceof DoubleToken) {
                        unitRecordArray[1] = unitConversionInfo;
                        unitRecordArray[2] = DoubleToken.ZERO;
                    } else if (unitConversionInfo instanceof ArrayToken &&
                            ((ArrayToken) unitConversionInfo).length() == 2 &&
                            ((ArrayToken) unitConversionInfo).getElementType().equals(BaseType.DOUBLE)) {
                        unitRecordArray[1] = ((ArrayToken) unitConversionInfo).getElement(0);
                        unitRecordArray[2] = ((ArrayToken) unitConversionInfo).getElement(1);
                    } else {
                        throw new IllegalActionException(this,
                                "Invalid unit specification parameter: " +
                                unitParameter);
                    }
                    return new RecordToken(BaseUnitConcept.unitRecordLabelArray,
                            unitRecordArray);
                }
            }
            throw new IllegalActionException(this, "No unit named " + unitName
                    + " for the " + this + " dimension.");
        } else {
            return userDefinedRecord;
        }
    }
}
