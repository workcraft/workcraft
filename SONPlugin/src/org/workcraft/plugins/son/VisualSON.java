package org.workcraft.plugins.son;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.plugins.son.elements.VisualPlaceNode;
import org.workcraft.util.Hierarchy;


@DisplayName ("Structured Occurrence Nets")
@CustomTools ( SONToolProvider.class )

public class VisualSON extends AbstractVisualModel{

	private String group="Invalid Group Selection";
	private String block="Invalid Block Selection";
	private String blockConnection="Block Connection Error";
	private SON net;

	public VisualSON(SON model){
		this(model, null);
	}

	public VisualSON (SON model, VisualGroup root){
		super (model, root);
	//	currentmathLevel = getCurrentLevel();
		if (root == null)
			try {
				createDefaultFlatStructure();
			} catch (NodeCreationException e) {
				throw new RuntimeException(e);
			}

		this.net = model;
		blockConnectionChecker();
	}


	public void validateConnection (Node first, Node second) throws InvalidConnectionException{
	}

	public void validateConnection (Node first, Node second, Semantics semantics) throws InvalidConnectionException{
		if ((first instanceof VisualCondition) && (second instanceof VisualCondition) && (semantics == Semantics.PNLINE))
			throw new InvalidConnectionException ("Connections between conditions are not valid(PN Connection)");
		if ((first instanceof VisualEvent) && (second instanceof VisualEvent))
			throw new InvalidConnectionException ("Connections between events are not valid (PN Connection)");
		if ((second instanceof VisualSONConnection) || (first instanceof VisualSONConnection))
			throw new InvalidConnectionException ("Invalid connection (Connection)");

		//asyn type
		if (!(first instanceof VisualChannelPlace) && !(second instanceof VisualChannelPlace)
				&& ((semantics == Semantics.ASYNLINE) || (semantics == Semantics.SYNCLINE))) {
			throw new InvalidConnectionException ("Invalid connection (A/Syn Communication)");
		}
		//Group
		if ((first instanceof VisualChannelPlace) && !isGrouped(second))
			throw new InvalidConnectionException ("Connections between channel places and un-grouped nodes are not valid (Group)");
		if ((second instanceof VisualChannelPlace) && !isGrouped(first))
			throw new InvalidConnectionException ("Connections between channel places and un-grouped nodes are not valid (Group)");
		if ((first instanceof VisualChannelPlace) && (second instanceof VisualChannelPlace))
			throw new InvalidConnectionException ("Connections between channel places are not valid (A/Syn Communication)");
		if (((first instanceof VisualChannelPlace) && (second instanceof VisualCondition))
				|| ((first instanceof VisualCondition) && (second instanceof VisualChannelPlace)))
			throw new InvalidConnectionException ("Connections between channel place and condition are not valid (A/Syn Communication)");

		if(isGrouped(first) && isGrouped(second) && !isInSameGroup(first, second)  &&
				(semantics == Semantics.PNLINE || semantics == Semantics.ASYNLINE || semantics == Semantics.SYNCLINE) )
			throw new InvalidConnectionException ("Direct connections between two different groups are not valid (PN Connection, A/Syn Communication)");

		if(!(first instanceof VisualChannelPlace) &&  !(second instanceof VisualChannelPlace)){
			if (isGrouped(first) && !isGrouped(second) || isGrouped(second) && !isGrouped(first))
			throw new InvalidConnectionException ("Connections between grouped node and un-grouped nodes are not valid (Group)");

		//Bhv Type
		if (semantics == Semantics.BHVLINE) {
			if ((first instanceof VisualEvent) || (second instanceof VisualEvent))
				throw new InvalidConnectionException ("Connections between non-conditions are not valid (Behavioural Abstraction)");
			if (!isGrouped(first) || !isGrouped(second) )
				throw new InvalidConnectionException ("Connections between ungrouped conditions are not valid (Behavioural Abstraction)");
			if (this.isInSameGroup(first, second))
				throw new InvalidConnectionException ("Connections between same grouped conditions are not valid (Behavioural Abstraction)");
			if (this.isInBlock(first) || this.isInBlock(second))
				throw new InvalidConnectionException ("Block cannot cross phases (Block)");
			}
		}

		//ChannelPlace
		if (first instanceof VisualChannelPlace)
			for (Node node : net.getPreset(((VisualChannelPlace) first).getReferencedComponent())){
				if (net.isInSameGroup(((VisualComponent)second).getReferencedComponent(), node)){
					throw new InvalidConnectionException ("The input and ouput nodes for a channel place belong to same group are not valid");
				}
			}

		if (second instanceof VisualChannelPlace)
			for (Node node : net.getPostset(((VisualChannelPlace) second).getReferencedComponent())){
				if (net.isInSameGroup(((VisualComponent)first).getReferencedComponent(), node)){
					throw new InvalidConnectionException ("The input and ouput nodes of a channel place belong to same group are not valid");
				}
			}

		//block
		if((first instanceof VisualEvent) && isInBlock(second))
			throw new InvalidConnectionException ("Block inputs must be conditions (Block)");

		if((second instanceof VisualEvent) && isInBlock(first))
			throw new InvalidConnectionException ("Block outputs must be conditions (Block)");
	}

	private boolean isGrouped(Node node){
		for (VisualONGroup group : getVisualONGroups()){
			if (group.getVisualComponents().contains(node)){
				return true;
			}
		}
		return false;
	}

	private boolean isInSameGroup (Node first, Node second){
		for (VisualONGroup group : getVisualONGroups()){
			if ( group.getVisualComponents().contains(first) && group.getVisualComponents().contains(second)){
				return true;
			}
		}
		return false;
	}

	private boolean isInBlock(Node node){
		for(VisualBlock block : this.getVisualBlocks())
			if(block.getComponents().contains(node))
				return true;
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VisualConnection connect (Node first, Node second) throws InvalidConnectionException{
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	public void connect (Node first, Node second, Semantics semantics) throws InvalidConnectionException{
		validateConnection(first, second, semantics);
		VisualComponent c1= (VisualComponent)first;
		VisualComponent c2= (VisualComponent)second;

		if ((c1 instanceof VisualChannelPlace) || (c2 instanceof VisualChannelPlace)) {
			if (semantics != Semantics.SYNCLINE) {
				semantics = Semantics.ASYNLINE;
			}
		}

		SONConnection con = (SONConnection)net.connect(c1.getReferencedComponent(), c2.getReferencedComponent(), semantics);
		VisualSONConnection ret = new VisualSONConnection(con, c1, c2);

		Hierarchy.getNearestContainer(c1,c2).add(ret);
	}

	private Collection<Node> getGroupableSelection(){
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> selection = new HashSet<Node>();
		boolean validate = false;

		if(getCurrentLevel() instanceof VisualONGroup){
			JOptionPane.showMessageDialog(null,
					"Grouping inside a group is invalid",group, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
		}

		for(Node node : getOrderedCurrentLevelSelection()){
			if(node instanceof VisualPage){
				selection.addAll(Hierarchy.getDescendantsOfType(node, VisualComponent.class));
			}
			if(node instanceof VisualTransformableNode) {
				selection.add((VisualTransformableNode)node);
			}
		}

		if(isPure(selection)){
			for(Node node : getOrderedCurrentLevelSelection()){
				if(node instanceof VisualTransformableNode){
					if (!(node instanceof VisualChannelPlace) && !(node instanceof VisualONGroup) ){
							result.add(node);
					}else{
						JOptionPane.showMessageDialog(null,
								"Group Selection containing Channel Places or other groups is invaild",group, JOptionPane.WARNING_MESSAGE);
						result.clear();
						return result;
					}
				}
			}
		}else{
			JOptionPane.showMessageDialog(null,
					"Grouping a partial occurrence net is invalid",group, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
		}

		for (Node node : result){
			if (node instanceof VisualCondition)
				validate = true;
			if (node instanceof VisualPage && !Hierarchy.getDescendantsOfType(node, VisualCondition.class).isEmpty()){
				validate = true;
			}
		}
		if (!validate) {
			JOptionPane.showMessageDialog(null,
					"An occurrence net must contain at least one condition",group, JOptionPane.WARNING_MESSAGE);
			result.removeAll(result);
			return result;
		}
		else
			return result;

	}

	private boolean isPure (Collection<Node> nodes) {
		for (VisualSONConnection connect : getVisualSONConnections()){
			if(nodes.contains(connect.getFirst()) && !(connect.getFirst() instanceof VisualChannelPlace)
				&& ! nodes.contains(connect.getSecond()) && !(connect.getSecond() instanceof VisualChannelPlace))
			return false;

			if(!nodes.contains(connect.getFirst()) && !(connect.getFirst() instanceof VisualChannelPlace)
					&& nodes.contains(connect.getSecond()) && !(connect.getSecond() instanceof VisualChannelPlace))
			return false;
		}
		return true;
	}

	public void groupSelection(){
		Collection<Node> selected = getGroupableSelection();

		if (selected.size() > 0) {

			ONGroup mathGroup = new ONGroup();
			VisualONGroup group = new VisualONGroup(mathGroup);
			Container currentLevel = getCurrentLevel();

			currentLevel.add(group);
			currentLevel.reparent(selected, group);

			VisualComponent visualContainer = (VisualComponent)Hierarchy.getNearestAncestor(currentLevel, VisualComponent.class);

			Container currentMathLevel;
			if(visualContainer==null)
				currentMathLevel = getMathModel().getRoot();
			else
				currentMathLevel = (Container)visualContainer.getReferencedComponent();
			currentMathLevel.add(mathGroup);

			ArrayList<Node> connectionsToGroup = new ArrayList<Node>();
			for(VisualConnection connection : Hierarchy.getChildrenOfType(currentLevel, VisualConnection.class)) {
				if(Hierarchy.isDescendant(connection.getFirst(), group) &&
						Hierarchy.isDescendant(connection.getSecond(), group)) {
					connectionsToGroup.add(connection);
				}
			}
			currentLevel.reparent(connectionsToGroup, group);

			// reparenting for the math model nodes
			ArrayList<Node> selectedMath = new ArrayList<Node>();
			for (Node node:selected) {
				if (node instanceof VisualComponent) {
					selectedMath.add(((VisualComponent)node).getReferencedComponent());
				}
			}
			for (Node node:connectionsToGroup) {
				if (node instanceof VisualConnection) {
					selectedMath.add(((VisualConnection)node).getReferencedConnection());
				}
			}

			for (Node node: selectedMath) {
				Container parent = (Container)node.getParent();
				ArrayList<Node> re = new ArrayList<Node>();
				re.add(node);


				// reparenting at the level of the reference manager
				ReferenceManager refMan = getMathModel().getReferenceManager();
				if (refMan instanceof HierarchicalUniqueNameReferenceManager) {
					HierarchicalUniqueNameReferenceManager manager = (HierarchicalUniqueNameReferenceManager)refMan;
					manager.setNamespaceProvider(node, mathGroup);
				}
				parent.reparent(re, mathGroup);

			}

			// final touch on visual part
			if (group != null) {
				Point2D groupCenter = centralizeComponents(selected);
				group.setPosition(groupCenter);
				select(group);
			}
		}
	}

/*	public void superGroupSelection(){
		ArrayList<Node> selected = new ArrayList<Node>();
		for(Node node : getOrderedCurrentLevelSelection()) {
			if(node instanceof VisualTransformableNode){
				if(node instanceof VisualCondition || node instanceof VisualTransitionNode){
					JOptionPane.showMessageDialog(null,
							"Selection containing ungroup component is invalid",superGroup, JOptionPane.WARNING_MESSAGE);
					selected.clear();
					return;
				}
				else if(node instanceof VisualSuperGroup){
					JOptionPane.showMessageDialog(null,
							"Selection containing super group is invalid",superGroup, JOptionPane.WARNING_MESSAGE);
					selected.clear();
					return;
				}
				else
					selected.add(node);
			}
		}

		if(selected.size() > 1) {
			VisualSuperGroup group = new VisualSuperGroup();
			getCurrentLevel().add(group);
			getCurrentLevel().reparent(selected, group);

			ArrayList<Node> connectionsToGroup = new ArrayList<Node>();
			for(VisualSONConnection connection : Hierarchy.getChildrenOfType(getCurrentLevel(), VisualSONConnection.class)) {
				if(Hierarchy.isDescendant(connection.getFirst(), group) &&
						Hierarchy.isDescendant(connection.getSecond(), group)) {
					connectionsToGroup.add(connection);
				}
			}
			getCurrentLevel().reparent(connectionsToGroup, group);

			if (group != null) {
				Point2D groupCenter = centralizeComponents(selected);
				group.setPosition(groupCenter);
				select(group);
			}

		}
	}*/

	//Block
	public void groupBlockSelection() {
		Collection<Node> selected = getBlockSelection();

		if (selected.size() > 1) {

			Block mathBlock = new Block();
			VisualBlock block = new VisualBlock(mathBlock);

			getCurrentLevel().add(block);
			getCurrentLevel().reparent(selected, block);

			VisualComponent visualContainer = (VisualComponent)Hierarchy.getNearestAncestor(getCurrentLevel(), VisualComponent.class);

			Container currentMathLevel;
			if(visualContainer==null)
				currentMathLevel = getMathModel().getRoot();
			else
				currentMathLevel = (Container)visualContainer.getReferencedComponent();
			currentMathLevel.add(mathBlock);

			ArrayList<Node> connectionsToGroup = new ArrayList<Node>();

			for(VisualConnection connection : Hierarchy.getChildrenOfType(getCurrentLevel(), VisualConnection.class)) {
				if(Hierarchy.isDescendant(connection.getFirst(), block) &&
						Hierarchy.isDescendant(connection.getSecond(), block)) {
					connectionsToGroup.add(connection);
				}
			}
			getCurrentLevel().reparent(connectionsToGroup, block);

			// reparenting for the math model nodes
			ArrayList<Node> selectedMath = new ArrayList<Node>();
			for (Node node:selected) {
				if (node instanceof VisualComponent) {
					selectedMath.add(((VisualComponent)node).getReferencedComponent());
				}
			}
			for (Node node:connectionsToGroup) {
				if (node instanceof VisualConnection) {
					selectedMath.add(((VisualConnection)node).getReferencedConnection());
				}
			}

			for (Node node: selectedMath) {
				Container parent = (Container)node.getParent();
				ArrayList<Node> re = new ArrayList<Node>();
				re.add(node);


				// reparenting at the level of the reference manager
				ReferenceManager refMan = getMathModel().getReferenceManager();
				if (refMan instanceof HierarchicalUniqueNameReferenceManager) {
					HierarchicalUniqueNameReferenceManager manager = (HierarchicalUniqueNameReferenceManager)refMan;
					manager.setNamespaceProvider(node, mathBlock);
				}
				parent.reparent(re, mathBlock);

			}


			// final touch on visual part
			if (block != null) {
				Point2D groupCenter = centralizeComponents(selected);
				block.setPosition(groupCenter);
				select(block);
			}
		}
	}


	private Collection<Node> getBlockSelection(){
		Collection<Node> result = new HashSet<Node>();
		RelationAlgorithm relationAlg = new RelationAlgorithm(net);
		int errorType = 0;

		for(Node node : getOrderedCurrentLevelSelection()){
			if((node instanceof VisualCondition) || (node instanceof VisualEvent)) {
				if(relationAlg.isFinal(((VisualComponent)node).getReferencedComponent())
						|| relationAlg.isInitial(((VisualComponent)node).getReferencedComponent()))
					errorType = 1;
				else
					result.add(node);
			}
			else if(node instanceof VisualComment){
				result.add(node);
			}
			else if(!(node instanceof VisualSONConnection))
				errorType = 2;
		}

		if(errorType==1){
			JOptionPane.showMessageDialog(null,
					"Block contianing initial or final node is invalid", block, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
		}


		if(errorType==2){
			JOptionPane.showMessageDialog(null,
					"Only condition and event can be set as a Block", block, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
			}

		for (VisualSONConnection connect : getVisualSONConnections()){
			if(connect.getReferencedSONConnection().getSemantics() == Semantics.PNLINE){
				if(result.contains(connect.getFirst()) && !result.contains(connect.getSecond())){
					if(connect.getSecond() instanceof VisualEvent)
						errorType = 3;
				}

				if(!result.contains(connect.getFirst()) && result.contains(connect.getSecond())){
					if(connect.getFirst() instanceof VisualEvent)
						errorType = 3;
				}
			}
			if(connect.getReferencedSONConnection().getSemantics() == Semantics.BHVLINE){
				if(result.contains(connect.getFirst()) || result.contains(connect.getSecond()))
					errorType =4;
			}
		}

		if(errorType==3){
			JOptionPane.showMessageDialog(null,
					"The inputs and outputs of a block must be conditions", block, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
			}

		if(errorType==4){
			JOptionPane.showMessageDialog(null,
					"Block cannot cross phases", block, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
			}

		if (result.size() == 1) {
			JOptionPane.showMessageDialog(null,
					"A single component cannot be set as a block",group, JOptionPane.WARNING_MESSAGE);
			result.removeAll(result);
			return result;
		}

		return result;
	}

	public Collection<VisualONGroup> getVisualONGroups()
	{
		return Hierarchy.getDescendantsOfType(getRoot(), VisualONGroup.class);
	}

	public Collection<VisualSuperGroup> getVisualSuperGroups()
	{
		return Hierarchy.getChildrenOfType(getRoot(), VisualSuperGroup.class);
	}

	public Collection<VisualComponent> getVisualComponent(){
		return Hierarchy.getDescendantsOfType(getRoot(), VisualComponent.class);
	}

	public Collection<VisualCondition> getVisualCondition(){
		return Hierarchy.getDescendantsOfType(getRoot(), VisualCondition.class);
	}

	public Collection<VisualPlaceNode> getVisualPlaceNode(){
		return Hierarchy.getDescendantsOfType(getRoot(), VisualPlaceNode.class);
	}

	public Collection<VisualEvent> getVisualEvent(){
		return Hierarchy.getDescendantsOfType(getRoot(), VisualEvent.class);
	}

	public Collection<VisualSONConnection> getVisualSONConnections()
	{
		return Hierarchy.getDescendantsOfType(getRoot(), VisualSONConnection.class);
	}

	public Collection<VisualBlock> getVisualBlocks()
	{
		return Hierarchy.getDescendantsOfType(getRoot(), VisualBlock.class);
	}

	public Collection<VisualPage> getVisualPages()
	{
		return Hierarchy.getDescendantsOfType(getRoot(), VisualPage.class);
	}

	public Collection<VisualSONConnection> getVisualConnections(VisualComponent node)
	{
		//input value
		ArrayList<VisualSONConnection> result = new ArrayList<VisualSONConnection>();
		for (VisualSONConnection con : this.getVisualSONConnections()){
			if (con.getFirst() == node)
				result.add(con);
			if (con.getSecond() == node)
				result.add(con);
		}
		return result;
	}

	public Collection<VisualSONConnection> getVisualConnections(VisualComponent first, VisualComponent second)
	{
		ArrayList<VisualSONConnection> result = new ArrayList<VisualSONConnection>();
		for (VisualSONConnection con : this.getVisualSONConnections()){
			if (con.getFirst() == first && con.getSecond() == second)
				result.add(con);
		}
		return result;
	}

	/**
	 * reconnect block interface to its bounding.
	 * have to use with captureMemento() & cancelMemento().
	 */
	public boolean connectToBlocks(){
		if(!beforeConToBlock())
			return false;

		for(VisualBlock vBlock : this.getVisualBlocks()){
			if(vBlock.getIsCollapsed()){
				 Collection<VisualComponent> components = vBlock.getComponents();
				 for(VisualSONConnection con : this.getVisualSONConnections()){
					 Node first = con.getFirst();
					 Node second = con.getSecond();
					 if(!components.contains(first) && components.contains(second)){
						if(first instanceof VisualPlaceNode){
							 //set input value
							String name = net.getNodeReference(((VisualEvent)second).getReferencedComponent());
							String type = "-"+con.getReferencedSONConnection().getSemantics();
							String value = "";
							if(((VisualPlaceNode)first).getInterface() == ""){
								value = "to-"+name+type;
							}else{
								value = ((VisualPlaceNode)first).getInterface()+";"+"to-"+name+type;
							}
							((VisualPlaceNode)first).setInterface(value);
							//((VisualCondition)first).setInterfaceGraphic(graphic);
							 //remove visual connection
							 Container parent = (Container)con.getParent();
							 parent.remove(con);
							 //remove math connection
							 SONConnection mathCon = con.getReferencedSONConnection();
							 Container mathParent = (Container)mathCon.getParent();
							 if(mathParent != null)
								 mathParent.remove(mathCon);
							 //create connection between first node and block
							 try {
								this.connect(first, vBlock, con.getReferencedSONConnection().getSemantics());
							} catch (InvalidConnectionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					 }
					 if(components.contains(first) && !components.contains(second)){
						if(second instanceof VisualPlaceNode){
							 //set output value
							String name = net.getNodeReference(((VisualEvent)first).getReferencedComponent());
							String type = "-"+con.getReferencedSONConnection().getSemantics();
							String value = "";
							if(((VisualPlaceNode)second).getInterface() == ""){
								value = "from-"+name+type;
							}else{
								value = ((VisualPlaceNode)second).getInterface()+";"+"from-"+name+type;
							}
							((VisualPlaceNode)second).setInterface(value);

							 //remove visual connection
							 Container parent = (Container)con.getParent();
							 parent.remove(con);
							 //remove math connection
							 SONConnection mathCon = con.getReferencedSONConnection();
							 Container mathParent = (Container)mathCon.getParent();
							 if(mathParent != null)
								 mathParent.remove(mathCon);
							 //create connection between first node and block
							 try {
								this.connect(vBlock, second, con.getReferencedSONConnection().getSemantics());
							} catch (InvalidConnectionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					 }
				 }
			}
		}
		return true;
	}

	private boolean beforeConToBlock(){
		Collection<String> errBlocks = new ArrayList<String>();
		for(VisualPlaceNode c : this.getVisualPlaceNode())
			c.setInterface("");

		boolean err = true;
		for(VisualBlock block : this.getVisualBlocks()){
			if(!net.getPreset(block.getReferencedComponent()).isEmpty() || !net.getPostset(block.getReferencedComponent()).isEmpty()){
				err = false;
				errBlocks.add(net.getName(block.getReferencedComponent())+" ");
				block.setForegroundColor(SONSettings.getRelationErrColor());
				}
		}
		if(!err){
			JOptionPane.showMessageDialog(null, "Connections from/to block bounding are not valid. Error may due to lost block information, " +
					"reconnect block components again)"+ errBlocks.toString(), blockConnection, JOptionPane.WARNING_MESSAGE);
		}
		return err;
	}

	/**
	 * reconnect from block bounding to its inside
	 */
	public void blockConnectionChecker(){
		ArrayList<String> compatibility = new ArrayList<String>();
		for(VisualPlaceNode p : getVisualPlaceNode()){
			if(p.getInterface() != ""){
				String[] infos = p.getInterface().trim().split(";");
				//interface information checking
				ArrayList<VisualSONConnection> connections = new ArrayList<VisualSONConnection>();
				for(VisualSONConnection con : this.getVisualSONConnections()){
					if(con.getFirst()==p && (con.getSecond() instanceof VisualBlock))
						connections.add(con);
					if(con.getSecond()==p && (con.getFirst() instanceof VisualBlock))
						connections.add(con);
				}
				if(connections.size() != infos.length)
					compatibility.add(net.getNodeReference(p.getReferencedComponent()));

				for(VisualSONConnection con :connections){
					//remove visual connection
					Container parent = (Container)con.getParent();
					SONConnection mathCon = con.getReferencedSONConnection();
					parent.remove(con);

					//remove math connection
					Container mathParent = (Container)mathCon.getParent();
					if(mathParent != null)
						mathParent.remove(mathCon);
				}

				for(String info : infos){
					String[] piece = info.trim().split("-");
					VisualEvent e = null;
					for(VisualEvent event : this.getVisualEvent()){
						if(net.getNodeReference(event.getReferencedComponent()).equals(piece[1]))
							e = event;
					}
					//c is an input
					if(piece[0].equals("to") && e!=null){
						try {
							if(piece[2].equals("PNLINE"))
								this.connect(p, e, Semantics.PNLINE);
							else if(piece[2].equals("SYNCLINE"))
								this.connect(p, e, Semantics.SYNCLINE);
							else if(piece[2].equals("ASYNLINE"))
								this.connect(p, e, Semantics.ASYNLINE);
							else if(piece[2].equals("BHVLINE"))
								this.connect(p, e, Semantics.BHVLINE);

						} catch (InvalidConnectionException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
						//c is an output
					}else if(piece[0].equals("from") && e!=null){
						try {
							if(piece[2].equals("PNLINE"))
								this.connect(e, p, Semantics.PNLINE);
							else if(piece[2].equals("SYNCLINE"))
								this.connect(e, p, Semantics.SYNCLINE);
							else if(piece[2].equals("ASYNLINE"))
								this.connect(e, p, Semantics.ASYNLINE);
							else if(piece[2].equals("BHVLINE"))
								this.connect(e, p, Semantics.BHVLINE);
						} catch (InvalidConnectionException ex) {
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
					}
				}
				p.setInterface("");
			}
		}
		if(!compatibility.isEmpty()){
			JOptionPane.showMessageDialog(null, "Incompatible connections. Error may due to lost block information, " +
					"reconnect block components again)"+ compatibility.toString(), blockConnection, JOptionPane.WARNING_MESSAGE);
		}
		beforeConToBlock();
	}
}
