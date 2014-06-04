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

package org.workcraft.plugins.xmas;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.plugins.xmas.components.XmasContact;
import org.workcraft.plugins.xmas.components.ForkComponent;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.JoinComponent;
import org.workcraft.plugins.xmas.components.MergeComponent;
import org.workcraft.plugins.xmas.components.QueueComponent;
import org.workcraft.plugins.xmas.components.SinkComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;
import org.workcraft.plugins.xmas.components.SwitchComponent;
import org.workcraft.plugins.xmas.components.XmasConnection;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class Xmas extends AbstractMathModel {

	public Xmas() {
		this(new MathGroup(), null);
	}

	public Xmas(Container root, References refs) {
		super(root, new HierarchicalUniqueNameReferenceManager((NamespaceProvider) root, refs, new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof SourceComponent)
					return "Src";
				if (arg instanceof FunctionComponent)
					return "Fun";
				if (arg instanceof QueueComponent)
					return "Qu";
				if (arg instanceof ForkComponent)
					return "Frk";
				if (arg instanceof JoinComponent)
					return "Jn";
				if (arg instanceof SwitchComponent)
					return "Sw";
				if (arg instanceof MergeComponent)
					return "Mrg";
				if (arg instanceof SinkComponent)
					return "Snk";
				if (arg instanceof XmasContact)
					return "Contact";
				if (arg instanceof XmasConnection)
					return "Con";
				return "node";
			}
		}));
	}

	public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
		MathConnection con = new MathConnection((MathNode)first, (MathNode)second);
		Hierarchy.getNearestContainer(first, second).add(con);
		return con;
	}

	@Override
	public Properties getProperties(Node node) {
		if (node != null) {
			return Properties.Mix.from(new NamePropertyDescriptor(this, node));
		}
		return null;
	}


	public Collection<Node> getNodes() {
        ArrayList<Node> result =  new ArrayList<Node>();
        for (Node node : Hierarchy.getDescendantsOfType(getRoot(), Node.class)){
            if (node instanceof SourceComponent)
                result.add(node);
            if (node instanceof FunctionComponent)
                result.add(node);
            if (node instanceof QueueComponent)
                result.add(node);
            if (node instanceof ForkComponent)
                result.add(node);
            if (node instanceof JoinComponent)
                result.add(node);
            if (node instanceof SwitchComponent)
                result.add(node);
            if (node instanceof MergeComponent)
                result.add(node);
            if (node instanceof SinkComponent)
                result.add(node);
        }
        return result;
	}

	public String getType(Node node) {
		String result = null;
		if(node instanceof SourceComponent) result = "source";
		if(node instanceof FunctionComponent) result = "function";
		if(node instanceof QueueComponent) result = "queue";
		if(node instanceof ForkComponent) result = "fork";
		if(node instanceof JoinComponent) result = "join";
		if(node instanceof SwitchComponent) result = "switch";
		if(node instanceof MergeComponent) result = "merge";
		if(node instanceof SwitchComponent) result = "switch";
		if(node instanceof SinkComponent) result = "sink";
		return result;
	}

	public Collection<SourceComponent> getSourceComponent(){
        return Hierarchy.getDescendantsOfType(getRoot(), SourceComponent.class);
    }

}
