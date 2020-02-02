package de.Zorro909.ConfigurationLibrary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

@SuppressWarnings("unchecked")
public class YamlConfiguration extends ConfigurationAbstraction {

    HashMap<String, Object> map = new HashMap<String, Object>();
    File confFile;

    public YamlConfiguration(Config configAnnotation, Configuration unusedPreviousConfiguration) {
        confFile = new File(configAnnotation.value());
        if (!confFile.exists()) {
            if (!confFile.getParentFile().exists()) {
                confFile.getParentFile().mkdirs();
            }
        } else {
            org.bukkit.configuration.file.YamlConfiguration tempConf = org.bukkit.configuration.file.YamlConfiguration
                    .loadConfiguration(confFile);
            loadConfig(map, tempConf);
        }
    }

    private void loadConfig(HashMap<String, Object> toAdd, ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                HashMap<String, Object> mapToAdd = new HashMap<>();
                toAdd.put(key, mapToAdd);
                loadConfig(mapToAdd, section.getConfigurationSection(key));
            } else if (section.isList(key)) {
                List list = section.getList(key);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof ConfigurationSection) {
                        HashMap<String, Object> mapToAdd = new HashMap<>();
                        ConfigurationSection previousSection = (ConfigurationSection) list.set(i,
                                mapToAdd);
                        loadConfig(mapToAdd, previousSection);
                    }
                }
                toAdd.put(key, list);
            } else {
                toAdd.put(key, section.get(key));
            }
        }
    }

    @Override
    public Object get(String path) {
        Object ret = traverseTree(map, path, false);
        if (ret instanceof Map || ret instanceof List) {
            return getRegion(path);
        }
        return ret;
    }

    private Object traverseTree(Object currentElement, String path, boolean createMissing) {
        if (!(currentElement instanceof Map || currentElement instanceof List)) {
            return null;
        }
        String next = "";
        if (path.contains(".")) {
            String[] split = path.split("\\.", 2);
            next = split[0];
            path = split[1];
        } else {
            next = path;
            path = "";
        }
        if (currentElement instanceof Map) {
            Object nextElement = ((Map) currentElement).get(next);
            if (nextElement == null) {
                if (!createMissing) {
                    return null;
                } else {
                    nextElement = new HashMap<String, Object>();
                    ((Map) currentElement).put(next, nextElement);
                }
            }
            if (!path.isEmpty()) {
                return traverseTree(nextElement, path, createMissing);
            }
            return nextElement;
        } else {
            int element = 0;
            try {
                element = Integer.valueOf(next);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Encountered a non-number while expecting an Index for a List (Value: "
                                + next + ")");
            }
            List list = (List) currentElement;
            if (list.size() > element) {
                currentElement = list.get(element);
                if (!path.isEmpty()) {
                    return traverseTree(currentElement, path, createMissing);
                }
                return currentElement;
            } else if (createMissing) {
                currentElement = new HashMap<String, Object>();
                while (list.size() < element) {
                    list.add(null);
                }
                list.add(element, currentElement);
                return currentElement;
            } else {
                return null;
            }
        }
    }

    @Override
    public <T> List<T> getList(String path) {
        Object ret = traverseTree(map, path, false);
        if (ret instanceof List) {
            return (List<T>) ret;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setInternal(String path, Object primitiveValue) {
        Object traversedPath = null;
        if (!path.contains(".")) {
            traversedPath = map;
        } else {
            traversedPath = traverseTree(map, path.substring(0, path.lastIndexOf(".")), true);
        }
        if (traversedPath instanceof Map) {
            ((Map) traversedPath).put(path.substring(path.lastIndexOf(".") + 1), primitiveValue);
        } else if (traversedPath instanceof List) {
            String childName = path.substring(path.lastIndexOf(".") + 1);
            List list = (List) traversedPath;
            int element = 0;
            try {
                element = Integer.valueOf(childName);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Encountered a non-number while expecting an Index for a List (Value: "
                                + childName + ")");
            }
            while (list.size() < element) {
                list.add(null);
            }
            list.add(element, primitiveValue);
        }
    }

    @Override
    public void setInternalList(String path, List primitiveList) {
        Object traversedPath = null;
        if (!path.contains(".")) {
            traversedPath = map;
        } else {
            traversedPath = traverseTree(map, path.substring(0, path.lastIndexOf(".")), true);
        }
        if (traversedPath instanceof Map) {
            ((Map) traversedPath).put(path.substring(path.lastIndexOf(".") + 1), primitiveList);
        } else if (traversedPath instanceof List) {
            String childName = path.substring(path.lastIndexOf(".") + 1);
            List list = (List) traversedPath;
            int element = 0;
            try {
                element = Integer.valueOf(childName);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Encountered a non-number while expecting an Index for a List (Value: "
                                + childName + ")");
            }
            while (list.size() < element) {
                list.add(null);
            }
            list.add(element, primitiveList);
        }
    }

    @Override
    public void save() {
        org.bukkit.configuration.file.YamlConfiguration conf = new org.bukkit.configuration.file.YamlConfiguration();
        for (String key : map.keySet()) {
            conf.set(key, map.get(key));
        }
        try {
            conf.save(confFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void markAsList(String path) {
        setInternalList(path, new ArrayList<Object>());
    }

    @Override
    public String getIdentifier() {
        return confFile.getPath();
    }

    @Override
    public List<String> getKeys() {
        return Arrays.asList(map.keySet().toArray(new String[] {}));
    }

    @Override
    public List<String> getKeys(String path) {
        Object ret = traverseTree(map, path, false);
        if (ret == null || !(ret instanceof Map)) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(((HashMap<String, Object>) ret).keySet().toArray(new String[] {}));
        }
    }

    @Override
    public Object get(String path, Object def) {
        Object ret = get(path);
        if(ret==null) {
            return def;
        }
        return ret;
    }

    @Override
    public <T> List<T> getList(String path, List<T> def) {
        List<T> ret = getList(path);
        if(ret==null) {
            return def;
        }
        return ret;
    }

    @Override
    public <T> List<T> getList(String path, Class<T> type, List<T> def) {
        List<T> ret = getList(path);
        if(ret==null) {
            return def;
        }
        return ret;
    }

    @Override
    public String getString(String path, String def) {
        String ret = getString(path);
        if(ret==null) {
            return def;
        }
        return ret;
    }

}
