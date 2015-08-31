package org.workcraft.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

@SuppressWarnings("serial")
public class IntDocument extends PlainDocument {
	private int limit;

	public IntDocument(int limit) {
		super();
		this.limit = limit;
	}

	@Override
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		if (str != null) {
			String s =  str.replaceAll("\\D++", "");
			if (getLength() + s.length() <= limit) {
				super.insertString(offset, s, attr);
			}
		}
	}

}
