package org.workcraft.workspace;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.types.LinkedTwoWayMap;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.XmlUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Workspace {
    public static final String EXTERNAL_PATH = "!External";
    private boolean temporary = true;
    private boolean changed = false;
    private File workspaceFile;
    private final Map<Path<String>, File> mounts = new HashMap<>();
    private final Map<Path<String>, File> permanentMounts = new HashMap<>();
    private final LinkedTwoWayMap<Path<String>, WorkspaceEntry> openFiles = new LinkedTwoWayMap<>();
    private final List<WorkspaceListener> workspaceListeners = new ArrayList<>();

    public WorkspaceTree getTree() {
        return new WorkspaceTree(this);
    }

    private File getBaseDir() {
        return workspaceFile.getParentFile();
    }

    public Workspace() {
        try {
            File baseDir = File.createTempFile("workspace", "");
            baseDir.delete();
            if (!baseDir.mkdir()) {
                throw new RuntimeException("Could not create a temporary workspace directory.");
            }
            baseDir.deleteOnExit();
            this.workspaceFile = new File(baseDir, "workspace.works");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        addMount(Path.empty(), getBaseDir(), true);
    }

    public File getFile(Path<String> wsPath) {
        List<String> names = Path.getPath(wsPath);
        MountTree current = getHardMountsRoot();
        for (String name : names) {
            current = current.getSubtree(name);
        }
        return current.mountTo;
    }

    public File getFile(WorkspaceEntry we) {
        return getFile(getPath(we));
    }

    public Path<String> getPath(File file) {
        Entry<Path<String>, File> bestMount = null;
        Path<String> bestRel = null;
        for (Entry<Path<String>, File> e : mounts.entrySet()) {
            Path<String> relative = getRelative(e.getValue(), file);
            if ((relative != null) && ((bestRel == null) || (Path.getPath(relative).size() < Path.getPath(bestRel).size()))) {
                bestRel = relative;
                bestMount = e;
            }
        }
        if (bestMount == null) {
            return null;
        }
        return Path.combine(bestMount.getKey(), bestRel);
    }

    private Path<String> getRelative(File ancestor, File descendant) {
        ancestor = ancestor.getAbsoluteFile();
        descendant = descendant.getAbsoluteFile();

        List<String> strs = new ArrayList<>();
        while (descendant != null) {
            if (descendant.equals(ancestor)) {
                Path<String> result = Path.empty();
                for (int i = 0; i < strs.size(); i++) {
                    result = Path.append(result, strs.get(strs.size() - 1 - i));
                }
                return result;
            }
            strs.add(descendant.getName());
            descendant = descendant.getParentFile();
        }
        return null;
    }

    public void addMount(Path<String> path, File file, boolean temporary) {
        final Path<String> wsPath = getPath(file);
        if (wsPath != null) {
            throw new RuntimeException("Path already in the workspace: " + wsPath);
        }
        mounts.put(path, file.getAbsoluteFile());
        if (!temporary) {
            final Path<String> relative = getRelative(getBaseDir(), file);
            if (relative != null) {
                file = new File(relative.toString().replace("/", File.pathSeparator));
            }
            permanentMounts.put(path, file);
        }
        fireWorkspaceChanged();
    }

    public void fireWorkspaceChanged() {
        // TODO : categorise and route events
        for (WorkspaceListener listener : workspaceListeners) {
            listener.workspaceLoaded();
        }
    }

    public Path<String> createWorkPath(Path<String> dir, String desiredName) {
        if ((desiredName == null) || desiredName.isEmpty()) {
            desiredName = "Untitled";
        }
        int i = 1;
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
        Path<String> path = Path.append(dir, desiredName);
        while (!isFreePath(path)) {
            path = Path.append(dir, name + " " + i++ + (ext == null ? "" : "." + ext));
        }
        return path;
    }

    private boolean isFreePath(Path<String> path) {
        return !mounts.containsKey(path) && !openFiles.containsKey(path) && !getFile(path).exists();
    }

    public void addWork(Path<String> path, WorkspaceEntry we) {
        openFiles.put(path, we);
        fireEntryAdded(we);
    }

    public void removeWork(WorkspaceEntry we) {
        openFiles.removeValue(we);
        fireEntryRemoved(we);
    }

    public List<WorkspaceEntry> getWorks() {
        return new ArrayList<>(openFiles.values());
    }

    public void addListener(WorkspaceListener l) {
        workspaceListeners.add(l);
    }

    public boolean isChanged() {
        return changed;
    }

    public void load(File workspaceFile) throws DeserialisationException {
        clear();
        this.workspaceFile = workspaceFile;
        try {
            Document doc = XmlUtils.loadDocument(workspaceFile);
            Element xmlroot = doc.getDocumentElement();

            if (!"workcraft-workspace".equals(xmlroot.getNodeName())) {
                throw new DeserialisationException("not a Workcraft workspace file");
            }
            List<Element> mounts = XmlUtils.getChildElements("mount", xmlroot);
            for (Element mountElement : mounts) {
                final String mountPoint = mountElement.getAttribute("mountPoint");
                final String filePath = mountElement.getAttribute("filePath");
                File file = new File(filePath);
                if (!file.isAbsolute()) {
                    file = new File(getBaseDir(), file.getPath());
                }
                addMount(Path.fromString(mountPoint), file, false);
            }
            addMount(Path.empty(), getBaseDir(), true);
            setTemporary(false);
        } catch (SAXException | IOException e) {
            throw new DeserialisationException(e);
        }
        fireWorkspaceChanged();
    }

    public void clear() {
        if (!openFiles.isEmpty()) {
            throw new RuntimeException("Current Workspace has some open files. Must close them before loading.");
        }
        mounts.clear();
        permanentMounts.clear();
        fireWorkspaceChanged();
    }

    public void save() {
        writeWorkspaceFile(workspaceFile);
    }

    public void saveAs(File newFile) {
        File newBaseDir = newFile.getParentFile();
        if (!newBaseDir.exists()) {
            if (!newBaseDir.mkdirs()) {
                throw new RuntimeException("Cannot create directory " + newBaseDir.getAbsolutePath());
            }
        }
        if (!newBaseDir.isDirectory()) {
            throw new RuntimeException("Workspace must be saved to a directory, not a file.");
        }
        try {
            for (File file : FileUtils.getDirectoryFiles(getBaseDir())) {
                if (!file.getAbsoluteFile().equals(workspaceFile.getAbsoluteFile())) {
                    FileUtils.copyAll(file, newBaseDir);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeWorkspaceFile(newFile);
        setWorkspaceFile(newFile);
    }

    private void setWorkspaceFile(File file) {
        workspaceFile = file;
        Path<String> empty = Path.empty();
        mounts.remove(empty);
        addMount(empty, getBaseDir(), temporary);
    }

    private void writeWorkspaceFile(File file) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
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

        for (Entry<Path<String>, File> mountEntry : permanentMounts.entrySet()) {
            Element e = doc.createElement("mount");
            e.setAttribute("mountPoint", mountEntry.getKey().toString());
            e.setAttribute("filePath", mountEntry.getValue().getPath());
            root.appendChild(e);
        }

        try {
            XmlUtils.saveDocument(doc, file);
            changed = false;
            fireWorkspaceSaved();
            setTemporary(false);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void fireWorkspaceSaved() {
        changed = false;
        for (WorkspaceListener listener : workspaceListeners) {
            listener.workspaceSaved();
        }
    }

    public void fireEntryAdded(WorkspaceEntry we) {
        changed = true;
        for (WorkspaceListener listener : workspaceListeners) {
            listener.entryAdded(we);
        }
    }

    public void fireEntryRemoved(WorkspaceEntry we) {
        changed = true;
        for (WorkspaceListener listener : workspaceListeners) {
            listener.entryRemoved(we);
        }
    }

    public void fireEntryChanged(WorkspaceEntry we) {
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

    private MountTree getHardMountsRoot() {
        return new MountTree(getBaseDir(), mounts, Path.empty());
    }

    public MountTree getRoot() {
        final Map<Path<String>, File> allMounts = new HashMap<>(mounts);
        for (WorkspaceEntry we : new HashSet<>(openFiles.values())) {
            final File file = getFile(we.getWorkspacePath());
            if (!file.exists()) {
                Path<String> key = openFiles.getKey(we);
                if (key != null) {
                    allMounts.put(key, file);
                }
            }
        }
        return new MountTree(getBaseDir(), allMounts, Path.empty());
    }

    public Path<String> tempMountExternalFile(File file) {
        final Path<String> path = createWorkPath(Path.root(EXTERNAL_PATH), file.getName());
        addMount(path, file, true);
        return path;
    }

    private void moveEntryHelper(Path<String> from, Path<String> to) throws IOException {
        File fileFrom = getFile(from);
        File fileTo = getFile(to);
        if (fileFrom.exists()) {
            FileUtils.moveFile(fileFrom, fileTo);
        }
        moveEntry(from, to);
        File mountFrom = mounts.get(from);
        if (mountFrom != null) {
            mounts.remove(from);
            final File perm = permanentMounts.get(from);
            mounts.put(to, mountFrom);
            if (perm != null) {
                permanentMounts.remove(from);
                permanentMounts.put(to, perm);
            }
        }
    }

    public void moveEntry(Path<String> from, Path<String> to) throws IOException {
        final WorkspaceEntry openFileTo = openFiles.getValue(to);
        if (openFileTo != null) {
            final File toDelete = getFile(openFileTo);
            if (toDelete.exists() && !toDelete.delete()) {
                throw new IOException("Unable to delete '" + toDelete.getAbsolutePath() + "'");
            }
            final Path<String> newName = createWorkPath(to.getParent(), to.getNode());
            moveEntryHelper(to, newName);
        }
        String msg = "Work moved from " + from + " to " + to;
        final WorkspaceEntry openFileFrom = openFiles.getValue(from);
        if (openFileFrom == null) {
            msg += ".";
        } else {
            msg += " and the open file path is corrected.";
            openFiles.removeKey(from);
            openFiles.put(to, openFileFrom);
        }
        LogUtils.logInfo(msg);
        fireWorkspaceChanged();
    }

    public MountTree getMountTree(Path<String> path) {
        MountTree result = getRoot();
        for (String s : Path.getPath(path)) {
            result = result.getSubtree(s);
        }
        return result;
    }

    public WorkspaceEntry getWork(Path<String> path) {
        return openFiles.getValue(path);
    }

    public void deleteEntry(Path<String> path) {
        final File file = getFile(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    deleteEntry(getPath(f));
                }
                if (!file.delete()) {
                    DialogUtils.showError("Deletion failed");
                }
            } else {
                deleteFile(path);
            }
        }
    }

    private void deleteFile(Path<String> path) {
        final WorkspaceEntry openFile = getWork(path);
        if (openFile != null) {
            final Framework framework = Framework.getInstance();
            framework.getMainWindow().closeEditors(openFile);
        }
        openFiles.removeValue(openFile);
        final File file = getFile(path);
        if (file.exists() && !file.delete()) {
            DialogUtils.showError("Deletion failed");
        }
    }

    public File getWorkspaceFile() {
        return workspaceFile;
    }

    public Path<String> getPath(WorkspaceEntry entry) {
        return openFiles.getKey(entry);
    }

}
