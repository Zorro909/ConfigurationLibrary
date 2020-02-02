package de.Zorro909.ConfigurationLibrary.Serializers;

import java.util.HashMap;

public class Serializers {

    private static HashMap<Class, Serializer> serializers = new HashMap<Class, Serializer>();

    public static <T> void registerSerializer(Class<T> type, Serializer<T> serializer) {
        serializers.put(type, serializer);
    }

    public static <T> Serializer<T> getSerializer(Class<T> type) {
        if (serializers.containsKey(type)) {
            return serializers.get(type);
        }
        return null;
    }

}
