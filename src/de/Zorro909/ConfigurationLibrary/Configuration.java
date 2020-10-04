package de.Zorro909.ConfigurationLibrary;

import java.util.List;

import de.Zorro909.ConfigurationLibrary.Serializers.SerializedObject;
import de.Zorro909.ConfigurationLibrary.Serializers.SerializedObjectStructure;

public interface Configuration {

    public void set(String path, Object value);

    public void setSerializedObject(String path, SerializedObject sObj);

    public void setList(String path, List<?> value);

    public void setSerializedObjectList(String path, List<SerializedObject> sObjects);

    public void markAsList(String path);

    public Object get(String path);

    public Object get(String path, Object def);

    public List<SerializedObject> getList(String path, SerializedObjectStructure sObjectStructure);

    public <T> List<T> getList(String path);

    public <T> List<T> getList(String path, List<T> def);

    public <T> T get(String path, Class<T> type);

    public SerializedObject get(String path, SerializedObjectStructure sObjectStructure);

    public <T> List<T> getList(String path, Class<T> type);

    public <T> List<T> getList(String path, Class<T> type, List<T> def);

    public String getString(String path);

    public String getString(String path, String def);

    public Configuration getRegion(String prefix);

    public List<String> getKeys();

    public List<String> getKeys(String path);

    public void save();

    public String getIdentifier();

    public boolean exists(String path);

}
