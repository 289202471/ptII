/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.basic;

import diva.gui.AppletContext;

/**
 * An applet version of the graph demo.
 *
 * @see BasicGraphDemo
 * @author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class BasicGraphDemoApplet extends AppletContext {
    public BasicGraphDemoApplet() {
       new BasicGraphDemo(this);
    }
}







