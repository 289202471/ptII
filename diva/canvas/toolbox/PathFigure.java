/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.toolbox;

import java.awt.Shape;
import java.awt.Paint;

/** A PathFigure is one that contains a single instance of
 *  Shape. The figure can have a fill with optional compositing (for
 *  translucency), and a stroke with a different fill. With this
 *  class, simple objects can be created on-the-fly simply by passing
 *  an instance of java.awt.Shape to the constructor. This class
 *  is mainly intended for use for open shapes (without fill).
 *  For filled shapes, use the BasicFigure class, and for more complex
 *  figures, use VectorFigure or create a custom Figure class.
 *
 * @version        $Revision$
 * @author         John Reekie
 * @deprecated  BasicFigure now does everything this class used to do.
 */
public class PathFigure extends BasicFigure {

    /** Create a new figure with the given shape. The figure, by
     *  default, has a unit-width continuous black outline and no
     *  fill.  The given shape will be cloned to prevent the original
     *  from being modified.
     */
    public PathFigure (Shape shape) {
        this(shape, null, 1.0f);
    }

    /** Create a new figure with the given shape and outline width.
     * It has no fill. The default outline paint is black.  The given
     * shape will be cloned to prevent the original from being
     * modified.
     *
     * @deprecated  Use the float constructor instead.
     */
    public PathFigure (Shape shape, int lineWidth) {
        this(shape, null, (float)lineWidth);
    }

    /** Create a new figure with the given shape and outline width.
     * It has no fill. The default outline paint is black.  The given
     * shape will be cloned to prevent the original from being
     * modified.
     */
    public PathFigure (Shape shape, float lineWidth) {
        this(shape, null, lineWidth);
    }

    /** Create a new figure with the given paint pattern. The figure,
     *  by default, has no stroke.  The given shape will be cloned to
     *  prevent the original from being modified.
     */
    public PathFigure (Shape shape, Paint fill) {
        this(shape, fill, 1.0f);
    }

    /** Create a new figure with the given paint pattern and line
     *  width.  The given shape will be cloned to prevent the original
     *  from being modified.
     */
    public PathFigure (Shape shape, Paint fill, float lineWidth) {
        super(shape, fill, lineWidth);
    }
}
