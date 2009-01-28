package org.workcraft.dom.visual;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class TransferableDocument implements Transferable {
	public static final DataFlavor DOCUMENT_FLAVOR = new DataFlavor(Document.class, "XML");

	DataFlavor flavors[] = { DOCUMENT_FLAVOR, DataFlavor.stringFlavor };
	Document doc;

	public TransferableDocument(Document doc) {
		this.doc = doc;
	}

	public synchronized DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (
				flavor.getRepresentationClass() == Document.class ||
				flavor == DataFlavor.stringFlavor
		);
	}
	public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(flavor)) {
			if (flavor == DataFlavor.stringFlavor)
			{
				try
				{
					TransformerFactory tFactory = TransformerFactory.newInstance();
					Transformer transformer = tFactory.newTransformer();

					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					DOMSource source = new DOMSource(doc);
					StreamResult result = new StreamResult(baos);


					transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					transformer.transform(source, result);

					return (baos.toString("utf-8"));
				} catch (TransformerException e) {
					e.printStackTrace();
					return null;
				}
			} else
				return doc;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}
}