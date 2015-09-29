package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.util.ColorUtils;

@SuppressWarnings("serial")
public class Core extends HashSet<String> {
	private Color color = ColorUtils.getLabColor(0.99f, (float)Math.random(), (float)Math.random());

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

	@Override
	public String toString() {
		String result = "";
		boolean first = true;
		for (String s: this) {
			if ( !first ) {
				result += ", ";
			}
			result += s;
			first = false;
		}
		return "{" + result + "}";
	}

}
