/* Soot - a J*va Optimization Framework
 * Copyright (C) 2004 Jennifer Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Created on Feb 26, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ca.mcgill.sable.soot.cfg.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.*;

/**
 * @author jlhotak
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CFGFlowFigure extends Figure {

	Panel background;
	/**
	 * 
	 */
	public CFGFlowFigure() {
		super();
		//background = new Panel();
		//this.add(background);
		FlowLayout layout = new FlowLayout(false);
		layout.setMinorAlignment(FlowLayout.ALIGN_CENTER);
		layout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
		
		//layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		//layout.setStretchMinorAxis(true);
		this.setLayoutManager(layout);
	}

}
