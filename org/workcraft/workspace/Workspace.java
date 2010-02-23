/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.workspace;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.util.FileUtils;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class Workspace {
	private LinkedList<WorkspaceEntry> workspace = new LinkedList<WorkspaceEntry>();
	private LinkedList<WorkspaceListener> workspaceListeners = new LinkedList<WorkspaceListener>();

	Framework framework;

	private File baseDir;
	private boolean temporary;

	private boolean changed = false;
	private String filePath = null;
	private int entryIDCounter = 0;

	public Workspace(Framework framework) {
		this.framework = framework;
		this.temporary = true;

		try {
			baseDir = File.createTempFile("workspace", "");
			baseDir.delete();
			if (!baseDir.mkdir())
				throw new RuntimeException ("Could not create a temporary workspace directory.");
			baseDir.deleteOnExit();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Workspace(Framework framework, File f) throws DeserialisationException {
		baseDir = f.getParentFile();
		load(f.getAbsolutePath());
	}

	public WorkspaceEntry createFile(String relativePath, boolean temporary) {
		String fullPath = baseDir.getAbsolutePath() + File.separator + relativePath;
		File f = new File(fullPath);
		f.getParentFile().mkdirs();
		WorkspaceEntry we = new WorkspaceEntry(this, f);
		we.setTemporary(temporary);
		return we;
	}

	public WorkspaceEntry add(String path, boolean temporary) throws DeserialisationException {
		for(WorkspaceEntry we : workspace)
			if(we.getFile() != null && we.getFile().getPath().equals(path))
				return we;

		File f = new File(path);

		WorkspaceEntry we = null;

		if (f.exists()) {
			we = new WorkspaceEntry(this);
			we.setTemporary(temporary);
			we.setFile(f);
			if (f.getName().endsWith(".work")) {
				Model model = framework.load(f.getPath());
				we.setObject(model);
			}
			workspace.add(we);
			fireEntryAdded(we);
		}

		return we;
	}

	public WorkspaceEntry add(Model model, boolean temporary) {
		WorkspaceEntry we = new WorkspaceEntry(this);
		we.setTemporary(temporary);
		we.setObject(model);
		workspace.add(we);
		fireEntryAdded(we);
		return we;
	}

	public void remove(WorkspaceEntry we) {
		workspace.remove(we);
		fireEntryRemoved(we);
	}

	public List<WorkspaceEntry> entries() {
		return Collections.unmodifiableList(workspace);
	}

	public Model loadModel(WorkspaceEntry we) throws DeserialisationException {
		Model model = framework.load(we.getFile().getPath());
		fireModelLoaded(we);
		return model;
	}

	public void addListener (WorkspaceListener l) {
		workspaceListeners.add(l);
	}

	public String getFilePath() {
		return filePath;
	}

	public boolean isChanged() {
		return changed;
	}

	public void load(String path) throws DeserialisationException {
		workspace.clear();

		try {
			Document doc = XmlUtil.loadDocument(path);
			Element xmlroot = doc.getDocumentElement();

			if (xmlroot.getNodeName()!="workcraft-workspace")
				throw new DeserialisationException("not a Workcraft workspace file");


			List<Element> entries = XmlUtil.getChildElements("entry", xmlroot);

			for (Element entryElement : entries)
				add(XmlUtil.readStringAttr(entryElement, "path"), false);

			for (WorkspaceEntry we : workspace)
				fireEntryAdded(we);

			filePath = path;

			setTemporary (false);

		} catch (ParserConfigurationException e) {
			throw new DeserialisationException (e);
		} catch (SAXException e) {
			throw new DeserialisationException (e);
		} catch (IOException e) {
			throw new DeserialisationException (e);
		}
	}

	public void saveAs(String path) {
		File newBaseDir = new File(path);
		if (!newBaseDir.exists())
			if (!newBaseDir.mkdirs())
				throw new RuntimeException("Cannot create directory " + newBaseDir.getAbsolutePath());

		if (!newBaseDir.isDirectory())
			throw new RuntimeException("Workspace must be saved to a directory, not a file.");

		try {
			FileUtils.copyAll(baseDir, newBaseDir);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		baseDir = newBaseDir;

		writeWorkspaceFile(baseDir.getAbsolutePath() + File.separator + "workcraft.workspace");
	}

	public void writeWorkspaceFile(String path) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc;
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch (ParserConfigurationException e) {
			System.err.println(e.getMessage());
			return;
		}

		Element root = doc.createElement("workcraft-workspace");
		doc.appendChild(root);

		for(WorkspaceEntry we : workspace) {
			if (we.getFile() == null || we.isTemporary())
				continue;
			Element e = doc.createElement("entry");
			e.setAttribute("path", we.getFile().getPath());
			root.appendChild(e);
		}

		try {
			XmlUtil.saveDocument(doc, new File(path));
			filePath = path;
			changed = false;
			fireWorkspaceSaved();

			setTemporary(false);
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private void fireWorkspaceSaved() {
		changed = false;
		for (WorkspaceListener listener : workspaceListeners)
			listener.workspaceSaved();
	}

	void fireModelLoaded(WorkspaceEntry we) {
		for (WorkspaceListener listener : workspaceListeners)
			listener.modelLoaded(we);
	}

	void fireEntryAdded(WorkspaceEntry we) {
		changed = true;
		for (WorkspaceListener listener : workspaceListeners)
			listener.entryAdded(we);
	}

	void fireEntryRemoved(WorkspaceEntry we) {
		changed = true;
		for (WorkspaceListener listener : workspaceListeners)
			listener.entryRemoved(we);
	}

	void fireEntryChanged(WorkspaceEntry we) {
		changed = true;
		for (WorkspaceListener listener : workspaceListeners)
			listener.entryChanged(we);
	}

	public int getNextEntryID() {
		return entryIDCounter++;
	}

	public void clear() {
		changed = false;
		filePath = null;
		entryIDCounter = 0;

		for (WorkspaceEntry we : workspace)
			remove(we);
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

}