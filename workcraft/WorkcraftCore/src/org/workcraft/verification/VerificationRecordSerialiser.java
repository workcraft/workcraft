package org.workcraft.verification;

import org.w3c.dom.Element;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Serialiser of cached verification records. Unknown or missing optional attributes and elements are ignored,
 * so that the format can be extended without invalidating stored records.
 * Note that an attribute name ending with "file" is treated as a file reference, and its value is stored relative
 * to the work file. Only the dependency file attribute is meant to have such a name.
 */
public class VerificationRecordSerialiser implements DataSerialiser<Map<String, VerificationRecord>> {

    private static final String RECORDS_ELEMENT = "records";
    private static final String RECORD_ELEMENT = "record";
    private static final String MESSAGE_ELEMENT = "message";
    private static final String EXCEPTIONS_ELEMENT = "exceptions";
    private static final String DEPENDENCY_ELEMENT = "dependency";

    private static final String GOAL_ATTRIBUTE = "goal";
    private static final String OUTCOME_ATTRIBUTE = "outcome";
    private static final String TIMESTAMP_ATTRIBUTE = "timestamp";
    private static final String BACKEND_TOOL_ATTRIBUTE = "backend-tool";
    private static final String WORKCRAFT_VERSION_ATTRIBUTE = "workcraft-version";
    private static final String OS_DESCRIPTION_ATTRIBUTE = "os-description";
    private static final String MODEL_HASH_ATTRIBUTE = "model-hash";
    private static final String REFS_ATTRIBUTE = "refs";
    private static final String HASH_ATTRIBUTE = "hash";

    // Attribute name must end with "file" to be recognised as a file reference when the work is saved or loaded,
    // so that the path is stored relative to the work file. It must also be the last attribute of the element,
    // which are sorted by name, hence it is named "work-file" to be stored after all other attributes.
    private static final String FILE_ATTRIBUTE = "work-file";

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
            String refs = XmlUtils.readTextAttribute(dependencyElement, REFS_ATTRIBUTE, "");
            String hash = XmlUtils.readTextAttribute(dependencyElement, HASH_ATTRIBUTE, null);
            String file = XmlUtils.readTextAttribute(dependencyElement, FILE_ATTRIBUTE, null);
            if (hash != null) {
                dependencies.put(refs, new VerificationRecord.Dependency(file, hash));
            }
        }
        return new VerificationRecord(goalId, outcome,
                XmlUtils.readTextAttribute(recordElement, TIMESTAMP_ATTRIBUTE, ""),
                XmlUtils.readTextAttribute(recordElement, BACKEND_TOOL_ATTRIBUTE, ""),
                XmlUtils.readTextAttribute(recordElement, WORKCRAFT_VERSION_ATTRIBUTE, ""),
                XmlUtils.readTextAttribute(recordElement, OS_DESCRIPTION_ATTRIBUTE, ""),
                readText(recordElement, MESSAGE_ELEMENT),
                readText(recordElement, EXCEPTIONS_ELEMENT),
                XmlUtils.readTextAttribute(recordElement, MODEL_HASH_ATTRIBUTE, ""),
                dependencies);
    }

    private String readText(Element parent, String elementName) {
        Element element = XmlUtils.getChildElement(elementName, parent);
        return element == null ? "" : element.getTextContent();
    }

    @Override
    public void toXML(Map<String, VerificationRecord> records, Element parent) {
        Element recordsElement = XmlUtils.createChildElement(RECORDS_ELEMENT, parent);
        for (VerificationRecord record : new TreeMap<>(records).values()) {
            Element recordElement = XmlUtils.createChildElement(RECORD_ELEMENT, recordsElement);
            recordElement.setAttribute(GOAL_ATTRIBUTE, record.goalId());
            recordElement.setAttribute(OUTCOME_ATTRIBUTE, record.outcome().name());
            recordElement.setAttribute(TIMESTAMP_ATTRIBUTE, record.timestamp());
            recordElement.setAttribute(BACKEND_TOOL_ATTRIBUTE, record.tool());
            recordElement.setAttribute(WORKCRAFT_VERSION_ATTRIBUTE, record.version());
            recordElement.setAttribute(OS_DESCRIPTION_ATTRIBUTE, record.os());
            recordElement.setAttribute(MODEL_HASH_ATTRIBUTE, record.modelHash());
            XmlUtils.createChildElement(MESSAGE_ELEMENT, recordElement).setTextContent(record.message());
            if (!record.exceptions().isEmpty()) {
                XmlUtils.createChildElement(EXCEPTIONS_ELEMENT, recordElement).setTextContent(record.exceptions());
            }
            record.dependencies().forEach((refs, dependency) -> {
                Element dependencyElement = XmlUtils.createChildElement(DEPENDENCY_ELEMENT, recordElement);
                dependencyElement.setAttribute(HASH_ATTRIBUTE, dependency.hash());
                dependencyElement.setAttribute(REFS_ATTRIBUTE, refs);
                if ((dependency.file() != null) && !dependency.file().isEmpty()) {
                    dependencyElement.setAttribute(FILE_ATTRIBUTE, dependency.file());
                }
            });
        }
    }

}
