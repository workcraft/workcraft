/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.cpog;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DefaultCreateButtons;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.util.Hierarchy;

@DisplayName("Conditional Partial Order Graph")
@DefaultCreateButtons( { Vertex.class, Variable.class, RhoClause.class })
@CustomTools ( CustomToolsProvider.class )
public class VisualCPOG extends AbstractVisualModel
{
	private final class BooleanFormulaPropertyDescriptor implements
			PropertyDescriptor {
		private final Node node;

		private BooleanFormulaPropertyDescriptor(Node node)
		{
			this.node = node;
		}

		@Override
		public Map<Object, String> getChoice() {
			return null;
		}

		@Override
		public String getName()
		{
			if (node instanceof VisualRhoClause) return "Function";
			return "Condition";
		}

		@Override
		public Class<?> getType() {
			return String.class;
		}

		@Override
		public Object getValue() throws InvocationTargetException
		{
			if (node instanceof VisualRhoClause) return FormulaToString.toString(((VisualRhoClause)node).getFormula());
			if (node instanceof VisualVertex) return FormulaToString.toString(((VisualVertex)node).getCondition());
			return FormulaToString.toString(((VisualArc)node).getCondition());
		}

		@Override
		public boolean isWritable() {
			return true;
		}

		@Override
		public void setValue(Object value) throws InvocationTargetException {
			try
			{
				if (node instanceof VisualRhoClause) ((VisualRhoClause)node).setFormula(BooleanParser.parse((String)value, mathModel.getVariables()));
				else
				if (node instanceof VisualArc) ((VisualArc)node).setCondition(BooleanParser.parse((String)value, mathModel.getVariables()));
				else
				if (node instanceof VisualVertex) ((VisualVertex)node).setCondition(BooleanParser.parse((String)value, mathModel.getVariables()));
			} catch (ParseException e) {
				throw new InvocationTargetException(e);
			}
		}
	}

	private CPOG mathModel;

	public VisualCPOG(CPOG model) throws VisualModelInstantiationException
	{
		this(model, null);
	}

	public VisualCPOG(CPOG model, VisualGroup root)
	{
		super(model, root);

		this.mathModel = model;

		if (root == null)
		{
			try
			{
				createDefaultFlatStructure();
			}
			catch (NodeCreationException e)
			{
				throw new RuntimeException(e);
			}
		}

		new ConsistencyEnforcer(this).attach(getRoot());
	}

	@Override
	public void validateConnection(Node first, Node second) throws InvalidConnectionException
	{
		if (first == second) throw new InvalidConnectionException("Self loops are not allowed");

		if (first instanceof VisualVariable && !getPreset(first).isEmpty()) throw new InvalidConnectionException("Variables do not support multiple connections");
		if (second instanceof VisualVariable && !getPreset(second).isEmpty()) throw new InvalidConnectionException("Variables do not support multiple connections");

		if (first instanceof VisualVertex && second instanceof VisualVertex) return;
		if (first instanceof VisualVertex && second instanceof VisualVariable) return;
		if (first instanceof VisualVariable && second instanceof VisualVertex) return;

		throw new InvalidConnectionException("Invalid connection");
	}

	@Override
	public void connect(Node first, Node second) throws InvalidConnectionException
	{
		validateConnection(first, second);

		if (first instanceof VisualVertex && second instanceof VisualVertex)
		{
			VisualVertex v = (VisualVertex) first;
			VisualVertex u = (VisualVertex) second;

			Arc con = mathModel.connect(v.getMathVertex(), u.getMathVertex());
			Hierarchy.getNearestContainer(v, u).add(new VisualArc(con, v, u));
		}
		else
		{
			VisualVertex v;
			VisualVariable u;

			if (first instanceof VisualVertex)
			{
				v = (VisualVertex) first;
				u = (VisualVariable) second;
			}
			else
			{
				v = (VisualVertex) second;
				u = (VisualVariable) first;
			}

			DynamicVariableConnection con = mathModel.connect(v.getMathVertex(), u.getMathVariable());
			Hierarchy.getNearestContainer(v, u).add(new VisualDynamicVariableConnection(con, v, u));
		}
	}

	private Collection<Node> getGroupableSelection()
	{
		HashSet<Node> result = new HashSet<Node>();

		for(Node node : getOrderedCurrentLevelSelection())
			if(node instanceof VisualVertex || node instanceof VisualVariable)
				result.add(node);

		return result;
	}

	@Override
	public void groupSelection()
	{
		Collection<Node> selected = getGroupableSelection();
		if (selected.size() < 1) return;

		VisualGroup group = new VisualScenario();

		Container currentLevel = getCurrentLevel();

		currentLevel.add(group);

		currentLevel.reparent(selected, group);

		ArrayList<Node> connectionsToGroup = new ArrayList<Node>();

		for (VisualConnection connection : Hierarchy.getChildrenOfType(currentLevel, VisualConnection.class))
		{
			if (Hierarchy.isDescendant(connection.getFirst(), group) &&
				Hierarchy.isDescendant(connection.getSecond(), group))
			{
				connectionsToGroup.add(connection);
			}
		}

		currentLevel.reparent(connectionsToGroup, group);

		select(group);
	}

	public Collection<VisualScenario> getGroups()
	{
		return Hierarchy.getChildrenOfType(getRoot(), VisualScenario.class);
	}

	public Collection<VisualVariable> getVariables()
	{
		return Hierarchy.getChildrenOfType(getRoot(), VisualVariable.class);
	}

	@Override
	public Properties getProperties(Node node) {
		Properties properties = super.getProperties(node);

		if(node instanceof VisualRhoClause || node instanceof VisualVertex || node instanceof VisualArc)
			properties = Properties.Merge.add(properties, new BooleanFormulaPropertyDescriptor(node));

		return properties;
	}
}