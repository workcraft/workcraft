package org.workcraft.plugins.xmas.components;

import org.workcraft.dom.math.MathGroup;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;
import org.workcraft.utils.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;

public class XmasComponent extends MathGroup {

    private int gr = 0;
    private String gp = "0";

    public Collection<XmasContact> getContacts() {
        return Hierarchy.filterNodesByType(getChildren(), XmasContact.class);
    }

    public Collection<XmasContact> getInputs() {
        ArrayList<XmasContact> result = new ArrayList<>();
        for (XmasContact c : getContacts()) {
            if (c.getIOType() == IOType.INPUT) {
                result.add(c);
            }
        }
        return result;
    }

    public Collection<XmasContact> getOutputs() {
        ArrayList<XmasContact> result = new ArrayList<>();
        for (XmasContact c : getContacts()) {
            if (c.getIOType() == IOType.OUTPUT) {
                result.add(c);
            }
        }
        return result;
    }

    public void setGr(int gr) {
        this.gr = gr;
    }

    public int getGr() {
        return gr;
    }

    public void setGp(String gp) {
        this.gp = gp;
    }

    public String getGp() {
        return gp;
    }

}
