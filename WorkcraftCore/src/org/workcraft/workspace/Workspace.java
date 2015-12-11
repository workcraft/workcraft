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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.workspace.Path;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LinkedTwoWayMap;
import org.workcraft.util.XmlUtil;
import org.xml.sax.SAXException;

public class Workspace {
	public static final String EXTERNAL_PATH = "!External";
	private LinkedTwoWayMap<Path<String>, WorkspaceEntry> openFiles = new LinkedTwoWayMap<Path<String>, WorkspaceEntry>();
	private List<WorkspaceListener> workspaceListeners = new ArrayList<WorkspaceListener>();

	private boolean temporary;

	private boolean changed = false;

	private File workspaceFile;
	private final Map<Path<String>, File> mounts;
	private Map<Path<String>, File> permanentMounts;

	private final DependencyManager dependencyManager = new DependencyManager();

	public WorkspaceTree getTree() {
		return new WorkspaceTree(this);
	}

	private File baseDir() {
		return workspaceFile.getParentFile();
	}

	public Workspace() {
		this.temporary = true;
		mounts = new HashMap<Path<String>, File>();
		permanentMounts = new HashMap<Path<String>, File>();

		try {
			File baseDir = File.createTempFile("workspace", "");
			baseDir.delete();
			if (!baseDir.mkdir())
				throw new RuntimeException ("Could not create a temporary workspace directory.");
			baseDir.deleteOnExit();
			this.workspaceFile = new File(baseDir, "workspace.works");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		addMount(Path.<String>empty(), baseDir(), true);
	}

	public WorkspaceEntry open(File file, boolean temporary) throws DeserialisationException {
		// Option 1: file does not exist
		if (!file.exists()) return null;

		// Option 2: file already loaded
		Path<String> workspacePath = getWorkspacePath(file);
		for(WorkspaceEntry we : openFiles.values()) {
			if(we.getWorkspacePath().equals(workspacePath))
				return we;
		}

		// Option 3: file needs loading (from *.work) or importing (other extensions)
		WorkspaceEntry we = new WorkspaceEntry(this);
		we.setTemporary(temporary);
		we.setChanged(false);
		final Framework framework = Framework.getInstance();
		if (file.getName().endsWith(FileFilters.DOCUMENT_EXTENSION)) {
			we.setModelEntry(framework.loadFile(file));
			if (workspacePath == null) {
				workspacePath = tempMountExternalFile(file);
			}
		} else {
			we.setModelEntry(framework.importFile(file));
			Path<String> parent;
			if(workspacePath == null) {
				parent = Path.empty();
			} else {
				parent = workspacePath.getParent();
			}
			workspacePath = newName(parent, FileUtils.getFileNameWithoutExtension(file));
		}
		openFiles.put(workspacePath, we);
		fireModelLoaded(we);
		return we;
	}

	public WorkspaceEntry merge(WorkspaceEntry we, File file) throws DeserialisationException {
		if (we != null && file.exists() && file.getName().endsWith(FileFilters.DOCUMENT_EXTENSION)) {
			final Framework framework = Framework.getInstance();
			we.insert(framework.loadFile(file));
		}
		return we;
	}

	public File getFile(Path<String> wsPath) {
		List<String> names = Path.getPath(wsPath);
		MountTree current = getHardMountsRoot();
		for(String name : names)
			current = current.getSubtree(name);
		return current.mountTo;
	}

	public File getFile(WorkspaceEntry we)	{
		return getFile(we.getWorkspacePath());
	}

	public Path<String> getWorkspacePath(File file) {
		Entry<Path<String>,File> bestMount = null;
		Path<String> bestRel = null;
		for(Entry<Path<String>,File> e : mounts.entrySet())
		{
			Path<String> relative = getRelative(e.getValue(), file);
			if(relative != null && (bestRel == null || Path.getPath(relative).size() < Path.getPath(bestRel).size()))
			{
				bestRel = relative;
				bestMount = e;
			}
		}
		if(bestMount == null)
			return null;
		return Path.combine(bestMount.getKey(), bestRel);
	}

	private Path<String> getRelative(File ancestor, File descendant) {
		ancestor = ancestor.getAbsoluteFile();
		descendant = descendant.getAbsoluteFile();

		List<String> strs = new ArrayList<String>();
		while(descendant != null)
		{
			if(descendant.equals(ancestor))
			{
				Path<String> result = Path.empty();
				for(int i=0;i<strs.size();i++)
					result = Path.append(result, strs.get(strs.size()-1-i));
				return result;
			}
			strs.add(descendant.getName());
			descendant = descendant.getParentFile();
		}
		return null;
	}

	public WorkspaceEntry add(Path<String> directory, String desiredName, ModelEntry modelEntry, boolean temporary, boolean open) {
		final Path<String> path = newName(directory, desiredName);
		WorkspaceEntry we = new WorkspaceEntry(this);
		we.setTemporary(temporary);
		we.setChanged(true);
		we.setModelEntry(modelEntry);
		openFiles.put(path, we);
		fireEntryAdded(we);
		if (open) {
			final Framework framework = Framework.getInstance();
			framework.getMainWindow().createEditorWindow(we);
		}
		return we;
	}

	public void add (String pathRelativeToWorkspace, File file, boolean temporary) {
		addMount(Path.fromString(pathRelativeToWorkspace), file, temporary);
	}

	public void addMount(Path<String> path, File file, boolean temporary)
	{
		final Path<String> wsPath = getWorkspacePath(file);
		if (wsPath != null) {
			throw new RuntimeException("Path already in the workspace: " + wsPath);
		}
		mounts.put(path, file.getAbsoluteFile());
		if (!temporary) {
			permanentMounts.put(path, tryMakeRelative(file, baseDir()));
		}
		fireWorkspaceChanged();
	}

	/*public void remove (String pathRelativeToWorkspace, boolean deleteFromDisk) {
		remove (Path.fromString(pathRelativeToWorkspace), deleteFromDisk);
	}

	public void remove (Path<String> path, boolean deleteFromDisk) {
		File file = getFile(path);
		if (file.isDirectory()) {

		}
	}*/

	private File tryMakeRelative(File file, File base) {
		final Path<String> relative = getRelative(base, file);
		if(relative == null) {
			return file;
		}
		return new File(relative.toString().replaceAll("/", File.pathSeparator));
	}

	public void fireWorkspaceChanged() {
		// TODO : categorize and route events
		for(WorkspaceListener listener : workspaceListeners) {
			listener.workspaceLoaded();
		}
	}

	private Path<String> newName(Path<String> dir, String desiredName) {
		if ((desiredName == null) || desiredName.isEmpty()) {
			desiredName = "Untitled";
		}
		int i=1;
		int dotIndex = desiredName.lastIndexOf(".");
		String name;
		String ext;
		if (dotIndex == -1) {
			name = desiredName;
			ext = null;
		} else {
			name = desiredName.substring(0, dotIndex);
			ext = desiredName.substring(dotIndex + 1);
		}
		Path<String> desiredPath = Path.append(dir, desiredName);
		while (pathTaken(desiredPath)) {
			desiredPath = Path.append(dir, name + " " + i++ + (ext==null?"":"."+ext));
		}
		return desiredPath;
	}


	private boolean pathTaken(Path<String> path) {
		return mounts.containsKey(path) || openFiles.containsKey(path) || getFile(path).exists();
	}

	public void close(WorkspaceEntry we) {
		openFiles.removeValue(we);
		fireEntryRemoved(we);
	}

	public List<WorkspaceEntry> getOpenFiles() {
		return new ArrayList<WorkspaceEntry>(openFiles.values());
	}

	public void addListener (WorkspaceListener l) {
		workspaceListeners.add(l);
	}

	public boolean isChanged() {
		return changed;
	}

	public void load(File workspaceFile) throws DeserialisationException {
		clear();

		this.workspaceFile = workspaceFile;

		try {
			Document doc = XmlUtil.loadDocument(workspaceFile.getPath());
			Element xmlroot = doc.getDocumentElement();

			if (xmlroot.getNodeName()!="workcraft-workspace")
				throw new DeserialisationException("not a Workcraft workspace file");

			List<Element> mounts = XmlUtil.getChildElements("mount", xmlroot);

			for (Element mountElement : mounts) {
				final String mountPoint = XmlUtil.readStringAttr(mountElement, "mountPoint");
				final String filePath = XmlUtil.readStringAttr(mountElement, "filePath");
				File file = new File(filePath);
				if(!file.isAbsolute()) {
					file = new File(baseDir(), file.getPath());
				}
				addMount(Path.fromString(mountPoint), file, false);
			}
			addMount(Path.<String>empty(), baseDir(), true);
			setTemporary (false);
		} catch (ParserConfigurationException e) {
			throw new DeserialisationException (e);
		} catch (SAXException e) {
			throw new DeserialisationException (e);
		} catch (IOException e) {
			throw new DeserialisationException (e);
		}
		fireWorkspaceChanged();
	}

	public void clear() {
		if (!openFiles.isEmpty()) {
			throw new RuntimeException("Current Workspace has some open files. Must close them before loading.");
		}
		mounts.clear();
		permanentMounts.clear();
	}

	public void save() {
		writeWorkspaceFile(workspaceFile);
	}

	public void saveAs(File newFile) {
		File newBaseDir = newFile.getParentFile();
		if (!newBaseDir.exists())
			if (!newBaseDir.mkdirs())
				throw new RuntimeException("Cannot create directory " + newBaseDir.getAbsolutePath());

		if (!newBaseDir.isDirectory())
			throw new RuntimeException("Workspace must be saved to a directory, not a file.");

		try {
			for(File f : baseDir().listFiles())
			{
				if(!f.getAbsoluteFile().equals(workspaceFile.getAbsoluteFile()))
					FileUtils.copyAll(f, newBaseDir);
			}
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		writeWorkspaceFile(newFile);
		setWorkspaceFile(newFile);
	}

	private void setWorkspaceFile(File newFile) {
		workspaceFile = newFile;
		mounts.remove(Path.<String>empty());
		addMount(Path.<String>empty(), baseDir(), temporary);
	}

	public void writeWorkspaceFile(File file) {
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

		for(Entry<Path<String>, File> mountEntry : permanentMounts.entrySet()) {
			Element e = doc.createElement("mount");
			e.setAttribute("mountPoint", mountEntry.getKey().toString());
			e.setAttribute("filePath", mountEntry.getValue().getPath());
			root.appendChild(e);
		}

		try {
			XmlUtil.saveDocument(doc, file);

			changed = false;
			fireWorkspaceSaved();

			setTemporary(false);
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	private void fireWorkspaceSaved() {
		changed = false;
		for (WorkspaceListener listener : workspaceListeners) {
			listener.workspaceSaved();
		}
	}

	void fireModelLoaded(WorkspaceEntry we) {
		for (WorkspaceListener listener : workspaceListeners) {
			listener.modelLoaded(we);
		}
	}

	void fireEntryAdded(WorkspaceEntry we) {
		changed = true;
		for (WorkspaceListener listener : workspaceListeners) {
			listener.entryAdded(we);
		}
	}

	void fireEntryRemoved(WorkspaceEntry we) {
		changed = true;
		for (WorkspaceListener listener : workspaceListeners) {
			listener.entryRemoved(we);
		}
	}

	void fireEntryChanged(WorkspaceEntry we) {
		changed = true;
		for (WorkspaceListener listener : workspaceListeners) {
			listener.entryChanged(we);
		}
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}


	public MountTree getHardMountsRoot() {
		return new MountTree(baseDir(), mounts, Path.<String>empty());
	}

	public MountTree getRoot() {
		Map<Path<String>, File> allMounts = new HashMap<Path<String>, File>(mounts);
		for(WorkspaceEntry we : openFiles.values())
		{
			final File file = getFile(we.getWorkspacePath());
			if(!file.exists())
				allMounts.put(openFiles.getKey(we), file);
		}
		return new MountTree(baseDir(), allMounts, Path.<String>empty());
	}

	public Path<String> tempMountExternalFile(File file) {
		final Path<String> path = newName(Path.root(EXTERNAL_PATH), file.getName());
		addMount(path, file, true);
		return path;
	}

	private void move(Path<String> from, Path<String> to) throws IOException {
		File fileFrom = getFile(from);
		File fileTo = getFile(to);
		if (fileFrom.exists()) {
			FileUtils.moveFile(fileFrom, fileTo);
		}
		moved(from, to);
		File mountFrom = mounts.get(from);
		if (mountFrom != null) {
			mounts.remove(from);
			final File perm = permanentMounts.get(from);
			mounts.put(to, mountFrom);
			if (perm!=null) {
				permanentMounts.remove(from);
				permanentMounts.put(to, perm);
			}
		}
	}

	public void moved(Path<String> from, Path<String> to) throws IOException {
		final WorkspaceEntry openFileFrom = openFiles.getValue(from);
		final WorkspaceEntry openFileTo = openFiles.getValue(to);
		if (openFileTo != null) {
			final Path<String> newName = newName(to.getParent(), to.getNode());
			final File toDelete = openFileTo.getFile();
			if (toDelete.exists() && !toDelete.delete()) {
				throw new IOException("Unable to delete '" + toDelete.getAbsolutePath() + "'");
			}
			move(to, newName);
		}

		System.out.println("moved from " + from + " to " + to);
		if (openFileFrom != null) {
			System.out.println("correcting open file path...");
			openFiles.removeKey(from);
			openFiles.put(to, openFileFrom);
		}
	}

	public MountTree getMountTree(Path<String> path) {
		MountTree result = getRoot();
		for(String s : Path.getPath(path)) {
			result = result.getSubtree(s);
		}
		return result;
	}


	public WorkspaceEntry getOpenFile(Path<String> path) {
		return openFiles.getValue(path);
	}

	private void deleteFile (Path<String> path) throws OperationCancelledException {
		final WorkspaceEntry openFile = getOpenFile(path);
		if (openFile != null) {
			final Framework framework = Framework.getInstance();
			framework.getMainWindow().closeEditors(openFile);
		}
		openFiles.removeValue(openFile);
		final File file = getFile(path);
		if (file.exists() && !file.delete()) {
			JOptionPane.showMessageDialog(null, "Deletion failed");
		}
	}

	public void delete(Path<String> path) throws OperationCancelledException {
		final File file = getFile(path);

		if (!file.exists())
			return;

		if (file.isDirectory())	{
			for (File f : file.listFiles()) {
				delete(getWorkspacePath(f));
			}
			if (!file.delete()) {
				JOptionPane.showMessageDialog(null, "Deletion failed");
			}
		} else {
			deleteFile(path);
		}
	}

	public File getWorkspaceFile() {
		return workspaceFile;
	}

	public Path<String> getPath(WorkspaceEntry entry) {
		return openFiles.getKey(entry);
	}

	public void createAssociation(Path<String> dependentFile, Path<String> masterFile) {
		dependencyManager.createAssociation(dependentFile, masterFile);
	}

	public List<Path<String>> getAssociatedFiles(Path<String> masterFile) {
		return dependencyManager.getAssociatedFiles(masterFile);
	}
}