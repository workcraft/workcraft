package org.workcraft.plugins.dfs.tools;

import java.util.LinkedHashSet;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.dfs.BinaryRegister;
import org.workcraft.plugins.dfs.MathDelayNode;
import org.workcraft.plugins.dfs.VisualBinaryRegister;
import org.workcraft.plugins.dfs.VisualDelayComponent;
import org.workcraft.plugins.dfs.VisualRegister;

public class Cycle implements Comparable<Cycle> {
	public final LinkedHashSet<VisualDelayComponent> components;
	public final int tokenCount;
	public final double totalDelay;
	public final double throughput;
	public final double minDelay;
	public final double maxDelay;
	private final String toString;

	public Cycle(LinkedHashSet<VisualDelayComponent> components, String toString) {
		this.components = components;
		this.tokenCount = getTokenCount();
		this.totalDelay = getTotalDelay();
		this.throughput = getThroughput();
		this.minDelay = getMinDelay();
		this.maxDelay = getMaxDelay();
		this.toString = toString;
	}

	private int getTokenCount() {
		Integer result = 0;
		boolean spreadTokenDetected = false;
		for (VisualComponent c: components) {
			if (c instanceof VisualRegister || c instanceof VisualBinaryRegister) {
				boolean hasToken = false;
				if (c instanceof VisualRegister) {
					hasToken = ((VisualRegister)c).getReferencedRegister().isMarked();
				}
				if (c instanceof VisualBinaryRegister) {
					BinaryRegister ref = ((VisualBinaryRegister)c).getReferencedBinaryRegister();
					hasToken = ref.isTrueMarked() || ref.isFalseMarked();
				}
				if (!hasToken) {
					spreadTokenDetected = false;
				} else {
					if (!spreadTokenDetected) {
						result++;
						spreadTokenDetected = true;
					}
				}
			}
		}
		return result;
	}

	private double getTotalDelay() {
		double result = 0.0;
		for (VisualComponent c: components) {
			if (c.getReferencedComponent() instanceof MathDelayNode) {
				result += ((MathDelayNode)c.getReferencedComponent()).getDelay();
			}
		}
		return result;
	}

	private double getMinDelay() {
		double result = 0.0;
		boolean first = true;
		for (VisualComponent c: components) {
			if (c.getReferencedComponent() instanceof MathDelayNode) {
				double delay = ((MathDelayNode)c.getReferencedComponent()).getDelay();
				if (first || delay < result) {
					result = delay;
					first = false;
				}
			}
		}
		return result;
	}

	private double getMaxDelay() {
		double result = 0.0;
		boolean first = true;
		for (VisualComponent c: components) {
			if (c.getReferencedComponent() instanceof MathDelayNode) {
				double delay = ((MathDelayNode)c.getReferencedComponent()).getDelay();
				if (first || delay > result) {
					result = delay;
					first = false;
				}
			}
		}
		return result;
	}

	private Double getThroughput() {
		double delay = getTotalDelay();
		if (delay == 0.0) {
			return -1.0;
		}
		return getTokenCount() / delay;
	}

	@Override
	public int compareTo(Cycle other) {
		double thisThroughput = this.getThroughput();
		double otherThroughput = other.getThroughput();
		if (thisThroughput > otherThroughput) {
			return 1;
		} else if (thisThroughput < otherThroughput) {
			return -1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return toString;
	}

}
