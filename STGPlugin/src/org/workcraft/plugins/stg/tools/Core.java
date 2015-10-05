package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.Trace;
import org.workcraft.util.MultiSet;

@SuppressWarnings("serial")
public class Core extends HashSet<String> {
	private Color color = Color.getHSBColor((float)Math.random(), 0.3f, 0.7f);
	private final Trace cut1;
	private final Trace cut2;

	public Core(Trace cut1, Trace cut2) {
		super();
		this.cut1 = cut1;
		this.cut2 = cut2;
		MultiSet<String> union = new MultiSet<>();
		MultiSet<String> intersection = new MultiSet<>();
		if (cut1 != null) {
			union.addAll(cut1);
			intersection.addAll(cut1);
		}
		if (cut2 != null) {
			union.addAll(cut2);
			intersection.retainAll(cut2);
		}
		union.removeAll(intersection);
		union.removeAll(intersection);
		addAll(union);
	}

	public Core(Collection<String> core) {
		super(core);
		this.cut1 = null;
		this.cut2 = null;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Trace getCut1() {
		return cut1;
	}

	public Trace getCut2() {
		return cut2;
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
