package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.PetriNetSettings;
import org.workcraft.plugins.petri.tools.SimulationTool;
import org.workcraft.plugins.stg.SignalTransition;

public class STGSimulationTool extends SimulationTool {
	private static Color inputsColor = Color.RED.darker();
	private static Color outputsColor = Color.BLUE.darker();
	private static Color internalsColor = Color.GREEN.darker();

	public STGSimulationTool() {
		super();
		createInterface();
	}

	@SuppressWarnings("serial")
	private void createInterface() {

		traceTable.setDefaultRenderer(Object.class,
			new TableCellRenderer() {
				JLabel label = new JLabel() {
					@Override
					public void paint( Graphics g ) {
						g.setColor( getBackground() );
						g.fillRect( 0, 0, getWidth() - 1, getHeight() - 1 );
						super.paint( g );
					}
				};

				boolean isActive(int row, int column) {
					if (column==0) {
						if (trace!=null&&branchTrace==null)
							return row==traceStep;
					} else {
						if (branchTrace!=null&&row>=traceStep&&row<traceStep+branchTrace.size()) {
							return (row-traceStep)==branchStep;
						}
					}

					return false;
				}

				@Override
				public Component getTableCellRendererComponent(JTable table,
						Object value, boolean isSelected, boolean hasFocus,
						int row, int column) {

					if (!(value instanceof String)) return null;

					label.setText((String)value);
					label.setForeground(Color.BLACK);

					Color fore = PetriNetSettings.getEnabledForegroundColor();
					Color back = PetriNetSettings.getEnabledBackgroundColor();

					Node n = net.getNodeByReference((String)value);
					if (n instanceof SignalTransition) {
						SignalTransition st = (SignalTransition)n;
						switch (st.getSignalType()) {
							case INPUT:    label.setForeground(inputsColor); break;
							case OUTPUT:   label.setForeground(outputsColor); break;
							case INTERNAL: label.setForeground(internalsColor); break;
						}
					}

					if (isActive(row, column)) {
						if (fore!=null&&back!=null) {
							label.setBackground(fore);
							label.setForeground(back);
						} else {
							label.setBackground(Color.YELLOW);
						}
					} else {
						label.setBackground(Color.WHITE);
					}

					return label;
				}

		});

	}

}
