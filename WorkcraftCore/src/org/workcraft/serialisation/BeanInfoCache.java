package org.workcraft.serialisation;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.HashMap;

public class BeanInfoCache {

    static final HashMap<Class<?>, BeanInfo> beanInfo = new HashMap<>();

    static BeanInfo getBeanInfo(Class<?> c) throws IntrospectionException {
        BeanInfo cached = beanInfo.get(c);
        if (cached != null) {
            return cached;
        }
        BeanInfo info = Introspector.getBeanInfo(c, c.getSuperclass());
        beanInfo.put(c, info);
        return info;
    }

}
