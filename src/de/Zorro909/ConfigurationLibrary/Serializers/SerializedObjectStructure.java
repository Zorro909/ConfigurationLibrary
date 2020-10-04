package de.Zorro909.ConfigurationLibrary.Serializers;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.internal.Primitives;

public class SerializedObjectStructure {

    private HashMap<String, Object> serializedMap = new HashMap<>();
    private ArrayList<String> markedLists = new ArrayList<>();

    private Class<?> underlyingClass;

    public SerializedObjectStructure(Class underlyingClass) {
        this.underlyingClass = underlyingClass;
    }

    public void addType(String path, Class<?> type) {
        if (type == String.class || Primitives.isPrimitive(type)
                || Primitives.isWrapperType(type)) {
            serializedMap.put(path, type);
        } else {
            Serializer serializer = Serializers.getSerializer(type);
            if (serializer == null) {
                throw new RuntimeException(
                        "No Serializer found for Type '" + type.getName() + "'!");
            }
            serializedMap.put(path, serializer.getObjectStructure());
        }
    }

    public void addListType(String path, Class<?> type) {
        if (type == String.class || Primitives.isPrimitive(type)
                || Primitives.isWrapperType(type)) {
            serializedMap.put(path, type);
        } else {
            Serializer serializer = Serializers.getSerializer(type);
            if (serializer == null) {
                throw new RuntimeException(
                        "No Serializer found for Type '" + type.getName() + "'!");
            }
            serializedMap.put(path, serializer.getObjectStructure());
        }
        markedLists.add(path);
    }

    public Class<?> getUnderlyingClass() {
        return underlyingClass;
    }

    public HashMap<String, Object> getSerializedMap() {
        return serializedMap;
    }

    public Object getType(String path) {
        return serializedMap.get(path);
    }

    public boolean isObject(String path) {
        return serializedMap.get(path) instanceof SerializedObjectStructure;
    }

    public boolean isList(String path) {
        return markedLists.contains(path);
    }
}
