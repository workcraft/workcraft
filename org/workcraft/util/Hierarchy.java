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
}
