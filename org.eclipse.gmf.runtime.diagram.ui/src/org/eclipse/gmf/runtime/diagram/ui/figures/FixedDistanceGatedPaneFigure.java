/******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 ****************************************************************************/

package org.eclipse.gmf.runtime.diagram.ui.figures;

import java.util.Iterator;
import java.util.ListIterator;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.DelegatingLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gmf.runtime.diagram.ui.internal.figures.FixedDistanceGatedPane;
import org.eclipse.gmf.runtime.diagram.ui.util.DrawConstant;
import org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure;


/**
 * Transitionary class.  We will eventually move GatedPaneFigures to this class.
 * Currently this class is very specific to classifier with scalable image figure.
 * This will have to be changed to be made more generic.
 * 
 * @author jbruck
 * @deprecated 01/04/2006 See API change documentation in bugzilla 111935
 *             (https://bugs.eclipse.org/bugs/show_bug.cgi?id=111935)
 */
public class FixedDistanceGatedPaneFigure extends BorderItemFigure {
	
	private FixedDistanceGatedPane gatedFigure = null;
	private IFigure elementFigure = null;

	/**
	 * @return true if point is contain within any of its children.
	 */
	public boolean containsPoint(int x, int y) {
		
		Iterator iter = this.getChildren().iterator();
		while (iter.hasNext()){
			Figure figure = (Figure)iter.next();
			if (figure.containsPoint(x,y)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a new ClassiferNodeFigire figure.
	 * @param elementFig the figure to accosiate with this figure
	 */
	public FixedDistanceGatedPaneFigure( IFigure elementFig ) {
		super(DrawConstant.INVALID);
		setOpaque(false); // set transparent by default
		//setBorder(new LineBorder(ColorConstants.red));
		setLayoutManager(null);
		this.elementFigure = elementFig;
							
		add(getGatePane());
		add(getElementPane());
	}
	
	/**
	 * 
	 * @return The gate pane
	 */
	public FixedDistanceGatedPane getGatePane() {     
		if (gatedFigure == null) {
			gatedFigure = new FixedDistanceGatedPane();
			gatedFigure.setLayoutManager(new DelegatingLayout());
			gatedFigure.setVisible(true);
			gatedFigure.setOpaque(false);
		}
		return gatedFigure;
	}
	
	/**
	 * 
	 * @return The "main" figure
	 */
	public IFigure getElementPane() {
		return elementFigure;
	}
		
	/**
	 * gets the handle bounds of the main figure
	 * @return  handle bounds of the main figure
	 * @see org.eclipse.gef.handles.HandleBounds#getHandleBounds()
	 */
	public Rectangle getHandleBounds() {
		ListIterator li = getChildren().listIterator();
		Rectangle unionBounds = null;
		while (li.hasNext()) {
			IFigure fig = (IFigure) li.next();
			if (fig.isVisible()) {
				Rectangle figBounds = fig.getBounds();
				if (fig instanceof NodeFigure) {
					figBounds = ((NodeFigure) fig).getHandleBounds();
				}
				if (unionBounds == null)
					unionBounds = new Rectangle(figBounds);
				else
					unionBounds.union(figBounds);
			}
		}
		if (unionBounds != null)
			return unionBounds;

		return super.getHandleBounds();
	}

	
	
	 
	/**
	 * @see IFigure#getMinimumSize(int, int)
	 */
	public Dimension getMinimumSize(int wHint, int hHint) {
		Dimension mp = elementFigure.getMinimumSize(wHint, hHint).getCopy();
		Dimension gp = getGatePane().getOffsets();
		Dimension ret = new Dimension( Math.max(mp.width, gp.width), mp.height+gp.height); 
		return ret;
	}
	
	/**
	 * @see org.eclipse.draw2d.IFigure#getPreferredSize(int, int)
	 */
	public Dimension getPreferredSize(int wHint, int hHint) {
		// return minimum size that will hold element figure and gates.
		// should return elementFigure.getBounds - positive insets.
		// for now the bounds of the BorderedFigure will hold everything.
		Dimension mp = elementFigure.getPreferredSize(wHint, hHint).getCopy();
		Dimension gp = getGatePane().getOffsets();
		Dimension ret = new Dimension( Math.max(mp.width, gp.width), mp.height+gp.height); 
		return ret;
	}
	
	/**
	 * Give the main figure the entire bounds of the wrapper.
	 */
	protected void layout() {
		super.layout();
		Rectangle r = getBounds().getCopy();
		Dimension gp = getGatePane().getPreferredSize().getCopy();
		Dimension mp = elementFigure.getPreferredSize(r.width, r.height).getCopy();
					
		if( gp.width == 0 && gp.height == 0) {
			elementFigure.setBounds(r);
			getGatePane().setBounds(new Rectangle(r.getTopLeft(), new Dimension(0,0)));
		} else {
			gp = getGatePane().getOffsets();
			if( gp.width == 0 && gp.height == 0) {
				elementFigure.setBounds(r);
				getGatePane().setBounds(new Rectangle(r.getTopLeft(), new Dimension(0,0)));
			} else {
				getGatePane().setBounds(new Rectangle(r.x, r.y, r.width, r.height-mp.height ));
				elementFigure.setBounds(new Rectangle(r.x, r.y + r.height-mp.height, r.width, mp.height));
			}
		}
	}
			
	
	/**
	 * @see org.eclipse.draw2d.IFigure#getToolTip()
	 */
	public IFigure getToolTip() {
		return elementFigure.getToolTip();
	}

	/**
	 * @see org.eclipse.draw2d.IFigure#setToolTip(IFigure)
	 */
	public void setToolTip(IFigure f) {
		elementFigure.setToolTip(f);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#getSourceConnectionAnchorAt(org.eclipse.draw2d.geometry.Point)
	 */
	public ConnectionAnchor getSourceConnectionAnchorAt(Point p) {
		if( elementFigure instanceof NodeFigure )
			return ((NodeFigure)elementFigure).getSourceConnectionAnchorAt(p);
		return super.getSourceConnectionAnchorAt(p);
	}
	
	/*
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#getTargetConnectionAnchorAt(org.eclipse.draw2d.geometry.Point)
	 */
	public ConnectionAnchor getTargetConnectionAnchorAt(Point p) {
		if( elementFigure instanceof NodeFigure )
			return ((NodeFigure)elementFigure).getTargetConnectionAnchorAt(p);
		return super.getTargetConnectionAnchorAt(p);
	}
	
	/*
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#getConnectionAnchor(java.lang.String)
	 */
	public ConnectionAnchor getConnectionAnchor(String terminal) {
		if( elementFigure instanceof NodeFigure )
			return ((NodeFigure)elementFigure).getConnectionAnchor(terminal);
		return super.getConnectionAnchor(terminal);
	}
	
	/*
	 * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#getConnectionAnchorTerminal(org.eclipse.draw2d.ConnectionAnchor)
	 */
	public String getConnectionAnchorTerminal(ConnectionAnchor c) {
		if( elementFigure instanceof NodeFigure )
			return ((NodeFigure)elementFigure).getConnectionAnchorTerminal(c);
		return super.getConnectionAnchorTerminal(c);		
	}

}
