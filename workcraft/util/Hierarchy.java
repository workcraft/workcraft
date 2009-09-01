package org.workcraft.util;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.dom.HierarchyNode;

import net.sf.jga.fn.UnaryFunctor;

public class Hierarchy {
	@SuppressWarnings("serial")
	public static <T> UnaryFunctor<HierarchyNode, Boolean> getTypeFilter(
			final Class<T> type) {
		return new UnaryFunctor<HierarchyNode, Boolean> (){
			public Boolean fn(HierarchyNode node) {
				if (type.isInstance(node))
					return true;
				else
					return false;
			}
		};
	}

	public static  Collection<HierarchyNode> fillterNodes (Collection<HierarchyNode> nodes, UnaryFunctor<HierarchyNode, Boolean> filter) {
		LinkedList<HierarchyNode> result = new LinkedList<HierarchyNode>();

		for (HierarchyNode node : nodes) {
			if (filter.fn(node))
				result.add(node);
		}

		return result;
	}


	public static <T extends HierarchyNode> Collection <T> filterNodesByType (Collection<HierarchyNode> nodes, final Class<T> type) {
		LinkedList<T> result = new LinkedList<T>();

		for (HierarchyNode node : nodes) {
			if (type.isInstance(node))
				result.add(type.cast(node));
		}
		return result;
	}
	public static HierarchyNode [] getPath(HierarchyNode node) {
		HierarchyNode n = node;
			int i = 0;
			while(n!=null)
			{
				i++;
				n = n.getParent();
			}
			HierarchyNode [] result = new HierarchyNode[i];

			n = node;
			while(n!=null)
			{
				result[--i] = n;
				n = n.getParent();
			}

			return result;
	}

	public static HierarchyNode getCommonParent(HierarchyNode node1, HierarchyNode node2) {
		HierarchyNode [] path1 = getPath(node1);
		HierarchyNode [] path2 = getPath(node2);
		int size = Math.min(path1.length, path2.length);
		HierarchyNode result = null;
		for(int i=0;i<size;i++)
			if(path1[i]==path2[i])
				result = path1[i];
			else
				break;
		return result;
	}

	public static boolean isDescendant(HierarchyNode descendant, HierarchyNode parent) {
		HierarchyNode node = descendant;
		while(node != parent)
		{
			if(node == null)
				return false;
			node = node.getParent();
		}
		return true;
	}

	@SuppressWarnings({ "unchecked", "serial" })
	public static <T> T getNearestAncestor(HierarchyNode node, final Class<T> type)
	{
		return (T)getNearestAncestor(node, new UnaryFunctor<HierarchyNode, Boolean>()
				{
					public Boolean fn(HierarchyNode node) {
						return type.isInstance(node);
					}
				});
	}

	public static HierarchyNode getNearestAncestor(HierarchyNode node, UnaryFunctor<HierarchyNode, Boolean> filter) {
		HierarchyNode parent = node;
		while(parent != null)
		{
			if(filter.fn(parent))
				return parent;
			parent = parent.getParent();
		}
		return null;
	}
}
