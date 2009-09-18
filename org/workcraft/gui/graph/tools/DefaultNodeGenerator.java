package org.workcraft.gui.graph.tools;

import org.workcraft.NodeFactory;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.util.Annotations;

public class DefaultNodeGenerator extends AbstractNodeGenerator {

	private Class<?> cls;
	private Class<?> vcls;
	private String displayName;
	private int hk;

	public DefaultNodeGenerator (Class<?> cls) {
		this.cls = cls;
		this.vcls = Annotations.getVisualClass(cls);
		this.displayName = Annotations.getDisplayName(vcls);
		this.hk = Annotations.getHotKeyCode(vcls);
	}

	@Override
	protected MathNode createMathNode() throws NodeCreationException {
		return NodeFactory.createNode(cls);
	}

	@Override
	public int getHotKeyCode() {
		return hk;
	}

	@Override
	public String getLabel() {
		return displayName;
	}
}
