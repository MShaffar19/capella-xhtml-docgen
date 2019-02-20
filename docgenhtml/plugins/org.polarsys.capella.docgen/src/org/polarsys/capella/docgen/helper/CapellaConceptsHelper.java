/*******************************************************************************
 * Copyright (c) 2006, 2019 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *   Thales - initial API and implementation
 ******************************************************************************/
package org.polarsys.capella.docgen.helper;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.kitalpha.doc.gen.business.core.helper.IConceptsHelper;
import org.polarsys.kitalpha.doc.gen.business.core.util.LabelProviderHelper;

public class CapellaConceptsHelper implements IConceptsHelper {

	public boolean accept(Object modelElement) {
		/*
		 * Original code
		 */
//		if (modelElement instanceof CapellaElement) {
//			return DocGenHtmlCapellaControl.isPageCandidate((CapellaElement) modelElement);
//		} else {
//			return false;
//		}
		
		/*
		 * FIXME: A workaround for avoiding computing index by the core index
		 * rather than the extension.
		 * 
		 * See: org.polarsys.kitalpha.doc.gen.business.core.visitor.DocgenCommonSubClassEmfModelVisitor#indexElement(Object)
		 * 
		 */
		return false;
	}

	public String getConceptLabel(Object modelElement) {
		if (modelElement instanceof EObject)
			return LabelProviderHelper.getText((EObject) modelElement);
		return modelElement.toString();
	}

}
