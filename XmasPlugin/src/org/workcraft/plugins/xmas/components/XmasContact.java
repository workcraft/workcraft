package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("Contact")
@IdentifierPrefix(value = "p", isInternal = true)
@VisualClass(VisualXmasContact.class)
public class XmasContact extends MathNode {

    public static final String PROPERTY_IO_TYPE = "I/O type";

    public enum IOType {
        INPUT("Input"),
        OUTPUT("Output");

        private final String name;

        IOType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    private IOType ioType = IOType.OUTPUT;

    public XmasContact() {
    }

    public XmasContact(IOType ioType) {
        super();
        setIOType(ioType);
    }

    public void setIOType(IOType value) {
        if (ioType != value) {
            ioType = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_IO_TYPE));
        }
    }

    public IOType getIOType() {
        return ioType;
    }

    public boolean isInput() {
        return getIOType() == IOType.INPUT;
    }

    public boolean isOutput() {
        return getIOType() == IOType.OUTPUT;
    }

}
