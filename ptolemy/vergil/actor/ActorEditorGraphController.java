/* The graph controller for vergil.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.actor;

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.Site;
import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.event.EventLayer;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.ActionInteractor;
import diva.canvas.interactor.CompositeInteractor;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionModel;
import diva.graph.EdgeController;
import diva.graph.GraphController;
import diva.graph.GraphException;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.NodeRenderer;
import diva.gui.toolbox.FigureIcon;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.moml.Location;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.Vertex;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.BasicGraphController.NewPortAction;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.kernel.PortDialogFactory;
import ptolemy.vergil.kernel.RelationController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.SnapConstraint;
import ptolemy.vergil.toolbox.FigureAction.SourceType;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

//////////////////////////////////////////////////////////////////////////
//// ActorEditorGraphController
/**
A Graph Controller for the Ptolemy II schematic editor.  In addition to the
interaction allowed in the viewer, this controller allows nodes to be
dragged and dropped onto its graph.  Relations can be created by
control-clicking on the background.  Links can be created by control-clicking
and dragging on a port or a relation.  In addition links can be created by
clicking and dragging on the ports that are inside an entity.
Anything can be deleted by selecting it and pressing
the delete key on the keyboard.

@author Steve Neuendorffer
@contributor Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
 */
public class ActorEditorGraphController extends ActorViewerGraphController {

    /** Create a new basic controller with default
     *  terminal and edge interactors.
     */
    public ActorEditorGraphController() {
	super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add commands to the specified menu and toolbar, as appropriate
     *  for this controller.  In this class, commands are added to create
     *  ports and relations.
     *  @param menu The menu to add to, or null if none.
     *  @param toolbar The toolbar to add to, or null if none.
     */
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);
	diva.gui.GUIUtilities.addMenuItem(menu, _newInputPortAction);
       	diva.gui.GUIUtilities.addToolBarButton(toolbar, _newInputPortAction);
	diva.gui.GUIUtilities.addMenuItem(menu, _newOutputPortAction);
       	diva.gui.GUIUtilities.addToolBarButton(toolbar, _newOutputPortAction);
	diva.gui.GUIUtilities.addMenuItem(menu, _newInoutPortAction);
       	diva.gui.GUIUtilities.addToolBarButton(toolbar, _newInoutPortAction);
	diva.gui.GUIUtilities.addMenuItem(menu, _newInputMultiportAction);
       	diva.gui.GUIUtilities.addToolBarButton(
                   toolbar, _newInputMultiportAction);
	diva.gui.GUIUtilities.addMenuItem(menu, _newOutputMultiportAction);
       	diva.gui.GUIUtilities.addToolBarButton(
                   toolbar, _newOutputMultiportAction);
	diva.gui.GUIUtilities.addMenuItem(menu, _newInoutMultiportAction);
       	diva.gui.GUIUtilities.addToolBarButton(
                   toolbar, _newInoutMultiportAction);

        menu.addSeparator();

        // Add an item that adds new relations.
	diva.gui.GUIUtilities.addMenuItem(menu, _newRelationAction);
	diva.gui.GUIUtilities.addToolBarButton(toolbar, _newRelationAction);
    }

    /** Set the configuration.  The configuration is used when
     *  opening documentation files.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        if (_portDialogFactory != null) {
            _portDialogFactory.setConfiguration(configuration);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the controllers for nodes in this graph.
     *  In this class, controllers with FULL access are created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    protected void _createControllers() {
	_attributeController = new AttributeController(this,
                 AttributeController.FULL);
	_entityController = new ActorController(this,
                 AttributeController.FULL);
	_entityPortController = new IOPortController(this,
                 AttributeController.FULL);
	_portController = new ExternalIOPortController(this,
                 AttributeController.FULL);
	_relationController = new RelationController(this);
	_linkController = new LinkController(this);
    }

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    protected void initializeInteraction() {
        super.initializeInteraction();
        GraphPane pane = getGraphPane();

        // Add a menu command to configure the ports.
        _portDialogFactory = new PortDialogFactory();
	_menuFactory.addMenuItemFactory(_portDialogFactory);
        _portDialogFactory.setConfiguration(getConfiguration());

        // Create a listener that creates new relations
	_relationCreator = new RelationCreator();
        _relationCreator.setMouseFilter(_controlFilter);
        pane.getBackgroundEventLayer().addInteractor(_relationCreator);

        // Create the interactor that drags new edges.
	_linkCreator = new LinkCreator();
	_linkCreator.setMouseFilter(_controlFilter);
        // NOTE: Do not use _initializeInteraction() because we are
        // still in the constructor, and that method is overloaded in
        // derived classes.
	((CompositeInteractor)_portController.getNodeInteractor())
                .addInteractor(_linkCreator);
        ((CompositeInteractor)_entityPortController.getNodeInteractor())
                .addInteractor(_linkCreator);
	((CompositeInteractor)_relationController.getNodeInteractor())
                .addInteractor(_linkCreator);

	LinkCreator linkCreator2 = new LinkCreator();
	linkCreator2.setMouseFilter(
                new MouseFilter(InputEvent.BUTTON1_MASK,0));
	((CompositeInteractor)_entityPortController.getNodeInteractor())
                .addInteractor(linkCreator2);
    }

    /** Initialize interactions for the specified controller.  This
     *  method is called when a new controller is constructed. In this
     *  class, this method attaches a link creator to the controller
     *  if the controller is an instance of ExternalIOPortController,
     *  IOPortController, or RelationController.
     *  @param controller The controller for which to initialize interaction.
     */
    protected void _initializeInteraction(NamedObjController controller) {
        super._initializeInteraction(controller);
        if (controller instanceof ExternalIOPortController
                || controller instanceof IOPortController
                || controller instanceof RelationController) {
            Interactor interactor = controller.getNodeInteractor();
            if (interactor instanceof CompositeInteractor) {
                ((CompositeInteractor)interactor).addInteractor(_linkCreator);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The filter for control operations. */
    private MouseFilter _controlFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /** The interactor that interactively creates edges. */
    private LinkCreator _linkCreator;

    /** Action for creating a new input port. */
    private Action _newInputPortAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INPUT, "New input port",
            KeyEvent.VK_I);

    /** Action for creating a new output port. */
    private Action _newOutputPortAction = new NewPortAction(
            ExternalIOPortController._GENERIC_OUTPUT, "New output port",
            KeyEvent.VK_O);

    /** Action for creating a new inout port. */
    private Action _newInoutPortAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INOUT, "New input/output port",
            KeyEvent.VK_P);

    /** Action for creating a new input multiport. */
    private Action _newInputMultiportAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INPUT_MULTIPORT,
            "New input multiport",
            KeyEvent.VK_N);

    /** Action for creating a new output multiport. */
    private Action _newOutputMultiportAction = new NewPortAction(
            ExternalIOPortController._GENERIC_OUTPUT_MULTIPORT,
            "New output multiport",
            KeyEvent.VK_U);

    /** Action for creating a new inout multiport. */
    private Action _newInoutMultiportAction = new NewPortAction(
            ExternalIOPortController._GENERIC_INOUT_MULTIPORT,
            "New input/output multiport",
            KeyEvent.VK_T);

    /** Action for creating a new relation. */
    private Action _newRelationAction = new NewRelationAction();

    /** The port dialog factory. */
    private PortDialogFactory _portDialogFactory;

    /** The interactor for creating new relations. */
    private RelationCreator _relationCreator;

    /** The filter for shift operations. */
    private MouseFilter _shiftFilter = new MouseFilter(
            InputEvent.BUTTON1_MASK,
            InputEvent.SHIFT_MASK);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// LinkCreator

    /** This class is an interactor that interactively drags edges from
     *  one terminal to another, creating a link to connect them.
     */
    protected class LinkCreator extends AbstractInteractor {

        /** Create a new edge when the mouse is pressed. */
        public void mousePressed(LayerEvent event) {
	    Figure source = event.getFigureSource();
            NamedObj sourceObject = (NamedObj) source.getUserObject();

	    // Create the new edge.
	    Link link = new Link();
	    // Set the tail, going through the model so the link is added
	    // to the list of links.
            ActorGraphModel model = (ActorGraphModel)getGraphModel();
            model.getLinkModel().setTail(link, sourceObject);

            try {
		// add it to the foreground layer.
		FigureLayer layer =
		    getGraphPane().getForegroundLayer();
		Site headSite, tailSite;

		// Temporary sites.  One of these will get blown away later.
		headSite = new AutonomousSite(layer,
                        event.getLayerX(),
                        event.getLayerY());
		tailSite = new AutonomousSite(layer,
                        event.getLayerX(),
                        event.getLayerY());
		// Render the edge.
                Connector c = getEdgeController(link)
                        .render(link, layer, tailSite, headSite);
		// get the actual attach site.
		tailSite =
		    getEdgeController(link).getConnectorTarget()
                            .getTailSite(c, source,
                            event.getLayerX(),
                            event.getLayerY());
		if (tailSite == null) {
		    throw new RuntimeException("Invalid connector target: " +
                            "no valid site found for tail of new connector.");
		}

		// And reattach the connector.
		c.setTailSite(tailSite);

                // Add it to the selection so it gets a manipulator, and
                // make events go to the grab-handle under the mouse
		getSelectionModel().addSelection(c);
                ConnectorManipulator cm =
                    (ConnectorManipulator) c.getParent();
                GrabHandle gh = cm.getHeadHandle();
                layer.grabPointer(event, gh);
            } catch (Exception ex) {
                MessageHandler.error("Drag connection failed:", ex);
            }
	}
    }

    ///////////////////////////////////////////////////////////////////
    //// NewRelationAction

    // An action to create a new relation.
    public class NewRelationAction extends FigureAction {
	public NewRelationAction() {
	    super("New Relation");
	    String dflt = "";
	    // Standard toolbar icons are 25x25 pixels.
	    NodeRenderer renderer = _relationController.getNodeRenderer();
	    Figure figure = renderer.render(null);

	    FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
	    putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
	    putValue("tooltip", "Control-click to create a new relation");
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_R));
	}

	public void actionPerformed(ActionEvent e) {
	    super.actionPerformed(e);
	    double x;
	    double y;
	    if (getSourceType() == TOOLBAR_TYPE ||
                    getSourceType() == MENUBAR_TYPE) {
		// No location in the action, so put it in the middle.
                BasicGraphFrame frame = getFrame();
                Point2D center;
                if (frame != null) {
                    // Put in the middle of the visible part.
                    center = frame.getCenter();
                    x = center.getX();
                    y = center.getY();
                } else {
                    // Put in the middle of the pane.
                    GraphPane pane = getGraphPane();
                    center = pane.getSize();
                    x = center.getX()/2;
                    y = center.getY()/2;
                }
	    } else {
                // Transform
                AffineTransform current = 
                    getGraphPane().getTransformContext().getTransform();
                AffineTransform inverse;
                try {
                    inverse = current.createInverse();
                }
                catch(NoninvertibleTransformException ex) {
                    throw new RuntimeException(ex.toString());
                }
                Point2D point = new Point2D.Double(getX(), getY());
                
                inverse.transform(point, point);
		x = point.getX();
		y = point.getY();
	    }

	    ActorGraphModel graphModel = (ActorGraphModel)getGraphModel();
            final double[] point = SnapConstraint.constrainPoint(x, y);
	    final CompositeEntity toplevel = graphModel.getPtolemyModel();
            NamedObj container = 
                MoMLChangeRequest.getDeferredToParent(toplevel);
            if (container == null) {
                container = toplevel;
            }

	    final String relationName = toplevel.uniqueName("relation");
	    final String vertexName = "vertex1";
	    // Create the relation.
	    StringBuffer moml = new StringBuffer();
            if (container != toplevel) {
                moml.append("<entity name=\"" + 
                        toplevel.getName(container) + "\">\n");
            }
	    moml.append("<relation name=\"" + relationName + "\">\n");
	    moml.append("<vertex name=\"" + vertexName + "\"/>\n");
	    moml.append("</relation>");
            if (container != toplevel) {
                moml.append("</entity>");
            }

	    ChangeRequest request =
		new MoMLChangeRequest(this, container, moml.toString()) {
                protected void _execute() throws Exception {
                    super._execute();
                    // Set the location of the icon.
                    // Note that this really needs to be done after
                    // the change request has succeeded, which is why
                    // it is done here.  When the graph controller
                    // gets around to handling this, it will draw
                    // the icon at this location.
                    NamedObj newObject = toplevel.getRelation(relationName);
                    Vertex vertex = (Vertex) newObject.getAttribute(vertexName);
                    vertex.setLocation(point);
                }
            };
	    container.requestChange(request);
	    try {
		request.waitForCompletion();
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    //// RelationCreator

    /** An interactor for creating relations upon control clicking.
     */
    protected class RelationCreator extends ActionInteractor {
	public RelationCreator() {
	    super();
            setAction(_newRelationAction);
	}
    }
}
