package de.Zorro909.ConfigurationLibrary;

import java.util.List;

public class ConfigurationPane extends ConfigurationAbstraction {

    private Configuration parent;
    private String prefix;

    public ConfigurationPane(Configuration parent, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException(
                    "Prefix for ConfigurationPane is not allowed to be empty!");
        }
        this.parent = parent;
        this.prefix = prefix;
    }

    @Override
    public Object get(String path) {
        return parent.getString(prefix + "." + path);
    }

    @Override
    public List getList(String path) {
        return parent.getList(prefix + "." + path);
    }

    @Override
    public void setInternal(String path, Object primitiveValue) {
        parent.set(prefix + "." + path, primitiveValue);
    }

    @Override
    public void setInternalList(String path, List primitiveList) {
        parent.set(prefix + "." + path, primitiveList);
    }

    @Override
    public void save() {
        parent.save();
    }

    @Override
    public void markAsList(String path) {
        parent.markAsList(prefix + "." + path);
    }

    @Override
    public String getIdentifier() {
        return parent.getIdentifier() + ":" + prefix;
    }

    @Override
    public List<String> getKeys() {
        return parent.getKeys(prefix);
    }

    @Override
    public List<String> getKeys(String path) {
        return parent.getKeys(prefix + "." + path);
    }

    public Configuration getParent() {
        if (prefix.contains(".")) {
            return new ConfigurationPane(parent,
                    prefix.subSequence(0, prefix.lastIndexOf(".")).toString());
        } else {
            return parent;
        }
    }

    @Override
    public Object get(String path, Object def) {
        return parent.get(prefix + "." + path, def);
    }

    @Override
    public <T> List<T> getList(String path, List<T> def) {
        return parent.getList(path, def);
    }

    @Override
    public <T> List<T> getList(String path, Class<T> type, List<T> def) {
        return parent.getList(path, type, def);
    }

    @Override
    public String getString(String path, String def) {
        return parent.getString(path, def);
    }

    public String getAbsolutePrefix() {
        if (parent instanceof ConfigurationPane) {
            return ((ConfigurationPane) parent).getAbsolutePrefix() + "." + prefix;
        } else {
            return prefix;
        }
    }

}
