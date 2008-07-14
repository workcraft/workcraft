package org.workcraft.gui;

import java.io.File;

import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.utils.SubstanceConstants.TabContentPaneBorderKind;
import org.workcraft.framework.Framework;
import org.workcraft.gui.edit.graph.GraphEditorWindow;
import org.workcraft.gui.edit.text.TextEditorWindow;
import org.workcraft.gui.workspace.FileFilters;
import org.workcraft.gui.workspace.WorkspaceWindow;


public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private JDesktopPane jDesktopPane = null;

	Framework framework;
	WorkspaceWindow workspaceView;
	ConsoleWindow consoleView;
	MDIPane content;

	InternalWindow testDoc;

	private JMenuBar menuBar;

	public void createViews() {
		workspaceView  = new WorkspaceWindow(framework);
		framework.getWorkspace().addEventListener(workspaceView);
		workspaceView.setVisible(true);

		consoleView = new ConsoleWindow(framework);
		consoleView.setVisible(true);
	}

	public MainWindow(Framework framework) {
		super();
		this.framework = framework;
	}

	public void setLAF(String laf) {
		if (JOptionPane.showConfirmDialog(this, "Changing Look and Feel requires GUI restart.\n\n" +
				"This will not affect the workspace (i.e. open documents will stay open),\n" +
				"but the visual editor windows will be closed.\n\nProceed?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		if (laf == null)
			laf = UIManager.getSystemLookAndFeelClassName();
		framework.shutdownGUI();
		framework.setConfigVar("gui.lookandfeel", laf);
		framework.startGUI();
	}

	public ConsoleWindow getConsoleView() {
		return consoleView;
	}

	public void startup() {
		JDialog.setDefaultLookAndFeelDecorated(true);
		UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
		SwingUtilities.updateComponentTreeUI(MainWindow.this);

		String laf = framework.getConfigVar("gui.lookandfeel");
		if (laf == null)
			laf = UIManager.getCrossPlatformLookAndFeelClassName();
		LAF.setLAF(laf);

		content = new MDIPane();
		this.setContentPane(content);

		this.setTitle("Workcraft 2dev");

		boolean maximised = Boolean.parseBoolean(framework.getConfigVar("gui.main.maximised"));
		String w = framework.getConfigVar("gui.main.width");
		String h = framework.getConfigVar("gui.main.height");
		int width = (w==null)?800:Integer.parseInt(w);
		int height = (h==null)?600:Integer.parseInt(h);

		this.setSize(width, height);

		if (maximised) {
			this.setExtendedState(MAXIMIZED_BOTH);
		}

		menuBar = new MainMenu(this);
		this.setJMenuBar(menuBar);
		createViews();

		content.addFrame(consoleView);
		content.addFrame(workspaceView);

		InternalWindow doc1 = new GraphEditorWindow("Document 1");
		//InternalWindow doc2 = new WorkEditorWindow("Document 2");
		//InternalWindow doc3 = new TextEditorWindow("Document 3");



		doc1.setSize(500, 300);
		//doc2.setSize(500, 300);
		//doc3.setSize(500, 300);

		doc1.setLocation(10, 10);
		//doc2.setLocation(20, 20);
		//doc3.setLocation(30, 30);

		content.addFrame(doc1);
		//content.addFrame(doc2);
		//content.addFrame(doc3);

		doc1.setVisible(true);
		//doc2.setVisible(true);
		//doc3.setVisible(true);

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);



		consoleView.startup();
		workspaceView.startup();

		consoleView.captureStreams();

		this.setVisible(true);

	}

	public void shutdown() {
		framework.setConfigVar("gui.lookandfeel", LAF.getCurrentLAF());
		framework.setConfigVar("gui.main.maximised", Boolean.toString((getExtendedState() & JFrame.MAXIMIZED_BOTH)!=0) );
		framework.setConfigVar("gui.main.width", Integer.toString(getWidth()));
		framework.setConfigVar("gui.main.height", Integer.toString(getHeight()));

		consoleView.releaseStreams();
		consoleView.shutdown();
		workspaceView.shutdown();
		setVisible(false);
	}

	public void addToWorkspace() {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.addChoosableFileFilter(FileFilters.DOCUMENT_FILES);
		fc.setFileFilter(fc.getAcceptAllFileFilter());
		if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION) {
			for(File file : fc.getSelectedFiles()) {
				framework.getWorkspace().add(file.getPath());
			}
		}
	}

	public void saveWorkspaceAs() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(FileFilters.WORKSPACE_FILES);
		if(fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
			framework.getWorkspace().save(FileFilters.checkSaveExtension(fc.getSelectedFile().getPath(), FileFilters.WORKSPACE_EXTENSION));
		}
	}

}