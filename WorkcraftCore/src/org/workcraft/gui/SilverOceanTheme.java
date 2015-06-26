package org.workcraft.gui;

import java.awt.Font;
import java.awt.Toolkit;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.UIDefaults;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

public class SilverOceanTheme extends OceanTheme {

	private static final double FONT_SIZE = 11.0;
	private static final String FONT_NAME = "Verdana";

	public static void enable() {
		MetalLookAndFeel.setCurrentTheme(new SilverOceanTheme());
	}

	@Override
	public FontUIResource getControlTextFont() {
	    int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
	    int fontSizeInPixels = (int)Math.round(FONT_SIZE * screenDpi / 72.0);
		return new FontUIResource(FONT_NAME, Font.PLAIN, fontSizeInPixels);
	}

	@Override
	public FontUIResource getMenuTextFont() {
		return getControlTextFont();
	}

	@Override
	public FontUIResource getSubTextFont() {
		return getControlTextFont();
	}

	@Override
	public FontUIResource getSystemTextFont() {
		return getControlTextFont();
	}

	@Override
	public FontUIResource getUserTextFont() {
		return getControlTextFont();
	}

	@Override
	public FontUIResource getWindowTitleFont() {
		return new FontUIResource(getControlTextFont().deriveFont(Font.BOLD));
	}

	@Override
	protected ColorUIResource getSecondary1() {
		return new ColorUIResource(0x999999);
	}

	@Override
	protected ColorUIResource getSecondary2() {
		return  new ColorUIResource(0xcccccc);
	}

	@Override
	protected ColorUIResource getSecondary3() {
		return  new ColorUIResource(0xeeeeee);
	}

	@Override
	protected ColorUIResource getPrimary1() {
		return new ColorUIResource(0x999999);
	}

	@Override
	protected ColorUIResource getPrimary2() {
		return new ColorUIResource(0xbbccdd);
	}

	@Override
	protected ColorUIResource getPrimary3() {
		return new ColorUIResource(0xbbccdd);
	}

	@Override
	public void addCustomEntriesToTable(UIDefaults table) {
		super.addCustomEntriesToTable(table);

		List<Serializable> buttonGradient = Arrays.asList(1.0, 0.0, getSecondary3(), getSecondary2(), getSecondary2());
		BorderUIResource menuItemBorder = new BorderUIResource(BorderFactory.createEmptyBorder(3, 6, 3, 6));

		Object[] uiDefaults = {
				"Button.gradient", buttonGradient,
				"CheckBox.gradient", buttonGradient,
				"CheckBoxMenuItem.gradient", buttonGradient,
				"InternalFrame.activeTitleGradient", buttonGradient,
				"RadioButton.gradient", buttonGradient,
				"RadioButtonMenuItem.gradient", buttonGradient,
				"ScrollBar.gradient", buttonGradient,
				"Slider.focusGradient", buttonGradient,
				"Slider.gradient", buttonGradient,
				"ToggleButton.gradient", buttonGradient,

				"TabbedPane.selected", getPrimary2(),
				"TabbedPane.contentAreaColor", getPrimary2(),

//				"OptionPane.errorIcon", new IconUIResource(ImageLoader.loadIcon("ix")),
//				"OptionPane.informationIcon", new IconUIResource(ImageLoader.loadIcon("ii")),
//				"OptionPane.questionIcon", new IconUIResource(ImageLoader.loadIcon("iq")),
//				"OptionPane.warningIcon", new IconUIResource(ImageLoader.loadIcon("iw")),

				"Menu.border", new BorderUIResource(BorderFactory.createEmptyBorder(3, 2, 3, 2)),
				"MenuItem.border", menuItemBorder,
				"CheckBoxMenuItem.border", menuItemBorder,
				"RadioButtonMenuItem.border", menuItemBorder,

				"ComboBox.background", getWhite(),
//				"CheckBox.background", white,
//				"OptionPane.background", white,
//				"Panel.background", white,
//				"Slider.background", white,

//				"TextField.background", new ColorUIResource(0xf4f4f4),
				"TextField.border", BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(getPrimary1()), BorderFactory.createEmptyBorder(4, 4, 4, 4)),
		};
		table.putDefaults(uiDefaults);
	}

}
