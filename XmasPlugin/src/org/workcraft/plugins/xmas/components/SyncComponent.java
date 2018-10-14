package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;

@IdentifierPrefix("sync")
@VisualClass(org.workcraft.plugins.xmas.components.VisualSyncComponent.class)
public class SyncComponent extends XmasComponent {

    public String gp1 = "0";
    public String gp2 = "0";
    public String typ = "a";

    public void setGp1(String gp1) {
        this.gp1 = gp1;
    }

    public String getGp1() {
        return gp1;
    }

    public void setGp2(String gp2) {
        this.gp2 = gp2;
    }

    public String getGp2() {
        return gp2;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getTyp() {
        return typ;
    }

}