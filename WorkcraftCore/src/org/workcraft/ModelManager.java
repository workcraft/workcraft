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
    private Hashtable<UUID, Class<?>> uuid_model_map;
    private Hashtable<UUID, LinkedList<Class<?>>> uuid_component_list_map;
    private Hashtable<UUID, LinkedList<Class<?>>> uuid_tool_list_map;
    private LinkedList<Class<?>> model_list;
    private LinkedList<Tool> multi_tool_list;

    public ModelManager() {
        uuid_model_map = new Hashtable<UUID, Class<?>>();
        uuid_component_list_map = new Hashtable<UUID, LinkedList<Class<?>>>();
        uuid_tool_list_map = new Hashtable<UUID, LinkedList<Class<?>>>();
        model_list = new LinkedList<Class<?>>();
        multi_tool_list = new LinkedList<Tool>();
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Class<?>> getComponentsByModelUUID(UUID uuid) {
        LinkedList<Class<?>> lst = uuid_component_list_map.get(uuid);
        if (lst!=null)
            return (LinkedList<Class<?>>)lst.clone();
        else
            return null;
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Class<?>> getToolsByModelUUID(UUID uuid) {
        LinkedList<Class<?>> lst = uuid_tool_list_map.get(uuid);
        if (lst!=null)
            return (LinkedList<Class<?>>)lst.clone();
        else
            return null;
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Tool> getMultiModelTools() {
        return (LinkedList<Tool>)multi_tool_list.clone();
    }

    @SuppressWarnings("unchecked")
    public LinkedList<Class<?>> getModelList() {
        return (LinkedList<Class<?>>)model_list.clone();
    }

    public Class<?> getModelByUUID(UUID uuid) {
        return uuid_model_map.get(uuid);
    }

    public static boolean isValidModelClass(Class<?> cls) {
        boolean if_ok = AbstractModel.class.isAssignableFrom(cls);
        return if_ok;
    }

    public static boolean isValidToolClass(Class<?> cls) {
        boolean if_ok = false;
        return if_ok;
    }

    public  UUID getModelUUID(Class<?> model_class) {
        UUID uuid = null;
        if (!isValidModelClass(model_class))
            return null;
        try {
            uuid = (UUID)model_class.getField("_modeluuid").get(null);
        }
        catch (NoSuchFieldException e) {
            System.err.println("Model implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        catch (IllegalAccessException e) {
            System.err.println("Model implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        return uuid;
    }

    public  UUID getModelUUID(String modelClassName) {
        UUID uuid = null;
        Class<?> model_class;
        try {
            model_class = ClassLoader.getSystemClassLoader().loadClass(modelClassName);
        } catch (ClassNotFoundException e1) {
            return null;
        }

        if (!isValidModelClass(model_class))
            return null;

        try {
            uuid = (UUID)model_class.getField("_modeluuid").get(null);
        }
        catch (NoSuchFieldException e) {
            System.err.println("Model implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        catch (IllegalAccessException e) {
            System.err.println("Model implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        return uuid;
    }

    public static String getModelDisplayName(Class<?> model_class) {
        if (!isValidModelClass(model_class))
            return null;
        try {
            return (String)model_class.getField("_displayname").get(null);
        }
        catch (NoSuchFieldException e) {
            System.err.println("Model implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        catch (IllegalAccessException e) {
            System.err.println("Model implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        return null;
    }

    public static String getToolDisplayName(Class<?> tool_class) {
        if (!isValidToolClass(tool_class))
            return null;
        try {
            return (String)tool_class.getField("_displayname").get(null);
        }
        catch (NoSuchFieldException e) {
            System.err.println("Tool implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        catch (IllegalAccessException e) {
            System.err.println("Tool implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        return null;
    }

    public void addModel(Class<?> cls) {
        try {
            UUID uuid = (UUID)cls.getField("_modeluuid").get(null);
            String model_name = (String)cls.getField("_displayname").get(null);
            if (uuid_model_map.get(uuid)!=null) {
                System.err.println ("Duplicate model id ("+uuid.toString()+"), skipping");
                return;
            }
            model_list.add(cls);
            uuid_model_map.put(uuid, cls);
            System.out.println("\t"+model_name+"\t OK");
        }
        catch (NoSuchFieldException e) {
            System.err.println("Model implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        catch (IllegalAccessException e) {
            System.err.println("Model implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
    }

    public void addComponent (Class<?> cls) {
        try {
            UUID uuid = (UUID)cls.getField("_modeluuid").get(null);
            String component_name = (String)cls.getField("_displayname").get(null);

            if (uuid_model_map.get(uuid)==null) {
                System.err.println ("Component "+component_name+"(class "+cls.getName()+") refers to unknown model (id "+uuid.toString()+"), skipping");
                return;
            }

            LinkedList<Class<?>> list = uuid_component_list_map.get(uuid);

            if (list == null) {
                list = new LinkedList<Class<?>>();
                uuid_component_list_map.put(uuid, list);
            }

            list.add(cls);

            System.out.println("\t"+component_name+"\t OK");
        }
        catch (NoSuchFieldException e) {
            System.err.println("Component implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
        catch (IllegalAccessException e) {
            System.err.println("Component implementation class is improperly declared: static final String "+e.getMessage()+" is required");
        }
    }


}
