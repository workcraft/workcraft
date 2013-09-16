package org.workcraft.serialisation.xml;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.serialisation.References;

public class XMLDualDeserialiserState implements References {
	private final References references1;
	private final References references2;
	private final Set<String> conflicts;
	private final String prefix;

	public XMLDualDeserialiserState(References references1, References references2) {
		this.references1 = references1;
		this.references2 = references2;
		// calculate reference conflicts
		conflicts = new HashSet<String>();
		for (String ref: references1.getReferences()) {
			if (references1.getObject(ref) != null && references2.getObject(ref) != null) {
				conflicts.add(ref);
			}
		}
		// find a non-conflicting prefix for model2
		String goodPrefix = null;
		for (int code = 0; goodPrefix == null; ++code) {
			boolean pass = true;
			String candidatePrefix = codeToString(code);
			for (String ref: conflicts) {
				String tmp = candidatePrefix + ref;
				if (references1.getObject(tmp) != null || references2.getObject(tmp) != null) {
					pass = false;
					break;
				}
			}
			if (pass) {
				goodPrefix = candidatePrefix;
			}
		}
		prefix = goodPrefix;
	}

	private static String codeToString(int code) {
		String result = "";
		do {
			result += (char)('a' + code % 26);
			code /= 26;
		} while (code > 0);
		return result;
	}

	@Override
	public Object getObject(String ref) {
		Object obj = null;
		if (conflicts.contains(ref)) {
			if (ref.startsWith(prefix)) {
				obj = references2.getObject(ref.substring(prefix.length()));
			} else {
				obj = references1.getObject(ref);
			}
		} else {
			obj = references2.getObject(ref);
			if (obj == null) {
				obj = references1.getObject(ref);
			}
		}
		return obj;
	}

	@Override
	public String getReference(Object obj) {
		String ref1 = references1.getReference(obj);
		String ref2 = references2.getReference(obj);
		if (conflicts.contains(ref1) || conflicts.contains(ref2)) {
			return (ref1 != null ? ref1 : prefix + ref2);
		} else {
			return (ref1 != null ? ref1 : ref2);
		}
	}

	@Override
	public Set<Object> getObjects() {
		Set<Object> result = references1.getObjects();
		result.addAll(references2.getObjects());
		return result;
	}

	@Override
	public Set<String> getReferences() {
		Set<String> result = references1.getReferences();
		result.addAll(references2.getReferences());
		for (String ref: conflicts) {
			result.add(prefix + ref);
		}
		return result;
	}
}
