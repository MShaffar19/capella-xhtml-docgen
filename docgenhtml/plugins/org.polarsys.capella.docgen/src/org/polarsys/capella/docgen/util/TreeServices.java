/*******************************************************************************
 * Copyright (c) 2019 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *   Thales - initial API and implementation
 ******************************************************************************/

package org.polarsys.capella.docgen.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.table.metamodel.table.DTable;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;
import org.polarsys.capella.docgen.diagram.CapellaHelper;

/**
 * 
 * @author Arnaud Dieumegard
 */
public class TreeServices {
	
	public static final String REQUIREMENT_TREE_ID = "requirementTreeView";
	public static final String DIAGRAMS_TREE_ID = "diagramsTreeView";
	
	/**
	 * Retrieve an HTML list representation for the <code>appliedReq</code> requirements including their complete <code>RequirementPkg</code> hierarchy
	 * @param appliedReq
	 * @param projectName
	 * @param outputFolder
	 * @return
	 */
	public static String getRequirementsTree(EList<Requirement> appliedReq, String projectName, String outputFolder) {
		StringBuilder sb = new StringBuilder();
		
		DefaultMutableTreeNode tree = buildRequirementsTree(appliedReq);
		
		sb.append("<ul id=\"").append(REQUIREMENT_TREE_ID).append("\" class=\"treeview\">");
		sb.append(printRequirementsTree(tree, projectName, outputFolder));
		sb.append(CapellaServices.UL_CLOSE);
		
		return sb.toString();
	}
	
	/**
	 * Builds a tree of <code>Requirement</code> objects as leafs and <code>RequirementPkg</code> objects as nodes
	 * @param appliedReq
	 * @return
	 */
	private static DefaultMutableTreeNode buildRequirementsTree(EList<Requirement> appliedReq) {
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode();
		
		// build the list of requirement packages involved
		Map<RequirementsPkg, DefaultMutableTreeNode> reqPkgToTreeNodeMap = new HashMap<RequirementsPkg, DefaultMutableTreeNode>();
		for (Requirement req : appliedReq){
			DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(req);
			EObject parent = req.eContainer();
			boolean hasFoundParent = false;
			while (parent instanceof RequirementsPkg) {
				DefaultMutableTreeNode pkgNode = reqPkgToTreeNodeMap.get(parent);
				// Build parent node if not yet created
				if (pkgNode == null) {
					pkgNode = new DefaultMutableTreeNode(parent);
					reqPkgToTreeNodeMap.put((RequirementsPkg) parent, pkgNode);
				} else {
					hasFoundParent = true;
				}
				// Add element
				pkgNode.add(currentNode);
				
				// Go one level up
				currentNode = pkgNode;
				parent = parent.eContainer();
				if (hasFoundParent) {
					break;
				}
			}
			if (!hasFoundParent) {
				tree.add(currentNode);
			}
		}
		return tree;
	}
	
	/**
	 * Prints tree elements as a bullet list. Does not print the root element.
	 * 
	 * @param tree A tree of EObjects
	 * @param projectName
	 * @param outputFolder
	 * @return
	 */
	private static String printRequirementsTree(DefaultMutableTreeNode tree, String projectName, String outputFolder) {
		StringBuilder sb = new StringBuilder();
		
		Enumeration<?> childs = tree.children();
		// Loop over root elements
		if (childs instanceof Enumeration) {
			while (childs.hasMoreElements()) {
				Object element = childs.nextElement(); 
				if (element instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) element;
					if (node.getUserObject() instanceof EObject) {
						EObject obj = (EObject) node.getUserObject();
						sb.append(CapellaServices.LI_OPEN);
						sb.append(CapellaServices.buildHyperlinkWithIcon(projectName, outputFolder, obj));
						if (obj instanceof RequirementsPkg) {
							sb.append(CapellaServices.UL_OPEN_SIMPLE);
							sb.append(printRequirementsTree(node, projectName, outputFolder));
							sb.append(CapellaServices.UL_CLOSE);
						}
						sb.append(CapellaServices.LI_CLOSE);
					}
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Retrieve an HTML list representation for the <code>appliedReq</code> requirements including their complete <code>RequirementPkg</code> hierarchy
	 * 
	 * @param appliedReq
	 * @param projectName
	 * @param outputFolder
	 * @return
	 */
	public static String getDiagramsTree(BlockArchitecture archi, String projectName, String outputFolder) {
		StringBuilder sb = new StringBuilder();
		
		DefaultMutableTreeNode tree = buildDiagramsTree(archi);
		
		sb.append("<ul id=\"").append(DIAGRAMS_TREE_ID).append("\" class=\"treeview\">");
		sb.append(printDiagramsTree(tree, projectName, outputFolder));
		sb.append(CapellaServices.UL_CLOSE);
		
		return sb.toString();
	}
	
	/**
	 * Builds a tree containing <code>Viewpoint</code> objects as top nodes, <code>RepresentationDescription</code> objects as middle nodes, and <code>DRepresentation</code> objects as leafs
	 * 
	 * @param archi
	 * @return
	 */
	private static DefaultMutableTreeNode buildDiagramsTree(BlockArchitecture archi) {
		DefaultMutableTreeNode tree = new DefaultMutableTreeNode();
		
		Map<RepresentationDescription, DefaultMutableTreeNode> repDescToTreeNodeMap = new HashMap<RepresentationDescription, DefaultMutableTreeNode>();
		Map<Viewpoint, DefaultMutableTreeNode> vpToTreeNodeMap = new HashMap<Viewpoint, DefaultMutableTreeNode>();
		
		// Get all representations
		for (DRepresentation rep : CapellaHelper.getAllDiagramsIn(archi)) {
			// Get description
			RepresentationDescription description = null;
			if (rep instanceof DSemanticDiagram) {
				description = ((DSemanticDiagram) rep).getDescription();
			} else if (rep instanceof DTable) {
				description = ((DTable) rep).getDescription();
			} else {
				break;
			}
			
			// Get Viewpoint
			Viewpoint vp = (Viewpoint) description.eContainer();
			
			// Get description node
			DefaultMutableTreeNode descNode = repDescToTreeNodeMap.get(description);
			if (descNode == null) {
				descNode = new DefaultMutableTreeNode(description);
				repDescToTreeNodeMap.put(description, descNode);
			}
			
			// Get viewpoint node
			DefaultMutableTreeNode vpNode = vpToTreeNodeMap.get(vp);
			if (vpNode == null) {
				vpNode = new DefaultMutableTreeNode(vp);
				vpToTreeNodeMap.put(vp, vpNode);
			}
			
			// Build representation node
			DefaultMutableTreeNode repNode = new DefaultMutableTreeNode(rep);
			
			// Add nodes to tree
			if (!tree.isNodeChild(vpNode)) {
				tree.add(vpNode);
			}
			if (!vpNode.isNodeChild(descNode)) {
				vpNode.add(descNode);
			}
			descNode.add(repNode);
		}
		return tree;
	}
	
	/**
	 * 
	 * 
	 * @param tree
	 * @param projectName
	 * @param outputFolder
	 * @return
	 */
	private static String printDiagramsTree(DefaultMutableTreeNode tree, String projectName, String outputFolder) {
		StringBuilder sb = new StringBuilder();

		Enumeration<?> childs = tree.children();
		if (childs instanceof Enumeration) {
			while (childs.hasMoreElements()) {
				sb.append(CapellaServices.LI_OPEN);
				Object element = childs.nextElement();
				if (element instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) element;
					if (node.getUserObject() instanceof EObject) {
						EObject obj = (EObject) node.getUserObject();
						if (obj instanceof Viewpoint) {
							Viewpoint vp = (Viewpoint) obj;
							sb.append(CapellaServices.getImageLinkFromElement(vp, projectName, outputFolder));
							sb.append(CapellaServices.getHyperlinkFromElement(vp, vp.getName()));
							sb.append(CapellaServices.UL_OPEN_SIMPLE);
							sb.append(printDiagramsTree(node, projectName, outputFolder));
							sb.append(CapellaServices.UL_CLOSE);
						} else if (obj instanceof RepresentationDescription) {
							RepresentationDescription description = (RepresentationDescription) obj;
							sb.append(CapellaServices.getImageLinkFromElement(description, projectName, outputFolder));
							sb.append(description.getName());
							sb.append(CapellaServices.UL_OPEN_SIMPLE);
							sb.append(printDiagramsTree(node, projectName, outputFolder));
							sb.append(CapellaServices.UL_CLOSE);
						} else if (obj instanceof DRepresentation) {
							DRepresentation rep = (DRepresentation) obj;
							sb.append(CapellaServices.getImageLinkFromElement(rep, projectName, outputFolder));
							sb.append(CapellaServices.getHyperlinkFromElement(rep, rep.getName()));
						}
					}
				}
				sb.append(CapellaServices.LI_CLOSE);
			}
		}
		return sb.toString();
	}

}