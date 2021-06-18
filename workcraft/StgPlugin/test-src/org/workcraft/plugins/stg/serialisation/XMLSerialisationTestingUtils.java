package org.workcraft.plugins.stg.serialisation;

import org.workcraft.dom.math.MathGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.Initialiser;
import org.workcraft.plugins.LegacyPluginInfo;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.PluginProvider;
import org.workcraft.plugins.builtin.serialisation.xml.*;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.serialisation.XMLDeserialiser;
import org.workcraft.serialisation.XMLSerialiser;

import java.util.ArrayList;
import java.util.Collection;

class XMLSerialisationTestingUtils {

    static class MockPluginManager implements PluginProvider {

        @SuppressWarnings("unchecked")
        @Override
        public <T> Collection<PluginInfo<? extends T>> getPlugins(Class<T> interfaceType) {
            Initialiser<Object>[] legacy = getLegacyPlugins(interfaceType);
            ArrayList<PluginInfo<? extends T>> result = new ArrayList<>();
            for (Initialiser<Object> l : legacy) {
                result.add(new PluginInfo<T>((Initialiser<? extends T>) l, true));
            }
            return result;
        }

        public LegacyPluginInfo[] getLegacyPlugins(Class<?> interfaceType) {

            if (interfaceType.equals(XMLSerialiser.class)) {
                return new LegacyPluginInfo[] {
                        new LegacyPluginInfo(IntSerialiser.class),
                        new LegacyPluginInfo(BooleanSerialiser.class),
                        new LegacyPluginInfo(StringSerialiser.class),
                        new LegacyPluginInfo(DoubleSerialiser.class),
                        new LegacyPluginInfo(ConnectionSerialiser.class),
                        new LegacyPluginInfo(IntSerialiser.class),
                        new LegacyPluginInfo(EnumSerialiser.class),
                        new LegacyPluginInfo(AffineTransformSerialiser.class),
                        new LegacyPluginInfo(VisualConnectionSerialiser.class),
                        new LegacyPluginInfo(ImplicitPlaceArcSerialiser.class),
                };
            } else if (interfaceType.equals(XMLDeserialiser.class)) {
                return new LegacyPluginInfo[] {
                        new LegacyPluginInfo(IntDeserialiser.class),
                        new LegacyPluginInfo(BooleanDeserialiser.class),
                        new LegacyPluginInfo(StringDeserialiser.class),
                        new LegacyPluginInfo(DoubleDeserialiser.class),
                        new LegacyPluginInfo(ConnectionDeserialiser.class),
                        new LegacyPluginInfo(IntDeserialiser.class),
                        new LegacyPluginInfo(EnumDeserialiser.class),
                        new LegacyPluginInfo(AffineTransformDeserialiser.class),
                        new LegacyPluginInfo(VisualConnectionDeserialiser.class),
                        new LegacyPluginInfo(ImplicitPlaceArcDeserialiser.class),
                };
            } else {
                throw new RuntimeException("Mock plugin manager doesn't know interface " + interfaceType.getCanonicalName());
            }
        }

    }

    public static PluginProvider createMockPluginManager() {
        return new MockPluginManager();
    }

    public static Stg createTestSTG1() {
        try {
            Stg stg = new Stg();

            Place p1 = stg.createPlace();
            Place p2 = stg.createPlace();

            SignalTransition t1 = stg.createSignalTransition(null, null);
            SignalTransition t2 = stg.createSignalTransition(null, null);

            stg.connect(t1, p1);

            stg.connect(p1, t2);
            stg.connect(t2, p2);
            stg.connect(p2, t1);

            return stg;
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stg createTestSTG2() {
        try {
            Stg stg = new Stg();

            Place p1 = stg.createPlace();
            Place p2 = stg.createPlace();

            SignalTransition t1 = stg.createSignalTransition(null, null);
            SignalTransition t2 = stg.createSignalTransition(null, null);

            stg.connect(t1, p1);

            stg.connect(p1, t2);
            stg.connect(t2, p2);
            stg.connect(p2, t1);

            MathGroup g1 = new MathGroup();

            Place p3 = new Place();
            SignalTransition t3 = new SignalTransition();

            g1.add(p3);
            g1.add(t3);

            stg.connect(p3, t3);

            return stg;
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    public static VisualStg createTestSTG3() {
        try {
            Stg stg = new Stg();

            SignalTransition t1 = stg.createSignalTransition(null, null);
            SignalTransition t2 = stg.createSignalTransition(null, null);
            SignalTransition t3 = stg.createSignalTransition(null, null);
            SignalTransition t4 = stg.createSignalTransition(null, null);

            VisualStg visualSTG = new VisualStg(stg);

            VisualSignalTransition vt1 = new VisualSignalTransition(t1);
            VisualSignalTransition vt2 = new VisualSignalTransition(t2);
            VisualSignalTransition vt3 = new VisualSignalTransition(t3);
            VisualSignalTransition vt4 = new VisualSignalTransition(t4);

            visualSTG.add(vt1);
            visualSTG.add(vt2);
            visualSTG.add(vt3);
            visualSTG.add(vt4);

            visualSTG.connect(vt1, vt2);
            visualSTG.connect(vt2, vt3);
            visualSTG.connect(vt3, vt4);
            visualSTG.connect(vt4, vt1);

            return visualSTG;
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
    }

}
