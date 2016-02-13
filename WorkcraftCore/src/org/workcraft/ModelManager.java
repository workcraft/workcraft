/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

import org.workcraft.dom.AbstractModel;

public class ModelManager {
    private Hashtable<UUID, Class<?>> uuidModelMap;
    private Hashtable<UUID, LinkedList<Class<?>>> uuidComponentListMap;
    private Hashtable<UUID, LinkedList<Class<?>>> uuidToolListMap;
    private LinkedList<Class<?>> modelList;
    private LinkedList<Tool> multiToolList;

    public ModelManager() {
        uuidModelMap = new Hashtable<UUID, Class<?>>();
        uuidComponentListMap = new Hashtable<UUID, LinkedList<Class<?>>>();
        uuidToolListMap = new Hashtable<UUID, LinkedList<Class<?>>>();
        modelList = new LinkedList<Class<?>>();
        multiToolList = new LinkedList<Tool>();
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Class<?>> getComponentsByModelUUID(UUID uuid) {
        LinkedList<Class<?>> lst = uuidComponentListMap.get(uuid);
        if (lst != null)
            return (LinkedList<Class<?>>) lst.clone();
        else
            return null;
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Class<?>> getToolsByModelUUID(UUID uuid) {
        LinkedList<Class<?>> lst = uuidToolListMap.get(uuid);
        if (lst != null)
            return (LinkedList<Class<?>>) lst.clone();
        else
            return null;
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Tool> getMultiModelTools() {
        return (LinkedList<Tool>) multiToolList.clone();
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Class<?>> getModelList() {
        return (LinkedList<Class<?>>) modelList.clone();
    }

    public Class<?> getModelByUUID(UUID uuid) {
        return uuidModelMap.get(uuid);
    }

    public static boolean isValidModelClass(Class<?> cls) {
        boolean ok = AbstractModel.class.isAssignableFrom(cls);
        return ok;
    }

    public static boolean isValidToolClass(Class<?> cls) {
        boolean ok = false;
        return ok;
    }

    public  UUID getModelUUID(Class<?> modelClass) {
        UUID uuid = null;
        if (!isValidModelClass(modelClass))
            return null;
        try {
            uuid = (UUID) modelClass.getField("_modeluuid").get(null);
        } catch (NoSuchFieldException e) {
            System.err.println("Model implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        } catch (IllegalAccessException e) {
            System.err.println("Model implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        }
        return uuid;
    }

    public  UUID getModelUUID(String modelClassName) {
        UUID uuid = null;
        Class<?> modelClass;
        try {
            modelClass = ClassLoader.getSystemClassLoader().loadClass(modelClassName);
        } catch (ClassNotFoundException e1) {
            return null;
        }

        if (!isValidModelClass(modelClass))
            return null;

        try {
            uuid = (UUID) modelClass.getField("_modeluuid").get(null);
        } catch (NoSuchFieldException e) {
            System.err.println("Model implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        } catch (IllegalAccessException e) {
            System.err.println("Model implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        }
        return uuid;
    }

    public static String getModelDisplayName(Class<?> modelClass) {
        if (!isValidModelClass(modelClass))
            return null;
        try {
            return (String) modelClass.getField("_displayname").get(null);
        } catch (NoSuchFieldException e) {
            System.err.println("Model implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        } catch (IllegalAccessException e) {
            System.err.println("Model implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        }
        return null;
    }

    public static String getToolDisplayName(Class<?> toolClass) {
        if (!isValidToolClass(toolClass))
            return null;
        try {
            return (String) toolClass.getField("_displayname").get(null);
        } catch (NoSuchFieldException e) {
            System.err.println("Tool implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        } catch (IllegalAccessException e) {
            System.err.println("Tool implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        }
        return null;
    }

    public void addModel(Class<?> cls) {
        try {
            UUID uuid = (UUID) cls.getField("_modeluuid").get(null);
            String modelName = (String) cls.getField("_displayname").get(null);
            if (uuidModelMap.get(uuid) != null) {
                System.err.println("Duplicate model id (" + uuid.toString() + "), skipping");
                return;
            }
            modelList.add(cls);
            uuidModelMap.put(uuid, cls);
            System.out.println("\t" + modelName + "\t OK");
        } catch (NoSuchFieldException e) {
            System.err.println("Model implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        } catch (IllegalAccessException e) {
            System.err.println("Model implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        }
    }

    public void addComponent(Class<?> cls) {
        try {
            UUID uuid = (UUID) cls.getField("_modeluuid").get(null);
            String componentName = (String) cls.getField("_displayname").get(null);

            if (uuidModelMap.get(uuid) == null) {
                System.err.println("Component " + componentName + "(class " + cls.getName() + ") refers to unknown model (id " + uuid.toString() + "), skipping");
                return;
            }

            LinkedList<Class<?>> list = uuidComponentListMap.get(uuid);

            if (list == null) {
                list = new LinkedList<Class<?>>();
                uuidComponentListMap.put(uuid, list);
            }

            list.add(cls);

            System.out.println("\t" + componentName + "\t OK");
        } catch (NoSuchFieldException e) {
            System.err.println("Component implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        } catch (IllegalAccessException e) {
            System.err.println("Component implementation class is improperly declared: static final String " + e.getMessage() + " is required");
        }
    }

}
