package org.workcraft.plugins.son.elements;

import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;


@DisplayName("Condition")
@Hotkey(KeyEvent.VK_B)
@SVGIcon("images/icons/svg/place_empty.svg")
public class VisualCondition extends VisualPlaceNode{

	public VisualCondition(Condition refNode) {
		super(refNode);
	}

}
