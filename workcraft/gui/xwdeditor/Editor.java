package org.workcraft.gui.xwdeditor;
import java.awt.Component;
import java.util.List;

import javax.swing.JPanel;

import org.workcraft.framework.Document;
import org.workcraft.framework.exceptions.DocumentOpenFailedException;


public interface Editor {
	public void save();
	public void saveAs();
	public void open();
	public Document load(String path) throws DocumentOpenFailedException;
	public void setDocument(Document document);
	public List<Component> getSelection();
	public Document getDocument();
	public String getFileName();
	public String getLastDirectory();
	public Component getSimControls();
	public void refresh();
}