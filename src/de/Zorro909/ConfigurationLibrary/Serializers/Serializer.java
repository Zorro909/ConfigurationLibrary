package de.Zorro909.ConfigurationLibrary.Serializers;

import de.Zorro909.ConfigurationLibrary.ConfigurationPane;

public interface Serializer<Type> {

    public SerializedObject serialize(Type object);

    public Type deserialize(SerializedObject object);

    public Class<Type> getSerializedType();

    public SerializedObjectStructure getObjectStructure();

}
