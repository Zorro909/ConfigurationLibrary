package de.Zorro909.ConfigurationLibrary;

import java.util.List;

public interface Configuration {

    public void set(String path, Object value);

    public void setList(String path, List value);

    public void markAsList(String path);

    public Object get(String path);
    
    public Object get(String path, Object def);

    public <T> List<T> getList(String path);
    
    public <T> List<T> getList(String path, List<T> def);

    public <T> T get(String path, Class<T> type);
    
    public <T> List<T> getList(String path, Class<T> type);

    public <T> List<T> getList(String path, Class<T> type, List<T> def);

    public String getString(String path);
    
    public String getString(String path, String def);

    public Configuration getRegion(String prefix);
    
    public List<String> getKeys();
    
    public List<String> getKeys(String path);

    public void save();

    public String getIdentifier();

}
