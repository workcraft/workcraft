package org.workcraft.plugins.sdfs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.sdfs.decorations.CounterflowRegisterDecoration;

@Hotkey(KeyEvent.VK_Q)
@DisplayName ("Counterflow Register")
@SVGIcon("images/icons/svg/sdfs-counterflow_register.svg")
public class VisualCounterflowRegister extends VisualComponent {

	public VisualCounterflowRegister(CounterflowRegister register) {
		super(register);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration (this, "Forward enabled", "isForwardEnabled", "setForwardEnabled", boolean.class));
		addPropertyDeclaration(new PropertyDeclaration (this, "Backward enabled", "isBackwardEnabled", "setBackwardEnabled", boolean.class));
		addPropertyDeclaration(new PropertyDeclaration (this, "Or-marked", "isOrMarked", "setOrMarked", boolean.class));
		addPropertyDeclaration(new PropertyDeclaration (this, "And-marked", "isAndMarked", "setAndMarked", boolean.class));
	}

	public CounterflowRegister getReferencedCounterflowRegister() {
		return (CounterflowRegister)getReferencedComponent();
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		double w = size - strokeWidth;
		double h = size - strokeWidth;
		double w2 = w/2;
		double h2 = h/2;
		double dx = size / 5;
		double v2 = w2 - dx;
		double v = v2 + v2;
		double dt = (size - strokeWidth) / 8;
		float strokeWidth1 = (float)strokeWidth;
		float strokeWidth2 = strokeWidth1 / 2;

		Shape shape = new Rectangle2D.Double (-w2, -h2, w, h);
		Shape forwardShape = new Rectangle2D.Double (-v2, -h2, v, h2);
		Shape backwardShape = new Rectangle2D.Double (-v2, 0, v, h2);

		Shape andTokenShape = new Rectangle2D.Double (-dt, dt, 2 * dt, 2 * dt);
		Path2D orTokenShape = new Path2D.Double();
		orTokenShape.moveTo(0, -3 * dt);
		orTokenShape.lineTo(dt, -dt);
		orTokenShape.lineTo(-dt, -dt);
		orTokenShape.closePath();

		boolean forwardEnabled = isForwardEnabled();
		boolean forwardEnabledExcited = false;
		if (d instanceof CounterflowRegisterDecoration) {
			forwardEnabled = ((CounterflowRegisterDecoration)d).isForwardEnabled();
			forwardEnabledExcited = ((CounterflowRegisterDecoration)d).isForwardEnabledExcited();
		}

		boolean backwardEnabled = isBackwardEnabled();
		boolean backwardEnabledExcited = false;
		if (d instanceof CounterflowRegisterDecoration) {
			backwardEnabled = ((CounterflowRegisterDecoration)d).isBackwardEnabled();
			backwardEnabledExcited = ((CounterflowRegisterDecoration)d).isBackwardEnabledExcited();
		}

		boolean orMarked = isOrMarked();
		boolean orMarkedExcited = false;
		if (d instanceof CounterflowRegisterDecoration) {
			orMarked = ((CounterflowRegisterDecoration)d).isOrMarked();
			orMarkedExcited = ((CounterflowRegisterDecoration)d).isOrMarkedExcited();
		}

		boolean andMarked = isAndMarked();
		boolean andMarkedExcited = false;
		if (d instanceof CounterflowRegisterDecoration) {
			andMarked = ((CounterflowRegisterDecoration)d).isAndMarked();
			andMarkedExcited = ((CounterflowRegisterDecoration)d).isAndMarkedExcited();
		}

		Color defaultColor = Coloriser.colorise(getForegroundColor(), d.getColorisation());
		if (d instanceof CounterflowRegisterDecoration) {
			defaultColor = getForegroundColor();
		}

		g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		g.fill(shape);

		if (forwardEnabled) {
			g.setColor(Coloriser.colorise(SDFSVisualSettings.getEnabledRegisterColor(), d.getBackground()));
		} else {
			g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		}
		g.fill(forwardShape);

		if (backwardEnabled) {
			g.setColor(Coloriser.colorise(SDFSVisualSettings.getEnabledRegisterColor(), d.getBackground()));
		} else {
			g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		}
		g.fill(backwardShape);

		g.setStroke(new BasicStroke(strokeWidth2));
		g.setColor(defaultColor);
		if (!forwardEnabledExcited) {
			g.draw(forwardShape);
		}
		if (!backwardEnabledExcited) {
			g.draw(backwardShape);
		}
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		if (forwardEnabledExcited) {
			g.draw(forwardShape);
		}
		if (backwardEnabledExcited) {
			g.draw(backwardShape);
		}

		if (orMarked) {
			if (orMarkedExcited) {
				g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
			} else {
				g.setColor(defaultColor);
			}
			g.fill(orTokenShape);
		} else 	if (orMarkedExcited) {
			g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
			g.setStroke(new BasicStroke(strokeWidth2));
			g.draw(orTokenShape);
		}

		if (andMarked) {
			if (andMarkedExcited) {
				g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
			} else {
				g.setColor(defaultColor);
			}
			g.fill(andTokenShape);
		} else if (andMarkedExcited) {
			g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
			g.setStroke(new BasicStroke(strokeWidth2));
			g.draw(andTokenShape);
		}

		g.setColor(defaultColor);
		g.setStroke(new BasicStroke(strokeWidth1));
		g.draw(shape);

		drawLabelInLocalSpace(r);
	}

	public boolean isForwardEnabled() {
		return getReferencedCounterflowRegister().isForwardEnabled();
	}

	public void setForwardEnabled(boolean value) {
		getReferencedCounterflowRegister().setForwardEnabled(value);
	}

	public boolean isBackwardEnabled() {
		return getReferencedCounterflowRegister().isBackwardEnabled();
	}

	public void setBackwardEnabled(boolean value) {
		getReferencedCounterflowRegister().setBackwardEnabled(value);
	}

	public boolean isOrMarked() {
		return getReferencedCounterflowRegister().isOrMarked();
	}

	public void setOrMarked(boolean value) {
		getReferencedCounterflowRegister().setOrMarked(value);
	}

	public boolean isAndMarked() {
		return getReferencedCounterflowRegister().isAndMarked();
	}

	public void setAndMarked(boolean value) {
		getReferencedCounterflowRegister().setAndMarked(value);
	}

}
