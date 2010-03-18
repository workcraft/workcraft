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

package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.NodeHelper;

import net.sf.jga.fn.UnaryFunctor;

public class Hierarchy {
	@SuppressWarnings("serial")
	public static <T> UnaryFunctor<Node, Boolean> getTypeFilter(
			final Class<T> type) {
		return new UnaryFunctor<Node, Boolean> (){
			public Boolean fn(Node node) {
				if (type.isInstance(node))
					return true;
				else
					return false;
			}
		};
	}

	public static  Collection<Node> fillterNodes (Collection<Node> nodes, UnaryFunctor<Node, Boolean> filter) {
		LinkedList<Node> result = new LinkedList<Node>();

		for (Node node : nodes) {
			if (filter.fn(node))
				result.add(node);
		}

		return result;
	}


	public static <T extends Node> Collection <T> filterNodesByType (Collection<Node> nodes, final Class<T> type) {
		LinkedList<T> result = new LinkedList<T>();

		for (Node node : nodes) {
			if (type.isInstance(node))
				result.add(type.cast(node));
		}
		return result;
	}
	public static Node [] getPath(Node node) {
		Node n = node;
			int i = 0;
			while(n!=null)
			{
				i++;
				n = n.getParent();
			}
			Node [] result = new Node[i];

			n = node;
			while(n!=null)
			{
				result[--i] = n;
				n = n.getParent();
			}

			return result;
	}

	public static Node getCommonParent(Node node1, Node node2) {
		Node [] path1 = getPath(node1);
		Node [] path2 = getPath(node2);
		int size = Math.min(path1.length, path2.length);
		Node result = null;
		for(int i=0;i<size;i++)
			if(path1[i]==path2[i])
				result = path1[i];
			else
				break;
		return result;
	}

	public static boolean isDescendant(Node descendant, Node parent) {
		Node node = descendant;
		while(node != parent)
		{
			if(node == null)
				return false;
			node = node.getParent();
		}
		return true;
	}

	@SuppressWarnings({ "unchecked", "serial" })
	public static <T> T getNearestAncestor(Node node, final Class<T> type)
	{
		return (T)getNearestAncestor(node, new UnaryFunctor<Node, Boolean>()
				{
					public Boolean fn(Node node) {
						return type.isInstance(node);
					}
				});
	}

	public static Node getNearestAncestor(Node node, UnaryFunctor<Node, Boolean> filter) {
		Node parent = node;
		while(parent != null)
		{
			if(filter.fn(parent))
				return parent;
			parent = parent.getParent();
		}
		return null;
	}

	public static <T> Collection<T> getChildrenOfType(Node node, Class<T> type)
	{
		return NodeHelper.filterByType(node.getChildren(), type);
	}

	public static <T> Collection<T> getDescendantsOfType(Node node, Class<T> type)
	{
		ArrayList<T> result = new ArrayList<T>();
		result.addAll(getChildrenOfType(node, type));
		for(Node n : node.getChildren())
			result.addAll(getDescendantsOfType(n, type));
		return result;
	}

	public static <T> Collection<T> getDescendantsOfType(Node node, Class<T> type, Func<T, Boolean> filter)
	{
		ArrayList<T> result = new ArrayList<T>();

		for (T t : getChildrenOfType(node, type))
			if (filter.eval(t))
				result.add(t);

		for(Node n : node.getChildren())
			result.addAll(getDescendantsOfType(n, type, filter));

		return result;
	}

	public static Collection<Node> getDescendants (Node node)
	{
		ArrayList<Node> result = new ArrayList<Node>();
		result.addAll(node.getChildren());
		for(Node n : node.getChildren())
			result.addAll(getDescendants(n));
		return result;
	}

	public static Collection<Node> getDescendants (Node node, Func<Node, Boolean> filter)
	{
		ArrayList<Node> result = new ArrayList<Node>();
		for(Node n : node.getChildren()) {
			if (filter.eval(n))
				result.add(n);
			result.addAll(getDescendants(n));
		}
		return result;
	}
}
