 package org.workcraft.plugins.son.elements;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.plugins.shared.CommonVisualSettings;

@DisplayName("ChannelPlace")
//@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/son-channel-place.svg")
public class VisualChannelPlace extends VisualPlaceNode{

	protected static double singleTokenSize = CommonVisualSettings.getBaseSize() / 1.9;
	protected double strokeWidth = CommonVisualSettings.getStrokeWidth()*2.0;
	protected double size = CommonVisualSettings.getBaseSize() * 1.2;

	public VisualChannelPlace(ChannelPlace cplace) {
		super(cplace);
	}

	@Override
	public double getSize(){
		return size;
	}

	@Override
	public double getStrokeWidth(){
		return strokeWidth;
	}

	@Override
	public double getSingleTokenSize(){
		return singleTokenSize;
	}
}
