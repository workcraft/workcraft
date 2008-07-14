package org.workcraft.framework;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.workcraft.dom.AbstractGraphModel;
import org.workcraft.framework.exceptions.DocumentOpenFailedException;
import org.workcraft.util.XmlUtil;


public class Workspace {
	LinkedList<Document> entries = new LinkedList<Document>();
	LinkedList<AbstractGraphModel> openDocs = new LinkedList<AbstractGraphModel>();
	LinkedList<WorkspaceEventListener> eventListeners = new LinkedList<WorkspaceEventListener>();
	Framework framework;

	private String filePath = "";
	private boolean isChanged = false;

	/**
	 * Checks if any of the currently open documents were loaded from the same path as passed file.
	 * @param file file to get the path from
	 * @return true if an open document was loaded from the path, false otherwise
	 */

	public boolean isAlreadyOpen (File file) {
		for (AbstractGraphModel doc : openDocs ) {
			if (doc.getSourcePath() != null)
				if (doc.getSourcePath().equals(file.getPath()))
					return true;
		}
		return false;
	}

	void fireDocumentOpened(AbstractGraphModel doc) {
		for (WorkspaceEventListener listener : eventListeners)
			listener.documentOpened(doc);
	}

	void fireWorkspaceUpdated() {
		for (WorkspaceEventListener listener : eventListeners)
			listener.workspaceUpdated();
	}

	public Workspace(Framework framework) {
		this.framework = framework;
	}

	public Document add(String path) {
		for(Document we : entries) {
			if(we.getPath().equals(path))
				return we;
		}
		Document we = new Document(path);
		if(we.isWorkDocument()) {
//			TODO we = new WorkpaceDocumentEntry(path);
		}
		entries.add(we);
		isChanged = true;
		fireWorkspaceUpdated();
		return we;
	}

	public void remove(Document we) {
		entries.remove(we);
		isChanged = true;
		fireWorkspaceUpdated();
	}

	public List<Document> entries() {
		return Collections.unmodifiableList(entries);
	}

	/**
	 * Loads an .xwd document from file and adds it to the workspace.
	 * @param framework
	 * @param path
	 * @return
	 * @throws DocumentOpenFailedException
	 */
	public AbstractGraphModel openDocument(String path) throws DocumentOpenFailedException {
		return null;
	/*	File f = new File(path);

		if (!f.exists())
			throw new DocumentOpenFailedException("File \""+f.getPath()+"\" does not exist");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document xmldoc;
		Document doc;
		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			xmldoc = db.parse(new File(path));
		} catch (ParserConfigurationException e) {
			throw new DocumentOpenFailedException(e.getMessage());
		} catch (IOException e) {
			throw new DocumentOpenFailedException(e.getMessage());
		} catch (SAXException e) {
			throw new DocumentOpenFailedException(e.getMessage());
		}

		Element xmlroot = xmldoc.getDocumentElement();

		try {
			// TODO: proper validation
			if (xmlroot.getNodeName()!="workcraft")
				throw new DocumentOpenFailedException("Invalid root element");
			NodeList nl;

			nl = xmlroot.getElementsByTagName("document");
			Element d = (Element)nl.item(0);

			UUID model_uuid = UUID.fromString(d.getAttribute("model-uuid"));

			Class<?> model_class = framework.getModelManager().getModelByUUID(model_uuid);
			if (model_class == null)
				throw new DocumentOpenFailedException("Unrecognized model id - "+d.getAttribute("model-uuid"));

			if (!Document.class.isAssignableFrom(model_class))
				throw new DocumentOpenFailedException("Improper model class - not inherited from 'Document'");

			try {
				Constructor<?> ctor = model_class.getConstructor(new Class[] { Framework.class, Element.class });
			} catch (NoSuchMethodException e) {
				throw new DocumentOpenFailedException("Missing constructor (Framework, XmlElement)");
			}


			doc = (Document)model_class.newInstance();

			nl = xmldoc.getElementsByTagName("editable");
			Element re = (Element)nl.item(0);

			ComponentGroup root = new ComponentGroup();
			//doc.setRoot(root);

			if (re.getAttribute("class").equals(ComponentGroup.class.getName()))
				try {
					root.fromXml(re);
				} catch (DuplicateIdException e1) {
					e1.printStackTrace();
				}
				else
					System.err.println("Invalid file format: invalid root group element (id="+re.getAttribute("id")+"; class="+ GroupNode.class.getName() +")");

			nl = xmldoc.getElementsByTagName("editable-connection");
			for (int i=0; i<nl.getLength(); i++ ) {
				Element e = (Element)nl.item(i);

				EditableBase first = doc.getComponentById(Integer.parseInt(e.getAttribute("first")));
				EditableBase second = doc.getComponentById(Integer.parseInt(e.getAttribute("second")));

				if (first == null) {
					System.err.println ("Component "+e.getAttribute("first")+" not found while creating connections!");
				} else if (second == null) {
					System.err.println ("Component "+e.getAttribute("second")+" not found while creating connections!");
				} else
					try {
						EditableConnection con = doc.createConnection(first, second);
						con.fromXmlDom(e);
					} catch (InvalidConnectionException ex) {
						ex.printStackTrace();
					} catch (DuplicateIdException ex) {
						ex.printStackTrace();
					}
			}

			doc.setSourcePath(f.getPath());
			openDocs.add(doc);
			fireDocumentOpened(doc);

			return doc;
		} 	 catch (InstantiationException e2) {
			throw new DocumentOpenFailedException (e2.getMessage());
		} catch (IllegalAccessException e2) {
			throw new DocumentOpenFailedException (e2.getMessage());
		}/* catch (UnsupportedComponentException e) {
				e.printStackTrace();
				throw new DocumentOpenException (e.getMessage());
			}*/
	}

	public void closeDocument (AbstractGraphModel doc) {

	}

	public void addEventListener (WorkspaceEventListener evt) {
		eventListeners.add(evt);
	}

	public String getFilePath() {
		return filePath;
	}

	public String getTitle() {
		String title = (isChanged)?"*":"";
		if(filePath.isEmpty())
			return title+"(unnamed)";
		else {
			File f = new File(filePath);
			return title+f.getName();
		}
	}

	public boolean changed() {
		return isChanged;
	}

	public void save(String path) {
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

		Element root = doc.createElement("workcraft");
		doc.appendChild(root);
		root = doc.getDocumentElement();

		Element works = doc.createElement("workspace");
		root.appendChild(works);
		for(Document we : entries) {
			Element e = doc.createElement("entity");
			we.toXml(e);
			works.appendChild(e);
		}

		try {
			XmlUtil.saveDocument(doc, path);
			filePath = path;
			isChanged = false;
			fireWorkspaceUpdated();
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void save() {
		if(filePath.isEmpty()) {
			System.err.println("File name undefined.");
		}
		else
			save(filePath);
	}
}
