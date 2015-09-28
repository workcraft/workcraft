package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.util.CieColorUtils;

@SuppressWarnings("serial")
public class Core extends HashSet<String> {
	private Color color = CieColorUtils.getLabColor(0.99f, (float)Math.random(), (float)Math.random());

	public Core(Collection<String> s) {
		this(s, null);
	}

	public Core(Collection<String> s, Color color) {
		super(s);
		if (color != null) {
			this.color = color;
		}
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

}
