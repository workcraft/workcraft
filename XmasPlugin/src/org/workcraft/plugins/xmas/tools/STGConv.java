package org.workcraft.plugins.xmas.tools;

import java.awt.geom.AffineTransform;
import java.util.Collection;

import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class STGConv {
    private final VisualSTG stg;
    private static final double xScaling = 6;
    private static final double yScaling = 6;

    public STGConv(int sel) throws InvalidConnectionException {
        this.stg = new VisualSTG(new STG());
        if (sel == 0) genSTG0();
        else genSTG2();
    }

    static void setPosition(Movable node, double x, double y) {
        TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
    }

    private void createArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
        if (p != null && t != null) {
            stg.connect(p, t);
        }
    }

    private void createArc2(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
        if (p != null && t != null) {
            stg.connect(t, p);
        }
    }

    private void connections(VisualPlace p0, VisualPlace p1, VisualSignalTransition t0, VisualSignalTransition t1) throws InvalidConnectionException {

        createArc(p0, t0);
        createArc(p1, t1);
    }

    private void connections2(VisualPlace p0, VisualPlace p1, VisualSignalTransition t0, VisualSignalTransition t1) throws InvalidConnectionException {

        createArc(p0, t1);
        createArc(p1, t0);
    }

    private class STGSignal {
        private static final double xScaling = 6;
        private static final double yScaling = 6;
        public final String name = "";
        public final int x;
        public final int y;
        public final VisualPlace p0;
        public final VisualPlace p1;
        public final VisualSignalTransition t0;
        public final VisualSTG stg;

        STGSignal(String name, int x, int y, VisualSTG stg) throws InvalidConnectionException {

            SignalTransition.Type type = SignalTransition.Type.INTERNAL;
            this.p0 = stg.createPlace(name.concat("_I1"), null);
            this.p1 = stg.createPlace(name.concat("_I0"), null);
            this.t0 = stg.createSignalTransition(name.concat("_d1"), type, SignalTransition.Direction.MINUS, null);
            this.x = x;
            this.y = y;
            this.stg = stg;

            setPosition(p0, x + 4, y);
            setPosition(p1, x + 4, y + 2);
            setPosition(t0, x, y + 2);
            //p0.setLabel("label");
            createArc(p0, t0);
            createArc2(p1, t0);
            p1.getReferencedPlace().setTokens(1);
        }

        void setPosition(Movable node, double x, double y) {
            TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
        }

        private void createArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
            if (p != null && t != null) {
                stg.connect(p, t);
                //stg.connect(t, p);
            }
        }

        private void createArc2(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
            if (p != null && t != null) {
                stg.connect(t, p);
            }
        }
    }

    public class STGInv {
        private static final double xScaling = 6;
        private static final double yScaling = 6;
        public final String name = "";
        public final int x;
        public final int y;
        public final VisualSTG stg;
        public final VisualPlace p0;
        public final VisualPlace p1;
        public final VisualSignalTransition t0;
        public final VisualSignalTransition t1;

        public STGInv(String name, int x, int y, VisualSTG stg) throws InvalidConnectionException {

            SignalTransition.Type type = SignalTransition.Type.INTERNAL;
            this.p0 = stg.createPlace(name.concat("_I1"), null);
            this.p1 = stg.createPlace(name.concat("_I0"), null);
            this.t0 = stg.createSignalTransition(name.concat("_d1"), type, SignalTransition.Direction.PLUS, null);
            this.t1 = stg.createSignalTransition(name.concat("_d1"), type, SignalTransition.Direction.MINUS, null);
            this.x = x;
            this.y = y;
            this.stg = stg;

            setPosition(p0, x + 4, y);
            setPosition(p1, x + 4, y + 2);
            setPosition(t0, x, y);
            setPosition(t1, x, y + 2);
            createArc(p0, t1);
            createArc(p1, t0);
            createArc2(p0, t0);
            createArc2(p1, t1);
            p1.getReferencedPlace().setTokens(1);
        }

        void setPosition(Movable node, double x, double y) {
            TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
        }

        private void createArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
            if (p != null && t != null) {
                stg.connect(p, t);
                //stg.connect(t, p);
            }
        }

        private void createArc2(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
            if (p != null && t != null) {
                stg.connect(t, p);
            }
        }
    }

    public class STGAnd {
        private static final double xScaling = 6;
        private static final double yScaling = 6;
        public final String name = "";
        public final int x;
        public final int y;
        public final VisualPlace p0;
        public final VisualPlace p1;
        public final VisualSignalTransition t0;
        public final VisualSignalTransition t1;
        public final VisualSignalTransition t2;
        public final VisualSTG stg;

        public STGAnd(String name, int x, int y, VisualSTG stg) throws InvalidConnectionException {

            SignalTransition.Type type = SignalTransition.Type.INTERNAL;
            this.p0 = stg.createPlace(name.concat("_I1"), null);
            this.p1 = stg.createPlace(name.concat("_I0"), null);
            this.t0 = stg.createSignalTransition(name.concat("_d1"), type, SignalTransition.Direction.PLUS, null);
            this.t1 = stg.createSignalTransition(name.concat("_d1"), type, SignalTransition.Direction.MINUS, null);
            this.t2 = stg.createSignalTransition(name.concat("_d1"), type, SignalTransition.Direction.MINUS, null);
            this.x = x;
            this.y = y;
            this.stg = stg;

            setPosition(p0, x + 4, y);
            setPosition(p1, x + 4, y + 4);
            setPosition(t0, x, y);
            setPosition(t1, x, y + 2);
            setPosition(t2, x, y + 4);
            createArc(p0, t1);
            createArc(p0, t2);
            createArc(p1, t0);
            createArc2(p0, t0);
            createArc2(p1, t1);
            createArc2(p1, t2);
            p1.getReferencedPlace().setTokens(1);
        }

        void setPosition(Movable node, double x, double y) {
            TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
        }

        private void createArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
            if (p != null && t != null) {
                stg.connect(p, t);
                //stg.connect(t, p);
            }
        }

        private void createArc2(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
            if (p != null && t != null) {
                stg.connect(t, p);
            }
        }
    }

    public class STGOr {
        private static final double xScaling = 6;
        private static final double yScaling = 6;
        public final String name = "";
        public final int x;
        public final int y;
        public final VisualPlace p0;
        public final VisualPlace p1;
        public final VisualSignalTransition t0;
        public final VisualSignalTransition t1;
        public final VisualSignalTransition t2;
        public final VisualSTG stg;

        public STGOr(String name, int x, int y, VisualSTG stg) throws InvalidConnectionException {

            SignalTransition.Type type = SignalTransition.Type.INTERNAL;
            this.p0 = stg.createPlace(name.concat("_I1"), null);
            this.p1 = stg.createPlace(name.concat("_I0"), null);
            this.t0 = stg.createSignalTransition(name.concat("_d1"), type, SignalTransition.Direction.PLUS, null);
            this.t1 = stg.createSignalTransition(name.concat("_d1"), type, SignalTransition.Direction.PLUS, null);
            this.t2 = stg.createSignalTransition(name.concat("_d1"), type, SignalTransition.Direction.MINUS, null);
            this.x = x;
            this.y = y;
            this.stg = stg;

            setPosition(p0, x + 4, y);
            setPosition(p1, x + 4, y + 4);
            setPosition(t0, x, y);
            setPosition(t1, x, y + 2);
            setPosition(t2, x, y + 4);
            createArc(p0, t1);
            createArc(p0, t2);
            createArc(p1, t0);
            createArc2(p0, t0);
            createArc2(p1, t1);
            createArc2(p1, t2);
            p1.getReferencedPlace().setTokens(1);
        }

        void setPosition(Movable node, double x, double y) {
            TransformHelper.applyTransform(node, AffineTransform.getTranslateInstance(x, y));
        }

        private void createArc(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
            if (p != null && t != null) {
                stg.connect(p, t);
                //stg.connect(t, p);
            }
        }

        private void createArc2(VisualPlace p, VisualSignalTransition t) throws InvalidConnectionException {
            if (p != null && t != null) {
                stg.connect(t, p);
            }
        }
    }

    private class  GenSTG {

        public final STGInv i1;
        public final STGInv i3;
        public final STGAnd a3;

        GenSTG(String name, int x, int offset) throws InvalidConnectionException {

            Collection<VisualPlace> p = null;

            SignalTransition.Type type = SignalTransition.Type.INTERNAL;

            i1 = new STGInv(name + "i1", 15 + x, 0 + offset, stg);
            STGAnd a1 = new STGAnd(name + "a1", 30 + x, 0 + offset, stg);
            STGInv i2 = new STGInv(name + "i2", 45 + x, 0 + offset, stg);
            STGAnd a2 = new STGAnd(name + "a2", 60 + x, 0 + offset, stg);
            i3 = new STGInv(name + "i3", 75 + x, 0 + offset, stg);

            STGSignal s2 = new STGSignal(name + "s2", 0 + x, 10 + offset, stg);
            a3 = new STGAnd(name + "a3", 30 + x, 10 + offset, stg);
            STGInv i4 = new STGInv(name + "i4", 45 + x, 10 + offset, stg);
            STGAnd a4 = new STGAnd(name + "a4", 60 + x, 10 + offset, stg);
            STGInv i5 = new STGInv(name + "i5", 75 + x, 10 + offset, stg);

            connections(s2.p0, s2.p1, a1.t0, a1.t2);
            connections(s2.p0, s2.p1, a3.t0, a3.t2);

            connections(i1.p0, i1.p1, a1.t0, a1.t1);
            connections(i2.p0, i2.p1, a2.t0, a2.t1);
            connections(i4.p0, i4.p1, a4.t0, a4.t1);
            connections(i3.p0, i3.p1, a4.t0, a4.t2);
            connections(i5.p0, i5.p1, a2.t0, a2.t2);

            connections2(a1.p0, a1.p1, i2.t0, i2.t1);
            connections2(a2.p0, a2.p1, i3.t0, i3.t1);
            connections2(a3.p0, a3.p1, i4.t0, i4.t1);
            connections2(a4.p0, a4.p1, i5.t0, i5.t1);
        }

    }

    private void genSTG0() throws InvalidConnectionException {

        double x = 100;
        double y = 1;

        Collection<VisualPlace> p = null;

        SignalTransition.Type type = SignalTransition.Type.INTERNAL;

        STGSignal s1 = new STGSignal("s1", 0, 0, stg);
        STGOr o1 = new STGOr("o1", 15, 0, stg);
        STGInv i1 = new STGInv("i1", 30, 0, stg);
        STGAnd a1 = new STGAnd("a1", 45, 0, stg);

        connections(s1.p0, s1.p1, o1.t1, o1.t2);
        connections2(o1.p0, o1.p1, i1.t0, i1.t1);
        connections(i1.p0, i1.p1, a1.t0, a1.t1);

        GenSTG a = new GenSTG("a", 0, 15);
        GenSTG b = new GenSTG("b", 0, 40);
        GenSTG c = new GenSTG("c", 0, 65);
        GenSTG d = new GenSTG("d", 0, 90);

        connections(a.i3.p0, a.i3.p1, o1.t0, o1.t2);
        connections(c.i3.p0, c.i3.p1, a1.t0, a1.t2);
        connections(b.i3.p0, b.i3.p1, a.i1.t0, a.i1.t1);
        connections(d.i3.p0, d.i3.p1, c.i1.t0, c.i1.t1);

        STGSignal s2 = new STGSignal("s2", 100, 0, stg);
        STGOr o2 = new STGOr("o2", 115, 0, stg);
        STGInv i2 = new STGInv("i2", 130, 0, stg);
        STGAnd a2 = new STGAnd("a2", 145, 0, stg);

        connections(s2.p0, s2.p1, o2.t1, o2.t2);
        connections2(o2.p0, o2.p1, i2.t0, i2.t1);
        connections(i2.p0, i2.p1, a2.t0, a2.t1);

        GenSTG e = new GenSTG("e", 100, 15);
        GenSTG f = new GenSTG("f", 100, 40);
        GenSTG g = new GenSTG("g", 100, 65);
        GenSTG h = new GenSTG("h", 100, 90);

        connections(e.i3.p0, e.i3.p1, o2.t0, o2.t2);
        connections(f.i3.p0, f.i3.p1, a2.t0, a2.t2);
        connections(g.i3.p0, g.i3.p1, e.i1.t0, e.i1.t1);
        connections(h.i3.p0, h.i3.p1, g.i1.t0, g.i1.t1);

    }

    private void genSTG1() throws InvalidConnectionException {

        double x = 1;
        double y = 1;

        Collection<VisualPlace> p = null;

        SignalTransition.Type type = SignalTransition.Type.INTERNAL;

        STGSignal s1 = new STGSignal("s1", 0, 0, stg);
        STGInv i1 = new STGInv("i1", 15, 0, stg);
        STGInv i2 = new STGInv("i2", 30, 0, stg);
        STGSignal s2 = new STGSignal("s2", 30, 5, stg);
        STGAnd a1 = new STGAnd("a1", 45, 0, stg);
        STGAnd a2 = new STGAnd("a2", 60, 0, stg);
        STGInv i3 = new STGInv("i3", 75, 0, stg);
        STGInv i4 = new STGInv("i4", 90, 0, stg);

        STGInv i5 = new STGInv("i5", 15, 10, stg);
        STGInv i6 = new STGInv("i6", 30, 10, stg);
        STGAnd a3 = new STGAnd("a3", 45, 10, stg);
        STGAnd a4 = new STGAnd("a4", 60, 10, stg);
        STGSignal s3 = new STGSignal("s3", 60, 5, stg);
        STGAnd a5 = new STGAnd("a5", 75, 10, stg);

        connections2(s1.p0, s1.p1, i1.t0, i1.t1);
        connections2(s1.p0, s1.p1, i5.t0, i5.t1);
        connections2(s2.p0, s2.p1, a1.t0, a1.t2);
        connections2(s2.p0, s2.p1, a4.t0, a4.t2);
        connections2(s3.p0, s3.p1, a5.t0, a5.t2);

        connections(i1.p0, i1.p1, i2.t0, i2.t1);
        connections(i2.p0, i2.p1, a1.t0, a1.t1);
        connections(i2.p0, i2.p1, a2.t0, a2.t2);
        connections(i3.p0, i3.p1, i4.t0, i4.t1);
        connections(i5.p0, i5.p1, i6.t0, i6.t1);
        connections(i6.p0, i6.p1, a3.t0, a3.t1);

        connections(a1.p0, a1.p1, a2.t0, a2.t1);
        connections2(a2.p0, a2.p1, i3.t0, i3.t1);
        connections(a3.p0, a3.p1, a4.t0, a4.t1);
        connections(a4.p0, a4.p1, a5.t0, a5.t1);

        GenSTG a = new GenSTG("a", 0, 20);
        GenSTG b = new GenSTG("b", 0, 45);

        connections(b.i3.p0, b.i3.p1, a3.t0, a3.t2);
        connections(i6.p0, i6.p1, a.i1.t0, a.i1.t1);
        connections(i6.p0, i6.p1, a.a3.t0, a.a3.t1);
        connections(a.i3.p0, a.i3.p1, b.i1.t0, b.i1.t1);

    }

    private void genSTG2() throws InvalidConnectionException {

        double x = 1;
        double y = 1;

        Collection<VisualPlace> p = null;

        //STGSignal s1 = new STGSignal("s1", 0, 0, stg);
        STGSignal s2 = new STGSignal("s2", 0, 10, stg);
        STGAnd a1 = new STGAnd("a1", 15, 0, stg);
        STGOr o1 = new STGOr("o1", 30, 0, stg);
        STGOr o2 = new STGOr("o2", 45, 0, stg);
        STGAnd a2 = new STGAnd("a2", 60, 0, stg);
        STGInv i1 = new STGInv("i1", 75, 0, stg);
        STGInv i2 = new STGInv("i2", 90, 0, stg);
        STGInv i3 = new STGInv("i3", 105, 0, stg);
        STGInv i4 = new STGInv("i4", 120, 0, stg);
        STGInv i5 = new STGInv("i5", 135, 0, stg);

        //connections2(s1.p0, s1.p1, a1.t0, a1.t2);
        connections(s2.p0, s2.p1, o1.t0, o1.t2);
        connections(a1.p0, a1.p1, o1.t1, o1.t2);
        connections(o1.p0, o1.p1, o2.t0, o2.t1);
        connections(o2.p0, o2.p1, a2.t0, a2.t1);
        connections2(a2.p0, a2.p1, i1.t0, i1.t1);
        connections(i1.p0, i1.p1, i2.t0, i2.t1);
        connections(i2.p0, i2.p1, i3.t0, i3.t1);
        connections(i3.p0, i3.p1, i4.t0, i4.t1);
        connections(i4.p0, i4.p1, i5.t0, i5.t1);

        GenSTG a = new GenSTG("a", 0, 20);

        connections(i2.p0, i2.p1, a.i1.t0, a.i1.t1);
        connections(a.i3.p0, a.i3.p1, a1.t0, a1.t2);

        //STGSignal s3 = new STGSignal("s3", 0, 50, stg);
        STGSignal s4 = new STGSignal("s4", 0, 60, stg);
        STGAnd a3 = new STGAnd("a3", 15, 50, stg);
        STGOr o3 = new STGOr("o3", 30, 50, stg);
        STGOr o4 = new STGOr("o4", 45, 50, stg);
        STGAnd a4 = new STGAnd("a4", 60, 50, stg);
        STGInv i6 = new STGInv("i6", 75, 50, stg);
        STGInv i7 = new STGInv("i7", 90, 50, stg);
        STGInv i8 = new STGInv("i8", 105, 50, stg);
        STGInv i9 = new STGInv("i9", 120, 50, stg);
        STGInv i10 = new STGInv("i10", 135, 50, stg);

        //connections2(s3.p0, s3.p1, a3.t0, a3.t2);
        connections(s4.p0, s4.p1, o3.t0, o3.t2);
        connections(a3.p0, a3.p1, o3.t1, o3.t2);
        connections(o3.p0, o3.p1, o4.t0, o4.t1);
        connections(o4.p0, o4.p1, a4.t0, a4.t1);
        connections2(a4.p0, a4.p1, i8.t0, i8.t1);
        connections(i6.p0, i6.p1, i7.t0, i7.t1);
        connections(i7.p0, i7.p1, i8.t0, i8.t1);
        connections(i8.p0, i8.p1, i9.t0, i9.t1);
        connections(i9.p0, i9.p1, i10.t0, i10.t1);

        GenSTG b = new GenSTG("b", 0, 70);

        connections(i6.p0, i6.p1, b.i1.t0, b.i1.t1);
        connections(b.i3.p0, b.i3.p1, a3.t0, a3.t2);
    }

    public VisualSTG getSTG() {
        return stg;
    }

}
