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

    private DataFlavor[] flavors = {DOCUMENT_FLAVOR, DataFlavor.stringFlavor };
    private Document doc;

    public TransferableDocument(Document doc) {
        this.doc = doc;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.getRepresentationClass() == Document.class ||
                flavor == DataFlavor.stringFlavor;
    }
    public synchronized Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            if (flavor == DataFlavor.stringFlavor) {
                try {
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    tFactory.setAttribute("indent-number", new Integer(2));

                    Transformer transformer = tFactory.newTransformer();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(baos);


                    transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.transform(source, result);

                    return baos.toString("utf-8");
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
