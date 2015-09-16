package org.workcraft.plugins.xmas.components;

public class SlotState {
	public final boolean isFull;
	public final boolean isHead;
	public final boolean isTail;
	public final boolean isMemExcited;
	public final boolean isHeadExcited;
	public final boolean isTailExcited;

	public SlotState(boolean isFull, boolean isHead, boolean isTail,
			boolean isMemExcited, boolean isHeadExcited, boolean isTailExcited) {
		this.isFull = isFull;
		this.isHead = isHead;
		this.isTail = isTail;
		this.isMemExcited = isMemExcited;
		this.isHeadExcited = isHeadExcited;
		this.isTailExcited = isTailExcited;
	}
}