package org.workcraft;

public abstract class ConversionTool extends PromotedTool implements MenuOrdering {

	@Override
	public String getSection() {
		return "!    Conversion"; // 4 spaces - positions 1st
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public Position getPosition() {
		return Position.TOP;
	}

}
