package org.workcraft.plugins.cpog;

public enum LabelPositioning {
		TOP("Top", 0, -1),
		LEFT("Left", -1, 0),
		RIGHT("Right", 1, 0),
		BOTTOM("Bottom", 0, 1),
		CENTER("Center", 0, 0);

		public final String name;
		public final int dx, dy;

		private LabelPositioning(String name, int dx, int dy)
		{
			this.name = name;
			this.dx = dx;
			this.dy = dy;
		}
}
