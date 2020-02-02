package de.Zorro909.ConfigurationLibrary.Serializers;

import de.Zorro909.ConfigurationLibrary.ConfigurationPane;

public interface Serializer<Type> {

    public void serialize(Type object, ConfigurationPane configPane);

    public Type deserialize(ConfigurationPane configPane);

    public Class<Type> getSerializedType();
    
}
