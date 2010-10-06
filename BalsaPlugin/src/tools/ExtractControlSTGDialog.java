package tools;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.balsa.io.BalsaExportConfig;
import org.workcraft.plugins.balsa.io.SynthesisSettings;
import org.workcraft.plugins.shared.gui.PresetManagerPanel;
import org.workcraft.plugins.shared.presets.Preset;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.plugins.shared.presets.SettingsToControlsMapper;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class ExtractControlSTGDialog extends JDialog {
	PresetManagerPanel<BalsaExportConfig> presetPanel;
	private JPanel buttonsPanel;
	private PresetManager<BalsaExportConfig> presetManager;
	private JButton runButton;
	private JButton cancelButton;
	private int modalResult;
	private JPanel content;
	private JScrollPane optionsPanel;
	private final Window owner;
	private JComboBox dummyContractionModeCombo;
	private JComboBox compositionModeCombo;
	private JComboBox synthesisToolCombo;
	private JComboBox protocolCombo;


	public ExtractControlSTGDialog(Window owner, PresetManager<BalsaExportConfig> presetManager) {
		super (owner, "Control STG extraction options", ModalityType.APPLICATION_MODAL);
		this.owner = owner;

		this.presetManager = presetManager;

		createPresetsPanel();
		createButtonsPanel();
		createOptionsPanel();

		double size[][] = new double[][] {
				{TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.FILL, buttonsPanel.getPreferredSize().height}
		};

		TableLayout layout = new TableLayout(size);
		layout.setHGap(3);
		layout.setVGap(3);

		content = new JPanel(layout);
		content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		content.add(presetPanel, "0 0");
		content.add(optionsPanel, "0 1");
		content.add(buttonsPanel, "0 2");

		setContentPane(content);

		presetPanel.selectFirst();

	}

	private void createOptionsPanel() {
		JPanel optionsPanelContent = new JPanel(new SimpleFlowLayout());

		optionsPanel = new JScrollPane(optionsPanelContent);
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
		optionsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		optionsPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		protocolCombo = new JComboBox();
		protocolCombo.setEditable(false);

		for (BalsaExportConfig.Protocol mode : BalsaExportConfig.Protocol.values())
			protocolCombo.addItem(mode);

		dummyContractionModeCombo = new JComboBox();
		dummyContractionModeCombo.setEditable(false);

		for (SynthesisSettings.DummyContractionMode mode : SynthesisSettings.DummyContractionMode.values())
			dummyContractionModeCombo.addItem(mode);

		compositionModeCombo = new JComboBox();
		compositionModeCombo.setEditable(false);

		for (BalsaExportConfig.CompositionMode mode : BalsaExportConfig.CompositionMode.values())
			compositionModeCombo.addItem(mode);

		synthesisToolCombo = new JComboBox();
		synthesisToolCombo.setEditable(false);

		for (SynthesisSettings.SynthesisTool mode : SynthesisSettings.SynthesisTool.values())
			synthesisToolCombo.addItem(mode);


		optionsPanelContent.add(GUI.createLabeledComponent(protocolCombo, "Protocol: "));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());
		optionsPanelContent.add(GUI.createLabeledComponent(dummyContractionModeCombo, "Dummy contraction mode: "));
		optionsPanelContent.add(GUI.createLabeledComponent(synthesisToolCombo, "Synthesis tool: "));
		optionsPanelContent.add(GUI.createLabeledComponent(compositionModeCombo, "STG composition mode: "));
	}

	private void createPresetsPanel() {

		ArrayList<Preset<BalsaExportConfig>> builtIn = new ArrayList<Preset<BalsaExportConfig>>();

		builtIn.add(new Preset<BalsaExportConfig> ("Default", BalsaExportConfig.DEFAULT, true));

		presetPanel = new PresetManagerPanel<BalsaExportConfig>(presetManager, builtIn, new SettingsToControlsMapper<BalsaExportConfig>() {
			@Override
			public void applySettingsToControls(BalsaExportConfig settings) {
				dummyContractionModeCombo.setSelectedItem(settings.getSynthesisSettings().getDummyContractionMode());
				compositionModeCombo.setSelectedItem(settings.getCompositionMode());
				synthesisToolCombo.setSelectedItem(settings.getSynthesisSettings().getSynthesisTool());
				protocolCombo.setSelectedItem(settings.getProtocol());
			}

			@Override
			public BalsaExportConfig getSettingsFromControls() {
				return ExtractControlSTGDialog.this.getSettingsFromControls();
			}
		}, owner);
	}

	private void createButtonsPanel() {
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		runButton = new JButton ("Run");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modalResult = 1;
				setVisible(false);
			}
		});

		cancelButton = new JButton ("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				modalResult = 0;
				setVisible(false);
			}
		});

		buttonsPanel.add(cancelButton);
		buttonsPanel.add(runButton);
	}

	public int getModalResult()	{
		return modalResult;
	}

	public BalsaExportConfig getSettingsFromControls()
	{
		SynthesisSettings synthesisSettings = new SynthesisSettings(
					(SynthesisSettings.SynthesisTool)synthesisToolCombo.getSelectedItem(),
					(SynthesisSettings.DummyContractionMode)dummyContractionModeCombo.getSelectedItem()
				);
		BalsaExportConfig.CompositionMode compMode = (BalsaExportConfig.CompositionMode)compositionModeCombo.getSelectedItem();
		BalsaExportConfig.Protocol protocol = (BalsaExportConfig.Protocol)protocolCombo.getSelectedItem();

		return new BalsaExportConfig(synthesisSettings, compMode, protocol);
	}
}
