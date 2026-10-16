package org.workcraft.verification;

import org.w3c.dom.Element;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Serialiser of cached verification records. The format is versioned, and unknown or missing optional
 * attributes and elements are ignored, so that it can be extended without invalidating stored records.
 * Note that an attribute name ending with "file" is treated as a file reference, and its value is stored relative
 * to the work file. Only the dependency file attribute is meant to have such a name.
 */
public class VerificationRecordSerialiser implements DataSerialiser<Map<String, VerificationRecord>> {

    private static final int VERSION = 1;

    private static final String RECORDS_ELEMENT = "records";
    private static final String RECORD_ELEMENT = "record";
    private static final String MESSAGE_ELEMENT = "message";
    private static final String EXCEPTIONS_ELEMENT = "exceptions";
    private static final String DEPENDENCY_ELEMENT = "dependency";

    private static final String VERSION_ATTRIBUTE = "version";
    private static final String GOAL_ATTRIBUTE = "goal";
    private static final String OUTCOME_ATTRIBUTE = "outcome";
    private static final String TOOL_ATTRIBUTE = "tool";
    private static final String TIMESTAMP_ATTRIBUTE = "timestamp";
    private static final String MODEL_HASH_ATTRIBUTE = "model-hash";
    private static final String EXCEPTIONS_HASH_ATTRIBUTE = "exceptions-hash";
    private static final String VALIDITY_HASH_ATTRIBUTE = "validity-hash";
    private static final String NAME_ATTRIBUTE = "name";
    // Attribute name must end with "file" to be recognised as a file reference when the work is saved or loaded,
    // so that the path is stored relative to the work file. It must also be the last attribute of the element.
    private static final String FILE_ATTRIBUTE = "reference-file";
    private static final String HASH_ATTRIBUTE = "hash";

    @Override
    public Map<String, VerificationRecord> fromXML(Element parent, Map<String, VerificationRecord> defaultData) {
        if (parent == null) {
            return defaultData == null ? Collections.emptyMap() : defaultData;
        }
        Map<String, VerificationRecord> result = new TreeMap<>();
        for (Element recordElement : XmlUtils.getChildElements(RECORDS_ELEMENT, parent).stream()
                .flatMap(recordsElement -> XmlUtils.getChildElements(RECORD_ELEMENT, recordsElement).stream())
                .toList()) {

            VerificationRecord record = readRecord(recordElement);
            if (record != null) {
                result.put(record.goalId(), record);
            }
        }
        return result;
    }

    private VerificationRecord readRecord(Element recordElement) {
        String goalId = XmlUtils.readTextAttribute(recordElement, GOAL_ATTRIBUTE, null);
        VerificationRecord.Outcome outcome = XmlUtils.readEnumAttribute(
                recordElement, OUTCOME_ATTRIBUTE, VerificationRecord.Outcome.class, null);

        if ((goalId == null) || (outcome == null)) {
            return null;
        }
        Map<String, VerificationRecord.Dependency> dependencies = new LinkedHashMap<>();
        for (Element dependencyElement : XmlUtils.getChildElements(DEPENDENCY_ELEMENT, recordElement)) {
            String name = XmlUtils.readTextAttribute(dependencyElement, NAME_ATTRIBUTE, null);
            String hash = XmlUtils.readTextAttribute(dependencyElement, HASH_ATTRIBUTE, null);
            String file = XmlUtils.readTextAttribute(dependencyElement, FILE_ATTRIBUTE, null);
            if ((name != null) && (hash != null)) {
                dependencies.put(name, new VerificationRecord.Dependency(file, hash));
            }
        }
        return new VerificationRecord(goalId, outcome,
                XmlUtils.readTextAttribute(recordElement, TOOL_ATTRIBUTE, ""),
                readText(recordElement, MESSAGE_ELEMENT),
                readText(recordElement, EXCEPTIONS_ELEMENT),
                XmlUtils.readTextAttribute(recordElement, MODEL_HASH_ATTRIBUTE, ""),
                XmlUtils.readTextAttribute(recordElement, EXCEPTIONS_HASH_ATTRIBUTE, ""),
                XmlUtils.readTextAttribute(recordElement, VALIDITY_HASH_ATTRIBUTE, ""),
                dependencies,
                XmlUtils.readTextAttribute(recordElement, TIMESTAMP_ATTRIBUTE, ""));
    }

    private String readText(Element parent, String elementName) {
        Element element = XmlUtils.getChildElement(elementName, parent);
        return element == null ? "" : element.getTextContent();
    }

    @Override
    public void toXML(Map<String, VerificationRecord> records, Element parent) {
        Element recordsElement = XmlUtils.createChildElement(RECORDS_ELEMENT, parent);
        recordsElement.setAttribute(VERSION_ATTRIBUTE, Integer.toString(VERSION));
        for (VerificationRecord record : new TreeMap<>(records).values()) {
            Element recordElement = XmlUtils.createChildElement(RECORD_ELEMENT, recordsElement);
            recordElement.setAttribute(GOAL_ATTRIBUTE, record.goalId());
            recordElement.setAttribute(OUTCOME_ATTRIBUTE, record.outcome().name());
            recordElement.setAttribute(TOOL_ATTRIBUTE, record.tool());
            recordElement.setAttribute(TIMESTAMP_ATTRIBUTE, record.timestamp());
            recordElement.setAttribute(MODEL_HASH_ATTRIBUTE, record.modelHash());
            recordElement.setAttribute(EXCEPTIONS_HASH_ATTRIBUTE, record.exceptionsHash());
            recordElement.setAttribute(VALIDITY_HASH_ATTRIBUTE, record.validityHash());
            XmlUtils.createChildElement(MESSAGE_ELEMENT, recordElement).setTextContent(record.message());
            XmlUtils.createChildElement(EXCEPTIONS_ELEMENT, recordElement).setTextContent(record.exceptions());
            record.dependencies().forEach((name, dependency) -> {
                Element dependencyElement = XmlUtils.createChildElement(DEPENDENCY_ELEMENT, recordElement);
                dependencyElement.setAttribute(NAME_ATTRIBUTE, name);
                dependencyElement.setAttribute(HASH_ATTRIBUTE, dependency.hash());
                if ((dependency.file() != null) && !dependency.file().isEmpty()) {
                    dependencyElement.setAttribute(FILE_ATTRIBUTE, dependency.file());
                }
            });
        }
    }

}
