package org.workcraft.serialisation.xml;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathNode;
import org.workcraft.serialisation.References;

public class XMLDualDeserialiserState implements References {
	private final References references1;
	private final References references2;
	private final Set<String> conflicts;
	private final String suffix;

	public XMLDualDeserialiserState(References references1, References references2) {
		this.references1 = references1;
		this.references2 = references2;
		// calculate reference conflicts
		conflicts = new HashSet<String>();
		for (String ref: references1.getReferences()) {
			Object obj1 = references1.getObject(ref);
			if (obj1 instanceof MathNode && ((MathNode)obj1).requireReferenceConflictResolution()) {
				Object obj2 = references2.getObject(ref);
				if (obj1 != null && obj2 != null) {
					conflicts.add(ref);
				}
			}
		}
		// find a non-conflicting prefix for model2
		String suffixGood = null;
		for (int code = 0; suffixGood == null; ++code) {
			boolean pass = true;
			String suffixCandidate = codeToString(code);
			for (String ref: conflicts) {
				String tmp = ref + suffixCandidate;
				if (references1.getObject(tmp) != null || references2.getObject(tmp) != null) {
					pass = false;
					break;
				}
			}
			if (pass) {
				suffixGood = suffixCandidate;
			}
		}
		suffix = suffixGood;
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
			if (ref.endsWith(suffix)) {
				obj = references2.getObject(ref.substring(0, ref.length() - suffix.length()));
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
			return (ref1 != null ? ref1 : ref2 + suffix);
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
			result.add(ref + suffix);
		}
		return result;
	}
}
